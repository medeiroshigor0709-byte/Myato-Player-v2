package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
    favoriteSongs: List<Song>,
    recentAddedSongs: List<Song>,
    historyPlayedSongs: List<Song>,
    mostPlayedSongs: List<Song>,
    neverPlayedSongs: List<Song>,
    myPlaylists: List<Playlist>,
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
    onEditSongTags: (Song) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onImportM3u: (name: String, content: String) -> Unit,
    onRescanLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var inCategorySearch by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showImportM3uDialog by remember { mutableStateOf(false) }
    var m3uNameInput by remember { mutableStateOf("") }
    var m3uContentInput by remember { mutableStateOf("") }

    // 8 Categories specified in Section 2 (Image 2 reference)
    val tabs = listOf(
        "Todas as Músicas",
        "Favoritas",
        "Adicionadas Recentemente",
        "Tocadas Recentemente",
        "Mais Tocadas",
        "Não Tocadas",
        "Minhas Playlists",
        "Playlists Externas (M3U)"
    )

    // Current category source list
    val currentSongList = remember(selectedTab, allSongs, favoriteSongs, recentAddedSongs, historyPlayedSongs, mostPlayedSongs, neverPlayedSongs) {
        when (selectedTab) {
            0 -> allSongs
            1 -> favoriteSongs
            2 -> recentAddedSongs
            3 -> historyPlayedSongs
            4 -> mostPlayedSongs
            5 -> neverPlayedSongs
            else -> emptyList()
        }
    }

    // Filter by search inside category
    val filteredSongs = remember(currentSongList, inCategorySearch) {
        if (inCategorySearch.isBlank()) currentSongList
        else {
            val q = inCategorySearch.trim().lowercase()
            currentSongList.filter {
                it.title.lowercase().contains(q) ||
                it.artist.lowercase().contains(q) ||
                it.album.lowercase().contains(q)
            }
        }
    }

    // Playlists filters
    val filteredMyPlaylists = remember(myPlaylists, inCategorySearch) {
        val base = myPlaylists.filter { !it.name.contains("M3U", ignoreCase = true) && !it.name.contains("Externa", ignoreCase = true) }
        if (inCategorySearch.isBlank()) base
        else base.filter { it.name.lowercase().contains(inCategorySearch.trim().lowercase()) }
    }

    val filteredExternalPlaylists = remember(myPlaylists, inCategorySearch) {
        val base = myPlaylists.filter { it.name.contains("M3U", ignoreCase = true) || it.name.contains("Externa", ignoreCase = true) }
        if (inCategorySearch.isBlank()) base
        else base.filter { it.name.lowercase().contains(inCategorySearch.trim().lowercase()) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MyatoBackground)
            .testTag("library_screen")
    ) {
        // Library Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Biblioteca Musical",
                    color = MyatoWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${tabs.getOrElse(selectedTab) { "Biblioteca" }} • ${if (selectedTab in 0..5) filteredSongs.size else if (selectedTab == 6) filteredMyPlaylists.size else filteredExternalPlaylists.size} itens",
                    color = MyatoGreen,
                    fontSize = 12.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Search in category toggle
                IconButton(onClick = { isSearchActive = !isSearchActive }) {
                    Icon(
                        imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Buscar na categoria",
                        tint = if (isSearchActive) MyatoGreen else MyatoWhite
                    )
                }

                // Rescan Library button
                IconButton(onClick = onRescanLibrary) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Atualizar biblioteca",
                        tint = MyatoGrayLight
                    )
                }

                // Sort Order dropdown button
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Ordenar",
                            tint = MyatoGreen
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
                                        text = order.label,
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
        }

        // In-Category Search TextField
        AnimatedVisibility(visible = isSearchActive) {
            OutlinedTextField(
                value = inCategorySearch,
                onValueChange = { inCategorySearch = it },
                placeholder = { Text("Filtrar nesta categoria...", color = MyatoGrayDark) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.FilterList, null, tint = MyatoGreen) },
                trailingIcon = {
                    if (inCategorySearch.isNotEmpty()) {
                        IconButton(onClick = { inCategorySearch = "" }) {
                            Icon(Icons.Default.Clear, null, tint = MyatoGrayLight)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MyatoGreen,
                    unfocusedBorderColor = MyatoBorder,
                    focusedTextColor = MyatoWhite,
                    unfocusedTextColor = MyatoWhite
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Scrollable 8 Tabs
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

        Spacer(modifier = Modifier.height(8.dp))

        // Content area based on selected tab
        when (selectedTab) {
            0, 1, 2, 3, 4, 5 -> {
                // Songs lists (Todas, Favoritas, Recentes, Tocadas, Mais Tocadas, Não Tocadas)
                if (filteredSongs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (selectedTab == 5) Icons.Default.MusicOff else Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MyatoGrayDark,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = when (selectedTab) {
                                    1 -> "Nenhuma música favoritada ainda."
                                    3 -> "Nenhuma reprodução registrada recentemente."
                                    4 -> "Nenhuma reprodução acumulada."
                                    5 -> "Parabéns! Todas as músicas já foram tocadas."
                                    else -> "Nenhuma música encontrada."
                                },
                                color = MyatoGrayLight,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredSongs, key = { it.id }) { song ->
                            SongListItem(
                                song = song,
                                isSelected = selectedSongIds.contains(song.id),
                                isSelectionMode = isSelectionMode,
                                isPlayingCurrent = currentSongId == song.id,
                                onClick = { onSongClick(song) },
                                onLongClick = { onSongLongClick(song) },
                                onToggleFavorite = { onToggleFavorite(song.id) },
                                onAddToQueue = { onAddToQueue(song) },
                                onAddToPlaylist = { onAddToPlaylist(song) },
                                onDelete = { onDeleteSong(song) },
                                onEditTags = { onEditSongTags(song) }
                            )
                        }
                    }
                }
            }

            6 -> {
                // Minhas Playlists
                Column(modifier = Modifier.fillMaxSize()) {
                    Button(
                        onClick = onCreatePlaylistClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Criar Nova Playlist", fontWeight = FontWeight.Bold)
                    }

                    if (filteredMyPlaylists.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nenhuma playlist criada.", color = MyatoGrayLight)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredMyPlaylists, key = { it.id }) { pl ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onPlaylistClick(pl) },
                                    colors = CardDefaults.cardColors(containerColor = MyatoCard)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MyatoSurfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.PlaylistPlay, null, tint = MyatoGreen)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(pl.name, color = MyatoWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text("${pl.songCount} músicas", color = MyatoGrayMedium, fontSize = 12.sp)
                                        }
                                        Icon(Icons.Default.ChevronRight, null, tint = MyatoGrayMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            7 -> {
                // Playlists Externas (M3U / M3U8)
                Column(modifier = Modifier.fillMaxSize()) {
                    Button(
                        onClick = {
                            m3uNameInput = "Minha Playlist Externa"
                            m3uContentInput = "#EXTM3U\n#EXTINF:180,Neon Horizon\nNeon Horizon.mp3\n#EXTINF:210,Deep Space Pulse\nDeep Space.mp3"
                            showImportM3uDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Importar Playlist M3U / M3U8", fontWeight = FontWeight.Bold)
                    }

                    if (filteredExternalPlaylists.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.FormatListBulleted, null, tint = MyatoGrayDark, modifier = Modifier.size(54.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "Nenhuma playlist externa importada.",
                                    color = MyatoWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Toque acima para importar listas .m3u ou .m3u8 do seu celular.",
                                    color = MyatoGrayMedium,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredExternalPlaylists, key = { it.id }) { pl ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onPlaylistClick(pl) },
                                    colors = CardDefaults.cardColors(containerColor = MyatoCard)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MyatoSurfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Article, null, tint = MyatoGreen)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(pl.name, color = MyatoWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text("${pl.songCount} faixas importadas • M3U", color = MyatoGreen, fontSize = 12.sp)
                                        }
                                        Icon(Icons.Default.ChevronRight, null, tint = MyatoGrayMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Import M3U Dialog
    if (showImportM3uDialog) {
        AlertDialog(
            onDismissRequest = { showImportM3uDialog = false },
            containerColor = MyatoCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FileUpload, null, tint = MyatoGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Importar Playlist M3U / M3U8", color = MyatoWhite, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        "Digite um nome para a playlist e insira o conteúdo M3U ou caminhos de arquivos:",
                        color = MyatoGrayLight,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = m3uNameInput,
                        onValueChange = { m3uNameInput = it },
                        label = { Text("Nome da Playlist") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MyatoGreen,
                            focusedTextColor = MyatoWhite,
                            unfocusedTextColor = MyatoWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = m3uContentInput,
                        onValueChange = { m3uContentInput = it },
                        label = { Text("Conteúdo do arquivo M3U") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MyatoGreen,
                            focusedTextColor = MyatoWhite,
                            unfocusedTextColor = MyatoWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onImportM3u(m3uNameInput, m3uContentInput)
                        showImportM3uDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground)
                ) {
                    Text("Importar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportM3uDialog = false }) {
                    Text("Cancelar", color = MyatoGrayLight)
                }
            }
        )
    }
}
