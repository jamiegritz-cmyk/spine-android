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
    val parsedColor = remember(release.spineColorHex) {
        try {
            Color(android.graphics.Color.parseColor(release.spineColorHex))
        } catch (_: Exception) {
            Color(0xFF222222)
        }
    }

    val caseHash = remember(release.id) { kotlin.math.abs(release.id.hashCode()) }
    val hasScratch = (caseHash % 3) != 0
    val scratchYFrac = remember(caseHash) { 0.22f + ((caseHash % 55) / 100f) }

    // 4-digit catalog code (e.g. 5802, 1772, 8279)
    val catalogCode = remember(release.id, release.catalogNumber) {
        val digits = release.catalogNumber.filter { it.isDigit() }
        if (digits.length >= 4) {
            digits.takeLast(4)
        } else {
            String.format(java.util.Locale.ROOT, "%04d", (caseHash % 9000) + 1000)
        }
    }

    // Outer physical jewel case shell container
    Box(
        modifier = modifier
            .offset(y = verticalOffset)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .width(caseWidth)
            .height(caseHeight)
            .shadow(
                elevation,
                shape = RoundedCornerShape(topStart = 1.5.dp, topEnd = 1.5.dp, bottomStart = 0.5.dp, bottomEnd = 0.5.dp)
            )
            .background(
                Color(0x0EFFFFFF),
                RoundedCornerShape(topStart = 1.5.dp, topEnd = 1.5.dp, bottomStart = 0.5.dp, bottomEnd = 0.5.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // --- 1. Clear Acrylic Case Base (Visible in top cap and bottom foot) ---
        // Sits behind the paper sleeve, creating authentic jewel case depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Top clear cap acrylic gradient (top 13dp)
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.16f),
                                Color.White.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        ),
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, 13.dp.toPx())
                    )
                    // Bottom clear foot acrylic gradient (bottom 9dp)
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.25f),
                                Color.Black.copy(alpha = 0.55f)
                            )
                        ),
                        topLeft = Offset(0f, size.height - 9.dp.toPx()),
                        size = Size(size.width, 9.dp.toPx())
                    )
                }
        )

        // --- 2. Inset Printed Paper Sleeve / Tray Card Inlay ---
        // Inset slightly inside the transparent plastic case (2dp left/right, 13dp top, 9dp bottom)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 2.dp, end = 2.dp, top = 13.dp, bottom = 9.dp)
                .clip(RoundedCornerShape(0.5.dp))
                .background(parsedColor)
        ) {
            // Background Artwork Slice or Paper Texture
            if (release.artworkUri != null) {
                AsyncImage(
                    model = release.artworkUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Printed Cardstock Paper Sheen & Grain (eliminates flat digital look)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.10f),
                                Color.Black.copy(alpha = 0.22f),
                                Color.Black.copy(alpha = 0.18f),
                                Color.Black.copy(alpha = 0.48f)
                            )
                        )
                    )
            )

            // Fine printed die-cut paper edge
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(
                            color = Color.Black.copy(alpha = 0.35f),
                            style = Stroke(width = 0.6f)
                        )
                    }
            )

            // Optional Top Artwork Badge / Thumbnail (as seen in reference image)
            val showArtworkBadge = (caseHash % 2 == 0) && release.artworkUri != null
            if (showArtworkBadge) {
                Box(
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .size(13.dp)
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
                                    color = Color.White.copy(alpha = 0.25f),
                                    style = Stroke(width = 0.5f)
                                )
                            }
                    )
                }
            }

            // Vertically Printed Spine Typography
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
                                color = typo.artistColor.copy(alpha = 0.65f)
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
                                color = typo.artistColor.copy(alpha = 0.65f)
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
                    .padding(top = if (showArtworkBadge) 18.dp else 6.dp, bottom = 22.dp),
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
                            blurRadius = 2.5f
                        )
                    ),
                    modifier = Modifier.verticalSpineText()
                )
            }

            // Bottom Section: Mini Record Label / Disc Emblem + 4-Digit Catalog Number
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tiny Compact Disc / Record Label Mark (subtle circle emblem)
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(Color.White.copy(alpha = 0.40f))
                        .drawBehind {
                            drawCircle(
                                color = Color.Black.copy(alpha = 0.6f),
                                radius = 1.0f
                            )
                        }
                )
                Spacer(modifier = Modifier.height(1.5.dp))
                // 4-Digit Printed Catalog Number (e.g. 5802, 1772, 8279)
                Text(
                    text = catalogCode,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 6.8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.8f),
                            offset = Offset(0f, 1f),
                            blurRadius = 1.5f
                        )
                    )
                )
            }
        }

        // --- 3. Authentic Clear Polystyrene Jewel Case Glass, Edges & Reflections ---
        // Top overlay layer: provides visible case thickness, edge refractions, and restrained highlights
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    val leftWallPx = 2.dp.toPx()
                    val rightWallPx = w - 2.dp.toPx()

                    // --- TOP CLEAR ACRYLIC CAP DETAILS ---
                    // Specular rim at very top edge
                    drawLine(
                        color = Color.White.copy(alpha = if (isSelected) 0.70f else 0.50f),
                        start = Offset(0.5f, 0.5f),
                        end = Offset(w - 0.5f, 0.5f),
                        strokeWidth = 1.0f
                    )
                    // Molded horizontal notch 1 (jewel case hinge tooth)
                    drawLine(
                        color = Color.Black.copy(alpha = 0.45f),
                        start = Offset(1.5.dp.toPx(), 4.dp.toPx()),
                        end = Offset(w - 1.5.dp.toPx(), 4.dp.toPx()),
                        strokeWidth = 0.8f
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.35f),
                        start = Offset(1.5.dp.toPx(), 4.8.dp.toPx()),
                        end = Offset(w - 1.5.dp.toPx(), 4.8.dp.toPx()),
                        strokeWidth = 0.6f
                    )
                    // Molded horizontal notch 2 (secondary ridge)
                    drawLine(
                        color = Color.Black.copy(alpha = 0.30f),
                        start = Offset(2.dp.toPx(), 8.dp.toPx()),
                        end = Offset(w - 2.dp.toPx(), 8.dp.toPx()),
                        strokeWidth = 0.7f
                    )

                    // --- LEFT TRANSPARENT PLASTIC WALL (Visible Thickness) ---
                    // Left outer specular hairline highlight
                    drawLine(
                        color = Color.White.copy(alpha = if (isSelected) 0.65f else 0.45f),
                        start = Offset(0.5f, 0f),
                        end = Offset(0.5f, h),
                        strokeWidth = 1.0f
                    )
                    // Left acrylic plastic wall refraction shadow (creates physical thickness)
                    drawLine(
                        color = Color.Black.copy(alpha = 0.50f),
                        start = Offset(1.2.dp.toPx(), 0f),
                        end = Offset(1.2.dp.toPx(), h),
                        strokeWidth = 1.0.dp.toPx()
                    )
                    // Left inner refraction highlight where plastic meets paper sleeve
                    drawLine(
                        color = Color.White.copy(alpha = 0.22f),
                        start = Offset(leftWallPx, 0f),
                        end = Offset(leftWallPx, h),
                        strokeWidth = 0.6f
                    )

                    // --- RIGHT TRANSPARENT PLASTIC WALL (Visible Thickness) ---
                    // Right inner paper-edge refraction shadow
                    drawLine(
                        color = Color.Black.copy(alpha = 0.40f),
                        start = Offset(rightWallPx, 0f),
                        end = Offset(rightWallPx, h),
                        strokeWidth = 0.8f
                    )
                    // Right acrylic plastic wall thickness
                    drawLine(
                        color = Color.Black.copy(alpha = 0.55f),
                        start = Offset(w - 1.2.dp.toPx(), 0f),
                        end = Offset(w - 1.2.dp.toPx(), h),
                        strokeWidth = 1.0.dp.toPx()
                    )
                    // Right edge case seam / hinge groove
                    drawLine(
                        color = Color.Black.copy(alpha = 0.70f),
                        start = Offset(w - 0.5f, 0f),
                        end = Offset(w - 0.5f, h),
                        strokeWidth = 1.2f
                    )
                    // Right edge corner specular highlight
                    drawLine(
                        color = Color.White.copy(alpha = if (isSelected) 0.35f else 0.20f),
                        start = Offset(w - 1.0f, 0f),
                        end = Offset(w - 1.0f, h),
                        strokeWidth = 0.8f
                    )

                    // --- BOTTOM CLEAR FOOT & PHYSICAL SHELF CONTACT ---
                    // Dark grounding ambient contact shadow line (case firmly seated on wood)
                    drawLine(
                        color = Color.Black.copy(alpha = 0.95f),
                        start = Offset(0f, h - 0.75f),
                        end = Offset(w, h - 0.75f),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    // Clear plastic bottom edge reflection catch
                    drawLine(
                        color = Color.White.copy(alpha = 0.20f),
                        start = Offset(1.dp.toPx(), h - 2.dp.toPx()),
                        end = Offset(w - 1.dp.toPx(), h - 2.dp.toPx()),
                        strokeWidth = 0.7f
                    )

                    // --- RESTRAINED FRONT SURFACE SHEEN ---
                    // Very subtle realistic polystyrene light sheen (no neon, no heavy glow)
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.06f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(w, h * 0.45f)
                        )
                    )

                    // Occasional fine physical hairline scratch
                    if (hasScratch) {
                        val sy = h * scratchYFrac
                        drawLine(
                            color = Color.White.copy(alpha = 0.12f),
                            start = Offset(w * 0.20f, sy),
                            end = Offset(w * 0.75f, sy + 5.dp.toPx()),
                            strokeWidth = 0.5f
                        )
                    }
                }
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

        // Curated palette of authentic CD spine text colours: cream, turquoise, red, yellow, white, mint, etc.
        val spineTextColorPalette = listOf(
            Color(0xFFFFF7ED), // Warm Cream
            Color(0xFF38BDF8), // Vivid Turquoise / Sky
            Color(0xFFFB7185), // Coral Red / Rose
            Color(0xFFFDE047), // Sunny Yellow / Gold
            Color(0xFFFFFFFF), // Crisp Clean White
            Color(0xFF86EFAC), // Mint Green
            Color(0xFFFDBA74), // Warm Tangerine / Amber
            Color(0xFFE9D5FF), // Lilac / Pale Purple
            Color(0xFF67E8F9), // Ice Aqua
            Color(0xFFF472B6)  // Vibrant Fuchsia Pink
        )
        val textPaletteIndex = abs((hash xor parsedColor.hashCode())) % spineTextColorPalette.size
        val primaryColor = spineTextColorPalette[textPaletteIndex]
        val secondaryColor = if (primaryColor == Color(0xFFFFFFFF)) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.92f)
        val shadowColor = Color.Black.copy(alpha = 0.95f)

        // Prominent, legible font scaling for title and artist so the complete text fits and pops
        val totalLength = release.title.length + release.artist.length
        val (titleSize, artistSize, spacing) = when {
            totalLength <= 18 -> Triple(9.4.sp, 8.4.sp, 0.4.sp)
            totalLength <= 28 -> Triple(8.5.sp, 7.6.sp, 0.2.sp)
            totalLength <= 40 -> Triple(7.8.sp, 6.8.sp, 0.sp)
            totalLength <= 55 -> Triple(7.0.sp, 6.2.sp, (-0.1).sp)
            else -> Triple(6.2.sp, 5.6.sp, (-0.2).sp)
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
                letterSpacing = (spacing.value + 0.3f).sp,
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
