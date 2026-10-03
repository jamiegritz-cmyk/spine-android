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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
        containerColor = Color(0xFF121110),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (isLandscape) {
            LandscapePlayerLayout(
                uiState = uiState,
                filterMode = filterMode,
                onFilterModeChange = { filterMode = it },
                onRefresh = onRefresh,
                onSelectRelease = onSelectRelease,
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
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeReleases = remember(uiState.releases, filterMode) {
        when (filterMode) {
            LibraryFilterMode.ALBUMS -> {
                val alb = uiState.releases.filter { it.tracks.size > 1 }
                if (alb.isNotEmpty()) alb else uiState.releases
            }
            LibraryFilterMode.SINGLES -> {
                val sgl = uiState.releases.filter { it.tracks.size <= 1 }
                if (sgl.isNotEmpty()) sgl else uiState.releases
            }
        }
    }
    val currentRelease = uiState.currentRelease

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF141312)),
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
                    onClick = { /* Menu interaction */ },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Menu,
                        contentDescription = "Menu",
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
                                LibraryFilterMode.ALBUMS -> {
                                    val alb = uiState.releases.filter { it.tracks.size > 1 }
                                    if (alb.isNotEmpty()) alb else uiState.releases
                                }
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

        // Album Title & Artist
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentRelease?.title ?: "Select an Album",
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
                text = currentRelease?.artist ?: "Physical Collection",
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
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeReleases = remember(uiState.releases, filterMode) {
        when (filterMode) {
            LibraryFilterMode.ALBUMS -> {
                val alb = uiState.releases.filter { it.tracks.size > 1 }
                if (alb.isNotEmpty()) alb else uiState.releases
            }
            LibraryFilterMode.SINGLES -> {
                val sgl = uiState.releases.filter { it.tracks.size <= 1 }
                if (sgl.isNotEmpty()) sgl else uiState.releases
            }
        }
    }
    val currentRelease = uiState.currentRelease

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF141312))
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
                    onClick = { /* Menu interaction */ },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Menu,
                        contentDescription = "Menu",
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
                                LibraryFilterMode.ALBUMS -> {
                                    val alb = uiState.releases.filter { it.tracks.size > 1 }
                                    if (alb.isNotEmpty()) alb else uiState.releases
                                }
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = currentRelease?.title ?: "Select an Album",
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
                    text = "${currentRelease?.artist ?: ""} · ${currentRelease?.year ?: ""}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFA8A29E),
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

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
    }
}

/**
 * Realistic Square CD Jewel Case Front Artwork.
 * Includes subtle transparent hinge margin, plastic casing rim, and soft surface sheen.
 */
@Composable
fun JewelCaseArtwork(
    release: Release?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .shadow(16.dp, RoundedCornerShape(4.dp))
            .background(Color(0xFF1E1D1B), RoundedCornerShape(4.dp))
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .drawBehind {
                // Subtle acrylic case edge bevel
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.35f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )
            }
    ) {
        // Inner Booklet Artwork
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 14.dp, top = 2.dp, end = 2.dp, bottom = 2.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            } else {
                // Procedural artwork placeholder with textured backdrop
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 14.dp, top = 2.dp, end = 2.dp, bottom = 2.dp)
                        .background(Color(0xFF262422), RoundedCornerShape(2.dp)),
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

        // Left clear acrylic hinge margin (standard CD jewel case feature)
        Box(
            modifier = Modifier
                .width(14.dp)
                .fillMaxHeight()
                .drawBehind {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.12f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.25f)
                            )
                        )
                    )
                    // Hinge circular tabs
                    drawCircle(
                        color = Color.White.copy(alpha = 0.2f),
                        radius = 2.5f,
                        center = Offset(size.width / 2, size.height * 0.25f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.2f),
                        radius = 2.5f,
                        center = Offset(size.width / 2, size.height * 0.75f)
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
