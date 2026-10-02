package com.spine.musicplayer.model

import android.net.Uri

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val trackNumber: Int,
    val contentUri: Uri
)

enum class ReleaseType {
    ALBUM, SINGLE
}

data class Release(
    val id: String,
    val title: String,
    val artist: String,
    val year: Int?,
    val artworkUri: Uri?,
    val tracks: List<Track>,
    val spineColorHex: String,
    val catalogNumber: String,
    val genre: String = "Physical Audio",
    val type: ReleaseType = if (tracks.size <= 2) ReleaseType.SINGLE else ReleaseType.ALBUM
) {
    val totalDurationMs: Long get() = tracks.sumOf { it.durationMs }
}

enum class RepeatMode {
    OFF, ALL, ONE
}
