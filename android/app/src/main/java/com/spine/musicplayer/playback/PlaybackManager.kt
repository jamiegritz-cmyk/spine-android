package com.spine.musicplayer.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import com.spine.musicplayer.MainActivity
import com.spine.musicplayer.model.Release

/**
 * Singleton holder providing ONE authoritative ExoPlayer and MediaLibrarySession instance
 * shared seamlessly between the GRAIZ phone UI (PlayerViewModel) and Android Auto (SpineMediaPlaybackService).
 */
object PlaybackManager {
    @Volatile
    private var sharedPlayer: ExoPlayer? = null

    @Volatile
    private var sharedLibrarySession: MediaLibrarySession? = null

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

    @Synchronized
    fun getSharedMediaLibrarySession(
        context: Context,
        callback: MediaLibrarySession.Callback
    ): MediaLibrarySession {
        if (sharedLibrarySession == null) {
            val exo = getSharedPlayer(context)
            val sessionActivityPendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            sharedLibrarySession = MediaLibrarySession.Builder(context, exo, callback)
                .setSessionActivity(sessionActivityPendingIntent)
                .build()
        }
        return sharedLibrarySession!!
    }

    fun releaseAll() {
        sharedLibrarySession?.release()
        sharedLibrarySession = null
        sharedPlayer?.release()
        sharedPlayer = null
    }
}
