package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.model.PlaybackQueue
import com.example.model.Song
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueuesManagerScreen(
    queues: List<PlaybackQueue>,
    activeQueueId: Long,
    onBack: () -> Unit,
    onSwitchQueue: (Long) -> Unit,
    onCreateQueue: (String) -> Unit,
    onDuplicateQueue: (Long, String) -> Unit,
    onRenameQueue: (Long, String) -> Unit,
    onDeleteQueue: (Long) -> Unit,
    onClearQueue: (Long) -> Unit,
    onSaveQueueAsPlaylist: (Long, String) -> Unit,
    onRestorePreviousQueue: () -> Unit,
    // Queue Song actions (14 options)
    onSongClick: (Int) -> Unit,
    onMoveSong: (fromIndex: Int, toIndex: Int) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToAnotherQueue: (Song, Long) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onViewSongInPlayer: (Song) -> Unit,
    onStopAfterThisSong: (Song) -> Unit,
    onEditSongTags: (Song) -> Unit,
    onMoveToFolder: (Song) -> Unit,
    onAudioCutter: (Song) -> Unit,
    onSetAsRingtone: (Song) -> Unit,
    onSpeedPitch: (Song) -> Unit,
    onShareSong: (Song) -> Unit,
    onDeleteSongPermanently: (Song) -> Unit,
    onShowSongInfo: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedViewTab by remember { mutableIntStateOf(1) } // 0 = Gerenciar Filas, 1 = Músicas da Fila Ativa
    var showCreateDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<PlaybackQueue?>(null) }
    var showSavePlaylistDialog by remember { mutableStateOf<PlaybackQueue?>(null) }
    var showAddToQueueSelector by remember { mutableStateOf<Song?>(null) }
    var inputName by remember { mutableStateOf("") }

    val activeQueue = remember(queues, activeQueueId) {
        queues.firstOrNull { it.id == activeQueueId } ?: queues.firstOrNull() ?: PlaybackQueue(id = 1L, name = "Fila 1")
    }

    val totalDurationMs = remember(activeQueue) {
        activeQueue.songs.sumOf { it.durationMs }
    }
    val totalMinutes = totalDurationMs / 60000
    val totalSeconds = (totalDurationMs % 60000) / 1000
    val formattedTotalDuration = "%d:%02d".format(totalMinutes, totalSeconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (selectedViewTab == 1) activeQueue.name else "Filas de Reprodução",
                            color = MyatoWhite,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (selectedViewTab == 1) "${activeQueue.songs.size} músicas • Duração: $formattedTotalDuration" else "${queues.size} de 20 filas ativas",
                            color = MyatoGreen,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MyatoWhite)
                    }
                },
                actions = {
                    IconButton(
                        onClick = onRestorePreviousQueue,
                        modifier = Modifier.testTag("restore_prev_queue_btn")
                    ) {
                        Icon(Icons.Default.History, contentDescription = "Restaurar Fila Anterior", tint = MyatoGrayLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MyatoBackground)
            )
        },
        containerColor = MyatoBackground,
        modifier = modifier.testTag("queues_manager_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Segmented Tab Row: Músicas da Fila vs Gerenciar 20 Filas
            TabRow(
                selectedTabIndex = selectedViewTab,
                containerColor = MyatoCard,
                contentColor = MyatoGreen,
                divider = { HorizontalDivider(color = MyatoBorder) }
            ) {
                Tab(
                    selected = selectedViewTab == 0,
                    onClick = { selectedViewTab = 0 },
                    text = { Text("Gerenciar Filas (${queues.size}/20)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedViewTab == 1,
                    onClick = { selectedViewTab = 1 },
                    text = { Text("Conteúdo da Fila Ativa", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                )
            }

            if (selectedViewTab == 0) {
                // Tab 0: List of up to 20 queues with controls
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Button(
                        onClick = {
                            inputName = "Fila ${queues.size + 1}"
                            showCreateDialog = true
                        },
                        enabled = queues.size < 20,
                        colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("create_new_queue_btn")
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (queues.size < 20) "Criar Nova Fila Independente" else "Limite de 20 Filas Atingido",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(queues.size) { idx ->
                            val q = queues[idx]
                            val isActive = q.id == activeQueueId
                            var showItemMenu by remember { mutableStateOf(false) }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        onSwitchQueue(q.id)
                                        selectedViewTab = 1
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isActive) MyatoSurfaceVariant else MyatoCard
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isActive) MyatoGreen.copy(alpha = 0.2f) else MyatoSurface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isActive) Icons.Default.Equalizer else Icons.Default.QueueMusic,
                                            contentDescription = null,
                                            tint = if (isActive) MyatoGreen else MyatoGrayLight,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = q.name,
                                                color = if (isActive) MyatoGreen else MyatoWhite,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (isActive) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    color = MyatoGreen,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "EM REPRODUÇÃO",
                                                        color = MyatoBackground,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${q.songs.size} faixas • Posição: ${q.currentSongIndex + 1}/${q.songs.size.coerceAtLeast(1)}",
                                            color = MyatoGrayMedium,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Box {
                                        IconButton(onClick = { showItemMenu = true }) {
                                            Icon(Icons.Default.MoreVert, contentDescription = "Opções da fila", tint = MyatoGrayLight)
                                        }

                                        DropdownMenu(
                                            expanded = showItemMenu,
                                            onDismissRequest = { showItemMenu = false },
                                            modifier = Modifier.background(MyatoCard)
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Ouvir esta Fila", color = MyatoWhite) },
                                                leadingIcon = { Icon(Icons.Default.PlayArrow, null, tint = MyatoGreen) },
                                                onClick = {
                                                    showItemMenu = false
                                                    onSwitchQueue(q.id)
                                                    selectedViewTab = 1
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Renomear Fila", color = MyatoWhite) },
                                                leadingIcon = { Icon(Icons.Default.Edit, null, tint = MyatoWhite) },
                                                onClick = {
                                                    showItemMenu = false
                                                    inputName = q.name
                                                    showRenameDialog = q
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Duplicar Fila", color = MyatoWhite) },
                                                leadingIcon = { Icon(Icons.Default.ContentCopy, null, tint = MyatoWhite) },
                                                onClick = {
                                                    showItemMenu = false
                                                    onDuplicateQueue(q.id, "${q.name} (Cópia)")
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Limpar Fila", color = MyatoWhite) },
                                                leadingIcon = { Icon(Icons.Default.ClearAll, null, tint = MyatoWhite) },
                                                onClick = {
                                                    showItemMenu = false
                                                    onClearQueue(q.id)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Salvar como Playlist", color = MyatoWhite) },
                                                leadingIcon = { Icon(Icons.Default.PlaylistAdd, null, tint = MyatoGreen) },
                                                onClick = {
                                                    showItemMenu = false
                                                    inputName = q.name
                                                    showSavePlaylistDialog = q
                                                }
                                            )
                                            if (queues.size > 1) {
                                                DropdownMenuItem(
                                                    text = { Text("Excluir Fila", color = MyatoError) },
                                                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = MyatoError) },
                                                    onClick = {
                                                        showItemMenu = false
                                                        onDeleteQueue(q.id)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Tab 1: Queue Content View (Image 3 reference: drag alça, total duration, 14 options per song)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Actions Bar: Limpar, Salvar Playlist, Renomear
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${activeQueue.songs.size} faixas na fila",
                            color = MyatoWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Row {
                            TextButton(onClick = {
                                inputName = activeQueue.name
                                showSavePlaylistDialog = activeQueue
                            }) {
                                Icon(Icons.Default.PlaylistAdd, null, tint = MyatoGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Salvar Playlist", color = MyatoGreen, fontSize = 12.sp)
                            }

                            TextButton(onClick = { onClearQueue(activeQueue.id) }) {
                                Icon(Icons.Default.ClearAll, null, tint = MyatoError, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Limpar", color = MyatoError, fontSize = 12.sp)
                            }
                        }
                    }

                    if (activeQueue.songs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.QueueMusic, null, tint = MyatoGrayDark, modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Esta fila está vazia.", color = MyatoWhite, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Adicione músicas da biblioteca ou de playlists.", color = MyatoGrayMedium, fontSize = 12.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            itemsIndexed(activeQueue.songs, key = { index, song -> "${song.id}_$index" }) { index, song ->
                                val isCurrentTrack = index == activeQueue.currentSongIndex
                                var showSongMenu by remember { mutableStateOf(false) }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onSongClick(index) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isCurrentTrack) MyatoSurfaceVariant else MyatoCard
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Reorder controls (Move Up / Down alça)
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(end = 4.dp)
                                        ) {
                                            IconButton(
                                                onClick = { if (index > 0) onMoveSong(index, index - 1) },
                                                enabled = index > 0,
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.KeyboardArrowUp, null, tint = if (index > 0) MyatoGrayLight else MyatoBorder)
                                            }
                                            IconButton(
                                                onClick = { if (index < activeQueue.songs.size - 1) onMoveSong(index, index + 1) },
                                                enabled = index < activeQueue.songs.size - 1,
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.KeyboardArrowDown, null, tint = if (index < activeQueue.songs.size - 1) MyatoGrayLight else MyatoBorder)
                                            }
                                        }

                                        // Song Artwork
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isCurrentTrack) MyatoGreen.copy(alpha = 0.2f) else MyatoSurface),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isCurrentTrack) Icons.Default.Equalizer else Icons.Default.MusicNote,
                                                contentDescription = null,
                                                tint = if (isCurrentTrack) MyatoGreen else MyatoGrayLight,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        // Title & Artist
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${index + 1}. ${song.title}",
                                                color = if (isCurrentTrack) MyatoGreen else MyatoWhite,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${song.artist} • ${song.formattedDuration}",
                                                color = MyatoGrayMedium,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // 14-Item Options Menu
                                        Box {
                                            IconButton(onClick = { showSongMenu = true }) {
                                                Icon(Icons.Default.MoreVert, contentDescription = "Mais opções", tint = MyatoGrayLight)
                                            }

                                            DropdownMenu(
                                                expanded = showSongMenu,
                                                onDismissRequest = { showSongMenu = false },
                                                modifier = Modifier.background(MyatoCard)
                                            ) {
                                                // 1. Informações da música
                                                DropdownMenuItem(
                                                    text = { Text("1. Informações da Música", color = MyatoWhite) },
                                                    leadingIcon = { Icon(Icons.Default.Info, null, tint = MyatoGreen) },
                                                    onClick = {
                                                        showSongMenu = false
                                                        onShowSongInfo(song)
                                                    }
                                                )
                                                // 2. Remover dessa fila
                                                DropdownMenuItem(
                                                    text = { Text("2. Remover dessa Fila", color = MyatoError) },
                                                    leadingIcon = { Icon(Icons.Default.DeleteSweep, null, tint = MyatoError) },
                                                    onClick = {
                                                        showSongMenu = false
                                                        onRemoveFromQueue(index)
                                                    }
                                                )
                                                // 3. Tocar depois da música atual
                                                DropdownMenuItem(
                                                    text = { Text("3. Tocar Depois da Atual", color = MyatoWhite) },
                                                    leadingIcon = { Icon(Icons.Default.SkipNext, null, tint = MyatoGreen) },
                                                    onClick = {
                                                        showSongMenu = false
                                                        onPlayNext(song)
                                                    }
                                                )
                                                // 4. Adicionar a uma fila
                                                DropdownMenuItem(
                                                    text = { Text("4. Adicionar a Outra Fila", color = MyatoWhite) },
                                                    leadingIcon = { Icon(Icons.Default.QueueMusic, null, tint = MyatoGreen) },
                                                    onClick = {
                                                        showSongMenu = false
                                                        showAddToQueueSelector = song
                                                    }
                                                )
                                                // 5. Adicionar às playlists
                                                DropdownMenuItem(
                                                    text = { Text("5. Adicionar às Playlists", color = MyatoWhite) },
                                                    leadingIcon = { Icon(Icons.Default.PlaylistAdd, null, tint = MyatoGreen) },
                                                    onClick = {
                                                        showSongMenu = false
                                                        onAddToPlaylist(song)
                                                    }
                                                )
                                                // 6. Visualizar música
                                                DropdownMenuItem(
                                                    text = { Text("6. Visualizar Música", color = MyatoWhite) },
                                                    leadingIcon = { Icon(Icons.Default.Visibility, null, tint = MyatoWhite) },
                                                    onClick = {
                                                        showSongMenu = false
                                                        onViewSongInPlayer(song)
                                                    }
                                                )
                                                // 7. Parar depois dessa música
                                                DropdownMenuItem(
                                                    text = { Text("7. Parar Depois Dessa Música", color = MyatoWhite) },
                                                    leadingIcon = { Icon(Icons.Default.Timer, null, tint = MyatoWhite) },
                                                    onClick = {
                                                        showSongMenu = false
                                                        onStopAfterThisSong(song)
                                                    }
                                                )
                                                // 8. Editar tags
                                                DropdownMenuItem(
                                                    text = { Text("8. Editar Tags ID3", color = MyatoWhite) },
                                                    leadingIcon = { Icon(Icons.Default.EditNote, null, tint = MyatoWhite) },
                                                    onClick = {
                                                        showSongMenu = false
                                                        onEditSongTags(song)
                                                    }
                                                )
                                                // 9. Mover para uma pasta
                                                DropdownMenuItem(
                                                    text = { Text("9. Mover para Pasta", color = MyatoWhite) },
                                                    leadingIcon = { Icon(Icons.Default.DriveFileMove, null, tint = MyatoWhite) },
                                                    onClick = {
                                                        showSongMenu = false
                                                        onMoveToFolder(song)
                                                    }
                                                )
                                                // 10. Cortador de áudio
                                                DropdownMenuItem(
                                                    text = { Text("10. Cortador de Áudio", color = MyatoWhite) },
                                                    leadingIcon = { Icon(Icons.Default.ContentCut, null, tint = MyatoGreen) },
                                                    onClick = {
                                                        showSongMenu = false
                                                        onAudioCutter(song)
                                                    }
                                                )
                                                // 11. Definir como toque
                                                DropdownMenuItem(
                                                    text = { Text("11. Definir como Toque", color = MyatoWhite) },
                                                    leadingIcon = { Icon(Icons.Default.RingVolume, null, tint = MyatoWhite) },
                                                    onClick = {
                                                        showSongMenu = false
                                                        onSetAsRingtone(song)
                                                    }
                                                )
                                                // 12. Velocidade e tom de reprodução
                                                DropdownMenuItem(
                                                    text = { Text("12. Velocidade e Tom", color = MyatoWhite) },
                                                    leadingIcon = { Icon(Icons.Default.Speed, null, tint = MyatoWhite) },
                                                    onClick = {
                                                        showSongMenu = false
                                                        onSpeedPitch(song)
                                                    }
                                                )
                                                // 13. Compartilhar
                                                DropdownMenuItem(
                                                    text = { Text("13. Compartilhar", color = MyatoWhite) },
                                                    leadingIcon = { Icon(Icons.Default.Share, null, tint = MyatoWhite) },
                                                    onClick = {
                                                        showSongMenu = false
                                                        onShareSong(song)
                                                    }
                                                )
                                                // 14. Apagar permanentemente
                                                DropdownMenuItem(
                                                    text = { Text("14. Apagar Permanentemente", color = MyatoError) },
                                                    leadingIcon = { Icon(Icons.Default.DeleteForever, null, tint = MyatoError) },
                                                    onClick = {
                                                        showSongMenu = false
                                                        onDeleteSongPermanently(song)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Selector: Add song to specific queue (1..20)
    showAddToQueueSelector?.let { song ->
        AlertDialog(
            onDismissRequest = { showAddToQueueSelector = null },
            containerColor = MyatoCard,
            title = { Text("Adicionar a qual fila?", color = MyatoWhite, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp)) {
                    items(queues.size) { idx ->
                        val targetQ = queues[idx]
                        ListItem(
                            headlineContent = { Text(targetQ.name, color = MyatoWhite) },
                            supportingContent = { Text("${targetQ.songs.size} músicas", color = MyatoGrayMedium) },
                            leadingContent = { Icon(Icons.Default.QueueMusic, null, tint = MyatoGreen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAddToAnotherQueue(song, targetQ.id)
                                    showAddToQueueSelector = null
                                },
                            colors = ListItemDefaults.colors(containerColor = MyatoCard)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddToQueueSelector = null }) {
                    Text("Cancelar", color = MyatoGrayLight)
                }
            }
        )
    }

    // Create Queue Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = MyatoCard,
            title = { Text("Nova Fila de Reprodução", color = MyatoWhite, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("Nome da Fila") },
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
                        onCreateQueue(inputName)
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground)
                ) {
                    Text("Criar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar", color = MyatoGrayLight)
                }
            }
        )
    }

    // Rename Queue Dialog
    showRenameDialog?.let { q ->
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            containerColor = MyatoCard,
            title = { Text("Renomear Fila", color = MyatoWhite, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
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
                        onRenameQueue(q.id, inputName)
                        showRenameDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground)
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) {
                    Text("Cancelar", color = MyatoGrayLight)
                }
            }
        )
    }

    // Save as Playlist Dialog
    showSavePlaylistDialog?.let { q ->
        AlertDialog(
            onDismissRequest = { showSavePlaylistDialog = null },
            containerColor = MyatoCard,
            title = { Text("Salvar Fila como Playlist", color = MyatoWhite, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("Nome da Playlist") },
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
                        onSaveQueueAsPlaylist(q.id, inputName)
                        showSavePlaylistDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground)
                ) {
                    Text("Salvar Playlist")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePlaylistDialog = null }) {
                    Text("Cancelar", color = MyatoGrayLight)
                }
            }
        )
    }
}
