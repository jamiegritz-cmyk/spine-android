package com.spine.musicplayer.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Premium physical CD shelf centerpiece.
 * - Continuous thumb-drag scrolling with 1:1 direct tracking and momentum decay.
 * - Continuous horizontal center selection: whatever CD is at the centre line becomes selected.
 * - The selected CD is brought forward/up (-26.dp) and slightly enlarged.
 * - Settles with the nearest CD centered when the finger is released.
 * - Subtle native haptic tick fires exactly once per CD transition.
 * - Authentic audio CD jewel cases with acrylic specular highlights and spine typography.
 */
@Composable
fun SpineShelf(
    releases: List<Release>,
    selectedIndex: Int,
    onSelectRelease: (Int) -> Unit,
    modifier: Modifier = Modifier,
    shelfHeight: Dp = 380.dp,
    caseHeight: Dp = 280.dp,
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
            Text("No releases found", color = Color(0xFF78716C), fontSize = 14.sp)
        }
        return
    }

    val density = LocalDensity.current
    val context = LocalContext.current
    val view = LocalView.current
    val hapticHelper = remember(context) { HapticFeedbackHelper(context) }
    val itemWidthPx = with(density) { caseWidth.toPx() }
    val n = releases.size

    // Smooth continuous offset tracking the finger
    val scrollOffset = remember { Animatable(-selectedIndex * itemWidthPx) }
    val coroutineScope = rememberCoroutineScope()
    var lastCenterIndex by remember { mutableIntStateOf(selectedIndex) }

    // Keep shelf centered on external programmatic selection (e.g. initial load or skip track)
    LaunchedEffect(selectedIndex) {
        val currentCenterInt = floor((-scrollOffset.value / itemWidthPx) + 0.5f).toInt()
        val currentCenterIndex = ((currentCenterInt % n) + n) % n
        if (currentCenterIndex != selectedIndex) {
            val diff = ((selectedIndex - currentCenterIndex) % n)
            val shortestDiff = when {
                diff > n / 2 -> diff - n
                diff < -n / 2 -> diff + n
                else -> diff
            }
            val targetInt = currentCenterInt + shortestDiff
            scrollOffset.animateTo(
                targetValue = -targetInt * itemWidthPx,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
            lastCenterIndex = selectedIndex
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(shelfHeight)
            .background(Color(0xFF0F0D0B))
            .pointerInput(releases, n) {
                coroutineScope {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val velocityTracker = VelocityTracker()
                        velocityTracker.addPosition(down.uptimeMillis, down.position)
                        var isDragging = false

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
                                    hapticHelper.performCdTick(view)
                                }
                            }
                        }

                        // When user lifts finger
                        if (isDragging) {
                            val velocity = velocityTracker.calculateVelocity().x
                            launch {
                                if (abs(velocity) > 120f) {
                                    scrollOffset.animateDecay(
                                        velocity,
                                        exponentialDecay(frictionMultiplier = 1.2f)
                                    ) {
                                        val rawCenterPos = -value / itemWidthPx
                                        val centerInt = floor(rawCenterPos + 0.5f).toInt()
                                        val activeIndex = ((centerInt % n) + n) % n
                                        if (activeIndex != lastCenterIndex) {
                                            lastCenterIndex = activeIndex
                                            onSelectRelease(activeIndex)
                                            hapticHelper.performCdTick(view)
                                        }
                                    }
                                }

                                // Settle with nearest CD centred
                                val finalCenterInt = floor((-scrollOffset.value / itemWidthPx) + 0.5f).toInt()
                                scrollOffset.animateTo(
                                    targetValue = -finalCenterInt * itemWidthPx,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                )
                                val settledIndex = ((finalCenterInt % n) + n) % n
                                if (settledIndex != lastCenterIndex) {
                                    lastCenterIndex = settledIndex
                                    onSelectRelease(settledIndex)
                                    hapticHelper.performCdTick(view)
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

        // Shelf Cavity Shadow & Dark Walnut Grain Backplate
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF040302),
                                Color(0xFF0F0C09),
                                Color(0xFF1B140E)
                            )
                        )
                    )
                    // Deep overhead cavity shadow
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xEE000000),
                                Color(0x88000000),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = 120f
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
                        isSelected = isCentered,
                        caseHeight = caseHeight,
                        caseWidth = caseWidth,
                        onClick = {
                            if (!isCentered) {
                                coroutineScope.launch {
                                    val targetOffset = scrollOffset.value - (xPos + (itemWidthPx / 2f) - centerX)
                                    scrollOffset.animateTo(
                                        targetOffset,
                                        spring(stiffness = Spring.StiffnessMediumLow)
                                    )
                                    val newCenterInt = floor((-targetOffset / itemWidthPx) + 0.5f).toInt()
                                    val activeIndex = ((newCenterInt % n) + n) % n
                                    if (activeIndex != lastCenterIndex) {
                                        lastCenterIndex = activeIndex
                                        onSelectRelease(activeIndex)
                                        hapticHelper.performCdTick(view)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        // Heavy Walnut Wooden Shelf Base & Lip
        WoodenShelfLip(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun CdSpineItem(
    release: Release,
    isSelected: Boolean,
    caseHeight: Dp = 280.dp,
    caseWidth: Dp = 21.dp,
    onClick: () -> Unit
) {
    // Selected spine pulled forward from shelf and enlarged
    val verticalOffset by animateDpAsState(
        targetValue = if (isSelected) (-26).dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "spineOffset"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "spineScale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 18.dp else 2.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "spineElevation"
    )
    val parsedColor = remember(release.spineColorHex) {
        try {
            Color(android.graphics.Color.parseColor(release.spineColorHex))
        } catch (_: Exception) {
            Color(0xFF222222)
        }
    }

    Box(
        modifier = Modifier
            .offset(y = verticalOffset)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .width(caseWidth)
            .height(caseHeight)
            .shadow(elevation, shape = RoundedCornerShape(1.5.dp))
            .background(Color(0xFF121110), RoundedCornerShape(1.5.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // Spine Background Color
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(parsedColor)
        )
        // Clear Acrylic Jewel Case Specular Highlights
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Left edge specular shine
                    drawLine(
                        color = Color.White.copy(alpha = if (isSelected) 0.45f else 0.22f),
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 1.8f
                    )
                    // Right edge groove shadow
                    drawLine(
                        color = Color.Black.copy(alpha = 0.7f),
                        start = Offset(size.width, 0f),
                        end = Offset(size.width, size.height),
                        strokeWidth = 2f
                    )
                }
        )
        // Top Molded Acrylic Tab & Artwork slice
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            // Plastic tab
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(1.dp))
                )
            }
            // Real Artwork Slice at the top of the spine
            if (release.artworkUri != null) {
                AsyncImage(
                    model = release.artworkUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .clip(RoundedCornerShape(0.dp))
                )
            }
        }
        // Rotated Spine Typography (Artist - Album Title)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 52.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${release.artist.uppercase()} / ${release.title}",
                color = Color.White.copy(alpha = if (isSelected) 1.0f else 0.75f),
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .graphicsLayer {
                        rotationZ = 90f
                    }
                    .width(180.dp)
            )
        }
        // Bottom Catalog Number & Digital Audio Symbol
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = release.catalogNumber.split("-").lastOrNull() ?: "CD",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 7.5.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(4.dp, 2.dp)
                    .background(Color.White.copy(alpha = 0.4f))
            )
        }
    }
}

@Composable
fun WoodenShelfLip(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.drawBehind {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF5A3D29), // Warm walnut highlight bevel
                        Color(0xFF3B271A),
                        Color(0xFF24160E)  // Dark walnut plank face
                    )
                )
            )
            // Shelf edge hairline rim
            drawLine(
                color = Color(0xFF7A5438).copy(alpha = 0.7f),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 2.5f
            )
        }
    )
}
