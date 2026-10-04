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
    val playingReleaseIndex: Int = 0,
    val currentTrackIndex: Int = 0,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isLoading: Boolean = true,
    val permissionGranted: Boolean = false
) {
    val selectedRelease: Release?
        get() = releases.getOrNull(selectedReleaseIndex)

    val playingRelease: Release?
        get() = releases.getOrNull(playingReleaseIndex)

    val currentRelease: Release?
        get() = releases.getOrNull(selectedReleaseIndex)

    val currentTrack: Track?
        get() = playingRelease?.tracks?.getOrNull(currentTrackIndex)
            ?: currentRelease?.tracks?.getOrNull(currentTrackIndex)
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
                    syncUiWithPlayerState()
                } else if (playbackState == Player.STATE_ENDED) {
                    handleTrackEnded()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                syncUiWithPlayerState()
            }
        })
    }

    fun syncUiWithPlayerState() {
        if (exoPlayer.mediaItemCount <= 0) return
        val currentItem = exoPlayer.currentMediaItem ?: return
        val currentItemIndex = exoPlayer.currentMediaItemIndex
        val mediaId = currentItem.mediaId

        val parts = mediaId.split(":::")
        val releaseId = if (parts.size >= 2) parts[0] else null
        val trackId = if (parts.size >= 2) parts[1].toLongOrNull() else mediaId.toLongOrNull()

        val releaseIdx = if (releaseId != null) {
            _uiState.value.releases.indexOfFirst { it.id == releaseId }
        } else {
            _uiState.value.releases.indexOfFirst { rel -> rel.tracks.any { it.id == trackId } }
        }

        val finalReleaseIdx = if (releaseIdx >= 0) releaseIdx else _uiState.value.selectedReleaseIndex
        val targetRelease = _uiState.value.releases.getOrNull(finalReleaseIdx)
        val finalTrackIdx = if (targetRelease != null && currentItemIndex in targetRelease.tracks.indices) {
            currentItemIndex
        } else {
            targetRelease?.tracks?.indexOfFirst { it.id == trackId }?.takeIf { it >= 0 } ?: currentItemIndex
        }
        val currentTrack = targetRelease?.tracks?.getOrNull(finalTrackIdx)
        val dur = if (exoPlayer.duration > 0) exoPlayer.duration else (currentTrack?.durationMs ?: 0L)

        _uiState.value = _uiState.value.copy(
            selectedReleaseIndex = finalReleaseIdx,
            playingReleaseIndex = finalReleaseIdx,
            currentTrackIndex = finalTrackIdx,
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
            durationMs = dur.coerceAtLeast(0L),
            isPlaying = exoPlayer.isPlaying
        )
    }

    private fun startPositionTracker() {
        viewModelScope.launch {
            while (true) {
                if (exoPlayer.isPlaying) {
                    _uiState.value = _uiState.value.copy(
                        currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
                        currentTrackIndex = exoPlayer.currentMediaItemIndex
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

            val currentId = _uiState.value.playingRelease?.id ?: _uiState.value.currentRelease?.id
            val currentTrackIdx = _uiState.value.currentTrackIndex
            val isCurrentlyPlaying = _uiState.value.isPlaying

            val matchedReleaseIdx = activeReleases.indexOfFirst { it.id == currentId }.takeIf { it >= 0 } ?: 0
            val targetRelease = activeReleases.getOrNull(matchedReleaseIdx)
            val safeTrackIdx = currentTrackIdx.coerceIn(0, (targetRelease?.tracks?.size?.minus(1))?.coerceAtLeast(0) ?: 0)
            val initialTrack = targetRelease?.tracks?.getOrNull(safeTrackIdx)

            _uiState.value = _uiState.value.copy(
                releases = activeReleases,
                selectedReleaseIndex = matchedReleaseIdx,
                playingReleaseIndex = matchedReleaseIdx,
                currentTrackIndex = safeTrackIdx,
                durationMs = initialTrack?.durationMs ?: _uiState.value.durationMs,
                isLoading = false,
                permissionGranted = true
            )

            // Only prepare initial queue if no music was previously loaded or playing
            if (!isCurrentlyPlaying && currentId == null && targetRelease != null) {
                prepareAlbumQueue(targetRelease, startIndex = 0, autoplay = false)
            }
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

    fun selectReleaseById(releaseId: String, startTrackIndex: Int = 0, autoplay: Boolean = false) {
        val idx = _uiState.value.releases.indexOfFirst { it.id == releaseId }
        if (idx >= 0) {
            selectRelease(idx, startTrackIndex, autoplay)
        }
    }

    fun selectRelease(index: Int, startTrackIndex: Int = 0, autoplay: Boolean = false) {
        if (index in _uiState.value.releases.indices) {
            val targetRelease = _uiState.value.releases[index]
            if (autoplay) {
                val safeTrackIdx = startTrackIndex.coerceIn(0, (targetRelease.tracks.size - 1).coerceAtLeast(0))
                val initialTrack = targetRelease.tracks.getOrNull(safeTrackIdx)
                _uiState.value = _uiState.value.copy(
                    selectedReleaseIndex = index,
                    playingReleaseIndex = index,
                    currentTrackIndex = safeTrackIdx,
                    currentPositionMs = 0L,
                    durationMs = initialTrack?.durationMs ?: 0L,
                    isPlaying = true
                )
                prepareAlbumQueue(targetRelease, startIndex = safeTrackIdx, autoplay = true)
            } else {
                // Shelf browsing / orientation centering:
                // Only updates the selected CD on the shelf and front jewel case.
                // Does NOT touch ExoPlayer, does NOT clear queue, does NOT change track or position, does NOT stop playback!
                _uiState.value = _uiState.value.copy(
                    selectedReleaseIndex = index
                )
            }
        }
    }

    fun selectTrack(trackIndex: Int, autoplay: Boolean = true) {
        val release = _uiState.value.selectedRelease ?: _uiState.value.currentRelease ?: return
        if (trackIndex in release.tracks.indices) {
            val track = release.tracks[trackIndex]
            val isSameRelease = (exoPlayer.currentMediaItem?.mediaId?.startsWith("${release.id}:::") == true)
            if (isSameRelease && exoPlayer.mediaItemCount == release.tracks.size) {
                exoPlayer.seekTo(trackIndex, 0L)
                if (autoplay) {
                    exoPlayer.play()
                }
                syncUiWithPlayerState()
            } else {
                prepareAlbumQueue(release, startIndex = trackIndex, autoplay = autoplay)
                syncUiWithPlayerState()
            }
        }
    }

    private fun prepareAlbumQueue(release: Release, startIndex: Int = 0, autoplay: Boolean = false) {
        if (release.tracks.isEmpty()) {
            exoPlayer.clearMediaItems()
            return
        }
        val mediaItems = release.tracks.mapIndexed { idx, track ->
            MediaItem.Builder()
                .setUri(track.contentUri)
                .setMediaId("${release.id}:::${track.id}:::$idx")
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
                val release = _uiState.value.selectedRelease ?: _uiState.value.playingRelease
                if (release != null) {
                    val trackIdx = _uiState.value.currentTrackIndex.coerceIn(0, (release.tracks.size - 1).coerceAtLeast(0))
                    _uiState.value = _uiState.value.copy(playingReleaseIndex = _uiState.value.selectedReleaseIndex)
                    prepareAlbumQueue(release, startIndex = trackIdx, autoplay = true)
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
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
            if (_uiState.value.isPlaying) {
                exoPlayer.play()
            }
            syncUiWithPlayerState()
        } else {
            // Reached final track of this physical album: do not randomly jump to another album
            if (_uiState.value.repeatMode == RepeatMode.ALL && exoPlayer.mediaItemCount > 0) {
                exoPlayer.seekTo(0, 0L)
                if (_uiState.value.isPlaying) {
                    exoPlayer.play()
                }
                syncUiWithPlayerState()
            } else {
                exoPlayer.seekTo(0, 0L)
                exoPlayer.pause()
                syncUiWithPlayerState()
            }
        }
    }

    fun previousTrack() {
        if (exoPlayer.currentPosition > 3000) {
            exoPlayer.seekTo(0L)
            _uiState.value = _uiState.value.copy(currentPositionMs = 0L)
            return
        }
        if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
            if (_uiState.value.isPlaying) {
                exoPlayer.play()
            }
            syncUiWithPlayerState()
        } else {
            exoPlayer.seekTo(0L)
            _uiState.value = _uiState.value.copy(currentPositionMs = 0L)
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
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
    }

    private fun handleTrackEnded() {
        when (_uiState.value.repeatMode) {
            RepeatMode.ONE -> {
                seekTo(0)
                exoPlayer.play()
            }
            RepeatMode.ALL -> {
                // Loop the current album from the first track in album order
                selectTrack(0, autoplay = true)
            }
            RepeatMode.OFF -> {
                // Final track of album has completed: pause cleanly at the end without jumping to another album
                exoPlayer.pause()
            }
        }
    }

    override fun onCleared() {
        exoPlayer.release()
        super.onCleared()
    }
}
