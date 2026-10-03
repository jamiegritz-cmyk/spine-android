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
import com.spine.musicplayer.model.RepeatMode
import com.spine.musicplayer.model.Track
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
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build()

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

    fun selectRelease(index: Int) {
        if (index in _uiState.value.releases.indices && index != _uiState.value.selectedReleaseIndex) {
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
            val nextReleaseIdx = (_uiState.value.selectedReleaseIndex + 1) % _uiState.value.releases.size
            selectRelease(nextReleaseIdx)
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
            val prevReleaseIdx = if (_uiState.value.selectedReleaseIndex - 1 < 0) {
                _uiState.value.releases.size - 1
            } else {
                _uiState.value.selectedReleaseIndex - 1
            }
            selectRelease(prevReleaseIdx)
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
