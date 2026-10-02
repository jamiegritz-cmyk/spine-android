package com.spine.musicplayer.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "releases")
data class ReleaseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val year: Int?,
    val artworkUri: String?,
    val spineColorHex: String,
    val catalogNumber: String,
    val genre: String,
    val isSingle: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tracks",
    foreignKeys = [
        ForeignKey(
            entity = ReleaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["releaseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("releaseId")]
)
data class TrackEntity(
    @PrimaryKey val id: Long,
    val releaseId: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val trackNumber: Int,
    val contentUri: String
)

data class ReleaseWithTracks(
    @Embedded val release: ReleaseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "releaseId"
    )
    val tracks: List<TrackEntity>
)

@Dao
interface MusicDao {
    @Transaction
    @Query("SELECT * FROM releases ORDER BY title ASC")
    fun getAllReleasesWithTracks(): Flow<List<ReleaseWithTracks>>

    @Transaction
    @Query("SELECT * FROM releases WHERE isSingle = 0 ORDER BY title ASC")
    fun getAlbums(): Flow<List<ReleaseWithTracks>>

    @Transaction
    @Query("SELECT * FROM releases WHERE isSingle = 1 ORDER BY title ASC")
    fun getSingles(): Flow<List<ReleaseWithTracks>>

    @Transaction
    @Query("""
        SELECT * FROM releases 
        WHERE title LIKE '%' || :query || '%' 
           OR artist LIKE '%' || :query || '%'
        ORDER BY title ASC
    """)
    fun searchReleases(query: String): Flow<List<ReleaseWithTracks>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReleases(releases: List<ReleaseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Query("UPDATE releases SET artworkUri = :artworkUri WHERE id = :releaseId")
    suspend fun updateArtwork(releaseId: String, artworkUri: String)

    @Query("DELETE FROM releases")
    suspend fun clearAll()
}

@Database(
    entities = [ReleaseEntity::class, TrackEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
}
