package com.spine.musicplayer.ui

import android.content.res.Configuration
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.spine.musicplayer.model.Release
import com.spine.musicplayer.model.RepeatMode
import com.spine.musicplayer.viewmodel.FilterMode
import com.spine.musicplayer.viewmodel.PlayerUiState
import java.util.Locale

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
    onFilterChange: (FilterMode) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        containerColor = Color(0xFF0F0E0D),
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) { innerPadding ->
        if (isLandscape) {
            LandscapePlayerLayout(
                uiState = uiState,
                onSelectRelease = onSelectRelease,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onPrevious = onPrevious,
                onSeek = onSeek,
                onToggleShuffle = onToggleShuffle,
                onCycleRepeat = onCycleRepeat,
                onRefresh = onRefresh,
                onFilterChange = onFilterChange,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            PortraitPlayerLayout(
                uiState = uiState,
                onSelectRelease = onSelectRelease,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onPrevious = onPrevious,
                onSeek = onSeek,
                onToggleShuffle = onToggleShuffle,
                onCycleRepeat = onCycleRepeat,
                onRefresh = onRefresh,
                onFilterChange = onFilterChange,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

/**
 * Faithfully matches the attached design mockup (file_000000001ac88210a2440dfcbf5fde5a.jpg):
 * - Top Bar: "SPINE" on left, Catalog number & controls on right.
 * - Upper Area: Large square jewel-case front artwork, track title, artist, progress bar, controls.
 * - Lower Area: Physical CD shelf with tall, narrow audio CD jewel cases on wooden shelf.
 */
@Composable
private fun PortraitPlayerLayout(
    uiState: PlayerUiState,
    onSelectRelease: (Int) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onRefresh: () -> Unit,
    onFilterChange: (FilterMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentRelease = uiState.currentRelease
    val currentTrack = uiState.currentTrack

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF11100F)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Top Bar: GRAIZ title on left, Albums / Singles / A-Z -> Refresh on right (no debug codes)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GRAIZ",
                style = MaterialTheme.typography.titleMedium.copy(
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF5F5F4),
                    fontSize = 15.sp
                )
            )

            // Albums | Singles Switch & A-Z indicator -> Refresh (no numbers or debug text)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AlbumsSinglesSegment(
                    currentMode = uiState.filterMode,
                    onModeSelected = onFilterChange
                )

                // Refresh button to rescan device local music
                RefreshButton(
                    isRefreshing = uiState.isRefreshing,
                    onClick = onRefresh
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.2f))

        // 2. Large Square CD Jewel Case Front Artwork (tapping toggles play/pause)
        JewelCaseArtwork(
            release = currentRelease,
            onClick = onPlayPause,
            modifier = Modifier
                .size(190.dp)
                .padding(4.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Track Title & Artist (Centered, prominent)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentTrack?.title ?: currentRelease?.title ?: "Select an Album",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFFF5F5F4),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.5.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = currentRelease?.artist ?: "Unknown Artist",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFFA8A29E),
                    fontSize = 13.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Playback Progress Bar with Timers (matching mock-up: 00:07 / 03:11)
        PlaybackProgressBar(
            positionMs = uiState.currentPositionMs,
            durationMs = uiState.durationMs,
            onSeek = onSeek,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Playback Controls Row: Shuffle, Previous, Large Center Play/Pause, Next, Repeat
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

        Spacer(modifier = Modifier.weight(0.4f))

        // 6. Dominant Physical CD Shelf: Authentic audio CD jewel cases sitting on a wooden shelf
        SpineShelf(
            releases = uiState.displayedReleases,
            selectedIndex = uiState.selectedReleaseIndex,
            onSelectRelease = onSelectRelease,
            shelfHeight = 375.dp,
            caseHeight = 295.dp,
            caseWidth = 22.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Responsive Landscape Layout:
 * Compact top bar and player area + full-width panoramic CD shelf along the bottom.
 * Keeps CD cases tall and narrow, displaying more CDs across the shelf.
 */
@Composable
private fun LandscapePlayerLayout(
    uiState: PlayerUiState,
    onSelectRelease: (Int) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onRefresh: () -> Unit,
    onFilterChange: (FilterMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentRelease = uiState.currentRelease
    val currentTrack = uiState.currentTrack

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF11100F))
    ) {
        // Upper compact player area (split: artwork + controls)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Square front artwork
            JewelCaseArtwork(
                release = currentRelease,
                onClick = onPlayPause,
                modifier = Modifier
                    .size(136.dp)
                    .padding(2.dp)
            )

            Spacer(modifier = Modifier.width(20.dp))

            // Center: Title, Artist, Progress Bar
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = currentTrack?.title ?: currentRelease?.title ?: "Select an Album",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFFF5F5F4),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${currentRelease?.artist ?: ""} · ${currentRelease?.year ?: ""}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFA8A29E)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Anchored Library Control Region: Fixed position, independent of metadata
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AlbumsSinglesSegment(
                            currentMode = uiState.filterMode,
                            onModeSelected = onFilterChange
                        )
                        RefreshButton(
                            isRefreshing = uiState.isRefreshing,
                            onClick = onRefresh
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                PlaybackProgressBar(
                    positionMs = uiState.currentPositionMs,
                    durationMs = uiState.durationMs,
                    onSeek = onSeek,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Right: Playback Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (uiState.isShuffle) Color(0xFFF5F5F4) else Color(0xFF57534E)
                    )
                }
                IconButton(onClick = onPrevious) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color(0xFFE7E5E4)
                    )
                }
                FilledIconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(52.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFFF5F5F4),
                        contentColor = Color(0xFF141312)
                    )
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = Color(0xFFE7E5E4)
                    )
                }
                IconButton(onClick = onCycleRepeat) {
                    Icon(
                        imageVector = when (uiState.repeatMode) {
                            RepeatMode.ONE -> Icons.Rounded.RepeatOne
                            else -> Icons.Rounded.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = if (uiState.repeatMode != RepeatMode.OFF) Color(0xFFF5F5F4) else Color(0xFF57534E)
                    )
                }
            }
        }

        // Full-width panoramic physical CD shelf along the bottom in landscape
        SpineShelf(
            releases = uiState.displayedReleases,
            selectedIndex = uiState.selectedReleaseIndex,
            onSelectRelease = onSelectRelease,
            shelfHeight = 220.dp,
            caseHeight = 175.dp,
            caseWidth = 20.dp,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

/**
 * Albums | Singles Segmented Toggle with A–Z sorting indication.
 */
@Composable
fun AlbumsSinglesSegment(
    currentMode: FilterMode,
    onModeSelected: (FilterMode) -> Unit,
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
                .background(if (currentMode == FilterMode.ALBUMS) Color(0xFF383430) else Color.Transparent)
                .clickable { onModeSelected(FilterMode.ALBUMS) }
                .padding(horizontal = 9.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Albums",
                color = if (currentMode == FilterMode.ALBUMS) Color.White else Color(0xFFA8A29E),
                fontSize = 11.sp,
                fontWeight = if (currentMode == FilterMode.ALBUMS) FontWeight.SemiBold else FontWeight.Normal
            )
        }

        // Singles Tab
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (currentMode == FilterMode.SINGLES) Color(0xFF383430) else Color.Transparent)
                .clickable { onModeSelected(FilterMode.SINGLES) }
                .padding(horizontal = 9.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Singles",
                color = if (currentMode == FilterMode.SINGLES) Color.White else Color(0xFFA8A29E),
                fontSize = 11.sp,
                fontWeight = if (currentMode == FilterMode.SINGLES) FontWeight.SemiBold else FontWeight.Normal
            )
        }

        // A-Z Sort Badge
        Box(
            modifier = Modifier
                .padding(start = 2.dp, end = 4.dp)
                .background(Color(0xFF282522), RoundedCornerShape(6.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(
                text = "A–Z",
                color = Color(0xFFD6D3D1),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Refresh Button to rescan device local music via MediaStore.
 */
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
            repeatMode = AnimRepeatMode.Restart
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
            contentDescription = "Rescan Library",
            tint = if (isRefreshing) Color(0xFFD6D3D1) else Color(0xFFA8A29E),
            modifier = Modifier
                .size(18.dp)
                .then(if (isRefreshing) Modifier.rotate(angle) else Modifier)
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
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.40f)
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
            animationSpec = tween(250),
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
                                Color.White.copy(alpha = 0.14f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.30f)
                            )
                        )
                    )
                    // Hinge circular tabs
                    drawCircle(
                        color = Color.White.copy(alpha = 0.22f),
                        radius = 2.5f,
                        center = Offset(size.width / 2, size.height * 0.25f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.22f),
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
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            )
            Text(
                text = formatDuration(durationMs),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF78716C),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
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
                tint = if (isShuffle) Color(0xFFF5F5F4) else Color(0xFF78716C),
                modifier = Modifier.size(24.dp)
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

        // Center Play/Pause Large White Circle (Prominent centerpiece from mock-up)
        FilledIconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(62.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color(0xFFF5F5F4),
                contentColor = Color(0xFF141312)
            )
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(34.dp)
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
                tint = if (repeatMode != RepeatMode.OFF) Color(0xFFF5F5F4) else Color(0xFF78716C),
                modifier = Modifier.size(24.dp)
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
