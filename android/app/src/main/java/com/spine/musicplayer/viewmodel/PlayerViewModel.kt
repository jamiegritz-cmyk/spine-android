package com.spine.musicplayer.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.spine.musicplayer.data.MediaStoreAudioScanner
import com.spine.musicplayer.model.Release
import com.spine.musicplayer.model.ReleaseType
import com.spine.musicplayer.model.RepeatMode
import com.spine.musicplayer.model.Track
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

enum class FilterMode {
    ALBUMS, SINGLES
}

data class PlayerUiState(
    val releases: List<Release> = emptyList(),
    val selectedReleaseIndex: Int = 0,
    val currentTrackIndex: Int = 0,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isLoading: Boolean = false,
    val permissionGranted: Boolean = false,
    val filterMode: FilterMode = FilterMode.ALBUMS,
    val isRefreshing: Boolean = false
) {
    val displayedReleases: List<Release>
        get() {
            val filtered = when (filterMode) {
                FilterMode.ALBUMS -> releases.filter { it.type == ReleaseType.ALBUM }
                FilterMode.SINGLES -> releases.filter { it.type == ReleaseType.SINGLE }
            }
            val active = if (filtered.isNotEmpty()) filtered else releases
            return active.sortedBy { it.title.lowercase(Locale.ROOT) }
        }

    val currentRelease: Release?
        get() = displayedReleases.getOrNull(selectedReleaseIndex) ?: displayedReleases.firstOrNull()

    val currentTrack: Track?
        get() = currentRelease?.tracks?.getOrNull(currentTrackIndex) ?: currentRelease?.tracks?.firstOrNull()
}

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val audioScanner = MediaStoreAudioScanner(application)
    private val coverArtRepository = CoverArtRepository(application)
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build()

    private val defaultReleases: List<Release> = listOf(
        Release(
            id = "album_rks_live",
            title = "Rainbow Kitten Surprise on Audiotree Live [Explicit]",
            artist = "Rainbow Kitten Surprise",
            year = 2017,
            artworkUri = Uri.parse("https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600&auto=format&fit=crop&q=80"),
            tracks = listOf(
                Track(1, "Counting Cards", "Rainbow Kitten Surprise", "Audiotree Live", 215000, 1, Uri.EMPTY),
                Track(2, "Cocaine Jesus", "Rainbow Kitten Surprise", "Audiotree Live", 231000, 2, Uri.EMPTY),
                Track(3, "Lady Lie", "Rainbow Kitten Surprise", "Audiotree Live", 198000, 3, Uri.EMPTY)
            ),
            spineColorHex = "#1C1A17",
            catalogNumber = "RA-6405",
            type = ReleaseType.ALBUM
        ),
        Release(
            id = "album_qotsa",
            title = "Songs for the Deaf",
            artist = "Queens of the Stone Age",
            year = 2002,
            artworkUri = Uri.parse("https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80"),
            tracks = listOf(
                Track(4, "No One Knows", "Queens of the Stone Age", "Songs for the Deaf", 255000, 1, Uri.EMPTY),
                Track(5, "Go with the Flow", "Queens of the Stone Age", "Songs for the Deaf", 187000, 2, Uri.EMPTY)
            ),
            spineColorHex = "#9E1B4D",
            catalogNumber = "QS-8821",
            type = ReleaseType.ALBUM
        ),
        Release(
            id = "album_radiohead",
            title = "OK Computer",
            artist = "Radiohead",
            year = 1997,
            artworkUri = Uri.parse("https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80"),
            tracks = listOf(
                Track(6, "Airbag", "Radiohead", "OK Computer", 284000, 1, Uri.EMPTY),
                Track(7, "Paranoid Android", "Radiohead", "OK Computer", 383000, 2, Uri.EMPTY)
            ),
            spineColorHex = "#C2A649",
            catalogNumber = "RH-1997",
            type = ReleaseType.ALBUM
        ),
        Release(
            id = "album_portishead",
            title = "Dummy",
            artist = "Portishead",
            year = 1994,
            artworkUri = Uri.parse("https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80"),
            tracks = listOf(
                Track(8, "Mysterons", "Portishead", "Dummy", 302000, 1, Uri.EMPTY),
                Track(9, "Glory Box", "Portishead", "Dummy", 308000, 2, Uri.EMPTY)
            ),
            spineColorHex = "#8B1E2D",
            catalogNumber = "PH-9411",
            type = ReleaseType.ALBUM
        ),
        Release(
            id = "album_pinkfloyd",
            title = "The Dark Side of the Moon",
            artist = "Pink Floyd",
            year = 1973,
            artworkUri = Uri.parse("https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80"),
            tracks = listOf(
                Track(10, "Speak to Me / Breathe", "Pink Floyd", "The Dark Side of the Moon", 238000, 1, Uri.EMPTY),
                Track(11, "Time", "Pink Floyd", "The Dark Side of the Moon", 413000, 2, Uri.EMPTY)
            ),
            spineColorHex = "#215A9E",
            catalogNumber = "PF-7301",
            type = ReleaseType.ALBUM
        ),
        Release(
            id = "album_kendrick",
            title = "DAMN.",
            artist = "Kendrick Lamar",
            year = 2017,
            artworkUri = Uri.parse("https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600&auto=format&fit=crop&q=80"),
            tracks = listOf(
                Track(12, "BLOOD.", "Kendrick Lamar", "DAMN.", 118000, 1, Uri.EMPTY),
                Track(13, "DNA.", "Kendrick Lamar", "DAMN.", 185000, 2, Uri.EMPTY)
            ),
            spineColorHex = "#2D3B2E",
            catalogNumber = "KL-2017",
            type = ReleaseType.ALBUM
        ),
        Release(
            id = "album_cash",
            title = "American IV: The Man Comes Around",
            artist = "Johnny Cash",
            year = 2002,
            artworkUri = Uri.parse("https://images.unsplash.com/photo-1511735111819-9a3f7709049c?w=600&auto=format&fit=crop&q=80"),
            tracks = listOf(
                Track(14, "The Man Comes Around", "Johnny Cash", "American IV", 266000, 1, Uri.EMPTY),
                Track(15, "Hurt", "Johnny Cash", "American IV", 218000, 2, Uri.EMPTY)
            ),
            spineColorHex = "#DCD6CD",
            catalogNumber = "JC-2002",
            type = ReleaseType.ALBUM
        ),
        Release(
            id = "album_daftpunk",
            title = "Discovery",
            artist = "Daft Punk",
            year = 2001,
            artworkUri = Uri.parse("https://images.unsplash.com/photo-1445985543469-221fb5617d05?w=600&auto=format&fit=crop&q=80"),
            tracks = listOf(
                Track(16, "One More Time", "Daft Punk", "Discovery", 320000, 1, Uri.EMPTY),
                Track(17, "Aerodynamic", "Daft Punk", "Discovery", 207000, 2, Uri.EMPTY)
            ),
            spineColorHex = "#B31D1D",
            catalogNumber = "DP-2001",
            type = ReleaseType.ALBUM
        ),
        Release(
            id = "album_beatles",
            title = "Abbey Road",
            artist = "The Beatles",
            year = 1969,
            artworkUri = Uri.parse("https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=600&auto=format&fit=crop&q=80"),
            tracks = listOf(
                Track(18, "Come Together", "The Beatles", "Abbey Road", 259000, 1, Uri.EMPTY),
                Track(19, "Something", "The Beatles", "Abbey Road", 182000, 2, Uri.EMPTY)
            ),
            spineColorHex = "#EAE8E4",
            catalogNumber = "TB-1969",
            type = ReleaseType.ALBUM
        ),
        Release(
            id = "album_winehouse",
            title = "Back to Black",
            artist = "Amy Winehouse",
            year = 2006,
            artworkUri = Uri.parse("https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600&auto=format&fit=crop&q=80"),
            tracks = listOf(
                Track(20, "Rehab", "Amy Winehouse", "Back to Black", 215000, 1, Uri.EMPTY),
                Track(21, "You Know I'm No Good", "Amy Winehouse", "Back to Black", 257000, 2, Uri.EMPTY)
            ),
            spineColorHex = "#181818",
            catalogNumber = "AW-2006",
            type = ReleaseType.ALBUM
        ),
        // Dedicated Singles for the Singles tab
        Release(
            id = "single_rks_fever",
            title = "Fever Pitch (Single)",
            artist = "Rainbow Kitten Surprise",
            year = 2018,
            artworkUri = Uri.parse("https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600&auto=format&fit=crop&q=80"),
            tracks = listOf(
                Track(22, "Fever Pitch", "Rainbow Kitten Surprise", "Fever Pitch", 202000, 1, Uri.EMPTY)
            ),
            spineColorHex = "#831843",
            catalogNumber = "RA-0101",
            type = ReleaseType.SINGLE
        ),
        Release(
            id = "single_radiohead_creep",
            title = "Creep (Single)",
            artist = "Radiohead",
            year = 1992,
            artworkUri = Uri.parse("https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80"),
            tracks = listOf(
                Track(23, "Creep", "Radiohead", "Creep", 236000, 1, Uri.EMPTY)
            ),
            spineColorHex = "#701A75",
            catalogNumber = "RH-0001",
            type = ReleaseType.SINGLE
        ),
        Release(
            id = "single_daft_getlucky",
            title = "Get Lucky (Single)",
            artist = "Daft Punk",
            year = 2013,
            artworkUri = Uri.parse("https://images.unsplash.com/photo-1445985543469-221fb5617d05?w=600&auto=format&fit=crop&q=80"),
            tracks = listOf(
                Track(24, "Get Lucky", "Daft Punk", "Get Lucky", 248000, 1, Uri.EMPTY)
            ),
            spineColorHex = "#C2410C",
            catalogNumber = "DP-0077",
            type = ReleaseType.SINGLE
        )
    )

    private val _uiState = MutableStateFlow(PlayerUiState(releases = defaultReleases))
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        setupPlayerListener()
        startPositionTracker()
    }

    private fun setupPlayerListener() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _uiState.value = _uiState.value.copy(durationMs = exoPlayer.duration.coerceAtLeast(0L))
                } else if (playbackState == Player.STATE_ENDED) {
                    handleTrackEnded()
                }
            }
        })
    }

    private fun startPositionTracker() {
        viewModelScope.launch {
            while (true) {
                if (exoPlayer.isPlaying) {
                    _uiState.value = _uiState.value.copy(
                        currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                    )
                }
                delay(200)
            }
        }
    }

    fun loadLocalMusic() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val scannedReleases = audioScanner.scanLocalReleases()
            val activeReleases = if (scannedReleases.isNotEmpty()) {
                scannedReleases
            } else {
                _uiState.value.releases // Retain preview/demo releases until real music is added
            }

            _uiState.value = _uiState.value.copy(
                releases = activeReleases,
                selectedReleaseIndex = 0,
                currentTrackIndex = 0,
                isLoading = false,
                permissionGranted = true
            )
            prepareCurrentTrack()
        }
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            val scannedReleases = audioScanner.scanLocalReleases()
            val activeReleases = if (scannedReleases.isNotEmpty()) {
                scannedReleases
            } else {
                _uiState.value.releases
            }
            _uiState.value = _uiState.value.copy(
                releases = activeReleases,
                isRefreshing = false
            )
            prepareCurrentTrack(autoplay = false)
        }
    }

    fun setFilterMode(mode: FilterMode) {
        _uiState.value = _uiState.value.copy(
            filterMode = mode,
            selectedReleaseIndex = 0,
            currentTrackIndex = 0
        )
        prepareCurrentTrack(autoplay = false)
    }

    fun updateReleaseArtwork(releaseId: String, newArtworkUri: Uri) {
        viewModelScope.launch {
            val targetRelease = _uiState.value.releases.find { it.id == releaseId }
            val savedUri = if (targetRelease != null) {
                coverArtRepository.saveUserSelectedArtwork(
                    targetRelease.title,
                    targetRelease.artist,
                    newArtworkUri
                ) ?: newArtworkUri
            } else {
                newArtworkUri
            }
            val updatedReleases = _uiState.value.releases.map { release ->
                if (release.id == releaseId) {
                    release.copy(artworkUri = savedUri)
                } else {
                    release
                }
            }
            _uiState.value = _uiState.value.copy(releases = updatedReleases)
        }
    }

    fun selectRelease(index: Int) {
        val displayed = _uiState.value.displayedReleases
        if (index in displayed.indices && index != _uiState.value.selectedReleaseIndex) {
            _uiState.value = _uiState.value.copy(
                selectedReleaseIndex = index,
                currentTrackIndex = 0,
                currentPositionMs = 0L,
                isPlaying = false
            )
            prepareCurrentTrack(autoplay = false)
        }
    }

    private fun prepareCurrentTrack(autoplay: Boolean = false) {
        val track = _uiState.value.currentTrack ?: return
        val mediaItem = MediaItem.fromUri(track.contentUri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        if (autoplay) {
            exoPlayer.play()
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (exoPlayer.playbackState == Player.STATE_IDLE) {
                prepareCurrentTrack(autoplay = true)
            } else {
                exoPlayer.play()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _uiState.value = _uiState.value.copy(currentPositionMs = positionMs)
    }

    fun nextTrack() {
        val release = _uiState.value.currentRelease ?: return
        val nextIdx = _uiState.value.currentTrackIndex + 1
        if (nextIdx < release.tracks.size) {
            _uiState.value = _uiState.value.copy(currentTrackIndex = nextIdx)
            prepareCurrentTrack(autoplay = _uiState.value.isPlaying)
        } else {
            // Next release on shelf
            val total = _uiState.value.displayedReleases.size
            if (total > 0) {
                val nextReleaseIdx = (_uiState.value.selectedReleaseIndex + 1) % total
                selectRelease(nextReleaseIdx)
            }
        }
    }

    fun previousTrack() {
        if (exoPlayer.currentPosition > 3000) {
            seekTo(0)
            return
        }
        val prevIdx = _uiState.value.currentTrackIndex - 1
        if (prevIdx >= 0) {
            _uiState.value = _uiState.value.copy(currentTrackIndex = prevIdx)
            prepareCurrentTrack(autoplay = _uiState.value.isPlaying)
        } else {
            val total = _uiState.value.displayedReleases.size
            if (total > 0) {
                val prevReleaseIdx = if (_uiState.value.selectedReleaseIndex - 1 < 0) {
                    total - 1
                } else {
                    _uiState.value.selectedReleaseIndex - 1
                }
                selectRelease(prevReleaseIdx)
            }
        }
    }

    fun toggleShuffle() {
        _uiState.value = _uiState.value.copy(isShuffle = !_uiState.value.isShuffle)
    }

    fun cycleRepeatMode() {
        val next = when (_uiState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _uiState.value = _uiState.value.copy(repeatMode = next)
    }

    private fun handleTrackEnded() {
        when (_uiState.value.repeatMode) {
            RepeatMode.ONE -> seekTo(0).also { exoPlayer.play() }
            RepeatMode.ALL -> nextTrack()
            RepeatMode.OFF -> {
                val release = _uiState.value.currentRelease ?: return
                if (_uiState.value.currentTrackIndex + 1 < release.tracks.size) {
                    nextTrack()
                } else {
                    exoPlayer.pause()
                }
            }
        }
    }

    override fun onCleared() {
        exoPlayer.release()
        super.onCleared()
    }
}
