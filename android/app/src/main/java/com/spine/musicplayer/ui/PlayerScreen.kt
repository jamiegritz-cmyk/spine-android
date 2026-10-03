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
        baseColor = Color(0xFF0F1012),
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
        baseColor = Color(0xFF0F1012),
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
 * Realistic Square CD Jewel Case Front Artwork.
 * Authentically models a physical CD jewel case:
 * - Square outer clear polystyrene case with physical bevelled edges and contact depth
 * - Real album artwork insert strictly preserving proportions without distortion
 * - Left clear acrylic hinge margin with molded pivot tabs and fluted ribbing
 * - Transparent plastic front lid with subtle specular sheen and restrained surface reflections
 * - Very subtle microscopic handling hairline scratches/scuffs
 * - Thumb tab opening notch on the right edge
 */
@Composable
fun JewelCaseArtwork(
    release: Release?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .aspectRatio(1f) // Strict square CD jewel case proportion
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(3.2.dp),
                ambientColor = Color.Black.copy(alpha = 0.6f),
                spotColor = Color.Black.copy(alpha = 0.8f)
            )
            .background(Color(0x08FFFFFF), RoundedCornerShape(3.2.dp))
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .drawBehind {
                // Outer clear acrylic perimeter bevel
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.36f),
                            Color.White.copy(alpha = 0.10f),
                            Color.Black.copy(alpha = 0.45f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    ),
                    cornerRadius = CornerRadius(3.2.dp.toPx(), 3.2.dp.toPx()),
                    style = Stroke(width = 1.0.dp.toPx())
                )

                // Visible plastic wall thickness refraction line
                val wallInset = 1.2.dp.toPx()
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.12f),
                    topLeft = Offset(wallInset, wallInset),
                    size = androidx.compose.ui.geometry.Size(size.width - wallInset * 2, size.height - wallInset * 2),
                    cornerRadius = CornerRadius(2.2.dp.toPx(), 2.2.dp.toPx()),
                    style = Stroke(width = 0.7f)
                )
            }
    ) {
        // Inner Booklet Insert (Nestled inside jewel case with left clear hinge margin)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 13.5.dp, top = 3.5.dp, end = 3.5.dp, bottom = 3.5.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(Color(0xFF1E1D1B))
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
                        contentScale = ContentScale.Crop, // Fills booklet insert crisply
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF262422)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Album,
                            contentDescription = null,
                            tint = Color(0xFF78716C),
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }
        }

        // Left Clear Acrylic Hinge Margin (Authentic physical CD jewel case hinge)
        Box(
            modifier = Modifier
                .width(13.5.dp)
                .fillMaxHeight()
                .drawBehind {
                    // Left translucent ribbed bevel
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.15f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.25f)
                            )
                        )
                    )
                    // Molded vertical ribs along the hinge
                    val rib1 = size.width * 0.28f
                    val rib2 = size.width * 0.50f
                    val rib3 = size.width * 0.72f
                    drawLine(
                        color = Color.White.copy(alpha = 0.16f),
                        start = Offset(rib1, 0f),
                        end = Offset(rib1, size.height),
                        strokeWidth = 0.8f
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.16f),
                        start = Offset(rib2, 0f),
                        end = Offset(rib2, size.height),
                        strokeWidth = 0.8f
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.16f),
                        start = Offset(rib3, 0f),
                        end = Offset(rib3, size.height),
                        strokeWidth = 0.8f
                    )
                    // Inner refraction vertical seam
                    drawLine(
                        color = Color.White.copy(alpha = 0.20f),
                        start = Offset(size.width - 0.5f, 0f),
                        end = Offset(size.width - 0.5f, size.height),
                        strokeWidth = 1.0f
                    )
                    // Molded circular hinge tabs
                    val tabRadius = 2.2.dp.toPx()
                    val tabX = size.width * 0.48f
                    drawCircle(
                        color = Color.White.copy(alpha = 0.38f),
                        radius = tabRadius,
                        center = Offset(tabX, size.height * 0.20f)
                    )
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.45f),
                        radius = tabRadius - 0.8f,
                        center = Offset(tabX, size.height * 0.20f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.38f),
                        radius = tabRadius,
                        center = Offset(tabX, size.height * 0.80f)
                    )
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.45f),
                        radius = tabRadius - 0.8f,
                        center = Offset(tabX, size.height * 0.80f)
                    )
                }
        )

        // Overlay: Clear Polystyrene Front Lid, Restrained Surface Sheen & Micro-scratches
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Restrained diagonal diffuse light sheen across the front lid
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.11f),
                                Color.White.copy(alpha = 0.02f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.05f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height * 0.72f)
                        )
                    )

                    // Secondary faint diagonal streak
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.06f),
                                Color.Transparent
                            ),
                            start = Offset(size.width * 0.20f, 0f),
                            end = Offset(size.width * 0.60f, size.height)
                        )
                    )

                    // Top/Left clear perimeter highlight line
                    drawLine(
                        color = Color.White.copy(alpha = 0.36f),
                        start = Offset(1f, 1f),
                        end = Offset(size.width - 2f, 1f),
                        strokeWidth = 1.0f
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.28f),
                        start = Offset(1f, 1f),
                        end = Offset(1f, size.height - 2f),
                        strokeWidth = 1.0f
                    )

                    // Bottom/Right dark bevel shadow line
                    drawLine(
                        color = Color.Black.copy(alpha = 0.55f),
                        start = Offset(size.width - 1f, 2f),
                        end = Offset(size.width - 1f, size.height - 1f),
                        strokeWidth = 1.2f
                    )
                    drawLine(
                        color = Color.Black.copy(alpha = 0.55f),
                        start = Offset(2f, size.height - 1f),
                        end = Offset(size.width - 1f, size.height - 1f),
                        strokeWidth = 1.2f
                    )

                    // Thumb tab opening notch on the right border
                    val notchY = size.height * 0.50f
                    drawLine(
                        color = Color.Black.copy(alpha = 0.40f),
                        start = Offset(size.width - 1.5.dp.toPx(), notchY - 8.dp.toPx()),
                        end = Offset(size.width - 1.5.dp.toPx(), notchY + 8.dp.toPx()),
                        strokeWidth = 1.2f
                    )

                    // Very subtle microscopic hairline scratches (authentic physical handling wear)
                    // Scratch 1: Near top-right
                    drawLine(
                        color = Color.White.copy(alpha = 0.12f),
                        start = Offset(size.width * 0.73f, size.height * 0.16f),
                        end = Offset(size.width * 0.81f, size.height * 0.20f),
                        strokeWidth = 0.6f
                    )
                    // Scratch 2: Near bottom-left
                    drawLine(
                        color = Color.White.copy(alpha = 0.10f),
                        start = Offset(size.width * 0.28f, size.height * 0.72f),
                        end = Offset(size.width * 0.34f, size.height * 0.74f),
                        strokeWidth = 0.5f
                    )
                    // Scratch 3: Near lower right
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(size.width * 0.62f, size.height * 0.81f),
                        end = Offset(size.width * 0.66f, size.height * 0.79f),
                        strokeWidth = 0.5f
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
