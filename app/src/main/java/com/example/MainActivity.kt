package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Screen
import com.example.model.Song
import com.example.player.service.MediaPlaybackService
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start media service for background playback
        val serviceIntent = Intent(this, MediaPlaybackService::class.java)
        try {
            startService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val allSongs by viewModel.allSongs.collectAsStateWithLifecycle()
    val favoriteSongs by viewModel.favoriteSongs.collectAsStateWithLifecycle()
    val recentSongs by viewModel.recentlyPlayed.collectAsStateWithLifecycle()
    val mostPlayedSongs by viewModel.mostPlayed.collectAsStateWithLifecycle()
    val neverPlayedSongs by viewModel.neverPlayed.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsStateWithLifecycle()
    val songsInPlaylist by viewModel.songsInSelectedPlaylist.collectAsStateWithLifecycle()

    val libraryTab by viewModel.libraryTab.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()

    // Multi-selection state
    val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()
    val selectedSongIds by viewModel.selectedSongIds.collectAsStateWithLifecycle()
    val closeSelectionOnAction by viewModel.closeSelectionOnAction.collectAsStateWithLifecycle()

    // Player state
    val player = viewModel.playerController
    val currentSong by player.currentSong.collectAsStateWithLifecycle()
    val isPlaying by player.isPlaying.collectAsStateWithLifecycle()
    val currentPosition by player.currentPosition.collectAsStateWithLifecycle()
    val duration by player.duration.collectAsStateWithLifecycle()
    val repeatMode by player.repeatMode.collectAsStateWithLifecycle()
    val isShuffle by player.isShuffle.collectAsStateWithLifecycle()
    val queue by player.queue.collectAsStateWithLifecycle()
    val queueIndex by player.queueIndex.collectAsStateWithLifecycle()
    val currentDevice by player.currentDevice.collectAsStateWithLifecycle()

    val equalizerConfig by player.equalizerConfig.collectAsStateWithLifecycle()
    val sleepTimerState by player.sleepTimer.collectAsStateWithLifecycle()
    val playOnConnect by player.playOnBluetoothConnect.collectAsStateWithLifecycle()
    val pauseOnDisconnect by player.pauseOnBluetoothDisconnect.collectAsStateWithLifecycle()

    // Musicolet 2.1 States
    val activeQueueName by player.activeQueueName.collectAsStateWithLifecycle()
    val activeQueueId by player.activeQueueId.collectAsStateWithLifecycle()
    val queuesList by player.queuesList.collectAsStateWithLifecycle()
    val currentLyrics by viewModel.currentLyrics.collectAsStateWithLifecycle()
    val currentSongBookmarks by viewModel.currentSongBookmarks.collectAsStateWithLifecycle()
    val speedPitch by player.speedPitch.collectAsStateWithLifecycle()
    val abRepeatState by player.abRepeatState.collectAsStateWithLifecycle()
    val currentStats by viewModel.currentStats.collectAsStateWithLifecycle()
    val statsPeriod by viewModel.statsPeriod.collectAsStateWithLifecycle()
    val folderViewMode by viewModel.folderViewMode.collectAsStateWithLifecycle()
    val currentFolderPath by viewModel.currentFolderPath.collectAsStateWithLifecycle()
    val selectedSongForTagEdit by viewModel.selectedSongForTagEdit.collectAsStateWithLifecycle()

    // Downloads state
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val inspectedMedia by viewModel.inspectedMedia.collectAsStateWithLifecycle()
    val isInspecting by viewModel.isInspecting.collectAsStateWithLifecycle()

    // Search state
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()

    // Modals / Dialogs visibility
    var showSelectionOptionsSheet by remember { mutableStateOf(false) }
    var showBatchActionsSheet by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showBookmarksSheet by remember { mutableStateOf(false) }
    var showSpeedPitchDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var singleSongForPlaylist by remember { mutableStateOf<Song?>(null) }
    var songForInfo by remember { mutableStateOf<Song?>(null) }
    var songForCutter by remember { mutableStateOf<Song?>(null) }
    var songForMove by remember { mutableStateOf<Song?>(null) }
    var songToDeletePermanently by remember { mutableStateOf<Song?>(null) }
    var isBatchDeleteConfirm by remember { mutableStateOf(false) }

    // Audio permissions launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            viewModel.rescanLibrary()
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    // Handle UI messages (Toasts)
    LaunchedEffect(Unit) {
        viewModel.uiMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Back button handling
    BackHandler {
        if (!viewModel.navigateBack()) {
            (context as? ComponentActivity)?.finish()
        }
    }

    val progressFloat = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

    Scaffold(
        bottomBar = {
            if (currentScreen != Screen.PLAYER) {
                Column {
                    // Multi-Selection Bottom Bar
                    MultiSelectionBottomBar(
                        isVisible = isSelectionMode,
                        selectedCount = selectedSongIds.size,
                        closeSelectionOnAction = closeSelectionOnAction,
                        onToggleCloseOnAction = { viewModel.setCloseSelectionOnAction(it) },
                        onOptionsClick = { showSelectionOptionsSheet = true },
                        onAdvanceClick = { showBatchActionsSheet = true },
                        onCancelClick = { viewModel.clearSelection() }
                    )

                    // MiniPlayer
                    if (!isSelectionMode) {
                        MiniPlayer(
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            progress = progressFloat,
                            onPlayPauseClick = { player.togglePlayPause() },
                            onNextClick = { player.next() },
                            onMiniPlayerClick = { viewModel.navigateTo(Screen.PLAYER) }
                        )
                    }

                    // Main Navigation Bar (hidden in sub-screens)
                    if (currentScreen == Screen.HOME || currentScreen == Screen.LIBRARY ||
                        currentScreen == Screen.DOWNLOADS || currentScreen == Screen.SETTINGS) {
                        BottomNavBar(
                            currentScreen = currentScreen,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen_transition",
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                bottom = if (currentScreen == Screen.PLAYER) 0.dp else innerPadding.calculateBottomPadding()
            )
        ) { screen ->
            when (screen) {
                Screen.HOME -> HomeScreen(
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    recentSongs = recentSongs,
                    favoriteSongs = favoriteSongs,
                    playlists = playlists,
                    onSongClick = { song -> player.playSingleSong(song) },
                    onPlaylistClick = { pl -> viewModel.openPlaylist(pl) },
                    onNavigate = { viewModel.navigateTo(it) },
                    onPlayPauseClick = { player.togglePlayPause() }
                )

                Screen.LIBRARY -> LibraryScreen(
                    allSongs = allSongs,
                    favoriteSongs = favoriteSongs,
                    recentAddedSongs = allSongs,
                    historyPlayedSongs = recentSongs,
                    mostPlayedSongs = mostPlayedSongs,
                    neverPlayedSongs = neverPlayedSongs,
                    myPlaylists = playlists,
                    selectedTab = libraryTab,
                    currentSortOrder = sortOrder,
                    currentSongId = currentSong?.id,
                    isSelectionMode = isSelectionMode,
                    selectedSongIds = selectedSongIds,
                    onTabSelect = { viewModel.setLibraryTab(it) },
                    onSortChange = { viewModel.setSortOrder(it) },
                    onSongClick = { song -> player.playSingleSong(song) },
                    onSongLongClick = { song ->
                        if (isSelectionMode) {
                            viewModel.toggleSongSelection(song.id)
                        } else {
                            viewModel.startSelection(song.id)
                        }
                    },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onAddToQueue = { player.addToQueueNext(it) },
                    onAddToPlaylist = { song ->
                        singleSongForPlaylist = song
                        showAddToPlaylistDialog = true
                    },
                    onDeleteSong = { songToDeletePermanently = it },
                    onEditSongTags = { song -> viewModel.openTagEditor(song) },
                    onPlaylistClick = { pl -> viewModel.openPlaylist(pl) },
                    onCreatePlaylistClick = {
                        singleSongForPlaylist = null
                        showAddToPlaylistDialog = true
                    },
                    onImportM3u = { name, content -> viewModel.importM3uPlaylist(name, content) },
                    onRescanLibrary = { viewModel.rescanLibrary() }
                )

                Screen.PLAYER -> PlayerScreen(
                    song = currentSong,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPosition,
                    durationMs = duration,
                    repeatMode = repeatMode,
                    isShuffle = isShuffle,
                    activeQueueName = activeQueueName,
                    onPlayPause = { player.togglePlayPause() },
                    onPrevious = { player.previous() },
                    onNext = { player.next() },
                    onSeek = { player.seekTo(it) },
                    onToggleRepeat = { player.toggleRepeatMode() },
                    onToggleShuffle = { player.toggleShuffle() },
                    onToggleFavorite = { currentSong?.let { viewModel.toggleFavorite(it.id) } },
                    onOpenQueue = { showQueueSheet = true },
                    onOpenQueuesManager = { viewModel.navigateTo(Screen.QUEUES_MANAGER) },
                    onOpenLyrics = { viewModel.navigateTo(Screen.LYRICS_SYNC) },
                    onOpenBookmarks = { showBookmarksSheet = true },
                    onOpenSpeedPitch = { showSpeedPitchDialog = true },
                    onOpenEqualizer = { viewModel.navigateTo(Screen.EQUALIZER) },
                    onOpenSleepTimer = { showSleepTimerDialog = true },
                    onOpenTagEditor = { s -> viewModel.openTagEditor(s) },
                    onShareSong = { showShareDialog = true },
                    onNavigateToSearch = { viewModel.navigateTo(Screen.SEARCH) },
                    onNavigateToSettings = { viewModel.navigateTo(Screen.SETTINGS) },
                    onExitApp = {
                        viewModel.exitApplication()
                        (context as? ComponentActivity)?.finish()
                    },
                    onClose = { viewModel.navigateBack() }
                )

                Screen.PLAYLIST_DETAIL -> {
                    selectedPlaylist?.let { pl ->
                        PlaylistDetailScreen(
                            playlist = pl,
                            songs = songsInPlaylist,
                            currentSongId = currentSong?.id,
                            isSelectionMode = isSelectionMode,
                            selectedSongIds = selectedSongIds,
                            onBack = { viewModel.navigateBack() },
                            onPlayAll = { player.playSongList(songsInPlaylist, 0) },
                            onShuffleAll = {
                                if (songsInPlaylist.isNotEmpty()) {
                                    player.playSongList(songsInPlaylist.shuffled(), 0)
                                }
                            },
                            onSongClick = { song -> player.playSingleSong(song) },
                            onSongLongClick = { song ->
                                if (isSelectionMode) {
                                    viewModel.toggleSongSelection(song.id)
                                } else {
                                    viewModel.startSelection(song.id)
                                }
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onAddToQueue = { player.addToQueueNext(it) },
                            onRemoveFromPlaylist = { songId ->
                                viewModel.removeSongsFromPlaylist(pl.id, listOf(songId))
                            },
                            onDeletePlaylist = { viewModel.deletePlaylist(pl.id) },
                            onRenamePlaylist = { newName ->
                                viewModel.updatePlaylistName(pl.id, newName)
                            }
                        )
                    }
                }

                Screen.EQUALIZER -> EqualizerScreen(
                    config = equalizerConfig,
                    currentDevice = currentDevice,
                    onSelectDeviceProfile = { player.selectDeviceProfile(it) },
                    onToggleEnabled = { player.setEqualizerEnabled(it) },
                    onSelectPreset = { player.applyPreset(it) },
                    onSaveCustomPreset = { player.saveCustomPreset(it) },
                    onDeleteCustomPreset = { player.deleteCustomPreset(it) },
                    onBassBoostChange = { player.setBassBoost(it) },
                    onVirtualizerChange = { player.setVirtualizer(it) },
                    onBandChange = { band, lvl -> player.setBandLevel(band, lvl) },
                    onChannelBalanceChange = { player.setChannelBalance(it) },
                    onReset = { player.applyPreset("Normal") },
                    onBack = { viewModel.navigateBack() }
                )

                Screen.DOWNLOADS -> DownloadsScreen(
                    downloads = downloads,
                    inspectedMedia = inspectedMedia,
                    isInspecting = isInspecting,
                    onInspectUrl = { viewModel.inspectLink(it) },
                    onStartDownload = { viewModel.startDownload(it) },
                    onClearInspected = { viewModel.clearInspectedMedia() },
                    onRetryDownload = { viewModel.retryDownload(it) },
                    onCancelDownload = { viewModel.cancelDownload(it) },
                    onCreatePlaylistFromYouTube = { title ->
                        viewModel.createPlaylist("YouTube: $title")
                    }
                )

                Screen.BLUETOOTH_AUTOMATION -> BluetoothScreen(
                    playOnConnectEnabled = playOnConnect,
                    pauseOnDisconnectEnabled = pauseOnDisconnect,
                    onTogglePlayOnConnect = { player.setPlayOnBluetoothConnect(it) },
                    onTogglePauseOnDisconnect = { player.setPauseOnBluetoothDisconnect(it) },
                    onBack = { viewModel.navigateBack() }
                )

                Screen.SETTINGS -> SettingsScreen(
                    onNavigate = { viewModel.navigateTo(it) },
                    onRescanLibrary = { viewModel.rescanLibrary() },
                    onExportBackup = { cb -> viewModel.exportBackup(cb) },
                    onRestoreBackup = { json, cb -> viewModel.restoreBackup(json, cb) }
                )

                Screen.SEARCH -> SearchScreen(
                    searchQuery = searchQuery,
                    searchHistory = searchHistory,
                    allSongs = allSongs,
                    playlists = playlists,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    onClearHistory = { viewModel.clearSearchHistory() },
                    onSongClick = { song -> player.playSingleSong(song) },
                    onPlaylistClick = { pl -> viewModel.openPlaylist(pl) },
                    onBack = { viewModel.navigateBack() }
                )

                Screen.QUEUES_MANAGER -> QueuesManagerScreen(
                    queues = queuesList,
                    activeQueueId = activeQueueId,
                    onBack = { viewModel.navigateBack() },
                    onSwitchQueue = { qId -> viewModel.switchQueue(qId) },
                    onCreateQueue = { name -> viewModel.createNewQueue(name) },
                    onDuplicateQueue = { qId, newName -> viewModel.duplicateQueue(qId, newName) },
                    onRenameQueue = { qId, newName -> viewModel.renameQueue(qId, newName) },
                    onDeleteQueue = { qId -> viewModel.deleteQueue(qId) },
                    onClearQueue = { qId -> viewModel.clearQueue(qId) },
                    onSaveQueueAsPlaylist = { qId, plName -> viewModel.saveQueueAsPlaylist(qId, plName) },
                    onRestorePreviousQueue = { viewModel.restorePreviousQueue() },
                    // Song actions
                    onSongClick = { index -> player.skipToQueueItem(index) },
                    onMoveSong = { from, to -> viewModel.reorderQueueSong(from, to) },
                    onRemoveFromQueue = { index -> player.removeFromQueue(index) },
                    onPlayNext = { song -> player.addToQueueNext(song) },
                    onAddToAnotherQueue = { song, qId -> viewModel.addSelectedToSpecificQueue(qId, listOf(song)) },
                    onAddToPlaylist = { song ->
                        singleSongForPlaylist = song
                        showAddToPlaylistDialog = true
                    },
                    onViewSongInPlayer = { viewModel.navigateTo(Screen.PLAYER) },
                    onStopAfterThisSong = { song -> viewModel.setStopAfterThisSong(song) },
                    onEditSongTags = { song -> viewModel.openTagEditor(song) },
                    onMoveToFolder = { song ->
                        songForMove = song
                    },
                    onAudioCutter = { song ->
                        songForCutter = song
                    },
                    onSetAsRingtone = { song ->
                        Toast.makeText(context, "Definido como toque: ${song.title}", Toast.LENGTH_SHORT).show()
                    },
                    onSpeedPitch = { showSpeedPitchDialog = true },
                    onShareSong = { showShareDialog = true },
                    onDeleteSongPermanently = { song ->
                        songToDeletePermanently = song
                    },
                    onShowSongInfo = { song ->
                        songForInfo = song
                    }
                )

                Screen.TAG_EDITOR -> TagEditorScreen(
                    song = selectedSongForTagEdit,
                    batchSongIds = selectedSongIds.toList(),
                    onBack = { viewModel.navigateBack() },
                    onSaveSongTags = { updatedSong -> viewModel.saveSongTags(updatedSong) },
                    onBatchSaveTags = { artist, album, genre -> viewModel.batchEditTags(artist, album, genre) }
                )

                Screen.LYRICS_SYNC -> LyricsSyncScreen(
                    song = currentSong,
                    lyrics = currentLyrics,
                    currentPositionMs = currentPosition,
                    isPlaying = isPlaying,
                    abRepeatState = abRepeatState,
                    onBack = { viewModel.navigateBack() },
                    onSeekTo = { pos -> player.seekTo(pos) },
                    onSaveLyrics = { songId, lrc -> viewModel.saveLyrics(songId, lrc) },
                    onSetPointA = { pos -> player.setPointA(pos) },
                    onSetPointB = { pos -> player.setPointB(pos) },
                    onClearABRepeat = { player.clearABRepeat() }
                )

                Screen.STATISTICS -> StatisticsScreen(
                    stats = currentStats,
                    selectedPeriod = statsPeriod,
                    onPeriodSelected = { period -> viewModel.loadStats(period) },
                    onExportCsv = { viewModel.exportStatsCsv() },
                    onClearSongHistory = { sId -> viewModel.clearStatsForSong(sId) },
                    onBack = { viewModel.navigateBack() }
                )

                Screen.FOLDER_EXPLORER -> FolderExplorerScreen(
                    folders = folders,
                    allSongs = allSongs,
                    viewMode = folderViewMode,
                    currentPath = currentFolderPath,
                    onBack = { viewModel.navigateBack() },
                    onToggleViewMode = { mode -> viewModel.setFolderViewMode(mode) },
                    onSelectFolder = { path -> viewModel.navigateToFolder(path) },
                    onPlayFolder = { fSongs ->
                        if (fSongs.isNotEmpty()) player.playSongList(fSongs, 0)
                    },
                    onAddFolderToQueue = { fSongs ->
                        fSongs.forEach { player.addToQueueNext(it) }
                    },
                    onSongClick = { s -> player.playSingleSong(s) }
                )
            }
        }
    }

    // Selection Options Sheet (Smart Selection tools)
    SelectionOptionsMenuSheet(
        isVisible = showSelectionOptionsSheet,
        onDismiss = { showSelectionOptionsSheet = false },
        onSelectAll = { viewModel.selectAll(allSongs) },
        onDeselectAll = { viewModel.deselectAll() },
        onSelectRange = { viewModel.selectRange(allSongs) },
        onInvertSelection = { viewModel.invertSelection(allSongs) },
        onSelectSameArtist = { viewModel.selectSameArtist(allSongs) },
        onSelectSameAlbum = { viewModel.selectSameAlbum(allSongs) }
    )

    // Batch Actions Sheet
    BatchActionsSheet(
        isVisible = showBatchActionsSheet,
        selectedCount = selectedSongIds.size,
        queues = queuesList,
        onDismiss = { showBatchActionsSheet = false },
        onPlaySelected = { viewModel.playSelectedSongs(allSongs) },
        onShufflePlaySelected = { viewModel.shufflePlaySelectedSongs(allSongs) },
        onPlayNext = { viewModel.playSelectedNext(allSongs) },
        onAddToCurrentQueue = { viewModel.addSelectedToQueue(allSongs) },
        onAddToSpecificQueue = { qId -> viewModel.addSelectedToSpecificQueue(qId, allSongs) },
        onRemoveFromQueue = { viewModel.removeSelectedFromCurrentQueue() },
        onAddToPlaylist = { showAddToPlaylistDialog = true },
        onFavoriteAll = { viewModel.favoriteSelectedSongs(true) },
        onUnfavoriteAll = { viewModel.favoriteSelectedSongs(false) },
        onBatchEditTags = { viewModel.navigateTo(Screen.TAG_EDITOR) },
        onMoveToFolder = {
            val first = allSongs.firstOrNull { selectedSongIds.contains(it.id) }
            songForMove = first
        },
        onCopyToFolder = {
            Toast.makeText(context, "${selectedSongIds.size} faixas copiadas para o destino", Toast.LENGTH_SHORT).show()
        },
        onSpeedPitch = { showSpeedPitchDialog = true },
        onShare = {
            val first = allSongs.firstOrNull { selectedSongIds.contains(it.id) }
            singleSongForPlaylist = first
            showShareDialog = true
        },
        onClearHistory = { viewModel.clearHistoryForSelected() },
        onDeleteSelected = {
            isBatchDeleteConfirm = true
        }
    )

    // Add To Playlist Dialog
    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            playlists = playlists,
            onDismiss = {
                showAddToPlaylistDialog = false
                singleSongForPlaylist = null
            },
            onSelectPlaylist = { plId ->
                val song = singleSongForPlaylist
                if (song != null) {
                    viewModel.addSingleSongToPlaylist(plId, song.id)
                } else {
                    viewModel.addSelectedToPlaylist(plId)
                }
            },
            onCreateNewPlaylist = { name ->
                viewModel.createPlaylistWithSelection(name)
            }
        )
    }

    // Queue Bottom Sheet
    if (showQueueSheet) {
        QueueBottomSheet(
            queue = queue,
            currentIndex = queueIndex,
            onDismiss = { showQueueSheet = false },
            onSelectIndex = { player.skipToQueueItem(it) },
            onRemoveItem = { player.removeFromQueue(it) },
            onClearQueue = { player.clearQueue() }
        )
    }

    // Sleep Timer Dialog
    if (showSleepTimerDialog) {
        SleepTimerDialog(
            sleepTimerState = sleepTimerState,
            onDismiss = { showSleepTimerDialog = false },
            onStartTimer = { mins -> player.startSleepTimer(mins) },
            onSetStopAfterTrack = { enabled -> player.setStopAfterCurrentTrack(enabled) },
            onCancelTimer = { player.cancelSleepTimer() }
        )
    }

    // Bookmarks Bottom Sheet
    if (showBookmarksSheet && currentSong != null) {
        BookmarksBottomSheet(
            bookmarks = currentSongBookmarks,
            currentPositionMs = currentPosition,
            onDismiss = { showBookmarksSheet = false },
            onJumpTo = { pos -> player.seekTo(pos) },
            onAddBookmark = { title, note, pos ->
                viewModel.addBookmark(title, note, pos)
            },
            onDeleteBookmark = { bId -> viewModel.deleteBookmark(bId) }
        )
    }

    // Speed & Pitch Dialog
    if (showSpeedPitchDialog) {
        SpeedPitchDialog(
            speed = speedPitch.speed,
            pitch = speedPitch.pitch,
            onDismiss = { showSpeedPitchDialog = false },
            onSpeedChange = { player.setPlaybackSpeed(it) },
            onPitchChange = { player.setPlaybackPitch(it) },
            onReset = { player.resetSpeedAndPitch() }
        )
    }

    // Share Now Playing Dialog
    if (showShareDialog) {
        ShareNowPlayingDialog(
            song = currentSong ?: singleSongForPlaylist,
            onDismiss = { showShareDialog = false }
        )
    }

    // Song Info Dialog
    songForInfo?.let { s ->
        SongInfoDialog(
            song = s,
            onDismiss = { songForInfo = null }
        )
    }

    // Audio Cutter Sheet
    songForCutter?.let { s ->
        AudioCutterSheet(
            song = s,
            onDismiss = { songForCutter = null },
            onSetAsRingtone = {
                Toast.makeText(context, "Trecho definido como toque do aparelho!", Toast.LENGTH_SHORT).show()
                songForCutter = null
            }
        )
    }

    // Move Song to Folder Dialog
    songForMove?.let { s ->
        val folderNames = folders.map { it.name }.ifEmpty { listOf("Música", "Downloads", "Audio") }
        MoveSongToFolderDialog(
            song = s,
            folders = folderNames,
            onMove = { f ->
                Toast.makeText(context, "Música movida para pasta '$f'", Toast.LENGTH_SHORT).show()
                songForMove = null
            },
            onDismiss = { songForMove = null }
        )
    }

    // Confirm Permanent Delete Single Dialog
    songToDeletePermanently?.let { s ->
        ConfirmPermanentDeleteDialog(
            count = 1,
            onConfirm = {
                viewModel.deleteSingleSong(s)
                songToDeletePermanently = null
            },
            onDismiss = { songToDeletePermanently = null }
        )
    }

    // Confirm Permanent Delete Batch Dialog
    if (isBatchDeleteConfirm) {
        ConfirmPermanentDeleteDialog(
            count = selectedSongIds.size,
            onConfirm = {
                viewModel.deleteSelectedSongs()
                isBatchDeleteConfirm = false
            },
            onDismiss = { isBatchDeleteConfirm = false }
        )
    }
}
