package com.spine.musicplayer.playback

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.spine.musicplayer.MainActivity
import com.spine.musicplayer.data.MediaStoreAudioScanner
import com.spine.musicplayer.model.Release
import com.spine.musicplayer.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * MediaLibraryService for Android Auto and background playback.
 * Connects directly to PlaybackManager so Android Auto and the GRAIZ phone UI
 * share the exact same ExoPlayer instance, playback state, and filtered MediaStore library.
 */
class SpineMediaPlaybackService : MediaLibraryService() {

    private var mediaLibrarySession: MediaLibrarySession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var audioScanner: MediaStoreAudioScanner

    companion object {
        const val ROOT_ID = "root"
        const val CATEGORY_ALBUMS = "category_albums"
        const val CATEGORY_ARTISTS = "category_artists"
        const val CATEGORY_SONGS = "category_songs"
        const val PREFIX_ALBUM = "album_"
        const val PREFIX_ARTIST = "artist_"
        const val PREFIX_TRACK = "track_"
    }

    override fun onCreate() {
        super.onCreate()
        audioScanner = MediaStoreAudioScanner(applicationContext)

        val player = PlaybackManager.getSharedPlayer(this)
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        mediaLibrarySession = MediaLibrarySession.Builder(this, player, LibraryCallback())
            .setSessionActivity(sessionActivityPendingIntent)
            .build()

        if (PlaybackManager.currentReleases.isEmpty()) {
            serviceScope.launch(Dispatchers.IO) {
                val scanned = audioScanner.scanLocalReleases()
                PlaybackManager.currentReleases = scanned
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    override fun onDestroy() {
        mediaLibrarySession?.run {
            release()
            mediaLibrarySession = null
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    private inner class LibraryCallback : MediaLibrarySession.Callback {

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val rootItem = MediaItem.Builder()
                .setMediaId(ROOT_ID)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("GRAIZ")
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                        .build()
                )
                .build()
            return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val currentReleases = PlaybackManager.currentReleases
            if (currentReleases.isEmpty()) {
                val future = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()
                serviceScope.launch(Dispatchers.IO) {
                    val scanned = audioScanner.scanLocalReleases()
                    PlaybackManager.currentReleases = scanned
                    val items = buildChildren(parentId, scanned)
                    future.set(LibraryResult.ofItemList(items, params))
                }
                return future
            } else {
                val items = buildChildren(parentId, currentReleases)
                return Futures.immediateFuture(LibraryResult.ofItemList(items, params))
            }
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val currentReleases = PlaybackManager.currentReleases
            if (mediaId == ROOT_ID) {
                val rootItem = MediaItem.Builder()
                    .setMediaId(ROOT_ID)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle("GRAIZ")
                            .setIsBrowsable(true)
                            .setIsPlayable(false)
                            .build()
                    )
                    .build()
                return Futures.immediateFuture(LibraryResult.ofItem(rootItem, null))
            }

            if (mediaId == CATEGORY_ALBUMS) {
                return Futures.immediateFuture(LibraryResult.ofItem(buildCategoryItem(CATEGORY_ALBUMS, "Albums", "${currentReleases.size} Albums"), null))
            }

            if (mediaId == CATEGORY_ARTISTS) {
                val artistsCount = currentReleases.map { it.artist }.distinct().size
                return Futures.immediateFuture(LibraryResult.ofItem(buildCategoryItem(CATEGORY_ARTISTS, "Artists", "$artistsCount Artists"), null))
            }

            if (mediaId == CATEGORY_SONGS) {
                val totalTracks = currentReleases.sumOf { it.tracks.size }
                return Futures.immediateFuture(LibraryResult.ofItem(buildCategoryItem(CATEGORY_SONGS, "Songs", "$totalTracks Songs"), null))
            }

            if (mediaId.startsWith(PREFIX_ALBUM)) {
                val albumId = mediaId.removePrefix(PREFIX_ALBUM)
                val release = currentReleases.find { it.id == albumId }
                if (release != null) {
                    return Futures.immediateFuture(LibraryResult.ofItem(buildAlbumItem(release), null))
                }
            }

            if (mediaId.startsWith(PREFIX_TRACK)) {
                val trackId = mediaId.removePrefix(PREFIX_TRACK).toLongOrNull()
                val match = findTrackAndRelease(trackId, currentReleases)
                if (match != null) {
                    return Futures.immediateFuture(LibraryResult.ofItem(buildTrackItem(match.first, match.second), null))
                }
            }

            return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            val currentReleases = PlaybackManager.currentReleases
            val resolvedList = mutableListOf<MediaItem>()
            for (item in mediaItems) {
                val id = item.mediaId
                if (id.startsWith(PREFIX_ALBUM)) {
                    val albumId = id.removePrefix(PREFIX_ALBUM)
                    val release = currentReleases.find { it.id == albumId }
                    if (release != null) {
                        for (track in release.tracks) {
                            resolvedList.add(buildTrackItem(track, release))
                        }
                    }
                } else if (id.startsWith(PREFIX_TRACK)) {
                    val trackId = id.removePrefix(PREFIX_TRACK).toLongOrNull()
                    val match = findTrackAndRelease(trackId, currentReleases)
                    if (match != null) {
                        resolvedList.add(buildTrackItem(match.first, match.second))
                    } else {
                        resolvedList.add(item)
                    }
                } else {
                    resolvedList.add(item)
                }
            }
            return Futures.immediateFuture(resolvedList)
        }

        override fun onSearch(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            val results = searchReleasesAndTracks(query, PlaybackManager.currentReleases)
            session.notifySearchResultChanged(browser, query, results.size, params)
            return Futures.immediateFuture(LibraryResult.ofVoid(params))
        }

        override fun onGetSearchResult(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val results = searchReleasesAndTracks(query, PlaybackManager.currentReleases)
            return Futures.immediateFuture(LibraryResult.ofItemList(results, params))
        }
    }

    private fun buildChildren(parentId: String, releases: List<Release>): ImmutableList<MediaItem> {
        val listBuilder = ImmutableList.builder<MediaItem>()

        when {
            parentId == ROOT_ID -> {
                listBuilder.add(buildCategoryItem(CATEGORY_ALBUMS, "Albums", "${releases.size} Albums"))
                val artistsCount = releases.map { it.artist }.distinct().size
                listBuilder.add(buildCategoryItem(CATEGORY_ARTISTS, "Artists", "$artistsCount Artists"))
                val totalTracks = releases.sumOf { it.tracks.size }
                listBuilder.add(buildCategoryItem(CATEGORY_SONGS, "Songs", "$totalTracks Songs"))
            }

            parentId == CATEGORY_ALBUMS -> {
                for (release in releases) {
                    listBuilder.add(buildAlbumItem(release))
                }
            }

            parentId == CATEGORY_ARTISTS -> {
                val uniqueArtists = releases.map { it.artist }.distinct().sorted()
                for (artist in uniqueArtists) {
                    val count = releases.count { it.artist == artist }
                    listBuilder.add(
                        MediaItem.Builder()
                            .setMediaId("$PREFIX_ARTIST$artist")
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setTitle(artist)
                                    .setSubtitle("$count Albums")
                                    .setIsBrowsable(true)
                                    .setIsPlayable(false)
                                    .setFolderType(MediaMetadata.FOLDER_TYPE_ARTISTS)
                                    .setMediaType(MediaMetadata.MEDIA_TYPE_ARTIST)
                                    .build()
                            )
                            .build()
                    )
                }
            }

            parentId == CATEGORY_SONGS -> {
                for (release in releases) {
                    for (track in release.tracks) {
                        listBuilder.add(buildTrackItem(track, release))
                    }
                }
            }

            parentId.startsWith(PREFIX_ARTIST) -> {
                val artistName = parentId.removePrefix(PREFIX_ARTIST)
                val matching = releases.filter { it.artist.equals(artistName, ignoreCase = true) }
                for (rel in matching) {
                    listBuilder.add(buildAlbumItem(rel))
                }
            }

            parentId.startsWith(PREFIX_ALBUM) -> {
                val albumId = parentId.removePrefix(PREFIX_ALBUM)
                val release = releases.find { it.id == albumId }
                if (release != null) {
                    for (track in release.tracks) {
                        listBuilder.add(buildTrackItem(track, release))
                    }
                }
            }
        }

        return listBuilder.build()
    }

    private fun buildCategoryItem(id: String, title: String, subtitle: String): MediaItem {
        return MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .setFolderType(
                        when (id) {
                            CATEGORY_ALBUMS -> MediaMetadata.FOLDER_TYPE_ALBUMS
                            CATEGORY_ARTISTS -> MediaMetadata.FOLDER_TYPE_ARTISTS
                            else -> MediaMetadata.FOLDER_TYPE_TITLES
                        }
                    )
                    .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                    .build()
            )
            .build()
    }

    private fun buildAlbumItem(release: Release): MediaItem {
        return MediaItem.Builder()
            .setMediaId("$PREFIX_ALBUM${release.id}")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(release.title)
                    .setArtist(release.artist)
                    .setArtworkUri(release.artworkUri)
                    .setIsBrowsable(true)
                    .setIsPlayable(true)
                    .setFolderType(MediaMetadata.FOLDER_TYPE_ALBUMS)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_ALBUM)
                    .build()
            )
            .build()
    }

    private fun buildTrackItem(track: Track, release: Release): MediaItem {
        return MediaItem.Builder()
            .setMediaId("$PREFIX_TRACK${track.id}")
            .setUri(track.contentUri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    .setAlbumTitle(release.title)
                    .setArtworkUri(release.artworkUri)
                    .setIsBrowsable(false)
                    .setIsPlayable(true)
                    .setTrackNumber(track.trackNumber)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                    .build()
            )
            .build()
    }

    private fun findTrackAndRelease(trackId: Long?, releases: List<Release>): Pair<Track, Release>? {
        if (trackId == null) return null
        for (release in releases) {
            val track = release.tracks.find { it.id == trackId }
            if (track != null) return Pair(track, release)
        }
        return null
    }

    private fun searchReleasesAndTracks(query: String, releases: List<Release>): ImmutableList<MediaItem> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return ImmutableList.of()

        val listBuilder = ImmutableList.builder<MediaItem>()
        for (release in releases) {
            if (release.title.lowercase().contains(q) || release.artist.lowercase().contains(q)) {
                listBuilder.add(buildAlbumItem(release))
            }
            for (track in release.tracks) {
                if (track.title.lowercase().contains(q) || track.artist.lowercase().contains(q)) {
                    listBuilder.add(buildTrackItem(track, release))
                }
            }
        }
        return listBuilder.build()
    }
}
