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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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

    val caseHeight = if (shelfHeight < 280.dp) (shelfHeight - 48.dp).coerceAtLeast(120.dp) else 280.dp
    val caseWidth = if (shelfHeight < 280.dp) 18.dp else 22.dp

    // Subtle tactile darker/warm charcoal texture for the shelf display cavity
    val shelfBgBrush = rememberTactileTextureBrush(
        baseColor = Color(0xFF0C0A09),
        grainVariance = 4,
        seed = 101L
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(shelfHeight)
            .background(shelfBgBrush)
            .drawBehind {
                // Subtle horizontal boundary line separating main player background from shelf cavity
                drawLine(
                    color = Color(0xFF242220).copy(alpha = 0.65f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.2f
                )
                // Ambient top cavity depth shadow extending fully across the shelf
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = 48.dp.toPx()
                    )
                )
            }
    ) {

        // Spines Row: Full-Height Authentic CD Jewel Cases Sitting Physically on Wooden Shelf
        LazyRow(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(horizontal = 140.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(2.5.dp),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 35.dp) // Directly touches top surface of 36dp shelf lip
        ) {
            itemsIndexed(releases) { index, release ->
                val isSelected = index == selectedIndex
                CdSpineItem(
                    release = release,
                    isSelected = isSelected,
                    caseHeight = caseHeight,
                    caseWidth = caseWidth,
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

/**
 * Continuous physical CD shelf for Landscape Mode:
 * - Spines underneath the main CD jewel case (x in caseStartDp..caseEndDp) remain partially hidden (115dp tall)
 * - Spines extending past the left and right edges become full-height (220dp tall)
 * - Smooth natural physical transition as spines scroll into or out from behind the case
 * - Continuous wooden shelf lip along the entire bottom of the screen
 */
@Composable
fun LandscapeSpineShelf(
    releases: List<Release>,
    selectedIndex: Int,
    onSelectRelease: (Int) -> Unit,
    modifier: Modifier = Modifier,
    caseStartDp: Dp = 30.dp,
    caseEndDp: Dp = 295.dp
) {
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val context = LocalContext.current
    val view = LocalView.current
    val hapticHelper = remember(context) { HapticFeedbackHelper(context) }
    val density = LocalDensity.current

    val caseStartPx = with(density) { caseStartDp.toPx() }
    val caseEndPx = with(density) { caseEndDp.toPx() }
    val transitionPx = with(density) { 16.dp.toPx() }

    // Detect centered CD near jewel case center
    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visible = layoutInfo.visibleItemsInfo
            if (visible.isEmpty()) return@derivedStateOf -1
            val caseCenterPx = (caseStartPx + caseEndPx) / 2f
            visible.minByOrNull { item ->
                val itemCenter = item.offset + item.size / 2f
                abs(itemCenter - caseCenterPx)
            }?.index ?: -1
        }
    }

    var lastCenterIndex by remember { mutableIntStateOf(-1) }

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

    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress && selectedIndex in releases.indices) {
            val layoutInfo = listState.layoutInfo
            val visible = layoutInfo.visibleItemsInfo
            val caseCenterPx = (caseStartPx + caseEndPx) / 2f
            val currentCenter = if (visible.isNotEmpty()) {
                visible.minByOrNull { item ->
                    val itemCenter = item.offset + item.size / 2f
                    abs(itemCenter - caseCenterPx)
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
            .height(265.dp)
    ) {
        LazyRow(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(start = 12.dp, end = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(2.5.dp),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 35.dp) // Directly touches top surface of 36dp shelf lip
        ) {
            itemsIndexed(releases) { index, release ->
                val isSelected = index == selectedIndex
                CdSpineItem(
                    release = release,
                    isSelected = isSelected,
                    caseHeight = 215.dp,
                    caseWidth = 20.dp,
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
    caseHeight: Dp = 280.dp,
    caseWidth: Dp = 22.dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Selected spine pulled forward from shelf and enlarged
    val targetOffset = if (isSelected) {
        if (caseHeight < 200.dp) (-14).dp else (-26).dp
    } else 0.dp

    val verticalOffset by animateDpAsState(
        targetValue = targetOffset,
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
    val style = remember(release.id, release.title, release.artist, release.spineColorHex) {
        AlbumVisualIdentityResolver.resolve(release)
    }

    val caseHash = remember(release.id) { kotlin.math.abs(release.id.hashCode()) }
    val hasScratch = (caseHash % 3) != 0
    val scratchYFrac = remember(caseHash) { 0.22f + ((caseHash % 55) / 100f) }

    Box(
        modifier = modifier
            .offset(y = verticalOffset)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .width(caseWidth) // Slender authentic jewel case spine width
            .height(caseHeight) // Dominant full-height CD spine
            .shadow(elevation, shape = RoundedCornerShape(1.5.dp))
            .background(Color(0x1AFFFFFF), RoundedCornerShape(1.5.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // --- 1. Printed Tray Card Inlay (Cover-Derived Artwork Insert) ---
        // Sits inside the clear jewel case, using the actual album cover image as the visual basis
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 1.2.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(0.5.dp))
                .background(style.dominantColor)
        ) {
            // Actual Album Cover Artwork (Intelligently scaled and cropped)
            if (release.artworkUri != null) {
                AsyncImage(
                    model = release.artworkUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = style.cropAlignment,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // Harmonious tonal wash derived directly from cover's dominant color:
            // Keeps typography crisp while allowing authentic cover textures and art to shine through
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                style.dominantColor.copy(alpha = (style.artworkWashAlpha + 0.15f).coerceAtMost(0.85f)),
                                style.dominantColor.copy(alpha = (style.artworkWashAlpha * 0.70f).coerceAtMost(0.60f)),
                                style.dominantColor.copy(alpha = (style.artworkWashAlpha * 0.85f).coerceAtMost(0.70f)),
                                style.dominantColor.copy(alpha = (style.artworkWashAlpha + 0.25f).coerceAtMost(0.90f))
                            )
                        )
                    )
            )

            // Miniature Cover Art Thumbnail Badge at top of spine for immediate visual recognition
            if (style.showCoverThumbnail && release.artworkUri != null) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(13.5.dp)
                        .align(Alignment.TopCenter)
                        .clip(RoundedCornerShape(1.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    AsyncImage(
                        model = release.artworkUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                drawRect(
                                    color = Color.White.copy(alpha = 0.35f),
                                    style = Stroke(width = 0.5f)
                                )
                            }
                    )
                }
            }
        }

        // --- 2. Rotated Spine Typography (Matching Album Cover Distinctive Font & Style) ---
        val formattedTitle = if (style.isTitleUppercase) release.title.uppercase(Locale.ROOT) else release.title
        val formattedArtist = if (style.isArtistUppercase) release.artist.uppercase(Locale.ROOT) else release.artist

        val totalLength = release.title.length + release.artist.length
        val (titleSize, artistSize) = when {
            totalLength <= 18 -> Pair(9.2.sp, 8.2.sp)
            totalLength <= 28 -> Pair(8.4.sp, 7.4.sp)
            totalLength <= 40 -> Pair(7.6.sp, 6.6.sp)
            totalLength <= 55 -> Pair(6.8.sp, 6.0.sp)
            else -> Pair(6.0.sp, 5.4.sp)
        }

        val fullSpineText = remember(release.title, release.artist, style, titleSize, artistSize) {
            buildAnnotatedString {
                if (style.artistFirst) {
                    withStyle(
                        SpanStyle(
                            fontSize = artistSize,
                            fontWeight = style.artistFontWeight,
                            fontFamily = style.artistFontFamily,
                            color = style.secondaryTextColor,
                            letterSpacing = style.letterSpacing
                        )
                    ) {
                        append(formattedArtist)
                    }
                    withStyle(
                        SpanStyle(
                            fontSize = artistSize,
                            color = style.secondaryTextColor.copy(alpha = 0.65f)
                        )
                    ) {
                        append(style.separator)
                    }
                    withStyle(
                        SpanStyle(
                            fontSize = titleSize,
                            fontWeight = style.titleFontWeight,
                            fontFamily = style.titleFontFamily,
                            color = style.primaryTextColor,
                            letterSpacing = style.letterSpacing
                        )
                    ) {
                        append(formattedTitle)
                    }
                } else {
                    withStyle(
                        SpanStyle(
                            fontSize = titleSize,
                            fontWeight = style.titleFontWeight,
                            fontFamily = style.titleFontFamily,
                            color = style.primaryTextColor,
                            letterSpacing = style.letterSpacing
                        )
                    ) {
                        append(formattedTitle)
                    }
                    withStyle(
                        SpanStyle(
                            fontSize = artistSize,
                            color = style.secondaryTextColor.copy(alpha = 0.65f)
                        )
                    ) {
                        append(style.separator)
                    }
                    withStyle(
                        SpanStyle(
                            fontSize = artistSize,
                            fontWeight = style.artistFontWeight,
                            fontFamily = style.artistFontFamily,
                            color = style.secondaryTextColor,
                            letterSpacing = style.letterSpacing
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
                .padding(
                    top = if (style.showCoverThumbnail && release.artworkUri != null) 20.dp else 14.dp,
                    bottom = 18.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fullSpineText,
                maxLines = 1,
                softWrap = false,
                style = TextStyle(
                    shadow = Shadow(
                        color = style.shadowColor,
                        offset = Offset(0f, 1f),
                        blurRadius = 3f
                    )
                ),
                modifier = Modifier.verticalSpineText()
            )
        }

        // --- 3. Bottom Compact Catalog Number & Distinctive Label Mark ---
        val catalogCode = remember(release.id, release.catalogNumber, style.catalogPrefix) {
            val digits = release.catalogNumber.filter { it.isDigit() }
            if (digits.length >= 4) {
                digits.takeLast(4)
            } else {
                String.format(Locale.ROOT, "%04d", (caseHash % 9000) + 1000)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (style.labelBadge != null) {
                Text(
                    text = style.labelBadge,
                    color = style.secondaryTextColor.copy(alpha = 0.80f),
                    fontSize = 5.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.sp
                )
                Spacer(modifier = Modifier.height(1.dp))
            }
            Text(
                text = catalogCode,
                color = style.secondaryTextColor.copy(alpha = 0.85f),
                fontSize = 6.8.sp,
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
                        color = Color.White.copy(alpha = if (isSelected) 0.70f else 0.45f),
                        start = Offset(0.5f, 0f),
                        end = Offset(0.5f, size.height),
                        strokeWidth = 1.2f
                    )
                    // Left dark recessed side edge (distinct jewel case plastic wall depth)
                    drawLine(
                        color = Color.Black.copy(alpha = 0.65f),
                        start = Offset(1.2.dp.toPx(), 0f),
                        end = Offset(1.2.dp.toPx(), size.height),
                        strokeWidth = 1.2.dp.toPx()
                    )
                    // Secondary inner refraction line
                    drawLine(
                        color = Color.White.copy(alpha = 0.22f),
                        start = Offset(2.2.dp.toPx(), 0f),
                        end = Offset(2.2.dp.toPx(), size.height),
                        strokeWidth = 0.8f
                    )
                    // Right dark recessed side edge (distinct jewel case plastic wall depth)
                    drawLine(
                        color = Color.Black.copy(alpha = 0.55f),
                        start = Offset(size.width - 2.0.dp.toPx(), 0f),
                        end = Offset(size.width - 2.0.dp.toPx(), size.height),
                        strokeWidth = 1.0.dp.toPx()
                    )
                    // Right edge seam / hinge groove shadow
                    drawLine(
                        color = Color.Black.copy(alpha = 0.75f),
                        start = Offset(size.width - 0.5f, 0f),
                        end = Offset(size.width - 0.5f, size.height),
                        strokeWidth = 1.5f
                    )
                    // Right edge subtle specular highlight
                    drawLine(
                        color = Color.White.copy(alpha = if (isSelected) 0.35f else 0.18f),
                        start = Offset(size.width - 1.0f, 0f),
                        end = Offset(size.width - 1.0f, size.height),
                        strokeWidth = 0.8f
                    )
                    // Top clear plastic edge highlight
                    drawLine(
                        color = Color.White.copy(alpha = 0.45f),
                        start = Offset(0f, 0.5f),
                        end = Offset(size.width, 0.5f),
                        strokeWidth = 1.0f
                    )
                    // Bottom edge physical shelf contact shadow
                    drawLine(
                        color = Color.Black.copy(alpha = 0.85f),
                        start = Offset(0f, size.height - 0.5f),
                        end = Offset(size.width, size.height - 0.5f),
                        strokeWidth = 1.8f
                    )
                    // Subtle transparent surface diagonal plastic sheen
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.14f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.10f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height * 0.4f)
                        )
                    )

                    // Occasional tiny physical micro-scratch across clear plastic case
                    if (hasScratch) {
                        val sy = size.height * scratchYFrac
                        drawLine(
                            color = Color.White.copy(alpha = 0.10f),
                            start = Offset(size.width * 0.20f, sy),
                            end = Offset(size.width * 0.75f, sy + 6.dp.toPx()),
                            strokeWidth = 0.6f
                        )
                    }
                }
        )

        // Top molded clear plastic tab highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.30f),
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Bottom molded clear plastic base lip resting on shelf
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.80f)
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

