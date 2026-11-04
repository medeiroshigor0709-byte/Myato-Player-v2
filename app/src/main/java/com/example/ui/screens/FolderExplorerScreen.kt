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
import com.example.model.Folder
import com.example.model.FolderViewMode
import com.example.model.Song
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderExplorerScreen(
    folders: List<Folder>,
    allSongs: List<Song>,
    viewMode: FolderViewMode,
    currentPath: String,
    onBack: () -> Unit,
    onToggleViewMode: (FolderViewMode) -> Unit,
    onSelectFolder: (String) -> Unit,
    onPlayFolder: (List<Song>) -> Unit,
    onAddFolderToQueue: (List<Song>) -> Unit,
    onSongClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val currentFolderSongs = remember(currentPath, allSongs, searchQuery) {
        if (currentPath == "/") emptyList()
        else allSongs.filter { it.folderName.equals(currentPath, ignoreCase = true) }
            .filter { searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (currentPath == "/") "Navegador de Pastas" else currentPath,
                            color = MyatoWhite,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (viewMode == FolderViewMode.LINEAR) "Modo Linear" else "Modo Hierárquico",
                            color = MyatoGreen,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentPath != "/") onSelectFolder("/") else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MyatoWhite)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val nextMode = if (viewMode == FolderViewMode.LINEAR) FolderViewMode.HIERARCHICAL else FolderViewMode.LINEAR
                            onToggleViewMode(nextMode)
                        },
                        modifier = Modifier.testTag("toggle_folder_mode_btn")
                    ) {
                        Icon(
                            imageVector = if (viewMode == FolderViewMode.LINEAR) Icons.Default.AccountTree else Icons.Default.List,
                            contentDescription = "Alternar Modo",
                            tint = MyatoGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MyatoBackground)
            )
        },
        containerColor = MyatoBackground,
        modifier = modifier.testTag("folder_explorer_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Quick Search within Folder
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filtrar nesta pasta...", color = MyatoGrayDark, fontSize = 13.sp) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null, tint = MyatoGrayMedium, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, null, tint = MyatoGrayLight)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MyatoGreen,
                    unfocusedBorderColor = MyatoBorder,
                    focusedTextColor = MyatoWhite,
                    unfocusedTextColor = MyatoWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Current Directory Action Header
            if (currentPath != "/") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onPlayFolder(currentFolderSongs) },
                        enabled = currentFolderSongs.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tocar Pasta (${currentFolderSongs.size})")
                    }

                    OutlinedButton(
                        onClick = { onAddFolderToQueue(currentFolderSongs) },
                        enabled = currentFolderSongs.isNotEmpty(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MyatoWhite),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.QueueMusic, null, tint = MyatoGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Adicionar à Fila")
                    }
                }
            }

            // List of folders or songs in folder
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (currentPath == "/") {
                    // Show folders
                    items(folders) { folder ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectFolder(folder.name) },
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
                                    Icon(Icons.Default.Folder, null, tint = MyatoGreen, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = folder.name,
                                        color = MyatoWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${folder.songCount} faixas de áudio",
                                        color = MyatoGrayMedium,
                                        fontSize = 12.sp
                                    )
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = MyatoGrayMedium)
                            }
                        }
                    }
                } else {
                    // Show songs in selected folder
                    if (currentFolderSongs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Nenhuma música nesta pasta", color = MyatoGrayMedium)
                            }
                        }
                    } else {
                        items(currentFolderSongs) { song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSongClick(song) }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MyatoSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.MusicNote, null, tint = MyatoGreen, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        color = MyatoWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${song.artist} • ${song.formattedDuration}",
                                        color = MyatoGrayMedium,
                                        fontSize = 12.sp
                                    )
                                }
                                Icon(Icons.Default.PlayArrow, null, tint = MyatoGrayLight)
                            }
                        }
                    }
                }
            }
        }
    }
}
