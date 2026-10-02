package com.spine.musicplayer.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * Separate, replaceable interface for optional online artwork providers.
 * Adheres to GRAIZ policy: do not assume online metadata services grant commercial rights
 * to reproduce artwork. Keeps online providers decoupled and non-reliant.
 */
interface OnlineArtworkProvider {
    suspend fun resolveCoverArtUri(album: String, artist: String): String?
}

/**
 * Repository enforcing the strict three-tier artwork architecture:
 * 1. Embedded Artwork: First priority. Uses genuine embedded picture metadata directly from user's audio file.
 * 2. Manual User Artwork: Second priority. Allows user to select a picture file from their device storage.
 * 3. Replaceable Online Provider: Third priority, completely decoupled and disabled by default
 *    unless an explicitly licensed commercial provider is supplied.
 */
class CoverArtRepository(
    private val context: Context,
    private val optionalOnlineProvider: OnlineArtworkProvider? = null
) {

    private val artworkCacheDir: File by lazy {
        File(context.cacheDir, "graiz_artwork").apply { mkdirs() }
    }

    /**
     * Resolves artwork with strict priority:
     * 1. Check local embedded or manually assigned cache file.
     * 2. If missing, optionally delegates to a licensed OnlineArtworkProvider if configured.
     */
    suspend fun getOrFetchArtwork(album: String, artist: String): Uri? = withContext(Dispatchers.IO) {
        // Priority 1 & 2: Local cache (embedded picture or user-selected picture)
        val cacheKey = hashKey("$album-$artist")
        val cachedFile = File(artworkCacheDir, "$cacheKey.jpg")

        if (cachedFile.exists() && cachedFile.length() > 0) {
            return@withContext Uri.fromFile(cachedFile)
        }

        // Priority 3: Optional, explicitly licensed provider (pluggable/replaceable)
        val onlineUrl = optionalOnlineProvider?.resolveCoverArtUri(album, artist) ?: return@withContext null
        try {
            val url = java.net.URL(onlineUrl)
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
     * Priority 1: Saves raw embedded picture bytes from MediaMetadataRetriever directly to local cache.
     */
    suspend fun saveEmbeddedPicture(album: String, artist: String, pictureBytes: ByteArray): Uri? =
        withContext(Dispatchers.IO) {
            try {
                val cacheKey = hashKey("$album-$artist")
                val targetFile = File(artworkCacheDir, "$cacheKey.jpg")
                FileOutputStream(targetFile).use { fos ->
                    fos.write(pictureBytes)
                }
                Uri.fromFile(targetFile)
            } catch (_: Exception) {
                null
            }
        }

    /**
     * Priority 2: Saves a user manually selected artwork from device storage for an album.
     */
    suspend fun saveUserSelectedArtwork(album: String, artist: String, sourceUri: Uri): Uri? =
        withContext(Dispatchers.IO) {
            try {
                val cacheKey = hashKey("$album-$artist")
                val targetFile = File(artworkCacheDir, "$cacheKey.jpg")
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
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
