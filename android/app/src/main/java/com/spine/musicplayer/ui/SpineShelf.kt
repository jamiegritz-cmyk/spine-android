package com.spine.musicplayer.ui

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.spine.musicplayer.model.Release
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Authentic physical CD shelf centerpiece.
 * Reproduces the visual reference from the design mock-up:
 * - Real audio CD jewel cases viewed from the side (tall, narrow, tightly packed).
 * - Clear acrylic outer shell, molded caps, top artwork thumbnail, vertical typography, catalog mark.
 * - Thumb-drag scrolling with continuous 1:1 direct tracking and momentum.
 * - Continuous horizontal centre selection: whatever CD is at the centre line pops forward.
 * - Short, subtle standard Android haptic click once per CD selection change.
 * - NO post-release snap: whichever CD is at the centre at the moment the finger lifts remains selected.
 * - Infinite seamless looping shelf navigation.
 */
@Composable
fun SpineShelf(
    releases: List<Release>,
    selectedIndex: Int,
    onSelectRelease: (Int) -> Unit,
    modifier: Modifier = Modifier,
    shelfHeight: Dp = 380.dp,
    caseHeight: Dp = 278.dp,
    caseWidth: Dp = 21.dp
) {
    if (releases.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(shelfHeight)
                .background(Color(0xFF0F0D0B)),
            contentAlignment = Alignment.Center
        ) {
            Text("No releases on shelf", color = Color(0xFF78716C), fontSize = 14.sp)
        }
        return
    }

    val density = LocalDensity.current
    val view = LocalView.current
    val itemWidthPx = with(density) { caseWidth.toPx() }
    val n = releases.size

    // Smooth continuous offset tracking the finger
    val scrollOffset = remember { Animatable(-selectedIndex * itemWidthPx) }
    val coroutineScope = rememberCoroutineScope()

    var lastCenterIndex by remember { mutableIntStateOf(selectedIndex) }

    // Keep shelf centered on external programmatic selection (e.g. skip track)
    LaunchedEffect(selectedIndex) {
        val currentCenter = ((floor((-scrollOffset.value / itemWidthPx) + 0.5f).toInt() % n) + n) % n
        if (currentCenter != selectedIndex) {
            // Smoothly align when selected externally without jarring jump
            val rawCenter = -scrollOffset.value / itemWidthPx
            val currentCenterInt = floor(rawCenter + 0.5f).toInt()
            val diff = ((selectedIndex - (currentCenterInt % n)) % n)
            val shortestDiff = when {
                diff > n / 2 -> diff - n
                diff < -n / 2 -> diff + n
                else -> diff
            }
            val targetInt = currentCenterInt + shortestDiff
            scrollOffset.animateTo(
                targetValue = -targetInt * itemWidthPx,
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            )
            lastCenterIndex = selectedIndex
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(shelfHeight)
            .background(Color(0xFF0C0A09))
            .pointerInput(releases, n) {
                coroutineScope {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val velocityTracker = VelocityTracker()
                        velocityTracker.addPosition(down.uptimeMillis, down.position)
                        var isDragging = false

                        // Direct 1:1 finger tracking
                        horizontalDrag(down.id) { change ->
                            val dragAmount = change.position.x - change.previousPosition.x
                            change.consume()
                            isDragging = true
                            velocityTracker.addPosition(change.uptimeMillis, change.position)

                            launch {
                                scrollOffset.snapTo(scrollOffset.value + dragAmount)

                                // Continuous center selection while dragging
                                val rawCenterPos = -scrollOffset.value / itemWidthPx
                                val centerInt = floor(rawCenterPos + 0.5f).toInt()
                                val activeIndex = ((centerInt % n) + n) % n

                                if (activeIndex != lastCenterIndex) {
                                    lastCenterIndex = activeIndex
                                    onSelectRelease(activeIndex)
                                    // Subtle standard Android haptic click: once per CD selection change
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                }
                            }
                        }

                        // When user lifts thumb:
                        // Apply natural momentum decay if flicked, but DO NOT SNAP to center!
                        // The shelf remains at the exact position where movement ceases.
                        if (isDragging) {
                            val velocity = velocityTracker.calculateVelocity().x
                            if (abs(velocity) > 120f) {
                                launch {
                                    scrollOffset.animateDecay(velocity, exponentialDecay(frictionMultiplier = 1.2f)) {
                                        val rawCenterPos = -value / itemWidthPx
                                        val centerInt = floor(rawCenterPos + 0.5f).toInt()
                                        val activeIndex = ((centerInt % n) + n) % n

                                        if (activeIndex != lastCenterIndex) {
                                            lastCenterIndex = activeIndex
                                            onSelectRelease(activeIndex)
                                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        }
                                    }
                                    // Notice: NO snapTo or animateTo after decay finishes!
                                    // It stays exactly where momentum left it.
                                }
                            }
                            // If low velocity or still, no snap at all.
                        }
                    }
                }
            }
    ) {
        val containerWidthPx = constraints.maxWidth.toFloat()
        val centerX = containerWidthPx / 2f
        val visibleSlots = (containerWidthPx / itemWidthPx).toInt() + 6

        // Deep Walnut Shelf Cavity Backplate with overhead shadow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Deep dark brown cavity gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF040302),
                                Color(0xFF0F0C09),
                                Color(0xFF1B140E)
                            )
                        )
                    )
                    // Ambient overhead cavity shadow
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xF5000000),
                                Color(0x99000000),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = 140f
                        )
                    )
                }
        )

        // Continuous horizontal shelf of audio CD jewel cases
        val currentCenterInt = floor((-scrollOffset.value / itemWidthPx) + 0.5f).toInt()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(caseHeight + 36.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        ) {
            for (slot in (-visibleSlots / 2)..(visibleSlots / 2)) {
                val caseIndex = (((currentCenterInt + slot) % n) + n) % n
                val release = releases[caseIndex]

                val xPos = centerX + (currentCenterInt + slot) * itemWidthPx + scrollOffset.value - (itemWidthPx / 2f)
                val distFromCenter = abs(xPos + (itemWidthPx / 2f) - centerX)
                val isCentered = distFromCenter < (itemWidthPx / 2f)

                Box(
                    modifier = Modifier
                        .offset { IntOffset(xPos.roundToInt(), 0) }
                        .align(Alignment.BottomStart)
                ) {
                    CdSpineItem(
                        release = release,
                        isCentered = isCentered,
                        caseHeight = caseHeight,
                        caseWidth = caseWidth,
                        onClick = {
                            if (!isCentered) {
                                coroutineScope.launch {
                                    val targetOffset = scrollOffset.value - (xPos + (itemWidthPx / 2f) - centerX)
                                    scrollOffset.animateTo(
                                        targetOffset,
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                    )
                                    lastCenterIndex = caseIndex
                                    onSelectRelease(caseIndex)
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                }
                            }
                        }
                    )
                }
            }
        }

        // Heavy Walnut Wooden Shelf Base & Lip with rich wood grain & specular bevel
        WoodenShelfLip(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .align(Alignment.BottomCenter)
        )
    }
}

/**
 * Authentic physical CD jewel case viewed from the side profile.
 * Exactly matches the visual mockup:
 * - Clear acrylic casing top, bottom, and edges.
 * - Printed paper spine insert inside.
 * - Square artwork thumbnail cropped at the top of the spine.
 * - Authentic album colors and vertical typography.
 * - Digital audio catalog markings at the bottom.
 * - Selected center CD visually pops forward and upward from the shelf.
 */
@Composable
fun CdSpineItem(
    release: Release,
    isCentered: Boolean,
    caseHeight: Dp,
    caseWidth: Dp,
    onClick: () -> Unit
) {
    // Physical pop forward effect when at horizontal centre
    val verticalOffset by animateDpAsState(
        targetValue = if (isCentered) (-26).dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "spinePopOffset"
    )

    val elevation by animateDpAsState(
        targetValue = if (isCentered) 18.dp else 2.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "spineElevation"
    )

    val parsedColor = remember(release.spineColorHex) {
        try {
            Color(android.graphics.Color.parseColor(release.spineColorHex))
        } catch (_: Exception) {
            Color(0xFF221F1D)
        }
    }

    Box(
        modifier = Modifier
            .offset(y = verticalOffset)
            .width(caseWidth)
            .height(caseHeight)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(1.5.dp),
                ambientColor = Color.Black,
                spotColor = if (isCentered) Color(0xCC000000) else Color(0x66000000)
            )
            .background(Color(0xFF141312), RoundedCornerShape(1.5.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // 1. Printed Paper Spine Inlay Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(parsedColor)
        )

        // 2. Clear Acrylic Jewel Case Specular Reflections & Edge Grooves
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Left acrylic specular reflection hairline
                    drawLine(
                        color = Color.White.copy(alpha = if (isCentered) 0.55f else 0.28f),
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 1.4f
                    )
                    // Inner hairline highlight
                    drawLine(
                        color = Color.White.copy(alpha = if (isCentered) 0.35f else 0.15f),
                        start = Offset(1.5f, 0f),
                        end = Offset(1.5f, size.height),
                        strokeWidth = 0.8f
                    )
                    // Right edge seam / contact shadow
                    drawLine(
                        color = Color.Black.copy(alpha = 0.75f),
                        start = Offset(size.width, 0f),
                        end = Offset(size.width, size.height),
                        strokeWidth = 2.0f
                    )
                }
        )

        // 3. Top Molded Acrylic Tab & Genuine Album Artwork Thumbnail
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            // Transparent top acrylic cap with molded injection mark
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isCentered) 0.40f else 0.25f),
                                Color.White.copy(alpha = 0.08f),
                                Color.Black.copy(alpha = 0.20f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Circular injection mold hub mark
                Box(
                    modifier = Modifier
                        .size(3.5.dp)
                        .background(Color.White.copy(alpha = 0.45f), RoundedCornerShape(1.dp))
                )
            }

            // Real Album Artwork Thumbnail cropped square at the top of the spine
            if (release.artworkUri != null) {
                AsyncImage(
                    model = release.artworkUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(caseWidth) // Authentic 1:1 square crop
                        .clip(RoundedCornerShape(0.dp))
                )
            } else {
                // Authentic procedural cover crop placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(caseWidth)
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = release.artist.take(1).uppercase(Locale.ROOT),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Fine horizontal acrylic parting line under artwork
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.Black.copy(alpha = 0.5f))
            )
        }

        // 4. Vertical Spine Typography (Album & Artist Branding)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = caseWidth + 22.dp, bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${release.artist.uppercase(Locale.ROOT)}  ${release.title.uppercase(Locale.ROOT)}",
                color = Color.White.copy(alpha = if (isCentered) 1.0f else 0.80f),
                fontSize = 8.2.sp,
                fontWeight = if (isCentered) FontWeight.Bold else FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.6.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .graphicsLayer {
                        rotationZ = 90f
                    }
                    .width(caseHeight - caseWidth - 54.dp)
            )
        }

        // 5. Bottom Acrylic Footcap & Compact Disc Digital Audio Catalog Mark
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 4-digit catalog number matching the mock-up
            Text(
                text = release.catalogNumber.split("-").lastOrNull()?.takeLast(4) ?: "6405",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 7.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(1.5.dp))
            // Digital Audio badge symbol
            Box(
                modifier = Modifier
                    .size(5.dp, 2.5.dp)
                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(0.5.dp))
            )
        }

        // 6. Center CD Highlight Rim: Subtle illumination when pulled forward
        if (isCentered) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Soft top rim highlight
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.35f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = 24f
                            )
                        )
                    }
            )
        }
    }
}

/**
 * Realistic Dark Walnut Wooden Shelf Base.
 * Recreates the heavy wooden shelf plank from the mock-up with warm wood grain,
 * top surface reflection, and edge bevel highlight.
 */
@Composable
fun WoodenShelfLip(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.drawBehind {
            // Warm walnut wood plank face
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6E482F), // Warm walnut highlight rim
                        Color(0xFF422B1C),
                        Color(0xFF28190F), // Dark deep walnut face
                        Color(0xFF170E08)
                    )
                )
            )

            // Fine wood grain horizontal striations
            for (y in listOf(6f, 14f, 22f, 30f)) {
                drawLine(
                    color = Color(0xFF1B1009).copy(alpha = 0.45f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }

            // Top shelf edge specular bevel rim
            drawLine(
                color = Color(0xFF9E6C45).copy(alpha = 0.85f),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 2.5f
            )

            // Deep shadow cast by jewel cases onto the shelf
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.75f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = 12f
                )
            )
        }
    )
}
