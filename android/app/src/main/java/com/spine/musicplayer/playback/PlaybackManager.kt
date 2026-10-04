package com.spine.musicplayer.playback

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import com.spine.musicplayer.model.Release

/**
 * Singleton holder providing ONE authoritative ExoPlayer instance
 * shared seamlessly between the GRAIZ phone UI (PlayerViewModel) and Android Auto (SpineMediaPlaybackService).
 */
object PlaybackManager {
    @Volatile
    private var sharedPlayer: ExoPlayer? = null

    @Volatile
    var currentReleases: List<Release> = emptyList()

    @Synchronized
    fun getSharedPlayer(context: Context): ExoPlayer {
        if (sharedPlayer == null) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()

            val exo = ExoPlayer.Builder(context.applicationContext)
                .setAudioAttributes(audioAttributes, true)
                .setHandleAudioBecomingNoisy(true)
                .build()
            sharedPlayer = exo
        }
        return sharedPlayer!!
    }

    fun releasePlayer() {
        sharedPlayer?.release()
        sharedPlayer = null
    }
}
