package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.model.Playlist
import com.example.model.Song
import com.example.ui.components.SongListItem
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    songs: List<Song>,
    currentSongId: Long?,
    isSelectionMode: Boolean,
    selectedSongIds: Set<Long>,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onSongClick: (Song) -> Unit,
    onSongLongClick: (Song) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onRemoveFromPlaylist: (Long) -> Unit,
    onDeletePlaylist: () -> Unit,
    onRenamePlaylist: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(playlist.name) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(playlist.name, color = MyatoWhite, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MyatoWhite)
                    }
                },
                actions = {
                    IconButton(onClick = { showRenameDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Renomear", tint = MyatoWhite)
                    }
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MyatoError)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MyatoBackground)
            )
        },
        containerColor = MyatoBackground,
        modifier = modifier.testTag("playlist_detail_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Hero Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MyatoSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.QueueMusic, null, tint = MyatoGreen, modifier = Modifier.size(42.dp))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = playlist.name,
                        color = MyatoWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${songs.size} músicas",
                        color = MyatoGrayMedium,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onPlayAll,
                            enabled = songs.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("playlist_play_all_btn")
                        ) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tocar")
                        }

                        OutlinedButton(
                            onClick = onShuffleAll,
                            enabled = songs.isNotEmpty(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MyatoWhite),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Shuffle, null, tint = MyatoGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Aleatório")
                        }
                    }
                }
            }

            HorizontalDivider(color = MyatoBorder)

            // Songs in Playlist
            if (songs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhuma música adicionada a esta playlist", color = MyatoGrayMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp, start = 8.dp, end = 8.dp)
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
                                    onSongLongClick(song)
                                } else {
                                    onSongClick(song)
                                }
                            },
                            onLongClick = { onSongLongClick(song) },
                            onToggleFavorite = { onToggleFavorite(song.id) },
                            onAddToQueue = { onAddToQueue(song) },
                            onAddToPlaylist = {},
                            onDelete = { onRemoveFromPlaylist(song.id) }
                        )
                    }
                }
            }
        }
    }

    // Rename Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = MyatoCard,
            title = { Text("Renomear Playlist", color = MyatoWhite, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MyatoGreen,
                        focusedTextColor = MyatoWhite,
                        unfocusedTextColor = MyatoWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onRenamePlaylist(newName.trim())
                            showRenameDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground)
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancelar", color = MyatoGrayLight)
                }
            }
        )
    }

    // Delete Confirm Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = MyatoCard,
            title = { Text("Excluir Playlist?", color = MyatoWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text("Tem certeza que deseja excluir esta playlist? As músicas continuarão na sua biblioteca.", color = MyatoGrayLight)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeletePlaylist()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MyatoError)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar", color = MyatoGrayLight)
                }
            }
        )
    }
}
