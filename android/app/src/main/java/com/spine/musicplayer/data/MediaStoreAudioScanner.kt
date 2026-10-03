package com.spine.musicplayer.data

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.spine.musicplayer.model.Release
import com.spine.musicplayer.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.abs

/**
 * Scans local storage on the Android device via MediaStore.Audio.
 * Extracts metadata: artist, album, track title, album artist, year, track number.
 * 1. Uses embedded album artwork if available.
 * 2. If missing, queries MusicBrainz & Cover Art Archive.
 * 3. Caches artwork locally.
 */
class MediaStoreAudioScanner(private val context: Context) {

    private val coverArtRepository = CoverArtRepository(context)

    suspend fun scanLocalReleases(): List<Release> = withContext(Dispatchers.IO) {
        val tracksByAlbum = mutableMapOf<String, MutableList<Track>>()
        val albumArtMap = mutableMapOf<String, Uri?>()
        val albumArtistMap = mutableMapOf<String, String>()
        val albumYearMap = mutableMapOf<String, Int>()
        val firstTrackUriMap = mutableMapOf<String, Uri>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 15000"
        val sortOrder = "${MediaStore.Audio.Media.ALBUM} ASC, ${MediaStore.Audio.Media.TRACK} ASC"

        val artworkBaseUri = Uri.parse("content://media/external/audio/albumart")

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol) ?: "Untitled Track"
                val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                val album = cursor.getString(albumCol) ?: "Unknown Album"
                val albumId = cursor.getLong(albumIdCol)
                val duration = cursor.getLong(durationCol)
                val trackNum = cursor.getInt(trackCol)
                val year = cursor.getInt(yearCol)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val track = Track(
                    id = id,
                    title = title,
                    artist = artist,
                    album = album,
                    durationMs = duration,
                    trackNumber = if (trackNum > 0) trackNum % 1000 else 1,
                    contentUri = contentUri
                )

                val albumTracks = tracksByAlbum.getOrPut(album) { mutableListOf() }
                albumTracks.add(track)

                if (!albumArtistMap.containsKey(album)) {
                    albumArtistMap[album] = artist
                }
                if (!firstTrackUriMap.containsKey(album)) {
                    firstTrackUriMap[album] = contentUri
                }
                if (!albumArtMap.containsKey(album)) {
                    val artUri = ContentUris.withAppendedId(artworkBaseUri, albumId)
                    albumArtMap[album] = artUri
                }
                if (year > 0 && !albumYearMap.containsKey(album)) {
                    albumYearMap[album] = year
                }
            }
        }

        // Palette of rich jewel case spine colors
        val palette = listOf(
            "#1E293B", "#334155", "#0F172A", "#18181B", "#27272A",
            "#1C1917", "#292524", "#0C4A6E", "#164E63", "#064E3B",
            "#701A75", "#831843", "#881337", "#431407", "#365314"
        )

        tracksByAlbum.entries.mapIndexed { index, entry ->
            val albumName = entry.key
            val albumTracks = entry.value
            val artist = albumArtistMap[albumName] ?: "Various Artists"
            var artworkUri = albumArtMap[albumName]

            // 1. Try extracting genuine embedded APIC picture bytes
            val trackUri = firstTrackUriMap[albumName]
            if (trackUri != null) {
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(context, trackUri)
                    val rawPicture = retriever.embeddedPicture
                    retriever.release()

                    if (rawPicture != null && rawPicture.isNotEmpty()) {
                        val cachedEmbedded = coverArtRepository.saveEmbeddedPicture(albumName, artist, rawPicture)
                        if (cachedEmbedded != null) {
                            artworkUri = cachedEmbedded
                        }
                    }
                } catch (_: Exception) {
                    // Fallback to MusicBrainz
                }
            }

            // 2. If embedded art is absent, query MusicBrainz & Cover Art Archive
            if (artworkUri == null) {
                val resolvedArt = coverArtRepository.getOrFetchArtwork(albumName, artist)
                if (resolvedArt != null) {
                    artworkUri = resolvedArt
                }
            }

            val hash = abs((albumName + artist).hashCode())
            val spineColor = palette[hash % palette.size]
            val catPrefix = (artist.take(2).uppercase(Locale.ROOT)).ifEmpty { "SP" }
            val catNum = String.format(Locale.ROOT, "%s-%04d", catPrefix, (hash % 9000) + 1000)

            Release(
                id = "album_${hash}_$index",
                title = albumName,
                artist = artist,
                year = albumYearMap[albumName],
                artworkUri = artworkUri,
                tracks = albumTracks.sortedBy { track -> track.trackNumber },
                spineColorHex = spineColor,
                catalogNumber = catNum
            )
        }
    }
}
