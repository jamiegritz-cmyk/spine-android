package com.spine.musicplayer.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.spine.musicplayer.data.network.CoverArtArchiveClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.security.MessageDigest

/**
 * Repository responsible for:
 * 1. Extracting and caching embedded ID3 album art from local audio files.
 * 2. Identifying missing releases on MusicBrainz.
 * 3. Fetching genuine front artwork from the Cover Art Archive.
 * 4. Caching all artwork locally on the Android device's disk.
 */
class CoverArtRepository(private val context: Context) {

    private val networkClient = CoverArtArchiveClient()
    private val artworkCacheDir: File by lazy {
        File(context.cacheDir, "spine_artwork").apply { mkdirs() }
    }

    /**
     * Obtains artwork for an album: checks local disk cache, then queries MusicBrainz + CAA.
     */
    suspend fun getOrFetchArtwork(album: String, artist: String): Uri? = withContext(Dispatchers.IO) {
        val cacheKey = hashKey("$album-$artist")
        val cachedFile = File(artworkCacheDir, "$cacheKey.jpg")

        if (cachedFile.exists() && cachedFile.length() > 0) {
            return@withContext Uri.fromFile(cachedFile)
        }

        // Query MusicBrainz and Cover Art Archive
        val onlineUrlString = networkClient.findCoverArt(album, artist) ?: return@withContext null

        try {
            val url = URL(onlineUrlString)
            url.openStream().use { input ->
                FileOutputStream(cachedFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(cachedFile)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Saves raw embedded picture bytes from MediaMetadataRetriever directly to local cache.
     */
    suspend fun saveEmbeddedPicture(album: String, artist: String, pictureBytes: ByteArray): Uri? =
        withContext(Dispatchers.IO) {
            try {
                val cacheKey = hashKey("$album-$artist-embedded")
                val targetFile = File(artworkCacheDir, "$cacheKey.jpg")
                FileOutputStream(targetFile).use { fos ->
                    fos.write(pictureBytes)
                }
                Uri.fromFile(targetFile)
            } catch (_: Exception) {
                null
            }
        }

    private fun hashKey(key: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val hash = digest.digest(key.lowercase().toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}
