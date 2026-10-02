package com.spine.musicplayer.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Automatically looks up missing album artwork via MusicBrainz and Cover Art Archive.
 * URL standard: https://coverartarchive.org/release/{mbid}/front
 */
class CoverArtArchiveClient {

    suspend fun findCoverArt(album: String, artist: String): String? = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode("release:\"$album\" AND artist:\"$artist\"", "UTF-8")
            val mbUrl = URL("https://musicbrainz.org/ws/2/release/?query=$encodedQuery&fmt=json&limit=1")

            val connection = (mbUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "SpineMusicPlayer/1.0.0 (android-client@spinemusic.app)")
                connectTimeout = 5000
                readTimeout = 5000
            }

            if (connection.responseCode != 200) return@withContext null

            val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)
            val releases = root.optJSONArray("releases") ?: return@withContext null
            if (releases.length() == 0) return@withContext null

            val mbid = releases.getJSONObject(0).optString("id")
            if (mbid.isNullOrEmpty()) return@withContext null

            // Check if Cover Art Archive has a front cover
            val caaUrl = "https://coverartarchive.org/release/$mbid/front-500"
            val caaCheck = (URL(caaUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "HEAD"
                instanceFollowRedirects = true
                connectTimeout = 4000
            }

            if (caaCheck.responseCode == 200 || caaCheck.responseCode == 302 || caaCheck.responseCode == 307) {
                return@withContext caaUrl
            }

            null
        } catch (_: Exception) {
            null
        }
    }
}
