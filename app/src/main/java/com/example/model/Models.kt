package com.example.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String = "<unknown>",
    val album: String = "<unknown>",
    val albumArtist: String = "",
    val genre: String = "Geral",
    val year: String = "",
    val trackNumber: String = "",
    val discNumber: String = "1",
    val composer: String = "",
    val comments: String = "",
    val lyrics: String = "",
    val durationMs: Long = 0L,
    val dataUri: String = "",
    val albumArtUri: String? = null,
    val folderName: String = "Music",
    val sizeBytes: Long = 0L,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val dateAdded: Long = 0L,
    val lastPlayedAt: Long = 0L
) {
    val formattedDuration: String
        get() {
            val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

data class Album(
    val name: String,
    val artist: String,
    val songCount: Int,
    val albumArtUri: String? = null
)

data class Artist(
    val name: String,
    val songCount: Int,
    val albumCount: Int = 1
)

data class Folder(
    val name: String,
    val path: String,
    val songCount: Int
)

data class Playlist(
    val id: Long = 0L,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val songCount: Int = 0,
    val isSmart: Boolean = false,
    val smartFilterType: String? = null,
    val isExternal: Boolean = false,
    val sourcePath: String? = null
)

enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    PAUSED
}

data class DownloadItem(
    val id: Long = 0L,
    val url: String,
    val title: String,
    val artist: String = "Unknown",
    val durationMs: Long = 0L,
    val format: String = "MP3",
    val thumbnail: String? = null,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val progress: Int = 0,
    val filePath: String = "",
    val errorMsg: String? = null,
    val source: String = "Direct",
    val addedAt: Long = System.currentTimeMillis(),
    val estimatedBytes: Long = 0L
)

enum class AudioOutputDevice(val label: String) {
    SPEAKER("Alto-falante do Celular"),
    WIRED_HEADSET("Fone de Ouvido com Fio"),
    BLUETOOTH("Fone / Dispositivo Bluetooth"),
    USB("Áudio USB / DAC")
}

data class EqualizerConfig(
    val isEnabled: Boolean = true,
    val presetName: String = "Normal",
    val bassBoost: Int = 0, // 0 to 1000
    val virtualizer: Int = 0, // 0 to 1000
    val bandLevels: Map<Int, Int> = emptyMap(), // bandIndex 0..9 to millibels (-1200 to +1200)
    val channelBalance: Float = 0f, // -1.0 (left) to 1.0 (right)
    val customPresets: Map<String, Map<Int, Int>> = emptyMap()
)

data class SleepTimerConfig(
    val isRunning: Boolean = false,
    val remainingSeconds: Long = 0L,
    val stopAfterCurrentTrack: Boolean = false,
    val stopAfterNextTrack: Boolean = false,
    val stopAfterTracksCount: Int = 0, // 0 = time based, >0 = count down tracks
    val fadeOutVolume: Boolean = true
)

enum class SortOrder(val label: String) {
    NAME_ASC("Nome (A-Z)"),
    NAME_DESC("Nome (Z-A)"),
    ARTIST_ASC("Artista (A-Z)"),
    ALBUM_ASC("Álbum (A-Z)"),
    DURATION_DESC("Duração (Maior)"),
    DATE_ADDED_DESC("Data de adição (Recente)"),
    PLAY_COUNT_DESC("Mais Tocadas")
}

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

// Musicolet-inspired: Multiple Independent Queues (up to 20 queues)
data class PlaybackQueue(
    val id: Long = 1L, // 1 to 20
    val name: String = "Fila 1",
    val songs: List<Song> = emptyList(),
    val currentSongIndex: Int = 0,
    val currentPositionMs: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val updatedAt: Long = System.currentTimeMillis()
)

// Musicolet-inspired: Bookmarks & Notes per song
data class SongBookmark(
    val id: Long = 0L,
    val songId: Long,
    val positionMs: Long,
    val name: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val formattedPosition: String
        get() {
            val totalSeconds = (positionMs / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

// Musicolet-inspired: Synchronized Lyrics
data class LyricLine(
    val timestampMs: Long,
    val text: String
)

data class SongLyrics(
    val songId: Long,
    val rawText: String,
    val lines: List<LyricLine>,
    val isSynchronized: Boolean
)

// A-B Repeat Mode
data class ABRepeatState(
    val isEnabled: Boolean = false,
    val pointA: Long? = null,
    val pointB: Long? = null
)

// Playback Speed & Pitch
data class PlaybackSpeedPitch(
    val speed: Float = 1.0f, // 0.25x to 3.0x
    val pitch: Float = 1.0f  // 0.5x to 2.0x
)

// Statistics Periods & Aggregations
enum class StatisticsPeriod(val label: String) {
    TODAY("Hoje"),
    LAST_7_DAYS("Últimos 7 dias"),
    LAST_30_DAYS("Últimos 30 dias"),
    THIS_MONTH("Este mês"),
    THIS_YEAR("Este ano"),
    ALL_TIME("Todo o período")
}

data class MusicStats(
    val topSongs: List<Song> = emptyList(),
    val topArtists: List<Pair<String, Int>> = emptyList(),
    val topAlbums: List<Pair<String, Int>> = emptyList(),
    val totalPlayTimeMs: Long = 0L,
    val totalSongsPlayed: Int = 0
)

// Folder Navigation Mode
enum class FolderViewMode {
    LINEAR,
    HIERARCHICAL
}

data class FolderNode(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val songCount: Int = 0,
    val children: List<FolderNode> = emptyList()
)

// Headset Controls Configuration
data class HeadsetControlsConfig(
    val singleClickAction: String = "PLAY_PAUSE",
    val doubleClickAction: String = "NEXT_TRACK",
    val tripleClickAction: String = "PREV_TRACK",
    val longPressAction: String = "SEEK_FORWARD_10S",
    val seekIntervalSeconds: Int = 10
)

enum class Screen {
    HOME,
    LIBRARY,
    PLAYER,
    PLAYLIST_DETAIL,
    EQUALIZER,
    DOWNLOADS,
    BLUETOOTH_AUTOMATION,
    SETTINGS,
    SEARCH,
    // New Musicolet-inspired screens:
    QUEUES_MANAGER,
    TAG_EDITOR,
    LYRICS_SYNC,
    STATISTICS,
    FOLDER_EXPLORER
}
