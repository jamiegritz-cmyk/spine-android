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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
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
        val typo = rememberSpineTypography(release = release, isSelected = isSelected)
        val formattedTitle = if (typo.isTitleUppercase) release.title.uppercase() else release.title
        val formattedArtist = if (typo.isArtistUppercase) release.artist.uppercase() else release.artist

        val fullSpineText = remember(release.title, release.artist, typo) {
            buildAnnotatedString {
                if (typo.artistFirst) {
                    withStyle(
                        SpanStyle(
                            fontSize = typo.artistFontSize,
                            fontWeight = typo.artistFontWeight,
                            fontFamily = typo.artistFontFamily,
                            color = typo.artistColor,
                            letterSpacing = typo.letterSpacing
                        )
                    ) {
                        append(formattedArtist)
                    }
                    withStyle(
                        SpanStyle(
                            fontSize = typo.artistFontSize,
                            color = typo.artistColor.copy(alpha = 0.6f)
                        )
                    ) {
                        append(typo.separator)
                    }
                    withStyle(
                        SpanStyle(
                            fontSize = typo.titleFontSize,
                            fontWeight = typo.titleFontWeight,
                            fontFamily = typo.titleFontFamily,
                            color = typo.titleColor,
                            letterSpacing = typo.letterSpacing
                        )
                    ) {
                        append(formattedTitle)
                    }
                } else {
                    withStyle(
                        SpanStyle(
                            fontSize = typo.titleFontSize,
                            fontWeight = typo.titleFontWeight,
                            fontFamily = typo.titleFontFamily,
                            color = typo.titleColor,
                            letterSpacing = typo.letterSpacing
                        )
                    ) {
                        append(formattedTitle)
                    }
                    withStyle(
                        SpanStyle(
                            fontSize = typo.artistFontSize,
                            color = typo.artistColor.copy(alpha = 0.6f)
                        )
                    ) {
                        append(typo.separator)
                    }
                    withStyle(
                        SpanStyle(
                            fontSize = typo.artistFontSize,
                            fontWeight = typo.artistFontWeight,
                            fontFamily = typo.artistFontFamily,
                            color = typo.artistColor,
                            letterSpacing = typo.letterSpacing
                        )
                    ) {
                        append(formattedArtist)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fullSpineText,
                maxLines = 1,
                softWrap = false,
                style = TextStyle(
                    shadow = Shadow(
                        color = typo.shadowColor,
                        offset = Offset(0f, 1f),
                        blurRadius = 3f
                    )
                ),
                modifier = Modifier.verticalSpineText()
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

/**
 * Custom layout modifier that allows single-line text to measure along the vertical spine length
 * (up to ~230dp) instead of being constrained to the narrow 22dp spine width.
 */
private fun Modifier.verticalSpineText(): Modifier = this.layout { measurable, constraints ->
    val placeable = measurable.measure(
        constraints.copy(
            minWidth = 0,
            maxWidth = constraints.maxHeight,
            minHeight = 0,
            maxHeight = constraints.maxWidth
        )
    )
    layout(placeable.height, placeable.width) {
        placeable.placeWithLayer(
            x = (placeable.height - placeable.width) / 2,
            y = (placeable.width - placeable.height) / 2
        ) {
            rotationZ = 90f
        }
    }
}

private data class SpineTypographyConfig(
    val titleColor: Color,
    val artistColor: Color,
    val titleFontWeight: FontWeight,
    val artistFontWeight: FontWeight,
    val titleFontFamily: FontFamily,
    val artistFontFamily: FontFamily,
    val titleFontSize: TextUnit,
    val artistFontSize: TextUnit,
    val letterSpacing: TextUnit,
    val isTitleUppercase: Boolean,
    val isArtistUppercase: Boolean,
    val separator: String,
    val artistFirst: Boolean,
    val shadowColor: Color
)

@Composable
private fun rememberSpineTypography(
    release: Release,
    isSelected: Boolean
): SpineTypographyConfig {
    return remember(release.title, release.artist, release.spineColorHex, isSelected) {
        val parsedColor = try {
            Color(android.graphics.Color.parseColor(release.spineColorHex))
        } catch (_: Exception) {
            Color(0xFF222222)
        }
        val luminance = 0.299f * parsedColor.red + 0.587f * parsedColor.green + 0.114f * parsedColor.blue
        val isLight = luminance > 0.55f

        val hash = abs((release.title + release.artist).hashCode())
        val styleIndex = hash % 6

        // Dynamic contrast colors
        val primaryColor = if (isLight) Color(0xFF151412) else Color(0xFFF9F7F4)
        val secondaryColor = if (isLight) Color(0xFF3C3834) else Color(0xFFD6D1C9)
        val shadowColor = if (isLight) Color.White.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.85f)

        // Progressive font scaling for title and artist so the complete text fits
        val totalLength = release.title.length + release.artist.length
        val (titleSize, artistSize, spacing) = when {
            totalLength <= 18 -> Triple(8.5.sp, 7.5.sp, 0.4.sp)
            totalLength <= 28 -> Triple(7.8.sp, 6.8.sp, 0.2.sp)
            totalLength <= 40 -> Triple(7.0.sp, 6.0.sp, 0.sp)
            totalLength <= 55 -> Triple(6.2.sp, 5.5.sp, (-0.2).sp)
            else -> Triple(5.5.sp, 5.0.sp, (-0.3).sp)
        }

        when (styleIndex) {
            0 -> SpineTypographyConfig(
                titleColor = primaryColor,
                artistColor = secondaryColor,
                titleFontWeight = FontWeight.Bold,
                artistFontWeight = FontWeight.Medium,
                titleFontFamily = FontFamily.SansSerif,
                artistFontFamily = FontFamily.SansSerif,
                titleFontSize = titleSize,
                artistFontSize = artistSize,
                letterSpacing = spacing,
                isTitleUppercase = true,
                isArtistUppercase = true,
                separator = "   |   ",
                artistFirst = false,
                shadowColor = shadowColor
            )
            1 -> SpineTypographyConfig(
                titleColor = primaryColor,
                artistColor = secondaryColor,
                titleFontWeight = FontWeight.SemiBold,
                artistFontWeight = FontWeight.Normal,
                titleFontFamily = FontFamily.Monospace,
                artistFontFamily = FontFamily.Monospace,
                titleFontSize = titleSize,
                artistFontSize = artistSize,
                letterSpacing = spacing,
                isTitleUppercase = false,
                isArtistUppercase = true,
                separator = "   /   ",
                artistFirst = true,
                shadowColor = shadowColor
            )
            2 -> SpineTypographyConfig(
                titleColor = primaryColor,
                artistColor = secondaryColor,
                titleFontWeight = FontWeight.Bold,
                artistFontWeight = FontWeight.SemiBold,
                titleFontFamily = FontFamily.Serif,
                artistFontFamily = FontFamily.SansSerif,
                titleFontSize = titleSize,
                artistFontSize = artistSize,
                letterSpacing = spacing,
                isTitleUppercase = false,
                isArtistUppercase = true,
                separator = "   •   ",
                artistFirst = true,
                shadowColor = shadowColor
            )
            3 -> SpineTypographyConfig(
                titleColor = primaryColor,
                artistColor = secondaryColor,
                titleFontWeight = FontWeight.ExtraBold,
                artistFontWeight = FontWeight.Bold,
                titleFontFamily = FontFamily.SansSerif,
                artistFontFamily = FontFamily.SansSerif,
                titleFontSize = titleSize,
                artistFontSize = artistSize,
                letterSpacing = spacing,
                isTitleUppercase = true,
                isArtistUppercase = true,
                separator = "   —   ",
                artistFirst = false,
                shadowColor = shadowColor
            )
            4 -> SpineTypographyConfig(
                titleColor = primaryColor,
                artistColor = secondaryColor,
                titleFontWeight = FontWeight.Medium,
                artistFontWeight = FontWeight.Normal,
                titleFontFamily = FontFamily.SansSerif,
                artistFontFamily = FontFamily.SansSerif,
                titleFontSize = titleSize,
                artistFontSize = artistSize,
                letterSpacing = spacing + 0.3.sp,
                isTitleUppercase = false,
                isArtistUppercase = false,
                separator = "   :   ",
                artistFirst = false,
                shadowColor = shadowColor
            )
            else -> SpineTypographyConfig(
                titleColor = primaryColor,
                artistColor = secondaryColor,
                titleFontWeight = FontWeight.Bold,
                artistFontWeight = FontWeight.Medium,
                titleFontFamily = FontFamily.SansSerif,
                artistFontFamily = FontFamily.Monospace,
                titleFontSize = titleSize,
                artistFontSize = artistSize,
                letterSpacing = spacing,
                isTitleUppercase = true,
                isArtistUppercase = false,
                separator = "   •   ",
                artistFirst = false,
                shadowColor = shadowColor
            )
        }
    }
}
