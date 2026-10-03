package com.spine.musicplayer.ui

import android.content.res.Configuration
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.spine.musicplayer.model.Release
import com.spine.musicplayer.model.RepeatMode
import com.spine.musicplayer.model.Track
import com.spine.musicplayer.viewmodel.PlayerUiState
import java.util.Locale

enum class LibraryFilterMode {
    ALBUMS, SINGLES
}

@Composable
fun PlayerScreen(
    uiState: PlayerUiState,
    onSelectRelease: (Int) -> Unit,
    onSelectTrack: (Int) -> Unit = {},
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var filterMode by remember { mutableStateOf(LibraryFilterMode.ALBUMS) }

    Scaffold(
        containerColor = Color(0xFF141312),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (isLandscape) {
            LandscapePlayerLayout(
                uiState = uiState,
                filterMode = filterMode,
                onFilterModeChange = { filterMode = it },
                onRefresh = onRefresh,
                onSelectRelease = onSelectRelease,
                onSelectTrack = onSelectTrack,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onPrevious = onPrevious,
                onSeek = onSeek,
                onToggleShuffle = onToggleShuffle,
                onCycleRepeat = onCycleRepeat,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            PortraitPlayerLayout(
                uiState = uiState,
                filterMode = filterMode,
                onFilterModeChange = { filterMode = it },
                onRefresh = onRefresh,
                onSelectRelease = onSelectRelease,
                onSelectTrack = onSelectTrack,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onPrevious = onPrevious,
                onSeek = onSeek,
                onToggleShuffle = onToggleShuffle,
                onCycleRepeat = onCycleRepeat,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

/**
 * Standard Portrait Phone Layout (Google Pixel optimized).
 * Clean, balanced, neutral lighting.
 */
@Composable
private fun PortraitPlayerLayout(
    uiState: PlayerUiState,
    filterMode: LibraryFilterMode,
    onFilterModeChange: (LibraryFilterMode) -> Unit,
    onRefresh: () -> Unit,
    onSelectRelease: (Int) -> Unit,
    onSelectTrack: (Int) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTracklistSheet by remember { mutableStateOf(false) }

    val activeReleases = remember(uiState.releases, filterMode) {
        when (filterMode) {
            LibraryFilterMode.ALBUMS -> {
                // Albums view displays the entire album collection without discarding single-track albums
                uiState.releases
            }
            LibraryFilterMode.SINGLES -> {
                val sgl = uiState.releases.filter { it.tracks.size <= 1 }
                if (sgl.isNotEmpty()) sgl else uiState.releases
            }
        }
    }
    val currentRelease = uiState.currentRelease

    // Very subtle tactile paper / lightly brushed texture on near-black background
    val tactileBgBrush = rememberTactileTextureBrush(
        baseColor = Color(0xFF0E0F11),
        grainVariance = 3,
        seed = 42L
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(tactileBgBrush),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar: GRAIZ [menu] on left, [Albums] [Singles] [Refresh] on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: GRAIZ brand title + menu icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "GRAIZ",
                    style = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = 2.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF5F5F4),
                        fontSize = 15.sp
                    )
                )
                IconButton(
                    onClick = { showTracklistSheet = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Menu,
                        contentDescription = "Tracklist Menu",
                        tint = Color(0xFFA8A29E),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Right: [Albums] [Singles] segment + [Refresh]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AlbumsSinglesSegment(
                    currentMode = filterMode,
                    onModeSelected = { newMode ->
                        if (filterMode != newMode) {
                            onFilterModeChange(newMode)
                            val targetList = when (newMode) {
                                LibraryFilterMode.ALBUMS -> uiState.releases
                                LibraryFilterMode.SINGLES -> {
                                    val sgl = uiState.releases.filter { it.tracks.size <= 1 }
                                    if (sgl.isNotEmpty()) sgl else uiState.releases
                                }
                            }
                            val firstTarget = targetList.firstOrNull()
                            if (firstTarget != null) {
                                val targetIdx = uiState.releases.indexOf(firstTarget)
                                if (targetIdx >= 0) onSelectRelease(targetIdx)
                            }
                        }
                    }
                )

                RefreshButton(
                    isRefreshing = uiState.isLoading,
                    onClick = onRefresh
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Compact Square Jewel Case Artwork (Secondary to the CD collection, tapping toggles play/pause)
        JewelCaseArtwork(
            release = currentRelease,
            onClick = onPlayPause,
            modifier = Modifier
                .size(170.dp)
                .padding(4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Track Title & Artist (Displays active track title; clicking opens album tracklist)
        val currentTrack = uiState.currentTrack
        val displayTitle = currentTrack?.title ?: currentRelease?.title ?: "Select an Album"
        val displayArtist = currentTrack?.artist ?: currentRelease?.artist ?: "Physical Collection"

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    if (currentRelease != null && currentRelease.tracks.isNotEmpty()) {
                        showTracklistSheet = true
                    }
                }
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFFF5F5F4),
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = displayArtist,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFFA8A29E)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Playback Progress Bar with Timers
        PlaybackProgressBar(
            positionMs = uiState.currentPositionMs,
            durationMs = uiState.durationMs,
            onSeek = onSeek,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Playback Controls Row: Shuffle, Prev, Play/Pause, Next, Repeat
        ControlsRow(
            isPlaying = uiState.isPlaying,
            isShuffle = uiState.isShuffle,
            repeatMode = uiState.repeatMode,
            onPlayPause = onPlayPause,
            onNext = onNext,
            onPrevious = onPrevious,
            onToggleShuffle = onToggleShuffle,
            onCycleRepeat = onCycleRepeat,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // DOMINANT PHYSICAL WOODEN SHELF: Lower 45-50% of the screen
        val shelfSelectedIndex = remember(activeReleases, currentRelease) {
            val idx = activeReleases.indexOf(currentRelease)
            if (idx >= 0) idx else 0
        }

        SpineShelf(
            releases = activeReleases,
            selectedIndex = shelfSelectedIndex,
            onSelectRelease = { index ->
                if (index in activeReleases.indices) {
                    val sel = activeReleases[index]
                    val orig = uiState.releases.indexOf(sel)
                    if (orig >= 0) {
                        onSelectRelease(orig)
                    }
                }
            },
            shelfHeight = 380.dp,
            modifier = Modifier.fillMaxWidth()
        )

        // Modal Album Tracklist Bottom Sheet
        if (showTracklistSheet && currentRelease != null) {
            AlbumTracklistSheet(
                release = currentRelease,
                currentTrackIndex = uiState.currentTrackIndex,
                isPlaying = uiState.isPlaying,
                onSelectTrack = { trackIdx ->
                    onSelectTrack(trackIdx)
                    showTracklistSheet = false
                },
                onDismiss = { showTracklistSheet = false }
            )
        }
    }
}

/**
 * Landscape / Tablet Layout.
 * Large artwork on left, playback controls & info on right, wooden CD shelf along bottom.
 */
@Composable
private fun LandscapePlayerLayout(
    uiState: PlayerUiState,
    filterMode: LibraryFilterMode,
    onFilterModeChange: (LibraryFilterMode) -> Unit,
    onRefresh: () -> Unit,
    onSelectRelease: (Int) -> Unit,
    onSelectTrack: (Int) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTracklistSheet by remember { mutableStateOf(false) }

    val activeReleases = remember(uiState.releases, filterMode) {
        when (filterMode) {
            LibraryFilterMode.ALBUMS -> {
                // Albums view displays the entire album collection without discarding single-track albums
                uiState.releases
            }
            LibraryFilterMode.SINGLES -> {
                val sgl = uiState.releases.filter { it.tracks.size <= 1 }
                if (sgl.isNotEmpty()) sgl else uiState.releases
            }
        }
    }
    val currentRelease = uiState.currentRelease

    val tactileBgBrush = rememberTactileTextureBrush(
        baseColor = Color(0xFF0E0F11),
        grainVariance = 3,
        seed = 42L
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(tactileBgBrush)
    ) {
        // Top Bar: GRAIZ [menu] on left, [Albums] [Singles] [Refresh] on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: GRAIZ brand title + menu icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "GRAIZ",
                    style = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = 2.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF5F5F4),
                        fontSize = 14.sp
                    )
                )
                IconButton(
                    onClick = { showTracklistSheet = true },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Menu,
                        contentDescription = "Tracklist Menu",
                        tint = Color(0xFFA8A29E),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Right: [Albums] [Singles] segment + [Refresh]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AlbumsSinglesSegment(
                    currentMode = filterMode,
                    onModeSelected = { newMode ->
                        if (filterMode != newMode) {
                            onFilterModeChange(newMode)
                            val targetList = when (newMode) {
                                LibraryFilterMode.ALBUMS -> uiState.releases
                                LibraryFilterMode.SINGLES -> {
                                    val sgl = uiState.releases.filter { it.tracks.size <= 1 }
                                    if (sgl.isNotEmpty()) sgl else uiState.releases
                                }
                            }
                            val firstTarget = targetList.firstOrNull()
                            if (firstTarget != null) {
                                val targetIdx = uiState.releases.indexOf(firstTarget)
                                if (targetIdx >= 0) onSelectRelease(targetIdx)
                            }
                        }
                    }
                )

                RefreshButton(
                    isRefreshing = uiState.isLoading,
                    onClick = onRefresh
                )
            }
        }

        // Main Content Split: Left Responsive Square Artwork, Right Controls
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Square Album Cover (always 1:1, never clipped, fits available height)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                JewelCaseArtwork(
                    release = currentRelease,
                    onClick = onPlayPause,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Right: Info and Playback Controls
            val currentTrack = uiState.currentTrack
            val displayTitle = currentTrack?.title ?: currentRelease?.title ?: "Select an Album"
            val displayArtist = currentTrack?.artist ?: currentRelease?.artist ?: "Physical Collection"

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            if (currentRelease != null && currentRelease.tracks.isNotEmpty()) {
                                showTracklistSheet = true
                            }
                        }
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFFF5F5F4),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$displayArtist · ${currentRelease?.year ?: ""}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFA8A29E),
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                PlaybackProgressBar(
                    positionMs = uiState.currentPositionMs,
                    durationMs = uiState.durationMs,
                    onSeek = onSeek,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                ControlsRow(
                    isPlaying = uiState.isPlaying,
                    isShuffle = uiState.isShuffle,
                    repeatMode = uiState.repeatMode,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onToggleShuffle = onToggleShuffle,
                    onCycleRepeat = onCycleRepeat,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Horizontal CD Shelf at the bottom
        val shelfSelectedIndex = remember(activeReleases, currentRelease) {
            val idx = activeReleases.indexOf(currentRelease)
            if (idx >= 0) idx else 0
        }

        SpineShelf(
            releases = activeReleases,
            selectedIndex = shelfSelectedIndex,
            onSelectRelease = { index ->
                if (index in activeReleases.indices) {
                    val sel = activeReleases[index]
                    val orig = uiState.releases.indexOf(sel)
                    if (orig >= 0) {
                        onSelectRelease(orig)
                    }
                }
            },
            shelfHeight = 160.dp,
            modifier = Modifier.fillMaxWidth()
        )

        // Modal Album Tracklist Bottom Sheet
        if (showTracklistSheet && currentRelease != null) {
            AlbumTracklistSheet(
                release = currentRelease,
                currentTrackIndex = uiState.currentTrackIndex,
                isPlaying = uiState.isPlaying,
                onSelectTrack = { trackIdx ->
                    onSelectTrack(trackIdx)
                    showTracklistSheet = false
                },
                onDismiss = { showTracklistSheet = false }
            )
        }
    }
}

/**
 * Realistic Physical CD Jewel Case Component.
 *
 * Visual stacking order:
 * 1. Faint physical shadow underneath the case
 * 2. Transparent outer case (clear acrylic with rounded corners)
 * 3. Album artwork insert (inset: left 15dp, top 7.5dp, right 7.5dp, bottom 7.5dp)
 * 4. Transparent plastic front lid / edge highlights (2-3dp outer edge, 1-2dp inner bevel, drawn over edges)
 * 5. Left hinge / spine details (15dp wide: darker inner depth, vertical ribs, hinge seam, 2 circular pivot points)
 */
@Composable
fun JewelCaseArtwork(
    release: Release?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val spineWidth = 15.dp
    val topMargin = 7.5.dp
    val rightMargin = 7.5.dp
    val bottomMargin = 7.5.dp

    // 1. SHADOW & 2. TRANSPARENT OUTER CASE
    Box(
        modifier = modifier
            .aspectRatio(1f) // Strict square CD jewel case proportion
            // 1. Faint physical shadow underneath
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(3.dp),
                ambientColor = Color.Black.copy(alpha = 0.45f),
                spotColor = Color.Black.copy(alpha = 0.60f)
            )
            // 2. Transparent outer case shell (clear plastic allowing dark material through)
            .background(Color(0x08FFFFFF), RoundedCornerShape(3.dp))
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
    ) {
        // 3. ALBUM ARTWORK INSERT
        // Sits INSIDE the case, strictly beginning AFTER the 15dp left spine, with 7.5dp margins on top/right/bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = spineWidth,
                    top = topMargin,
                    end = rightMargin,
                    bottom = bottomMargin
                )
                // Subtle paper booklet drop shadow inside the tray recess
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(1.dp),
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = Color.Black.copy(alpha = 0.7f)
                )
                .clip(RoundedCornerShape(1.dp))
                .background(Color(0xFF191816))
        ) {
            Crossfade(
                targetState = release?.artworkUri,
                animationSpec = tween(300),
                label = "albumArtCrossfade"
            ) { artUri ->
                if (artUri != null) {
                    AsyncImage(
                        model = artUri,
                        contentDescription = release?.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF242220)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Album,
                            contentDescription = null,
                            tint = Color(0xFF78716C),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            // Paper booklet insert edge border
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(
                            color = Color.Black.copy(alpha = 0.35f),
                            style = Stroke(width = 1.0f)
                        )
                    }
            )
        }

        // 4. TRANSPARENT PLASTIC FRONT LID & EDGE HIGHLIGHTS / BEVELS
        // Drawn OVER the artwork at the edges
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val w = size.width
                    val h = size.height

                    // 2-3dp visible outer edge:
                    // Subtle transparent white highlight along top & left outer edge
                    val outerEdgeW = 2.2.dp.toPx()
                    drawLine(
                        color = Color.White.copy(alpha = 0.40f),
                        start = Offset(0f, outerEdgeW / 2),
                        end = Offset(w, outerEdgeW / 2),
                        strokeWidth = outerEdgeW
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.35f),
                        start = Offset(outerEdgeW / 2, 0f),
                        end = Offset(outerEdgeW / 2, h),
                        strokeWidth = outerEdgeW
                    )

                    // Subtle dark edge/refraction along bottom & right outer edge
                    drawLine(
                        color = Color.Black.copy(alpha = 0.55f),
                        start = Offset(w - outerEdgeW / 2, 0f),
                        end = Offset(w - outerEdgeW / 2, h),
                        strokeWidth = outerEdgeW
                    )
                    drawLine(
                        color = Color.Black.copy(alpha = 0.55f),
                        start = Offset(0f, h - outerEdgeW / 2),
                        end = Offset(w, h - outerEdgeW / 2),
                        strokeWidth = outerEdgeW
                    )

                    // 1-2dp inner bevel refraction line inset by ~2.5dp
                    val bevelInset = 2.5.dp.toPx()
                    val bevelW = 1.2.dp.toPx()
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.22f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.35f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(w, h)
                        ),
                        topLeft = Offset(bevelInset, bevelInset),
                        size = androidx.compose.ui.geometry.Size(
                            w - bevelInset * 2,
                            h - bevelInset * 2
                        ),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                        style = Stroke(width = bevelW)
                    )

                    // Subtle front lid surface light sheen
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.White.copy(alpha = 0.02f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.04f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(w * 0.85f, h * 0.70f)
                        )
                    )

                    // Faint highlight on right edge
                    val rightEdgeX = w - 1.2.dp.toPx()
                    drawLine(
                        color = Color.White.copy(alpha = 0.18f),
                        start = Offset(rightEdgeX, topMargin.toPx()),
                        end = Offset(rightEdgeX, h - bottomMargin.toPx()),
                        strokeWidth = 1.0.dp.toPx()
                    )
                }
        )

        // 5. HINGE DETAILS (Left 15dp transparent plastic hinge / spine)
        // Remains visibly separate from the artwork; artwork begins strictly AFTER this spine
        Box(
            modifier = Modifier
                .width(spineWidth)
                .fillMaxHeight()
                .drawBehind {
                    val hw = size.width
                    val hh = size.height

                    // Transparent darker inner spine with depth
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.18f),
                                Color.Black.copy(alpha = 0.22f),
                                Color.Black.copy(alpha = 0.45f)
                            )
                        )
                    )

                    // 2-3 very subtle vertical moulded ribs
                    val rib1 = hw * 0.30f
                    val rib2 = hw * 0.55f
                    val rib3 = hw * 0.75f
                    // Rib highlights
                    drawLine(
                        color = Color.White.copy(alpha = 0.20f),
                        start = Offset(rib1, 0f),
                        end = Offset(rib1, hh),
                        strokeWidth = 0.8f
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.20f),
                        start = Offset(rib2, 0f),
                        end = Offset(rib2, hh),
                        strokeWidth = 0.8f
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.18f),
                        start = Offset(rib3, 0f),
                        end = Offset(rib3, hh),
                        strokeWidth = 0.8f
                    )
                    // Rib shadows for moulded depth
                    drawLine(
                        color = Color.Black.copy(alpha = 0.35f),
                        start = Offset(rib1 + 1f, 0f),
                        end = Offset(rib1 + 1f, hh),
                        strokeWidth = 0.8f
                    )
                    drawLine(
                        color = Color.Black.copy(alpha = 0.35f),
                        start = Offset(rib2 + 1f, 0f),
                        end = Offset(rib2 + 1f, hh),
                        strokeWidth = 0.8f
                    )

                    // Thin clear highlight along the hinge seam where the spine meets the artwork area
                    drawLine(
                        color = Color.White.copy(alpha = 0.30f),
                        start = Offset(hw - 1.2f, 0f),
                        end = Offset(hw - 1.2f, hh),
                        strokeWidth = 1.0f
                    )
                    drawLine(
                        color = Color.Black.copy(alpha = 0.50f),
                        start = Offset(hw - 0.2f, 0f),
                        end = Offset(hw - 0.2f, hh),
                        strokeWidth = 1.0f
                    )

                    // 2 small circular hinge / pivot details (one near upper 18%, one near lower 82%)
                    val pivotRadius = 3.0.dp.toPx()
                    val pivotCenterX = hw * 0.48f
                    val topPivotY = hh * 0.18f
                    val bottomPivotY = hh * 0.82f

                    // Top circular hinge pivot
                    drawCircle(
                        color = Color.White.copy(alpha = 0.45f),
                        radius = pivotRadius,
                        center = Offset(pivotCenterX, topPivotY),
                        style = Stroke(width = 1.0.dp.toPx())
                    )
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.55f),
                        radius = pivotRadius - 0.7.dp.toPx(),
                        center = Offset(pivotCenterX, topPivotY)
                    )

                    // Bottom circular hinge pivot
                    drawCircle(
                        color = Color.White.copy(alpha = 0.45f),
                        radius = pivotRadius,
                        center = Offset(pivotCenterX, bottomPivotY),
                        style = Stroke(width = 1.0.dp.toPx())
                    )
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.55f),
                        radius = pivotRadius - 0.7.dp.toPx(),
                        center = Offset(pivotCenterX, bottomPivotY)
                    )
                }
        )
    }
}

@Composable
fun PlaybackProgressBar(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    Column(modifier = modifier) {
        Slider(
            value = progress,
            onValueChange = { frac ->
                onSeek((frac * durationMs).toLong())
            },
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFF5F5F4),
                activeTrackColor = Color(0xFFD6D3D1),
                inactiveTrackColor = Color(0xFF292524)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(positionMs),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF78716C),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            )
            Text(
                text = formatDuration(durationMs),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF78716C),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            )
        }
    }
}

@Composable
fun ControlsRow(
    isPlaying: Boolean,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle Button
        IconButton(onClick = onToggleShuffle) {
            Icon(
                imageVector = Icons.Rounded.Shuffle,
                contentDescription = "Shuffle",
                tint = if (isShuffle) Color(0xFFF5F5F4) else Color(0xFF57534E)
            )
        }

        // Previous Button
        IconButton(
            onClick = onPrevious,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = "Previous Track",
                tint = Color(0xFFE7E5E4),
                modifier = Modifier.size(28.dp)
            )
        }

        // Play/Pause Floating Circle
        FilledIconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(60.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color(0xFFF5F5F4),
                contentColor = Color(0xFF141312)
            )
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(32.dp)
            )
        }

        // Next Button
        IconButton(
            onClick = onNext,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Next Track",
                tint = Color(0xFFE7E5E4),
                modifier = Modifier.size(28.dp)
            )
        }

        // Repeat Button
        IconButton(onClick = onCycleRepeat) {
            Icon(
                imageVector = when (repeatMode) {
                    RepeatMode.ONE -> Icons.Rounded.RepeatOne
                    else -> Icons.Rounded.Repeat
                },
                contentDescription = "Repeat",
                tint = if (repeatMode != RepeatMode.OFF) Color(0xFFF5F5F4) else Color(0xFF57534E)
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
}

@Composable
fun AlbumsSinglesSegment(
    currentMode: LibraryFilterMode,
    onModeSelected: (LibraryFilterMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0xFF1F1D1B), RoundedCornerShape(12.dp))
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Albums Tab
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (currentMode == LibraryFilterMode.ALBUMS) Color(0xFF383430) else Color.Transparent)
                .clickable { onModeSelected(LibraryFilterMode.ALBUMS) }
                .padding(horizontal = 9.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Albums",
                color = if (currentMode == LibraryFilterMode.ALBUMS) Color.White else Color(0xFFA8A29E),
                fontSize = 11.sp,
                fontWeight = if (currentMode == LibraryFilterMode.ALBUMS) FontWeight.SemiBold else FontWeight.Normal
            )
        }
        // Singles Tab
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (currentMode == LibraryFilterMode.SINGLES) Color(0xFF383430) else Color.Transparent)
                .clickable { onModeSelected(LibraryFilterMode.SINGLES) }
                .padding(horizontal = 9.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Singles",
                color = if (currentMode == LibraryFilterMode.SINGLES) Color.White else Color(0xFFA8A29E),
                fontSize = 11.sp,
                fontWeight = if (currentMode == LibraryFilterMode.SINGLES) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun RefreshButton(
    isRefreshing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "refreshSpin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "spinAngle"
    )
    IconButton(
        onClick = onClick,
        enabled = !isRefreshing,
        modifier = modifier.size(32.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Refresh,
            contentDescription = "Refresh Library",
            tint = if (isRefreshing) Color(0xFFD6D3D1) else Color(0xFFA8A29E),
            modifier = Modifier
                .size(18.dp)
                .then(if (isRefreshing) Modifier.rotate(angle) else Modifier)
        )
    }
}

/**
 * Album Tracklist Modal Bottom Sheet.
 * Lists the album's tracks in authentic metadata order (1, 2, 3...)
 * Allows tapping any track to play and continue through the album.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumTracklistSheet(
    release: Release,
    currentTrackIndex: Int,
    isPlaying: Boolean,
    onSelectTrack: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161514),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = Color(0xFF55504A)
            )
        },
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = release.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF5F5F4)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${release.artist} • ${release.tracks.size} tracks",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFFA8A29E)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(release.tracks) { index, track ->
                    val isCurrent = index == currentTrackIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCurrent) Color(0xFF282624) else Color.Transparent)
                            .clickable {
                                onSelectTrack(index)
                                onDismiss()
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${track.trackNumber}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isCurrent) Color(0xFFF5F5F4) else Color(0xFF78716C),
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            ),
                            modifier = Modifier.width(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isCurrent) Color(0xFFF5F5F4) else Color(0xFFD6D3D1),
                                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (track.artist != release.artist) {
                                Text(
                                    text = track.artist,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF78716C)
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (isCurrent && isPlaying) {
                            Icon(
                                imageVector = Icons.Rounded.VolumeUp,
                                contentDescription = "Playing",
                                tint = Color(0xFFF5F5F4),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = formatDuration(track.durationMs),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF78716C)
                            )
                        )
                    }
                }
            }
        }
    }
}
