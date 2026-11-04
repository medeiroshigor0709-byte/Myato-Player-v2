package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.MyatoDatabase
import com.example.downloads.DownloadRepository
import com.example.downloads.ParsedMediaInfo
import com.example.model.*
import com.example.player.PlayerController
import com.example.repository.MusicRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MyatoDatabase.getInstance(application)
    val musicRepository = MusicRepository(application, database)
    val downloadRepository = DownloadRepository(application, database.downloadDao(), database.songDao())
    val playerController = PlayerController.getInstance(application)

    // Navigation
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.HOME))
    val currentScreen: StateFlow<Screen> = _screenStack.map { it.lastOrNull() ?: Screen.HOME }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Screen.HOME)

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    // Library tab & Sort
    private val _libraryTab = MutableStateFlow(0)
    val libraryTab: StateFlow<Int> = _libraryTab.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.NAME_ASC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    // Multi-Selection State (Section 5)
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedSongIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedSongIds: StateFlow<Set<Long>> = _selectedSongIds.asStateFlow()

    private var firstSelectedId: Long? = null
    private var lastSelectedId: Long? = null

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(
        listOf("Rock", "Synthwave", "Lofi", "Overdrive")
    )
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    // Download inspector
    private val _inspectedMedia = MutableStateFlow<ParsedMediaInfo?>(null)
    val inspectedMedia: StateFlow<ParsedMediaInfo?> = _inspectedMedia.asStateFlow()

    private val _isInspecting = MutableStateFlow(false)
    val isInspecting: StateFlow<Boolean> = _isInspecting.asStateFlow()

    // UI Feedback
    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    // Musicolet 2.1: Multiple Queues
    val queuesList: StateFlow<List<PlaybackQueue>> = playerController.queuesList
    val activeQueueId: StateFlow<Long> = playerController.activeQueueId
    val activeQueueName: StateFlow<String> = playerController.activeQueueName

    // Musicolet 2.1: Tag Editor
    private val _selectedSongForTagEdit = MutableStateFlow<Song?>(null)
    val selectedSongForTagEdit: StateFlow<Song?> = _selectedSongForTagEdit.asStateFlow()

    // Musicolet 2.1: Folder Navigation (Linear vs Hierarchical)
    private val _folderViewMode = MutableStateFlow(FolderViewMode.LINEAR)
    val folderViewMode: StateFlow<FolderViewMode> = _folderViewMode.asStateFlow()

    private val _currentFolderPath = MutableStateFlow("/")
    val currentFolderPath: StateFlow<String> = _currentFolderPath.asStateFlow()

    // Musicolet 2.1: Musical Statistics
    private val _statsPeriod = MutableStateFlow(StatisticsPeriod.ALL_TIME)
    val statsPeriod: StateFlow<StatisticsPeriod> = _statsPeriod.asStateFlow()

    private val _currentStats = MutableStateFlow(MusicStats())
    val currentStats: StateFlow<MusicStats> = _currentStats.asStateFlow()

    // Musicolet 2.1: Lyrics
    val currentLyrics: StateFlow<List<LyricLine>> = playerController.currentSong
        .map { song ->
            if (song != null && song.lyrics.isNotBlank()) {
                musicRepository.parseLrc(song.lyrics)
            } else {
                emptyList()
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Musicolet 2.1: Bookmarks for Current Song
    val currentSongBookmarks: StateFlow<List<SongBookmark>> = playerController.currentSong
        .flatMapLatest { song ->
            if (song != null) musicRepository.getBookmarksForSong(song.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Songs & Collections
    val allSongs: StateFlow<List<Song>> = combine(
        musicRepository.allSongs,
        _sortOrder
    ) { songs, order ->
        sortSongs(songs, order)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = musicRepository.favoriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed: StateFlow<List<Song>> = musicRepository.recentlyPlayed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostPlayed: StateFlow<List<Song>> = musicRepository.mostPlayed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val neverPlayed: StateFlow<List<Song>> = musicRepository.neverPlayed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = musicRepository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<Album>> = allSongs.map { songs ->
        songs.groupBy { it.album }.map { (albumName, songList) ->
            Album(
                name = albumName,
                artist = songList.firstOrNull()?.artist ?: "Vários",
                songCount = songList.size,
                albumArtUri = songList.firstOrNull()?.albumArtUri
            )
        }.sortedBy { it.name }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artists: StateFlow<List<Artist>> = allSongs.map { songs ->
        songs.groupBy { it.artist }.map { (artistName, songList) ->
            val albumCount = songList.map { it.album }.distinct().size
            Artist(name = artistName, songCount = songList.size, albumCount = albumCount)
        }.sortedBy { it.name }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<Folder>> = allSongs.map { songs ->
        songs.groupBy { it.folderName }.map { (folderName, songList) ->
            Folder(name = folderName, path = folderName, songCount = songList.size)
        }.sortedBy { it.name }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val songsInSelectedPlaylist: StateFlow<List<Song>> = _selectedPlaylist.flatMapLatest { pl ->
        if (pl != null) musicRepository.getSongsInPlaylist(pl.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloads = downloadRepository.allDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        playerController.onSongPlayed = { songId ->
            viewModelScope.launch {
                musicRepository.recordSongPlayed(songId)
            }
        }
        playerController.onQueueChangedCallback = { q ->
            viewModelScope.launch {
                musicRepository.saveQueue(q)
            }
        }
        viewModelScope.launch {
            musicRepository.syncMediaStore()
            loadStats(StatisticsPeriod.ALL_TIME)
        }
    }

    private fun sortSongs(songs: List<Song>, order: SortOrder): List<Song> {
        return when (order) {
            SortOrder.NAME_ASC -> songs.sortedBy { it.title.lowercase() }
            SortOrder.NAME_DESC -> songs.sortedByDescending { it.title.lowercase() }
            SortOrder.ARTIST_ASC -> songs.sortedBy { it.artist.lowercase() }
            SortOrder.ALBUM_ASC -> songs.sortedBy { it.album.lowercase() }
            SortOrder.DURATION_DESC -> songs.sortedByDescending { it.durationMs }
            SortOrder.DATE_ADDED_DESC -> songs.sortedByDescending { it.dateAdded }
        }
    }

    // Navigation methods
    fun navigateTo(screen: Screen) {
        if (_screenStack.value.lastOrNull() != screen) {
            _screenStack.value = _screenStack.value + screen
        }
    }

    fun openPlaylist(playlist: Playlist) {
        _selectedPlaylist.value = playlist
        navigateTo(Screen.PLAYLIST_DETAIL)
    }

    fun navigateBack(): Boolean {
        if (_isSelectionMode.value) {
            clearSelection()
            return true
        }
        val stack = _screenStack.value
        return if (stack.size > 1) {
            _screenStack.value = stack.dropLast(1)
            true
        } else {
            false
        }
    }

    fun setLibraryTab(tabIndex: Int) {
        _libraryTab.value = tabIndex
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank() && !_searchHistory.value.contains(query.trim())) {
            _searchHistory.value = listOf(query.trim()) + _searchHistory.value.take(7)
        }
    }

    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
    }

    // Multi-Selection System (Section 5)
    fun startSelection(initialSongId: Long) {
        _isSelectionMode.value = true
        _selectedSongIds.value = setOf(initialSongId)
        firstSelectedId = initialSongId
        lastSelectedId = initialSongId
    }

    fun toggleSongSelection(songId: Long) {
        val current = _selectedSongIds.value.toMutableSet()
        if (current.contains(songId)) {
            current.remove(songId)
            if (current.isEmpty()) {
                clearSelection()
                return
            }
        } else {
            current.add(songId)
            if (firstSelectedId == null) firstSelectedId = songId
            lastSelectedId = songId
        }
        _selectedSongIds.value = current
    }

    fun selectAll(songs: List<Song>) {
        _isSelectionMode.value = true
        _selectedSongIds.value = songs.map { it.id }.toSet()
        firstSelectedId = songs.firstOrNull()?.id
        lastSelectedId = songs.lastOrNull()?.id
    }

    fun deselectAll() {
        clearSelection()
    }

    fun selectRange(songs: List<Song>) {
        val first = firstSelectedId
        val last = lastSelectedId
        if (first == null || last == null || songs.isEmpty()) return

        val idx1 = songs.indexOfFirst { it.id == first }
        val idx2 = songs.indexOfFirst { it.id == last }
        if (idx1 != -1 && idx2 != -1) {
            val start = minOf(idx1, idx2)
            val end = maxOf(idx1, idx2)
            val rangeIds = songs.subList(start, end + 1).map { it.id }.toSet()
            _selectedSongIds.value = _selectedSongIds.value + rangeIds
        }
    }

    fun invertSelection(songs: List<Song>) {
        val current = _selectedSongIds.value
        val inverted = songs.map { it.id }.filterNot { current.contains(it) }.toSet()
        if (inverted.isEmpty()) {
            clearSelection()
        } else {
            _selectedSongIds.value = inverted
        }
    }

    fun clearSelection() {
        _isSelectionMode.value = false
        _selectedSongIds.value = emptySet()
        firstSelectedId = null
        lastSelectedId = null
    }

    // Batch Actions on Multi-Selection
    fun playSelectedSongs(songs: List<Song>) {
        val selected = songs.filter { _selectedSongIds.value.contains(it.id) }
        if (selected.isNotEmpty()) {
            playerController.playSongList(selected, 0)
            emitMessage("Reproduzindo ${selected.size} músicas selecionadas")
            clearSelection()
        }
    }

    fun addSelectedToQueue(songs: List<Song>) {
        val selected = songs.filter { _selectedSongIds.value.contains(it.id) }
        if (selected.isNotEmpty()) {
            playerController.addToQueueEnd(selected)
            emitMessage("${selected.size} músicas adicionadas à fila")
            clearSelection()
        }
    }

    fun addSelectedToPlaylist(playlistId: Long) {
        val ids = _selectedSongIds.value.toList()
        viewModelScope.launch {
            musicRepository.addSongsToPlaylist(playlistId, ids)
            emitMessage("${ids.size} músicas adicionadas à playlist")
            clearSelection()
        }
    }

    fun createPlaylistWithSelection(name: String) {
        val ids = _selectedSongIds.value.toList()
        viewModelScope.launch {
            val newId = musicRepository.createPlaylist(name)
            if (ids.isNotEmpty()) {
                musicRepository.addSongsToPlaylist(newId, ids)
            }
            emitMessage("Playlist '$name' criada com ${ids.size} músicas")
            clearSelection()
        }
    }

    fun favoriteSelectedSongs(isFavorite: Boolean) {
        val ids = _selectedSongIds.value
        viewModelScope.launch {
            musicRepository.setFavoriteForMultiple(ids, isFavorite)
            val msg = if (isFavorite) "adicionadas às favoritas" else "removidas das favoritas"
            emitMessage("${ids.size} músicas $msg")
            clearSelection()
        }
    }

    fun deleteSelectedSongs() {
        val ids = _selectedSongIds.value
        viewModelScope.launch {
            musicRepository.deleteSongs(ids)
            emitMessage("${ids.size} músicas excluídas da biblioteca")
            clearSelection()
        }
    }

    // Single item actions
    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(songId)
        }
    }

    fun createPlaylist(name: String, isSmart: Boolean = false, smartType: String? = null) {
        viewModelScope.launch {
            musicRepository.createPlaylist(name, isSmart, smartType)
            emitMessage("Playlist criada com sucesso")
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            musicRepository.deletePlaylist(id)
            if (_selectedPlaylist.value?.id == id) {
                navigateBack()
            }
            emitMessage("Playlist excluída")
        }
    }

    fun addSingleSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            musicRepository.addSongsToPlaylist(playlistId, listOf(songId))
            emitMessage("Música adicionada à playlist")
        }
    }

    fun removeSongsFromPlaylist(playlistId: Long, songIds: List<Long>) {
        viewModelScope.launch {
            musicRepository.removeSongsFromPlaylist(playlistId, songIds)
            emitMessage("Música removida da playlist")
        }
    }

    fun updatePlaylistName(playlistId: Long, newName: String) {
        viewModelScope.launch {
            musicRepository.updatePlaylistName(playlistId, newName)
            emitMessage("Nome da playlist atualizado")
        }
    }

    fun deleteSingleSong(song: Song) {
        viewModelScope.launch {
            musicRepository.deleteSong(song)
            emitMessage("Música excluída")
        }
    }

    fun rescanLibrary() {
        viewModelScope.launch {
            musicRepository.syncMediaStore()
            emitMessage("Biblioteca atualizada com sucesso")
        }
    }

    // Musicolet 2.1: Multiple Queues Management (Section 1)
    fun switchQueue(queueId: Long) {
        playerController.switchQueue(queueId, allSongs.value)
        emitMessage("Alternado para ${playerController.activeQueueName.value}")
    }

    fun createNewQueue(name: String, songs: List<Song> = emptyList()) {
        val newId = playerController.createNewQueue(name, songs)
        if (newId != null) {
            emitMessage("Fila '$name' criada")
        } else {
            emitMessage("Limite de 20 filas atingido")
        }
    }

    fun duplicateQueue(queueId: Long, newName: String) {
        val newId = playerController.duplicateQueue(queueId, newName)
        if (newId != null) {
            emitMessage("Fila duplicada com sucesso")
        } else {
            emitMessage("Limite de 20 filas atingido")
        }
    }

    fun renameQueue(queueId: Long, newName: String) {
        playerController.renameQueue(queueId, newName)
        emitMessage("Fila renomeada")
    }

    fun deleteQueue(queueId: Long) {
        playerController.deleteQueue(queueId)
        viewModelScope.launch {
            musicRepository.deleteQueue(queueId)
        }
        emitMessage("Fila removida")
    }

    fun restorePreviousQueue() {
        playerController.restorePreviousQueue()
        emitMessage("Fila anterior restaurada")
    }

    fun saveQueueAsPlaylist(queueId: Long, playlistName: String) {
        val q = playerController.queuesList.value.firstOrNull { it.id == queueId } ?: return
        viewModelScope.launch {
            val plId = musicRepository.createPlaylist(playlistName)
            musicRepository.addSongsToPlaylist(plId, q.songs.map { it.id })
            emitMessage("Fila salva como playlist '$playlistName'")
        }
    }

    fun createQueueFromAlbum(album: Album) {
        val albumSongs = allSongs.value.filter { it.album == album.name }
        if (albumSongs.isNotEmpty()) {
            val name = "Álbum: ${album.name}"
            val newId = playerController.createNewQueue(name, albumSongs)
            if (newId != null) {
                switchQueue(newId)
            }
        }
    }

    fun createQueueFromArtist(artist: Artist) {
        val artistSongs = allSongs.value.filter { it.artist == artist.name }
        if (artistSongs.isNotEmpty()) {
            val name = "Artista: ${artist.name}"
            val newId = playerController.createNewQueue(name, artistSongs)
            if (newId != null) {
                switchQueue(newId)
            }
        }
    }

    fun createQueueFromFolder(folder: Folder) {
        val folderSongs = allSongs.value.filter { it.folderName == folder.name }
        if (folderSongs.isNotEmpty()) {
            val name = "Pasta: ${folder.name}"
            val newId = playerController.createNewQueue(name, folderSongs)
            if (newId != null) {
                switchQueue(newId)
            }
        }
    }

    // Musicolet 2.1: Tag Editor (Section 2)
    fun openTagEditor(song: Song) {
        _selectedSongForTagEdit.value = song
        navigateTo(Screen.TAG_EDITOR)
    }

    fun saveSongTags(updatedSong: Song) {
        viewModelScope.launch {
            musicRepository.updateSongMetadata(updatedSong)
            emitMessage("Metadados atualizados com sucesso")
            navigateBack()
        }
    }

    fun batchEditTags(artist: String, album: String, genre: String) {
        val ids = _selectedSongIds.value.toList()
        viewModelScope.launch {
            musicRepository.batchUpdateTags(ids, artist, album, genre)
            emitMessage("Metadados atualizados para ${ids.size} músicas")
            clearSelection()
        }
    }

    // Musicolet 2.1: Bookmarks & Notes (Section 6)
    fun addBookmark(name: String, note: String, positionMs: Long) {
        val song = playerController.currentSong.value ?: return
        viewModelScope.launch {
            musicRepository.addBookmark(song.id, positionMs, name, note)
            emitMessage("Marcador salvo em ${Song(0, "", durationMs = positionMs).formattedDuration}")
        }
    }

    fun deleteBookmark(bookmarkId: Long) {
        viewModelScope.launch {
            musicRepository.deleteBookmark(bookmarkId)
            emitMessage("Marcador excluído")
        }
    }

    fun jumpToBookmark(positionMs: Long) {
        playerController.seekTo(positionMs)
    }

    // Musicolet 2.1: Synchronized Lyrics (Section 5)
    fun saveLyrics(songId: Long, rawLyrics: String) {
        viewModelScope.launch {
            musicRepository.saveLyrics(songId, rawLyrics)
            emitMessage("Letras salvas com sucesso")
        }
    }

    // Musicolet 2.1: Musical Statistics (Section 10)
    fun loadStats(period: StatisticsPeriod) {
        _statsPeriod.value = period
        viewModelScope.launch {
            val st = musicRepository.getStats(period)
            _currentStats.value = st
        }
    }

    fun exportStatsCsv(): String {
        return musicRepository.exportStatsCsv(_currentStats.value)
    }

    fun clearStatsForSong(songId: Long) {
        viewModelScope.launch {
            musicRepository.clearHistoryForSong(songId)
            loadStats(_statsPeriod.value)
            emitMessage("Histórico da música limpo")
        }
    }

    // Musicolet 2.1: Folder Explorer (Section 4)
    fun setFolderViewMode(mode: FolderViewMode) {
        _folderViewMode.value = mode
    }

    fun navigateToFolder(path: String) {
        _currentFolderPath.value = path
    }

    // Download / Link inspection
    fun inspectLink(url: String) {
        if (url.isBlank()) {
            emitMessage("Insira um link válido")
            return
        }
        viewModelScope.launch {
            _isInspecting.value = true
            try {
                val info = downloadRepository.inspectUrl(url)
                _inspectedMedia.value = info
            } catch (e: Exception) {
                emitMessage("Erro ao inspecionar link: ${e.localizedMessage}")
            } finally {
                _isInspecting.value = false
            }
        }
    }

    fun clearInspectedMedia() {
        _inspectedMedia.value = null
    }

    fun startDownload(mediaInfo: ParsedMediaInfo) {
        viewModelScope.launch {
            downloadRepository.startDownload(mediaInfo)
            emitMessage("Download de '${mediaInfo.title}' iniciado!")
            _inspectedMedia.value = null
        }
    }

    fun retryDownload(downloadId: Long) {
        viewModelScope.launch {
            downloadRepository.retryDownload(downloadId)
            emitMessage("Tentando download novamente...")
        }
    }

    fun cancelDownload(downloadId: Long) {
        downloadRepository.cancelDownload(downloadId)
        emitMessage("Download cancelado")
    }

    // Backup & Restore (Section 20)
    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = musicRepository.exportBackupJson()
            onResult(json)
            emitMessage("Backup gerado com sucesso!")
        }
    }

    fun restoreBackup(json: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = musicRepository.restoreBackupJson(json)
            onResult(success)
            if (success) {
                emitMessage("Backup restaurado com sucesso!")
            } else {
                emitMessage("Falha ao restaurar: Formato inválido")
            }
        }
    }

    private fun emitMessage(msg: String) {
        viewModelScope.launch {
            _uiMessage.emit(msg)
        }
    }
}
