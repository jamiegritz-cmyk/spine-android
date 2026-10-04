package com.spine.musicplayer.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.spine.musicplayer.data.MediaStoreAudioScanner
import com.spine.musicplayer.data.MusicFolderPreferences
import com.spine.musicplayer.model.Release
import com.spine.musicplayer.model.RepeatMode
import com.spine.musicplayer.model.Track
import com.spine.musicplayer.playback.PlaybackManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlayerUiState(
    val releases: List<Release> = emptyList(),
    val selectedReleaseIndex: Int = 0,
    val currentTrackIndex: Int = 0,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isLoading: Boolean = true,
    val permissionGranted: Boolean = false
) {
    val currentRelease: Release?
        get() = releases.getOrNull(selectedReleaseIndex)

    val currentTrack: Track?
        get() = currentRelease?.tracks?.getOrNull(currentTrackIndex)
}

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val audioScanner = MediaStoreAudioScanner(application)
    private val exoPlayer: ExoPlayer = PlaybackManager.getSharedPlayer(application)

    private val _uiState = MutableStateFlow(PlayerUiState())
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
                    val dur = exoPlayer.duration
                    val finalDuration = if (dur > 0) dur else (_uiState.value.currentTrack?.durationMs ?: 0L)
                    _uiState.value = _uiState.value.copy(durationMs = finalDuration.coerceAtLeast(0L))
                } else if (playbackState == Player.STATE_ENDED) {
                    handleTrackEnded()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val newIndex = exoPlayer.currentMediaItemIndex
                val release = _uiState.value.currentRelease
                if (release != null && newIndex in release.tracks.indices) {
                    val track = release.tracks[newIndex]
                    _uiState.value = _uiState.value.copy(
                        currentTrackIndex = newIndex,
                        currentPositionMs = 0L,
                        durationMs = if (track.durationMs > 0) track.durationMs else 0L
                    )
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

            val currentId = _uiState.value.currentRelease?.id
            val currentTrackIdx = _uiState.value.currentTrackIndex
            val isCurrentlyPlaying = _uiState.value.isPlaying

            val matchedReleaseIdx = activeReleases.indexOfFirst { it.id == currentId }.takeIf { it >= 0 } ?: 0
            val targetRelease = activeReleases.getOrNull(matchedReleaseIdx)
            val safeTrackIdx = currentTrackIdx.coerceIn(0, (targetRelease?.tracks?.size?.minus(1))?.coerceAtLeast(0) ?: 0)
            val initialTrack = targetRelease?.tracks?.getOrNull(safeTrackIdx)

            _uiState.value = _uiState.value.copy(
                releases = activeReleases,
                selectedReleaseIndex = matchedReleaseIdx,
                currentTrackIndex = safeTrackIdx,
                durationMs = initialTrack?.durationMs ?: _uiState.value.durationMs,
                isLoading = false,
                permissionGranted = true
            )
            PlaybackManager.currentReleases = activeReleases

            // Only prepare initial queue if no music was previously loaded or playing
            if (!isCurrentlyPlaying && currentId == null && targetRelease != null) {
                prepareAlbumQueue(targetRelease, startIndex = 0, autoplay = false)
            }
        }
    }

    fun onFolderSelected(uri: Uri) {
        MusicFolderPreferences.saveSelectedFolder(getApplication(), uri)
        loadLocalMusic()
    }

    fun onFolderCleared() {
        MusicFolderPreferences.clearSelectedFolder(getApplication())
        loadLocalMusic()
    }

    fun updateReleaseArtwork(releaseId: String, newArtworkUri: Uri) {
        val updatedReleases = _uiState.value.releases.map { release ->
            if (release.id == releaseId) {
                release.copy(artworkUri = newArtworkUri)
            } else {
                release
            }
        }
        _uiState.value = _uiState.value.copy(releases = updatedReleases)
    }

    fun selectReleaseById(releaseId: String, startTrackIndex: Int = 0, autoplay: Boolean = false) {
        val idx = _uiState.value.releases.indexOfFirst { it.id == releaseId }
        if (idx >= 0) {
            selectRelease(idx, startTrackIndex, autoplay)
        }
    }

    fun selectRelease(index: Int, startTrackIndex: Int = 0, autoplay: Boolean = false) {
        if (index in _uiState.value.releases.indices) {
            val targetRelease = _uiState.value.releases[index]
            val safeTrackIdx = startTrackIndex.coerceIn(0, (targetRelease.tracks.size - 1).coerceAtLeast(0))
            val initialTrack = targetRelease.tracks.getOrNull(safeTrackIdx)
            _uiState.value = _uiState.value.copy(
                selectedReleaseIndex = index,
                currentTrackIndex = safeTrackIdx,
                currentPositionMs = 0L,
                durationMs = initialTrack?.durationMs ?: 0L,
                isPlaying = autoplay
            )
            prepareAlbumQueue(targetRelease, startIndex = safeTrackIdx, autoplay = autoplay)
        }
    }

    fun selectTrack(trackIndex: Int, autoplay: Boolean = true) {
        val release = _uiState.value.currentRelease ?: return
        if (trackIndex in release.tracks.indices) {
            val track = release.tracks[trackIndex]
            _uiState.value = _uiState.value.copy(
                currentTrackIndex = trackIndex,
                currentPositionMs = 0L,
                durationMs = track.durationMs
            )
            if (exoPlayer.mediaItemCount == release.tracks.size) {
                exoPlayer.seekTo(trackIndex, 0L)
                if (autoplay) {
                    exoPlayer.play()
                }
            } else {
                prepareAlbumQueue(release, startIndex = trackIndex, autoplay = autoplay)
            }
        }
    }

    private fun prepareAlbumQueue(release: Release, startIndex: Int = 0, autoplay: Boolean = false) {
        if (release.tracks.isEmpty()) {
            exoPlayer.clearMediaItems()
            return
        }
        val mediaItems = release.tracks.map { track ->
            MediaItem.Builder()
                .setUri(track.contentUri)
                .setMediaId(track.id.toString())
                .build()
        }
        val safeIndex = startIndex.coerceIn(0, release.tracks.size - 1)
        exoPlayer.setMediaItems(mediaItems, safeIndex, 0L)
        exoPlayer.prepare()
        if (autoplay) {
            exoPlayer.play()
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (exoPlayer.playbackState == Player.STATE_IDLE || exoPlayer.mediaItemCount == 0) {
                val release = _uiState.value.currentRelease
                if (release != null) {
                    prepareAlbumQueue(release, startIndex = _uiState.value.currentTrackIndex, autoplay = true)
                }
            } else if (exoPlayer.playbackState == Player.STATE_ENDED) {
                selectTrack(0, autoplay = true)
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
        val nextTrackIdx = _uiState.value.currentTrackIndex + 1
        val shouldPlay = _uiState.value.isPlaying || exoPlayer.playWhenReady
        if (nextTrackIdx < release.tracks.size) {
            selectTrack(nextTrackIdx, autoplay = shouldPlay)
        } else {
            // Reached final track of current album: advance to the next album in library order
            val nextReleaseIdx = _uiState.value.selectedReleaseIndex + 1
            if (nextReleaseIdx < _uiState.value.releases.size) {
                selectRelease(nextReleaseIdx, startTrackIndex = 0, autoplay = shouldPlay)
            } else if (_uiState.value.repeatMode == RepeatMode.ALL) {
                // Loop back to beginning of the library
                selectRelease(0, startTrackIndex = 0, autoplay = shouldPlay)
            } else {
                seekTo(0)
                exoPlayer.pause()
            }
        }
    }

    fun previousTrack() {
        if (exoPlayer.currentPosition > 3000) {
            seekTo(0)
            return
        }
        val prevTrackIdx = _uiState.value.currentTrackIndex - 1
        val shouldPlay = _uiState.value.isPlaying || exoPlayer.playWhenReady
        if (prevTrackIdx >= 0) {
            selectTrack(prevTrackIdx, autoplay = shouldPlay)
        } else {
            // Reached beginning of current album: go to previous album's last track in library order
            val prevReleaseIdx = _uiState.value.selectedReleaseIndex - 1
            if (prevReleaseIdx >= 0) {
                val prevRelease = _uiState.value.releases[prevReleaseIdx]
                val lastTrackIdx = (prevRelease.tracks.size - 1).coerceAtLeast(0)
                selectRelease(prevReleaseIdx, startTrackIndex = lastTrackIdx, autoplay = shouldPlay)
            } else if (_uiState.value.repeatMode == RepeatMode.ALL) {
                val lastReleaseIdx = (_uiState.value.releases.size - 1).coerceAtLeast(0)
                val lastRelease = _uiState.value.releases[lastReleaseIdx]
                val lastTrackIdx = (lastRelease.tracks.size - 1).coerceAtLeast(0)
                selectRelease(lastReleaseIdx, startTrackIndex = lastTrackIdx, autoplay = shouldPlay)
            } else {
                seekTo(0)
            }
        }
    }

    fun toggleShuffle() {
        val newShuffle = !_uiState.value.isShuffle
        _uiState.value = _uiState.value.copy(isShuffle = newShuffle)
        exoPlayer.shuffleModeEnabled = newShuffle
    }

    fun cycleRepeatMode() {
        val next = when (_uiState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _uiState.value = _uiState.value.copy(repeatMode = next)
        exoPlayer.repeatMode = when (next) {
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    private fun handleTrackEnded() {
        when (_uiState.value.repeatMode) {
            RepeatMode.ONE -> {
                seekTo(0)
                exoPlayer.play()
            }
            RepeatMode.ALL -> {
                // Loop continually: advance to next album or loop back to first album in library order
                val nextReleaseIdx = _uiState.value.selectedReleaseIndex + 1
                if (nextReleaseIdx < _uiState.value.releases.size) {
                    selectRelease(nextReleaseIdx, startTrackIndex = 0, autoplay = true)
                } else {
                    selectRelease(0, startTrackIndex = 0, autoplay = true)
                }
            }
            RepeatMode.OFF -> {
                // Advance through library; stop cleanly only when the entire music library reaches the end
                val nextReleaseIdx = _uiState.value.selectedReleaseIndex + 1
                if (nextReleaseIdx < _uiState.value.releases.size) {
                    selectRelease(nextReleaseIdx, startTrackIndex = 0, autoplay = true)
                } else {
                    seekTo(0)
                    exoPlayer.pause()
                    _uiState.value = _uiState.value.copy(isPlaying = false)
                }
            }
        }
    }

    override fun onCleared() {
        // Shared ExoPlayer lifecycle is retained across UI recreation and background/Android Auto playback
        super.onCleared()
    }
}
