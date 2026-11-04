package com.example.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongsFlow(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoritesFlow(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE lastPlayedAt > 0 ORDER BY lastPlayedAt DESC LIMIT 50")
    fun getRecentlyPlayedFlow(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE playCount > 0 ORDER BY playCount DESC LIMIT 50")
    fun getMostPlayedFlow(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE playCount = 0 ORDER BY dateAdded DESC")
    fun getNeverPlayedFlow(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongById(id: Long): SongEntity?

    @Query("SELECT * FROM songs WHERE id IN (:ids)")
    suspend fun getSongsByIds(ids: List<Long>): List<SongEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Update
    suspend fun updateSong(song: SongEntity)

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayedAt = :timestamp WHERE id = :id")
    suspend fun recordPlay(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE songs 
        SET title = :title, artist = :artist, album = :album, albumArtist = :albumArtist,
            genre = :genre, year = :year, trackNumber = :trackNumber, discNumber = :discNumber,
            composer = :composer, comments = :comments, lyrics = :lyrics
        WHERE id = :id
    """)
    suspend fun updateMetadata(
        id: Long,
        title: String,
        artist: String,
        album: String,
        albumArtist: String,
        genre: String,
        year: String,
        trackNumber: String,
        discNumber: String,
        composer: String,
        comments: String,
        lyrics: String
    )

    @Query("""
        UPDATE songs
        SET artist = CASE WHEN :artist != '' THEN :artist ELSE artist END,
            album = CASE WHEN :album != '' THEN :album ELSE album END,
            genre = CASE WHEN :genre != '' THEN :genre ELSE genre END
        WHERE id IN (:ids)
    """)
    suspend fun batchUpdateTags(ids: List<Long>, artist: String, album: String, genre: String)

    @Query("UPDATE songs SET lyrics = :lyrics WHERE id = :id")
    suspend fun updateLyrics(id: Long, lyrics: String)

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSong(id: Long)

    @Query("DELETE FROM songs WHERE id IN (:ids)")
    suspend fun deleteSongs(ids: List<Long>)
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylistsFlow(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylistById(id: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongsToPlaylist(crossRefs: List<PlaylistSongCrossRef>)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId IN (:songIds)")
    suspend fun removeSongsFromPlaylist(playlistId: Long, songIds: List<Long>)

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_songs ps ON s.id = ps.songId
        WHERE ps.playlistId = :playlistId
        ORDER BY ps.orderIndex ASC
    """)
    fun getSongsInPlaylistFlow(playlistId: Long): Flow<List<SongEntity>>

    @Query("SELECT playlistId FROM playlist_songs WHERE songId = :songId")
    suspend fun getPlaylistIdsContainingSong(songId: Long): List<Long>

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun getSongCountForPlaylist(playlistId: Long): Int

    @Query("SELECT playlistId, COUNT(songId) as count FROM playlist_songs GROUP BY playlistId")
    suspend fun getSongCounts(): List<PlaylistCountResult>
}

data class PlaylistCountResult(
    val playlistId: Long,
    val count: Int
)

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY addedAt DESC")
    fun getAllDownloadsFlow(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun getDownloadById(id: Long): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity): Long

    @Update
    suspend fun updateDownload(download: DownloadEntity)

    @Query("UPDATE downloads SET progress = :progress, status = :status WHERE id = :id")
    suspend fun updateProgress(id: Long, progress: Int, status: String)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownload(id: Long)
}

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: PlayHistoryEntity)

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN play_history h ON s.id = h.songId
        ORDER BY h.playedAt DESC LIMIT 100
    """)
    fun getRecentHistorySongsFlow(): Flow<List<SongEntity>>

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN play_history h ON s.id = h.songId
        WHERE h.playedAt >= :sinceTimestamp
        ORDER BY h.playedAt DESC
    """)
    suspend fun getHistorySince(sinceTimestamp: Long): List<SongEntity>

    @Query("DELETE FROM play_history WHERE songId = :songId")
    suspend fun clearHistoryForSong(songId: Long)

    @Query("DELETE FROM play_history WHERE songId IN (:songIds)")
    suspend fun clearHistoryForSongs(songIds: List<Long>)

    @Query("DELETE FROM play_history")
    suspend fun clearHistory()
}

@Dao
interface QueueDao {
    @Query("SELECT * FROM playback_queues ORDER BY id ASC")
    fun getAllQueuesFlow(): Flow<List<PlaybackQueueEntity>>

    @Query("SELECT * FROM playback_queues WHERE id = :id LIMIT 1")
    suspend fun getQueueById(id: Long): PlaybackQueueEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateQueue(queue: PlaybackQueueEntity)

    @Query("DELETE FROM playback_queues WHERE id = :id")
    suspend fun deleteQueue(id: Long)

    @Query("SELECT COUNT(*) FROM playback_queues")
    suspend fun getQueueCount(): Int
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM song_bookmarks WHERE songId = :songId ORDER BY positionMs ASC")
    fun getBookmarksForSongFlow(songId: Long): Flow<List<SongBookmarkEntity>>

    @Query("SELECT * FROM song_bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarksFlow(): Flow<List<SongBookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: SongBookmarkEntity): Long

    @Query("DELETE FROM song_bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: Long)

    @Query("DELETE FROM song_bookmarks WHERE songId = :songId")
    suspend fun deleteBookmarksForSong(songId: Long)
}

@Dao
interface DeviceEqualizerDao {
    @Query("SELECT * FROM device_equalizers WHERE deviceType = :deviceType LIMIT 1")
    suspend fun getProfile(deviceType: String): DeviceEqualizerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: DeviceEqualizerEntity)

    @Query("SELECT * FROM device_equalizers")
    fun getAllProfilesFlow(): Flow<List<DeviceEqualizerEntity>>
}
