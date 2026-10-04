package com.spine.musicplayer.ui

import android.content.res.Configuration
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
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
    ALBUMS, SINGLES, ARTIST
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
    onSelectFolder: () -> Unit = {},
    selectedFolderName: String? = null,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var filterMode by rememberSaveable { mutableStateOf(LibraryFilterMode.ALBUMS) }

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
                onSelectFolder = onSelectFolder,
                selectedFolderName = selectedFolderName,
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
                onSelectFolder = onSelectFolder,
                selectedFolderName = selectedFolderName,
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
    onSelectFolder: () -> Unit,
    selectedFolderName: String?,
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
            LibraryFilterMode.ARTIST -> {
                // Sorted A → Z by artist name (case-insensitive)
                // If artist metadata is missing, place those items consistently at the end
                uiState.releases.sortedWith { r1, r2 ->
                    val a1 = r1.artist.trim()
                    val a2 = r2.artist.trim()
                    val a1Empty = a1.isEmpty() || a1.equals("Various Artists", ignoreCase = true) || a1.equals("Unknown Artist", ignoreCase = true)
                    val a2Empty = a2.isEmpty() || a2.equals("Various Artists", ignoreCase = true) || a2.equals("Unknown Artist", ignoreCase = true)
                    when {
                        a1Empty && a2Empty -> r1.title.compareTo(r2.title, ignoreCase = true)
                        a1Empty -> 1
                        a2Empty -> -1
                        else -> {
                            val comp = a1.compareTo(a2, ignoreCase = true)
                            if (comp != 0) comp else r1.title.compareTo(r2.title, ignoreCase = true)
                        }
                    }
                }
            }
        }
    }
    val currentRelease = uiState.currentRelease

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Supplied Background PNG: Scale/crop to fill the available player-screen background
        Image(
            painter = painterResource(id = com.spine.musicplayer.R.drawable.graiz_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: GRAIZ [menu] on left, [Albums] [Singles] [Artist] [Refresh] on right
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

            // Right: [Albums] [Singles] [Artist] segment + [Refresh]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AlbumsSinglesSegment(
                    currentMode = filterMode,
                    onModeSelected = { newMode ->
                        if (filterMode != newMode) {
                            onFilterModeChange(newMode)
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

        // Physical CD Jewel Case Artwork (Noticeably larger, authentic physical jewel case)
        JewelCaseArtwork(
            release = currentRelease,
            onClick = onPlayPause,
            modifier = Modifier
                .width(235.dp)
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
        val shelfSelectedIndex = remember(activeReleases, currentRelease?.id) {
            val idx = activeReleases.indexOfFirst { it.id == currentRelease?.id }
            if (idx >= 0) idx else 0
        }

        SpineShelf(
            releases = activeReleases,
            selectedIndex = shelfSelectedIndex,
            selectedReleaseId = currentRelease?.id,
            onSelectRelease = { index ->
                if (index in activeReleases.indices) {
                    val sel = activeReleases[index]
                    val orig = uiState.releases.indexOfFirst { it.id == sel.id }
                    if (orig >= 0) {
                        onSelectRelease(orig)
                    }
                }
            },
            shelfHeight = 380.dp,
            modifier = Modifier.fillMaxWidth()
        )

        // Modal Album Tracklist Bottom Sheet
        if (showTracklistSheet) {
            AlbumTracklistSheet(
                release = currentRelease,
                currentTrackIndex = uiState.currentTrackIndex,
                isPlaying = uiState.isPlaying,
                onSelectFolder = {
                    onSelectFolder()
                    showTracklistSheet = false
                },
                selectedFolderName = selectedFolderName,
                onSelectTrack = { trackIdx ->
                    onSelectTrack(trackIdx)
                    showTracklistSheet = false
                },
                onDismiss = { showTracklistSheet = false }
            )
        }
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
    onSelectFolder: () -> Unit,
    selectedFolderName: String?,
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
            LibraryFilterMode.ARTIST -> {
                // Sorted A → Z by artist name (case-insensitive)
                // If artist metadata is missing, place those items consistently at the end
                uiState.releases.sortedWith { r1, r2 ->
                    val a1 = r1.artist.trim()
                    val a2 = r2.artist.trim()
                    val a1Empty = a1.isEmpty() || a1.equals("Various Artists", ignoreCase = true) || a1.equals("Unknown Artist", ignoreCase = true)
                    val a2Empty = a2.isEmpty() || a2.equals("Various Artists", ignoreCase = true) || a2.equals("Unknown Artist", ignoreCase = true)
                    when {
                        a1Empty && a2Empty -> r1.title.compareTo(r2.title, ignoreCase = true)
                        a1Empty -> 1
                        a2Empty -> -1
                        else -> {
                            val comp = a1.compareTo(a2, ignoreCase = true)
                            if (comp != 0) comp else r1.title.compareTo(r2.title, ignoreCase = true)
                        }
                    }
                }
            }
        }
    }
    val currentRelease = uiState.currentRelease
    val shelfSelectedIndex = remember(activeReleases, currentRelease?.id) {
        val idx = activeReleases.indexOfFirst { it.id == currentRelease?.id }
        if (idx >= 0) idx else 0
    }

    val currentTrack = uiState.currentTrack
    val displayTitle = currentTrack?.title ?: currentRelease?.title ?: "Select an Album"
    val displayArtist = currentTrack?.artist ?: currentRelease?.artist ?: "Physical Collection"

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Supplied Background PNG: Scale/crop to fill the available player-screen background
        Image(
            painter = painterResource(id = com.spine.musicplayer.R.drawable.graiz_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Continuous CD Shelf across the entire bottom:
        // Spines under the jewel case (x in 30.dp..295.dp) remain partially hidden (~115dp tall)
        // Spines extending past the left and right edges become full-height (~220dp tall)
        LandscapeSpineShelf(
            releases = activeReleases,
            selectedIndex = shelfSelectedIndex,
            selectedReleaseId = currentRelease?.id,
            onSelectRelease = { index ->
                if (index in activeReleases.indices) {
                    val sel = activeReleases[index]
                    val orig = uiState.releases.indexOfFirst { it.id == sel.id }
                    if (orig >= 0) {
                        onSelectRelease(orig)
                    }
                }
            },
            caseStartDp = 30.dp,
            caseEndDp = 295.dp,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        )

        // Main Album Jewel Case (Positioned on the left, sitting directly in front of the shelf)
        // Tapping the main CD/jewel case plays/pauses the current track
        // Horizontal swipe gestures: Left -> Next Track, Right -> Previous Track
        var totalDragX by remember { mutableFloatStateOf(0f) }
        var isSwipeGesture by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .padding(start = 30.dp, top = 52.dp)
                .width(265.dp)
                .align(Alignment.TopStart)
                .pointerInput(onNext, onPrevious, onPlayPause) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            totalDragX = 0f
                            isSwipeGesture = false
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            totalDragX += dragAmount
                            if (kotlin.math.abs(totalDragX) > 20f) {
                                isSwipeGesture = true
                                change.consume()
                            }
                        },
                        onDragEnd = {
                            if (isSwipeGesture && kotlin.math.abs(totalDragX) >= 40f) {
                                if (totalDragX < 0) {
                                    onNext()
                                } else {
                                    onPrevious()
                                }
                            }
                            totalDragX = 0f
                            isSwipeGesture = false
                        },
                        onDragCancel = {
                            totalDragX = 0f
                            isSwipeGesture = false
                        }
                    )
                }
        ) {
            JewelCaseArtwork(
                release = currentRelease,
                onClick = {
                    if (!isSwipeGesture) {
                        onPlayPause()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Track Information: Positioned to the right of the jewel case, above the full-height spines
        // Top of track title is level with top edge of the main CD jewel case (52.dp)
        Column(
            modifier = Modifier
                .padding(start = 315.dp, top = 52.dp, end = 24.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    if (currentRelease != null && currentRelease.tracks.isNotEmpty()) {
                        showTracklistSheet = true
                    }
                }
                .padding(horizontal = 4.dp, vertical = 0.dp)
                .align(Alignment.TopStart)
        ) {
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color(0xFFF5F5F4),
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    letterSpacing = 0.2.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$displayArtist · ${currentRelease?.year ?: ""}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFFA8A29E),
                    fontSize = 13.5.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Top Bar: GRAIZ [menu] on left, [Albums] [Singles] [Refresh] on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .align(Alignment.TopCenter),
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

            // Right: [Albums] [Singles] [Artist] segment + [Refresh]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AlbumsSinglesSegment(
                    currentMode = filterMode,
                    onModeSelected = { newMode ->
                        if (filterMode != newMode) {
                            onFilterModeChange(newMode)
                        }
                    }
                )

                RefreshButton(
                    isRefreshing = uiState.isLoading,
                    onClick = onRefresh
                )
            }
        }

        // Modal Album Tracklist Bottom Sheet
        if (showTracklistSheet) {
            AlbumTracklistSheet(
                release = currentRelease,
                currentTrackIndex = uiState.currentTrackIndex,
                isPlaying = uiState.isPlaying,
                onSelectFolder = {
                    onSelectFolder()
                    showTracklistSheet = false
                },
                selectedFolderName = selectedFolderName,
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
 * Authentic Physical CD Jewel Case Component:
 * Rendering order:
 * 1. Printed paper inlay / existing album artwork clipped to the inner square of the case
 * 2. Clear plastic CD jewel-case frame, transparent center, edges and reflections on top
 */
@Composable
fun JewelCaseArtwork(
    release: Release?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(883f / 796f) // Authentic physical CD jewel case ratio (~1.109:1)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
    ) {
        val caseWidth = maxWidth
        val caseHeight = maxHeight

        // Physical jewel case geometry:
        // Case: 883w x 796h
        // Inner Square Booklet: left=141px (15.97%), top=71px (8.92%), right=87px (9.85%), bottom=71px (8.92%)
        // Booklet dimensions: 655px x 654px (Strict 1:1 physical square)
        val startPadding = caseWidth * (141f / 883f)
        val endPadding = caseWidth * (87f / 883f)
        val topPadding = caseHeight * (71f / 796f)
        val bottomPadding = caseHeight * (71f / 796f)

        // 1. Existing Album Artwork (Paper Inlay inside the case, clipped precisely to the inner square)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = startPadding,
                    top = topPadding,
                    end = endPadding,
                    bottom = bottomPadding
                )
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
                        contentScale = ContentScale.Crop, // Scaled precisely to fit the inner square
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

        // 2. Supplied Clear Plastic CD Jewel-Case Structure & Reflections Overlay (On Top)
        Image(
            painter = painterResource(id = com.spine.musicplayer.R.drawable.graiz_cd_case),
            contentDescription = "CD Jewel Case",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
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
                .padding(horizontal = 8.dp, vertical = 4.dp),
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
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Singles",
                color = if (currentMode == LibraryFilterMode.SINGLES) Color.White else Color(0xFFA8A29E),
                fontSize = 11.sp,
                fontWeight = if (currentMode == LibraryFilterMode.SINGLES) FontWeight.SemiBold else FontWeight.Normal
            )
        }
        // Artist Tab
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (currentMode == LibraryFilterMode.ARTIST) Color(0xFF383430) else Color.Transparent)
                .clickable { onModeSelected(LibraryFilterMode.ARTIST) }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Artist",
                color = if (currentMode == LibraryFilterMode.ARTIST) Color.White else Color(0xFFA8A29E),
                fontSize = 11.sp,
                fontWeight = if (currentMode == LibraryFilterMode.ARTIST) FontWeight.SemiBold else FontWeight.Normal
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
    release: Release?,
    currentTrackIndex: Int,
    isPlaying: Boolean,
    onSelectTrack: (Int) -> Unit,
    onSelectFolder: () -> Unit,
    selectedFolderName: String?,
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
            // Music Folder Selection Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF22201E))
                    .clickable {
                        onSelectFolder()
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MUSIC FOLDER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFA8A29E),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = selectedFolderName ?: "All Audio (Tap to choose folder)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFF5F5F4),
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedFolderName != null) "Change" else "Choose",
                    color = Color(0xFFD6D3D1),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .background(Color(0xFF383430), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFF2E2C2A))
            Spacer(modifier = Modifier.height(12.dp))

            if (release != null) {
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
}
