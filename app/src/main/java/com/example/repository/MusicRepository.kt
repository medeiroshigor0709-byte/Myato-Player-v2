package com.example.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.database.*
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.regex.Pattern

class MusicRepository(
    private val context: Context,
    private val database: MyatoDatabase
) {
    private val songDao = database.songDao()
    private val playlistDao = database.playlistDao()
    private val historyDao = database.historyDao()
    private val queueDao = database.queueDao()
    private val bookmarkDao = database.bookmarkDao()
    private val deviceEqualizerDao = database.deviceEqualizerDao()

    val allSongs: Flow<List<Song>> = songDao.getAllSongsFlow()
        .map { list -> list.map { it.toSong() } }

    val favoriteSongs: Flow<List<Song>> = songDao.getFavoritesFlow()
        .map { list -> list.map { it.toSong() } }

    val recentlyPlayed: Flow<List<Song>> = songDao.getRecentlyPlayedFlow()
        .map { list -> list.map { it.toSong() } }

    val mostPlayed: Flow<List<Song>> = songDao.getMostPlayedFlow()
        .map { list -> list.map { it.toSong() } }

    val neverPlayed: Flow<List<Song>> = songDao.getNeverPlayedFlow()
        .map { list -> list.map { it.toSong() } }

    val playlists: Flow<List<Playlist>> = playlistDao.getAllPlaylistsFlow()
        .map { list ->
            val counts = runCatching { playlistDao.getSongCounts() }.getOrDefault(emptyList())
            val countMap = counts.associate { it.playlistId to it.count }
            list.map { entity ->
                entity.toPlaylist(countMap[entity.id] ?: 0)
            }
        }

    val allQueues: Flow<List<PlaybackQueue>> = queueDao.getAllQueuesFlow()
        .combine(allSongs) { queueEntities, songs ->
            val songsMap = songs.associateBy { it.id }
            queueEntities.map { entity ->
                val qSongs = entity.getSongIds().mapNotNull { songsMap[it] }
                PlaybackQueue(
                    id = entity.id,
                    name = entity.name,
                    songs = qSongs,
                    currentSongIndex = entity.currentSongIndex,
                    currentPositionMs = entity.currentPositionMs,
                    isShuffle = entity.isShuffle,
                    repeatMode = runCatching { RepeatMode.valueOf(entity.repeatMode) }.getOrDefault(RepeatMode.OFF),
                    updatedAt = entity.updatedAt
                )
            }
        }

    suspend fun syncMediaStore() = withContext(Dispatchers.IO) {
        val scannedSongs = mutableListOf<SongEntity>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 5000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val dateCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val sizeCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val title = it.getString(titleCol) ?: "Áudio Sem Título"
                    val artist = it.getString(artistCol) ?: "Artista Desconhecido"
                    val album = it.getString(albumCol) ?: "Álbum Desconhecido"
                    val duration = it.getLong(durCol)
                    val data = it.getString(dataCol) ?: ""
                    val dateAdded = it.getLong(dateCol)
                    val size = it.getLong(sizeCol)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    val folderName = if (data.isNotEmpty()) {
                        File(data).parentFile?.name ?: "Música"
                    } else "Música"

                    val existing = songDao.getSongById(id)
                    scannedSongs.add(
                        SongEntity(
                            id = id,
                            title = existing?.title ?: title,
                            artist = existing?.artist ?: (if (artist == "<unknown>") "Artista Desconhecido" else artist),
                            album = existing?.album ?: (if (album == "<unknown>") "Álbum Desconhecido" else album),
                            albumArtist = existing?.albumArtist ?: "",
                            genre = existing?.genre ?: "Geral",
                            year = existing?.year ?: "",
                            trackNumber = existing?.trackNumber ?: "",
                            discNumber = existing?.discNumber ?: "1",
                            composer = existing?.composer ?: "",
                            comments = existing?.comments ?: "",
                            lyrics = existing?.lyrics ?: "",
                            durationMs = duration,
                            dataUri = contentUri,
                            albumArtUri = null,
                            folderName = folderName,
                            sizeBytes = size,
                            isFavorite = existing?.isFavorite ?: false,
                            playCount = existing?.playCount ?: 0,
                            dateAdded = dateAdded,
                            lastPlayedAt = existing?.lastPlayedAt ?: 0L
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (scannedSongs.isEmpty()) {
            scannedSongs.addAll(getAuthorizedSampleTracks())
        }

        if (scannedSongs.isNotEmpty()) {
            songDao.insertSongs(scannedSongs)
        }

        // Initialize primary queue if empty
        val queueCount = queueDao.getQueueCount()
        if (queueCount == 0 && scannedSongs.isNotEmpty()) {
            val q1Songs = scannedSongs.take(10).map { it.id }
            queueDao.insertOrUpdateQueue(
                PlaybackQueueEntity(
                    id = 1L,
                    name = "Fila Principal 1",
                    songIdsJson = PlaybackQueueEntity.createJson(q1Songs),
                    currentSongIndex = 0,
                    currentPositionMs = 0L
                )
            )
        }
    }

    private fun getAuthorizedSampleTracks(): List<SongEntity> {
        val sampleLrc = """
            [00:00.00]Myato-Player 2.1 — High Fidelity Audio
            [00:04.00]Synthwave rhythms pulsing through the night
            [00:08.50]Cybernetic echoes glowing emerald bright
            [00:15.00]Feel the frequency in your mind
            [00:22.00]The sound of tomorrow, leaving yesterday behind
        """.trimIndent()

        return listOf(
            SongEntity(
                id = 1001L,
                title = "Neon Horizon (Synthwave)",
                artist = "Myato Collective",
                album = "Cybernetic Beats",
                genre = "Synthwave",
                year = "2026",
                trackNumber = "01",
                discNumber = "1",
                composer = "Myato Audio Team",
                lyrics = sampleLrc,
                durationMs = 195000L,
                dataUri = "https://actions.google.com/sounds/v1/science_fiction/alien_beacon.ogg",
                albumArtUri = null,
                folderName = "Myato Audio",
                sizeBytes = 3500000L,
                isFavorite = true,
                playCount = 4,
                dateAdded = System.currentTimeMillis() - 86400000L,
                lastPlayedAt = System.currentTimeMillis() - 3600000L
            ),
            SongEntity(
                id = 1002L,
                title = "Deep Space Pulse",
                artist = "Aurora Nova",
                album = "Galactic Echoes",
                genre = "Ambient",
                year = "2025",
                trackNumber = "02",
                discNumber = "1",
                durationMs = 210000L,
                dataUri = "https://actions.google.com/sounds/v1/science_fiction/teleport.ogg",
                albumArtUri = null,
                folderName = "Myato Audio",
                sizeBytes = 4100000L,
                isFavorite = false,
                playCount = 2,
                dateAdded = System.currentTimeMillis() - 172800000L,
                lastPlayedAt = System.currentTimeMillis() - 7200000L
            ),
            SongEntity(
                id = 1003L,
                title = "Midnight Drive",
                artist = "Retro Future",
                album = "Overdrive",
                genre = "Retrowave",
                year = "2026",
                trackNumber = "03",
                discNumber = "1",
                durationMs = 175000L,
                dataUri = "https://actions.google.com/sounds/v1/science_fiction/scifi_laser_sweep.ogg",
                albumArtUri = null,
                folderName = "Soundtrack",
                sizeBytes = 2900000L,
                isFavorite = true,
                playCount = 8,
                dateAdded = System.currentTimeMillis() - 259200000L,
                lastPlayedAt = System.currentTimeMillis() - 1800000L
            ),
            SongEntity(
                id = 1004L,
                title = "Emerald Frequency",
                artist = "Myato Soundlab",
                album = "Acoustic Waves",
                genre = "Electronic",
                year = "2025",
                trackNumber = "04",
                discNumber = "1",
                durationMs = 240000L,
                dataUri = "https://actions.google.com/sounds/v1/ambiences/humming_room_tone.ogg",
                albumArtUri = null,
                folderName = "Electronic",
                sizeBytes = 5200000L,
                isFavorite = false,
                playCount = 1,
                dateAdded = System.currentTimeMillis() - 345600000L,
                lastPlayedAt = 0L
            ),
            SongEntity(
                id = 1005L,
                title = "Rainy Street Beats",
                artist = "Lofi Traveler",
                album = "Midnight Cafe",
                genre = "Lofi Hip Hop",
                year = "2026",
                trackNumber = "05",
                discNumber = "1",
                durationMs = 160000L,
                dataUri = "https://actions.google.com/sounds/v1/weather/rain_heavy.ogg",
                albumArtUri = null,
                folderName = "Lofi",
                sizeBytes = 3100000L,
                isFavorite = true,
                playCount = 5,
                dateAdded = System.currentTimeMillis() - 432000000L,
                lastPlayedAt = System.currentTimeMillis() - 86400000L
            )
        )
    }

    suspend fun toggleFavorite(songId: Long) = withContext(Dispatchers.IO) {
        val song = songDao.getSongById(songId)
        if (song != null) {
            songDao.setFavorite(songId, !song.isFavorite)
        }
    }

    suspend fun setFavoriteForMultiple(songIds: Set<Long>, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        songIds.forEach { id ->
            songDao.setFavorite(id, isFavorite)
        }
    }

    suspend fun recordSongPlayed(songId: Long) = withContext(Dispatchers.IO) {
        songDao.recordPlay(songId)
        historyDao.insertHistory(PlayHistoryEntity(songId = songId))
    }

    // Playlists Management
    suspend fun createPlaylist(name: String, isSmart: Boolean = false, smartType: String? = null): Long = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylist(PlaylistEntity(name = name.trim(), isSmart = isSmart, smartFilterType = smartType))
    }

    suspend fun updatePlaylistName(id: Long, newName: String) = withContext(Dispatchers.IO) {
        val existing = playlistDao.getPlaylistById(id)
        if (existing != null) {
            playlistDao.updatePlaylist(existing.copy(name = newName.trim()))
        }
    }

    suspend fun deletePlaylist(id: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(id)
    }

    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) = withContext(Dispatchers.IO) {
        val currentCount = playlistDao.getSongCountForPlaylist(playlistId)
        val refs = songIds.mapIndexed { index, songId ->
            PlaylistSongCrossRef(playlistId = playlistId, songId = songId, orderIndex = currentCount + index)
        }
        playlistDao.addSongsToPlaylist(refs)
    }

    suspend fun removeSongsFromPlaylist(playlistId: Long, songIds: List<Long>) = withContext(Dispatchers.IO) {
        playlistDao.removeSongsFromPlaylist(playlistId, songIds)
    }

    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getSongsInPlaylistFlow(playlistId).map { list -> list.map { it.toSong() } }
    }

    suspend fun getPlaylistIdsContainingSong(songId: Long): List<Long> = withContext(Dispatchers.IO) {
        playlistDao.getPlaylistIdsContainingSong(songId)
    }

    suspend fun updateSongPlaylists(songId: Long, targetPlaylistIds: Set<Long>) = withContext(Dispatchers.IO) {
        val currentPlaylists = playlistDao.getPlaylistIdsContainingSong(songId).toSet()
        val toAdd = targetPlaylistIds - currentPlaylists
        val toRemove = currentPlaylists - targetPlaylistIds

        toAdd.forEach { plId ->
            val count = playlistDao.getSongCountForPlaylist(plId)
            playlistDao.addSongToPlaylist(PlaylistSongCrossRef(plId, songId, count))
        }
        toRemove.forEach { plId ->
            playlistDao.removeSongFromPlaylist(plId, songId)
        }
    }

    // Musicolet: Multiple Queues Persistence (Section 1)
    suspend fun saveQueue(queue: PlaybackQueue) = withContext(Dispatchers.IO) {
        val entity = PlaybackQueueEntity(
            id = queue.id,
            name = queue.name,
            songIdsJson = PlaybackQueueEntity.createJson(queue.songs.map { it.id }),
            currentSongIndex = queue.currentSongIndex,
            currentPositionMs = queue.currentPositionMs,
            isShuffle = queue.isShuffle,
            repeatMode = queue.repeatMode.name,
            updatedAt = System.currentTimeMillis()
        )
        queueDao.insertOrUpdateQueue(entity)
    }

    suspend fun deleteQueue(queueId: Long) = withContext(Dispatchers.IO) {
        queueDao.deleteQueue(queueId)
    }

    // Musicolet: Tag Editor (Section 2)
    suspend fun updateSongMetadata(song: Song) = withContext(Dispatchers.IO) {
        songDao.updateMetadata(
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
            lyrics = song.lyrics
        )
    }

    suspend fun batchUpdateTags(songIds: List<Long>, artist: String, album: String, genre: String) = withContext(Dispatchers.IO) {
        songDao.batchUpdateTags(songIds, artist.trim(), album.trim(), genre.trim())
    }

    // Musicolet: Bookmarks & Notes (Section 6)
    fun getBookmarksForSong(songId: Long): Flow<List<SongBookmark>> {
        return bookmarkDao.getBookmarksForSongFlow(songId).map { list -> list.map { it.toBookmark() } }
    }

    suspend fun addBookmark(songId: Long, positionMs: Long, name: String, note: String): Long = withContext(Dispatchers.IO) {
        bookmarkDao.insertBookmark(
            SongBookmarkEntity(
                songId = songId,
                positionMs = positionMs,
                name = name.trim().ifEmpty { "Marcador em ${Song(0, "", durationMs = positionMs).formattedDuration}" },
                note = note.trim()
            )
        )
    }

    suspend fun deleteBookmark(bookmarkId: Long) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmark(bookmarkId)
    }

    // Musicolet: Synchronized Lyrics (.LRC Parser & Exporter) (Section 5)
    fun parseLrc(rawLrc: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        val pattern = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")

        rawLrc.lines().forEach { line ->
            val matcher = pattern.matcher(line.trim())
            if (matcher.matches()) {
                val min = matcher.group(1)?.toLongOrNull() ?: 0L
                val sec = matcher.group(2)?.toLongOrNull() ?: 0L
                val msStr = matcher.group(3) ?: "0"
                val ms = if (msStr.length == 2) msStr.toLong() * 10 else msStr.toLong()
                val totalMs = (min * 60 * 1000) + (sec * 1000) + ms
                val text = matcher.group(4)?.trim() ?: ""
                lines.add(LyricLine(totalMs, text))
            } else if (line.isNotBlank() && !line.startsWith("[")) {
                // Unsynchronized fallback line
                lines.add(LyricLine(-1L, line.trim()))
            }
        }
        return lines.sortedBy { it.timestampMs }
    }

    fun exportLrc(lines: List<LyricLine>): String {
        val sb = StringBuilder()
        lines.forEach { line ->
            if (line.timestampMs >= 0) {
                val totalSec = line.timestampMs / 1000
                val min = totalSec / 60
                val sec = totalSec % 60
                val ms = (line.timestampMs % 1000) / 10
                sb.append(String.format("[%02d:%02d.%02d] %s\n", min, sec, ms, line.text))
            } else {
                sb.append(line.text).append("\n")
            }
        }
        return sb.toString()
    }

    suspend fun saveLyrics(songId: Long, rawLyrics: String) = withContext(Dispatchers.IO) {
        songDao.updateLyrics(songId, rawLyrics)
    }

    // Musicolet: Musical Statistics (Section 10)
    suspend fun getStats(period: StatisticsPeriod): MusicStats = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val since = when (period) {
            StatisticsPeriod.TODAY -> now - (24 * 3600 * 1000L)
            StatisticsPeriod.LAST_7_DAYS -> now - (7 * 24 * 3600 * 1000L)
            StatisticsPeriod.LAST_30_DAYS -> now - (30 * 24 * 3600 * 1000L)
            StatisticsPeriod.THIS_MONTH -> now - (30 * 24 * 3600 * 1000L)
            StatisticsPeriod.THIS_YEAR -> now - (365 * 24 * 3600 * 1000L)
            StatisticsPeriod.ALL_TIME -> 0L
        }

        val historySongs = if (since > 0L) historyDao.getHistorySince(since) else songDao.getMostPlayedFlow().firstOrNull() ?: emptyList()
        val songsMap = historySongs.groupBy { it.id }

        val topSongs = historySongs.distinctBy { it.id }.sortedByDescending { it.playCount }.take(20).map { it.toSong() }
        val topArtists = historySongs.groupBy { it.artist }.map { (artist, list) -> artist to list.size }.sortedByDescending { it.second }.take(10)
        val topAlbums = historySongs.groupBy { it.album }.map { (album, list) -> album to list.size }.sortedByDescending { it.second }.take(10)
        val totalPlayTime = historySongs.sumOf { it.durationMs }

        MusicStats(
            topSongs = topSongs,
            topArtists = topArtists,
            topAlbums = topAlbums,
            totalPlayTimeMs = totalPlayTime,
            totalSongsPlayed = historySongs.size
        )
    }

    fun exportStatsCsv(stats: MusicStats): String {
        val sb = StringBuilder()
        sb.append("Ranking,Música,Artista,Álbum,Reproduções\n")
        stats.topSongs.forEachIndexed { idx, s ->
            sb.append("${idx + 1},\"${s.title}\",\"${s.artist}\",\"${s.album}\",${s.playCount}\n")
        }
        sb.append("\nTop Artistas,Reproduções\n")
        stats.topArtists.forEach { (a, count) ->
            sb.append("\"$a\",$count\n")
        }
        sb.append("\nTop Álbuns,Reproduções\n")
        stats.topAlbums.forEach { (al, count) ->
            sb.append("\"$al\",$count\n")
        }
        return sb.toString()
    }

    suspend fun clearHistoryForSong(songId: Long) = withContext(Dispatchers.IO) {
        historyDao.clearHistoryForSong(songId)
    }

    suspend fun clearHistoryForSongs(songIds: List<Long>) = withContext(Dispatchers.IO) {
        historyDao.clearHistoryForSongs(songIds)
    }

    suspend fun deleteSong(song: Song): Boolean = withContext(Dispatchers.IO) {
        try {
            if (song.dataUri.startsWith("content://")) {
                val uri = Uri.parse(song.dataUri)
                context.contentResolver.delete(uri, null, null)
            } else if (song.dataUri.startsWith("file://") || song.dataUri.startsWith("/")) {
                val file = File(song.dataUri.removePrefix("file://"))
                if (file.exists()) file.delete()
            }
            songDao.deleteSong(song.id)
            bookmarkDao.deleteBookmarksForSong(song.id)
            true
        } catch (e: Exception) {
            songDao.deleteSong(song.id)
            bookmarkDao.deleteBookmarksForSong(song.id)
            false
        }
    }

    suspend fun deleteSongs(songIds: Set<Long>) = withContext(Dispatchers.IO) {
        songDao.deleteSongs(songIds.toList())
    }

    // Comprehensive Backup & Restore (Section 20)
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "Myato-Player")
        root.put("version", "2.1")
        root.put("exportedAt", System.currentTimeMillis())

        // Playlists
        val playlistsArray = JSONArray()
        val allPl = playlistDao.getAllPlaylistsFlow().firstOrNull() ?: emptyList()
        for (pl in allPl) {
            val plObj = JSONObject()
            plObj.put("id", pl.id)
            plObj.put("name", pl.name)
            plObj.put("isSmart", pl.isSmart)
            val songsInPl = playlistDao.getSongsInPlaylistFlow(pl.id).firstOrNull() ?: emptyList()
            val songIdsArray = JSONArray()
            songsInPl.forEach { songIdsArray.put(it.id) }
            plObj.put("songIds", songIdsArray)
            playlistsArray.put(plObj)
        }
        root.put("playlists", playlistsArray)

        // Favorites
        val favs = songDao.getFavoritesFlow().firstOrNull() ?: emptyList()
        val favsArray = JSONArray()
        favs.forEach { favsArray.put(it.id) }
        root.put("favorites", favsArray)

        // Queues
        val queues = queueDao.getAllQueuesFlow().firstOrNull() ?: emptyList()
        val queuesArray = JSONArray()
        for (q in queues) {
            val qObj = JSONObject()
            qObj.put("id", q.id)
            qObj.put("name", q.name)
            qObj.put("songIds", q.songIdsJson)
            queuesArray.put(qObj)
        }
        root.put("queues", queuesArray)

        // Bookmarks
        val bookmarks = bookmarkDao.getAllBookmarksFlow().firstOrNull() ?: emptyList()
        val bookmarksArray = JSONArray()
        for (b in bookmarks) {
            val bObj = JSONObject()
            bObj.put("songId", b.songId)
            bObj.put("positionMs", b.positionMs)
            bObj.put("name", b.name)
            bObj.put("note", b.note)
            bookmarksArray.put(bObj)
        }
        root.put("bookmarks", bookmarksArray)

        root.toString(2)
    }

    suspend fun restoreBackupJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("app") || root.getString("app") != "Myato-Player") return@withContext false

            if (root.has("favorites")) {
                val favsArray = root.getJSONArray("favorites")
                for (i in 0 until favsArray.length()) {
                    val songId = favsArray.getLong(i)
                    songDao.setFavorite(songId, true)
                }
            }

            if (root.has("playlists")) {
                val plArray = root.getJSONArray("playlists")
                for (i in 0 until plArray.length()) {
                    val plObj = plArray.getJSONObject(i)
                    val name = plObj.getString("name")
                    val isSmart = plObj.optBoolean("isSmart", false)
                    val plId = playlistDao.insertPlaylist(PlaylistEntity(name = name, isSmart = isSmart))
                    val songIdsArray = plObj.getJSONArray("songIds")
                    val refs = mutableListOf<PlaylistSongCrossRef>()
                    for (j in 0 until songIdsArray.length()) {
                        refs.add(PlaylistSongCrossRef(plId, songIdsArray.getLong(j), j))
                    }
                    playlistDao.addSongsToPlaylist(refs)
                }
            }

            if (root.has("bookmarks")) {
                val bArray = root.getJSONArray("bookmarks")
                for (i in 0 until bArray.length()) {
                    val bObj = bArray.getJSONObject(i)
                    bookmarkDao.insertBookmark(
                        SongBookmarkEntity(
                            songId = bObj.getLong("songId"),
                            positionMs = bObj.getLong("positionMs"),
                            name = bObj.getString("name"),
                            note = bObj.optString("note", "")
                        )
                    )
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
