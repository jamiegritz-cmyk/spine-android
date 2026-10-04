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
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA
        )

        val selectedFolderPath = MusicFolderPreferences.getSelectedFolderPath(context)?.trim('/')

        // Broad audio selection: includes IS_MUSIC != 0 OR audio mime-type to capture all local MP3/audio files
        val selection = "(${MediaStore.Audio.Media.IS_MUSIC} != 0 OR ${MediaStore.Audio.Media.MIME_TYPE} LIKE 'audio/%') AND (${MediaStore.Audio.Media.DURATION} >= 5000 OR ${MediaStore.Audio.Media.DURATION} IS NULL OR ${MediaStore.Audio.Media.DURATION} = 0)"
        val sortOrder = "${MediaStore.Audio.Media.ALBUM} ASC, ${MediaStore.Audio.Media.TRACK} ASC, ${MediaStore.Audio.Media.TITLE} ASC"

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
            val displayNameCol = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
            val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val dataPath = if (dataCol >= 0) cursor.getString(dataCol) ?: "" else ""

                // Filter by authoritative selected folder if configured
                if (!selectedFolderPath.isNullOrEmpty()) {
                    val cleanDataPath = dataPath.replace('\\', '/')
                    val isInFolder = cleanDataPath.contains("/$selectedFolderPath/", ignoreCase = true) ||
                            cleanDataPath.contains("/$selectedFolderPath", ignoreCase = true)
                    if (!isInFolder) {
                        continue
                    }
                }
                val id = cursor.getLong(idCol)
                val rawTitle = cursor.getString(titleCol)
                val displayName = if (displayNameCol >= 0) cursor.getString(displayNameCol) else null
                val title = when {
                    !rawTitle.isNullOrBlank() -> rawTitle
                    !displayName.isNullOrBlank() -> displayName.substringBeforeLast(".")
                    else -> "Track $id"
                }

                val rawArtist = cursor.getString(artistCol)
                val artist = if (!rawArtist.isNullOrBlank() && rawArtist != "<unknown>") rawArtist else "Unknown Artist"

                val rawAlbum = cursor.getString(albumCol)
                val album = if (!rawAlbum.isNullOrBlank() && rawAlbum != "<unknown>") rawAlbum else "Unknown Album"

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

                // Group by album if album tag is present; if no album tag, group by song so singles are not lost
                val albumKey = if (album != "Unknown Album") album else "${title} - $artist"

                val albumTracks = tracksByAlbum.getOrPut(albumKey) { mutableListOf() }
                albumTracks.add(track)

                if (!albumArtistMap.containsKey(albumKey)) {
                    albumArtistMap[albumKey] = artist
                }
                if (!firstTrackUriMap.containsKey(albumKey)) {
                    firstTrackUriMap[albumKey] = contentUri
                }
                if (!albumArtMap.containsKey(albumKey)) {
                    val artUri = ContentUris.withAppendedId(artworkBaseUri, albumId)
                    albumArtMap[albumKey] = artUri
                }
                if (year > 0 && !albumYearMap.containsKey(albumKey)) {
                    albumYearMap[albumKey] = year
                }
            }
        }

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
            val spineColor = com.spine.musicplayer.ui.AlbumVisualIdentityResolver.resolveDominantColorHex(albumName, artist)
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
