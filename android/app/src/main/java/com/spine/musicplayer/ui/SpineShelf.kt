package com.spine.musicplayer.ui

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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
 * Recreates genuine physical audio CD jewel cases (125mm x 10mm proportions):
 * - Clear transparent plastic outer shell with visible side walls, top & bottom rails.
 * - Recessed printed paper spine inlay inside the clear case (artwork doesn't touch outer edge).
 * - Subtle plastic specular highlights, corner bevels, and inter-case shadow seams.
 * - Direct 1:1 thumb-drag tracking, momentum scrolling, and NO post-release snapping.
 * - Robust hardware haptic tick via HapticFeedbackHelper on every single center CD transition.
 * - Selected center CD pulled forward with accentuated transparent acrylic edge reflections.
 */
@Composable
fun SpineShelf(
    releases: List<Release>,
    selectedIndex: Int,
    onSelectRelease: (Int) -> Unit,
    modifier: Modifier = Modifier,
    shelfHeight: Dp = 380.dp,
    caseHeight: Dp = 278.dp,
    caseWidth: Dp = 22.dp
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

    val context = LocalContext.current
    val view = LocalView.current
    val hapticHelper = remember(context) { HapticFeedbackHelper(context) }
    val density = LocalDensity.current
    val itemWidthPx = with(density) { caseWidth.toPx() }
    val n = releases.size

    // Continuous offset tracking the user's thumb
    val scrollOffset = remember { Animatable(-selectedIndex * itemWidthPx) }
    val coroutineScope = rememberCoroutineScope()

    var lastCenterIndex by remember { mutableIntStateOf(selectedIndex) }

    // Keep shelf centered on external programmatic selection (e.g. skip track or library select)
    LaunchedEffect(selectedIndex) {
        val currentCenter = ((floor((-scrollOffset.value / itemWidthPx) + 0.5f).toInt() % n) + n) % n
        if (currentCenter != selectedIndex) {
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
            // Fire haptic tick on programmatic selection change
            hapticHelper.performCdTick(view)
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
                                    // Robust native Android haptic tick: exactly once per CD transition
                                    hapticHelper.performCdTick(view)
                                }
                            }
                        }

                        // When thumb is lifted:
                        // If flicked with velocity, apply exponential momentum decay.
                        // IMPORTANT: DO NOT SNAP! Leave the shelf exactly where momentum ends.
                        if (isDragging) {
                            val velocity = velocityTracker.calculateVelocity().x
                            if (abs(velocity) > 120f) {
                                launch {
                                    scrollOffset.animateDecay(velocity, exponentialDecay(frictionMultiplier = 1.25f)) {
                                        val rawCenterPos = -value / itemWidthPx
                                        val centerInt = floor(rawCenterPos + 0.5f).toInt()
                                        val activeIndex = ((centerInt % n) + n) % n

                                        if (activeIndex != lastCenterIndex) {
                                            lastCenterIndex = activeIndex
                                            onSelectRelease(activeIndex)
                                            hapticHelper.performCdTick(view)
                                        }
                                    }
                                    // Movement ceased: no snapTo or animateTo.
                                }
                            }
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
                                Color(0xFF0E0C09),
                                Color(0xFF19130D)
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

        // Continuous horizontal shelf of authentic CD jewel cases
        val currentCenterInt = floor((-scrollOffset.value / itemWidthPx) + 0.5f).toInt()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(caseHeight + 40.dp)
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
                                    hapticHelper.performCdTick(view)
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
 * Authentic physical CD jewel case viewed from the spine side profile.
 * - Fully transparent/translucent polystyrene (plastic) outer shell.
 * - Visible transparent left & right plastic walls (2.5dp) and top/bottom rails (14dp/12dp).
 * - Printed album spine and square artwork thumbnail visibly RECESSED inside the plastic shell.
 * - Artwork is bounded by the inner paper insert and does NOT touch the outer edge.
 * - Molded injection marks, specular glass/plastic reflections, and realistic contact shadows.
 * - Centered CD pulled forward with accentuated transparent acrylic edge illumination.
 */
@Composable
fun CdSpineItem(
    release: Release,
    isCentered: Boolean,
    caseHeight: Dp,
    caseWidth: Dp,
    onClick: () -> Unit
) {
    // Selected CD pulled forward and upward from the shelf
    val verticalOffset by animateDpAsState(
        targetValue = if (isCentered) (-26).dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "spinePopOffset"
    )

    val elevation by animateDpAsState(
        targetValue = if (isCentered) 20.dp else 2.5.dp,
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
                shape = RoundedCornerShape(2.5.dp),
                ambientColor = Color.Black,
                spotColor = if (isCentered) Color(0xEE000000) else Color(0x77000000)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // =========================================================================
        // 1. OUTER TRANSPARENT / TRANSLUCENT PLASTIC JEWEL CASE SHELL
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(2.5.dp))
                .background(
                    // Translucent polystyrene plastic casing (visible along all outer borders)
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0x40FFFFFF), // Left transparent plastic wall reflection
                            Color(0x18FFFFFF),
                            Color(0x0A000000),
                            Color(0x20000000), // Right transparent plastic wall
                            Color(0x55000000)  // Edge crease with adjacent case
                        )
                    )
                )
                .drawBehind {
                    // Outer transparent casing specular hairline on the left edge
                    drawLine(
                        color = Color.White.copy(alpha = if (isCentered) 0.85f else 0.45f),
                        start = Offset(0.5f, 0f),
                        end = Offset(0.5f, size.height),
                        strokeWidth = 1.2f
                    )
                    // Secondary internal plastic refraction line
                    drawLine(
                        color = Color.White.copy(alpha = if (isCentered) 0.45f else 0.20f),
                        start = Offset(2.2f, 0f),
                        end = Offset(2.2f, size.height),
                        strokeWidth = 0.8f
                    )
                    // Right edge outer plastic seam / adjacent case shadow
                    drawLine(
                        color = Color.Black.copy(alpha = 0.85f),
                        start = Offset(size.width - 0.5f, 0f),
                        end = Offset(size.width - 0.5f, size.height),
                        strokeWidth = 1.6f
                    )
                }
        )

        // =========================================================================
        // 2. INNER RECESSED PRINTED PAPER SPINE INLAY (Inset within clear plastic)
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Physical inset: paper insert sits visibly RECESSED behind clear plastic walls
                .padding(start = 2.4.dp, end = 2.4.dp, top = 14.dp, bottom = 12.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(parsedColor)
                .drawBehind {
                    // Inner paper insert shadow crease against the clear plastic walls
                    drawLine(
                        color = Color.Black.copy(alpha = 0.35f),
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = Color.Black.copy(alpha = 0.45f),
                        start = Offset(size.width, 0f),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1f
                    )
                }
        ) {
            // A. Genuine Album Artwork Thumbnail cropped square at top of inner paper insert
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                if (release.artworkUri != null) {
                    AsyncImage(
                        model = release.artworkUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(caseWidth - 4.8.dp) // Square crop recessed inside paper
                            .clip(RoundedCornerShape(0.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(caseWidth - 4.8.dp)
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = release.artist.take(1).uppercase(Locale.ROOT),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Fine paper division line under artwork
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.8.dp)
                        .background(Color.Black.copy(alpha = 0.4f))
                )
            }

            // B. Vertical Printed Spine Typography (Artist - Title)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = caseWidth + 8.dp, bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${release.artist.uppercase(Locale.ROOT)}  ${release.title.uppercase(Locale.ROOT)}",
                    color = Color.White.copy(alpha = if (isCentered) 1.0f else 0.82f),
                    fontSize = 7.8.sp,
                    fontWeight = if (isCentered) FontWeight.Bold else FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .graphicsLayer {
                            rotationZ = 90f
                        }
                        .width(caseHeight - caseWidth - 58.dp)
                )
            }

            // C. Bottom Printed Catalog Number
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = release.catalogNumber.split("-").lastOrNull()?.takeLast(4) ?: "6405",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 6.8.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(1.dp))
                // Digital Audio compact disc symbol
                Box(
                    modifier = Modifier
                        .size(4.dp, 2.dp)
                        .background(Color.White.copy(alpha = 0.55f), RoundedCornerShape(0.5.dp))
                )
            }
        }

        // =========================================================================
        // 3. TOP MOLDED ACRYLIC RAIL (Transparent Polystyrene Cap with Hub Mark)
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isCentered) 0.55f else 0.35f),
                            Color.White.copy(alpha = 0.12f),
                            Color.Black.copy(alpha = 0.25f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Circular injection mold hub dimple
            Box(
                modifier = Modifier
                    .size(3.5.dp)
                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(1.dp))
            )
            // Hinge mold parting line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.6.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.White.copy(alpha = 0.4f))
            )
        }

        // =========================================================================
        // 4. BOTTOM MOLDED ACRYLIC FOOT RAIL (Transparent Plastic Base)
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.40f),
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = if (isCentered) 0.45f else 0.25f)
                        )
                    )
                )
        )

        // =========================================================================
        // 5. ACCENTUATED TRANSPARENT CASE ILLUMINATION ON POPPED-FORWARD SELECTED CD
        // =========================================================================
        if (isCentered) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Clear plastic top rim highlight
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.40f),
                            size = Size(size.width, 16.dp.toPx()),
                            cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx())
                        )
                        // Left acrylic side glow
                        drawLine(
                            color = Color.White.copy(alpha = 0.60f),
                            start = Offset(0f, 0f),
                            end = Offset(0f, size.height),
                            strokeWidth = 2.0f
                        )
                    }
            )
        }
    }
}

/**
 * Realistic Dark Walnut Wooden Shelf Base.
 * Recreates the heavy wooden shelf plank with warm wood grain,
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
