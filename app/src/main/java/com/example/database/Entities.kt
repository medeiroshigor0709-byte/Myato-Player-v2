package com.example.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.*
import org.json.JSONArray

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String = "",
    val genre: String = "Geral",
    val year: String = "",
    val trackNumber: String = "",
    val discNumber: String = "1",
    val composer: String = "",
    val comments: String = "",
    val lyrics: String = "",
    val durationMs: Long,
    val dataUri: String,
    val albumArtUri: String?,
    val folderName: String,
    val sizeBytes: Long,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val dateAdded: Long = 0L,
    val lastPlayedAt: Long = 0L
) {
    fun toSong(): Song = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        albumArtist = albumArtist,
        genre = genre,
        year = year,
        trackNumber = trackNumber,
        discNumber = discNumber,
        composer = composer,
        comments = comments,
        lyrics = lyrics,
        durationMs = durationMs,
        dataUri = dataUri,
        albumArtUri = albumArtUri,
        folderName = folderName,
        sizeBytes = sizeBytes,
        isFavorite = isFavorite,
        playCount = playCount,
        dateAdded = dateAdded,
        lastPlayedAt = lastPlayedAt
    )

    companion object {
        fun fromSong(song: Song): SongEntity = SongEntity(
            id = song.id,
            title = song.title,
            artist = song.artist,
            album = song.album,
            albumArtist = song.albumArtist,
            genre = song.genre,
            year = song.year,
            trackNumber = song.trackNumber,
            discNumber = song.discNumber,
            composer = song.composer,
            comments = song.comments,
            lyrics = song.lyrics,
            durationMs = song.durationMs,
            dataUri = song.dataUri,
            albumArtUri = song.albumArtUri,
            folderName = song.folderName,
            sizeBytes = song.sizeBytes,
            isFavorite = song.isFavorite,
            playCount = song.playCount,
            dateAdded = song.dateAdded,
            lastPlayedAt = song.lastPlayedAt
        )
    }
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isSmart: Boolean = false,
    val smartFilterType: String? = null
) {
    fun toPlaylist(count: Int = 0): Playlist = Playlist(
        id = id,
        name = name,
        createdAt = createdAt,
        songCount = count,
        isSmart = isSmart,
        smartFilterType = smartFilterType
    )
}

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    indices = [Index("playlistId"), Index("songId")]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long,
    val orderIndex: Int = 0
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val url: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val format: String,
    val thumbnail: String?,
    val status: String,
    val progress: Int,
    val filePath: String,
    val errorMsg: String?,
    val source: String,
    val addedAt: Long,
    val estimatedBytes: Long = 0L
) {
    fun toDownloadItem(): DownloadItem = DownloadItem(
        id = id,
        url = url,
        title = title,
        artist = artist,
        durationMs = durationMs,
        format = format,
        thumbnail = thumbnail,
        status = runCatching { DownloadStatus.valueOf(status) }.getOrDefault(DownloadStatus.QUEUED),
        progress = progress,
        filePath = filePath,
        errorMsg = errorMsg,
        source = source,
        addedAt = addedAt,
        estimatedBytes = estimatedBytes
    )
}

@Entity(tableName = "play_history")
data class PlayHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val songId: Long,
    val playedAt: Long = System.currentTimeMillis()
)

// Musicolet Multiple Queues (up to 20 queues)
@Entity(tableName = "playback_queues")
data class PlaybackQueueEntity(
    @PrimaryKey val id: Long, // 1 to 20
    val name: String,
    val songIdsJson: String,
    val currentSongIndex: Int = 0,
    val currentPositionMs: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: String = "OFF",
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getSongIds(): List<Long> {
        return runCatching {
            val arr = JSONArray(songIdsJson)
            val list = mutableListOf<Long>()
            for (i in 0 until arr.length()) {
                list.add(arr.getLong(i))
            }
            list
        }.getOrDefault(emptyList())
    }

    companion object {
        fun createJson(songIds: List<Long>): String {
            val arr = JSONArray()
            songIds.forEach { arr.put(it) }
            return arr.toString()
        }
    }
}

// Musicolet Bookmarks & Notes per song
@Entity(tableName = "song_bookmarks", indices = [Index("songId")])
data class SongBookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val songId: Long,
    val positionMs: Long,
    val name: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toBookmark(): SongBookmark = SongBookmark(
        id = id,
        songId = songId,
        positionMs = positionMs,
        name = name,
        note = note,
        createdAt = createdAt
    )
}

// Musicolet Device-Specific Equalizer Profiles
@Entity(tableName = "device_equalizers")
data class DeviceEqualizerEntity(
    @PrimaryKey val deviceType: String, // SPEAKER, WIRED_HEADSET, BLUETOOTH, USB
    val isEnabled: Boolean = true,
    val presetName: String = "Normal",
    val bassBoost: Int = 0,
    val virtualizer: Int = 0,
    val bandLevelsJson: String = "{}",
    val channelBalance: Float = 0f
)
