package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.SongListItem
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    allSongs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    folders: List<Folder>,
    playlists: List<Playlist>,
    favoriteSongs: List<Song>,
    recentSongs: List<Song>,
    mostPlayedSongs: List<Song>,
    selectedTab: Int,
    currentSortOrder: SortOrder,
    currentSongId: Long?,
    isSelectionMode: Boolean,
    selectedSongIds: Set<Long>,
    onTabSelect: (Int) -> Unit,
    onSortChange: (SortOrder) -> Unit,
    onSongClick: (Song) -> Unit,
    onSongLongClick: (Song) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    val tabs = listOf(
        "Músicas",
        "Álbuns",
        "Artistas",
        "Pastas",
        "Playlists",
        "Favoritas",
        "Recentes",
        "Mais Tocadas"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MyatoBackground)
            .testTag("library_screen")
    ) {
        // Library Header with Title and Sort menu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Biblioteca Musical",
                    color = MyatoWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${allSongs.size} faixas no dispositivo",
                    color = MyatoGrayMedium,
                    fontSize = 12.sp
                )
            }

            Box {
                OutlinedButton(
                    onClick = { showSortMenu = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MyatoWhite),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                    modifier = Modifier.testTag("sort_button")
                ) {
                    Icon(Icons.Default.Sort, null, tint = MyatoGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currentSortOrder.label.substringBefore(" "),
                        fontSize = 12.sp
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.background(MyatoCard)
                ) {
                    SortOrder.values().forEach { order ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    order.label,
                                    color = if (order == currentSortOrder) MyatoGreen else MyatoWhite,
                                    fontWeight = if (order == currentSortOrder) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                showSortMenu = false
                                onSortChange(order)
                            }
                        )
                    }
                }
            }
        }

        // Scrollable Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MyatoBackground,
            contentColor = MyatoGreen,
            edgePadding = 16.dp,
            divider = { HorizontalDivider(color = MyatoBorder) }
        ) {
            tabs.forEachIndexed { index, tabName ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { onTabSelect(index) },
                    text = {
                        Text(
                            text = tabName,
                            color = if (selectedTab == index) MyatoGreen else MyatoGrayMedium,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // Content based on tab
        when (selectedTab) {
            0 -> SongsTabContent(
                songs = allSongs,
                selectedSongIds = selectedSongIds,
                isSelectionMode = isSelectionMode,
                currentSongId = currentSongId,
                onSongClick = onSongClick,
                onSongLongClick = onSongLongClick,
                onToggleFavorite = onToggleFavorite,
                onAddToQueue = onAddToQueue,
                onAddToPlaylist = onAddToPlaylist,
                onDeleteSong = onDeleteSong
            )
            1 -> AlbumsTabContent(albums = albums, onAlbumClick = {})
            2 -> ArtistsTabContent(artists = artists, onArtistClick = {})
            3 -> FoldersTabContent(folders = folders, onFolderClick = {})
            4 -> PlaylistsTabContent(
                playlists = playlists,
                onPlaylistClick = onPlaylistClick,
                onCreatePlaylistClick = onCreatePlaylistClick
            )
            5 -> SongsTabContent(
                songs = favoriteSongs,
                selectedSongIds = selectedSongIds,
                isSelectionMode = isSelectionMode,
                currentSongId = currentSongId,
                onSongClick = onSongClick,
                onSongLongClick = onSongLongClick,
                onToggleFavorite = onToggleFavorite,
                onAddToQueue = onAddToQueue,
                onAddToPlaylist = onAddToPlaylist,
                onDeleteSong = onDeleteSong
            )
            6 -> SongsTabContent(
                songs = recentSongs,
                selectedSongIds = selectedSongIds,
                isSelectionMode = isSelectionMode,
                currentSongId = currentSongId,
                onSongClick = onSongClick,
                onSongLongClick = onSongLongClick,
                onToggleFavorite = onToggleFavorite,
                onAddToQueue = onAddToQueue,
                onAddToPlaylist = onAddToPlaylist,
                onDeleteSong = onDeleteSong
            )
            7 -> SongsTabContent(
                songs = mostPlayedSongs,
                selectedSongIds = selectedSongIds,
                isSelectionMode = isSelectionMode,
                currentSongId = currentSongId,
                onSongClick = onSongClick,
                onSongLongClick = onSongLongClick,
                onToggleFavorite = onToggleFavorite,
                onAddToQueue = onAddToQueue,
                onAddToPlaylist = onAddToPlaylist,
                onDeleteSong = onDeleteSong
            )
        }
    }
}

@Composable
private fun SongsTabContent(
    songs: List<Song>,
    selectedSongIds: Set<Long>,
    isSelectionMode: Boolean,
    currentSongId: Long?,
    onSongClick: (Song) -> Unit,
    onSongLongClick: (Song) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit
) {
    if (songs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Nenhuma música nesta categoria", color = MyatoGrayMedium)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp, start = 8.dp, end = 8.dp)
        ) {
            items(songs, key = { it.id }) { song ->
                val isSelected = selectedSongIds.contains(song.id)
                val isCurrent = song.id == currentSongId

                SongListItem(
                    song = song,
                    isSelected = isSelected,
                    isSelectionMode = isSelectionMode,
                    isPlayingCurrent = isCurrent,
                    onClick = {
                        if (isSelectionMode) {
                            onSongLongClick(song) // toggle
                        } else {
                            onSongClick(song)
                        }
                    },
                    onLongClick = { onSongLongClick(song) },
                    onToggleFavorite = { onToggleFavorite(song.id) },
                    onAddToQueue = { onAddToQueue(song) },
                    onAddToPlaylist = { onAddToPlaylist(song) },
                    onDelete = { onDeleteSong(song) }
                )
            }
        }
    }
}

@Composable
private fun AlbumsTabContent(albums: List<Album>, onAlbumClick: (Album) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(albums) { album ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onAlbumClick(album) },
                colors = CardDefaults.cardColors(containerColor = MyatoCard)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MyatoSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Album, null, tint = MyatoGreen, modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = album.name,
                            color = MyatoWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${album.artist} • ${album.songCount} músicas",
                            color = MyatoGrayMedium,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistsTabContent(artists: List<Artist>, onArtistClick: (Artist) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(artists) { artist ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onArtistClick(artist) },
                colors = CardDefaults.cardColors(containerColor = MyatoCard)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .background(MyatoSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = MyatoGreen, modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = artist.name,
                            color = MyatoWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${artist.songCount} faixas",
                            color = MyatoGrayMedium,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FoldersTabContent(folders: List<Folder>, onFolderClick: (Folder) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(folders) { folder ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onFolderClick(folder) },
                colors = CardDefaults.cardColors(containerColor = MyatoCard)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MyatoSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Folder, null, tint = MyatoGreen, modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = folder.name,
                            color = MyatoWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${folder.songCount} arquivos de áudio",
                            color = MyatoGrayMedium,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistsTabContent(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
    onCreatePlaylistClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Button(
                onClick = onCreatePlaylistClick,
                colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("create_playlist_btn")
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Criar Nova Playlist", fontWeight = FontWeight.Bold)
            }
        }

        items(playlists) { playlist ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onPlaylistClick(playlist) },
                colors = CardDefaults.cardColors(containerColor = MyatoCard)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MyatoSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.QueueMusic, null, tint = MyatoGreen, modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playlist.name,
                            color = MyatoWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${playlist.songCount} músicas",
                            color = MyatoGrayMedium,
                            fontSize = 12.sp
                        )
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = MyatoGrayMedium)
                }
            }
        }
    }
}
