package com.spine.musicplayer.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.spine.musicplayer.model.Release
import kotlin.math.abs

/**
 * Premium physical CD shelf component - Dominant visual centerpiece.
 * Restored from the known-working d447de9 baseline:
 * - Built on LazyRow with rememberSnapFlingBehavior and rememberLazyListState.
 * - Dynamically determines the centered CD on initial layout and during scrolling.
 * - Triggers a single subtle haptic tick when the centered CD changes.
 * - Pulls the centered/selected CD forward (-26.dp) with specular highlights.
 */
@Composable
fun SpineShelf(
    releases: List<Release>,
    selectedIndex: Int,
    onSelectRelease: (Int) -> Unit,
    modifier: Modifier = Modifier,
    shelfHeight: Dp = 380.dp
) {
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val context = LocalContext.current
    val view = LocalView.current
    val hapticHelper = remember(context) { HapticFeedbackHelper(context) }

    // Continuously detect which CD is closest to the horizontal center
    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visible = layoutInfo.visibleItemsInfo
            if (visible.isEmpty()) return@derivedStateOf -1
            val center = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            visible.minByOrNull { item ->
                val itemCenter = item.offset + item.size / 2
                abs(itemCenter - center)
            }?.index ?: -1
        }
    }

    var lastCenterIndex by remember { mutableIntStateOf(-1) }

    // Establish centered CD on initial layout and update during scrolling with haptic tick
    LaunchedEffect(centerIndex) {
        if (centerIndex in releases.indices && centerIndex != lastCenterIndex) {
            val isInitial = (lastCenterIndex == -1)
            lastCenterIndex = centerIndex

            if (centerIndex != selectedIndex) {
                onSelectRelease(centerIndex)
            }

            if (!isInitial) {
                hapticHelper.performCdTick(view)
            }
        }
    }

    // Scroll to selected item only when programmatic (not during active user scroll)
    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress && selectedIndex in releases.indices) {
            val layoutInfo = listState.layoutInfo
            val visible = layoutInfo.visibleItemsInfo
            val currentCenter = if (visible.isNotEmpty()) {
                val center = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                visible.minByOrNull { item ->
                    val itemCenter = item.offset + item.size / 2
                    abs(itemCenter - center)
                }?.index ?: -1
            } else -1

            if (currentCenter != selectedIndex) {
                listState.animateScrollToItem(
                    index = (selectedIndex - 2).coerceAtLeast(0)
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(shelfHeight)
            .background(Color(0xFF0F0D0B))
    ) {
        // Shelf Cavity Shadow & Dark Walnut Grain Backplate
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF050504),
                                Color(0xFF14110E),
                                Color(0xFF1F1A15)
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

        // Spines Row: Full-Height Authentic CD Jewel Cases Centered Vertically
        LazyRow(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(horizontal = 140.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            itemsIndexed(releases) { index, release ->
                val isSelected = index == selectedIndex
                CdSpineItem(
                    release = release,
                    isSelected = isSelected,
                    onClick = { onSelectRelease(index) }
                )
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
            .width(22.dp) // Slender authentic jewel case spine width
            .height(280.dp) // Tall, dominant full-height CD spine
            .shadow(elevation, shape = RoundedCornerShape(1.5.dp))
            .background(Color(0xFF0C0B0A), RoundedCornerShape(1.5.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // --- 1. Printed Tray Card Inlay (Full-Height Artwork Insert) ---
        // Sits inside the clear jewel case, extending vertically through the full spine
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 1.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(0.5.dp))
                .background(parsedColor)
        ) {
            if (release.artworkUri != null) {
                AsyncImage(
                    model = release.artworkUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // Subtle darkening wash so rotated typography is crisp and legible over any artwork
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.35f),
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.20f),
                                Color.Black.copy(alpha = 0.45f)
                            )
                        )
                    )
            )
        }

        // --- 2. Rotated Spine Typography (Artist - Album Title) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 36.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${release.artist.uppercase()} / ${release.title}",
                color = Color.White.copy(alpha = if (isSelected) 1.0f else 0.85f),
                fontSize = 8.5.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .graphicsLayer {
                        rotationZ = 90f
                    }
                    .width(200.dp)
            )
        }

        // --- 3. Bottom Compact Catalog Number ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = release.catalogNumber.split("-").lastOrNull() ?: "CD",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 7.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
        }

        // --- 4. Authentic Clear Polystyrene Jewel Case Glass & Edge Highlights ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Left edge specular highlight (clear acrylic bevel reflection)
                    drawLine(
                        color = Color.White.copy(alpha = if (isSelected) 0.55f else 0.30f),
                        start = Offset(0.5f, 0f),
                        end = Offset(0.5f, size.height),
                        strokeWidth = 1.2f
                    )
                    // Secondary inner refraction line
                    drawLine(
                        color = Color.White.copy(alpha = 0.12f),
                        start = Offset(2f, 0f),
                        end = Offset(2f, size.height),
                        strokeWidth = 0.8f
                    )
                    // Right edge seam / hinge groove shadow
                    drawLine(
                        color = Color.Black.copy(alpha = 0.65f),
                        start = Offset(size.width - 0.5f, 0f),
                        end = Offset(size.width - 0.5f, size.height),
                        strokeWidth = 1.5f
                    )
                    // Top clear plastic edge highlight
                    drawLine(
                        color = Color.White.copy(alpha = 0.35f),
                        start = Offset(0f, 0.5f),
                        end = Offset(size.width, 0.5f),
                        strokeWidth = 1.0f
                    )
                    // Bottom edge shelf shadow
                    drawLine(
                        color = Color.Black.copy(alpha = 0.70f),
                        start = Offset(0f, size.height - 0.5f),
                        end = Offset(size.width, size.height - 0.5f),
                        strokeWidth = 1.5f
                    )
                }
        )

        // Top molded clear plastic tab highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )
        )
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
