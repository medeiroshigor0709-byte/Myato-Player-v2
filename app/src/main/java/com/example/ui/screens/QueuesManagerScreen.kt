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
import com.example.model.PlaybackQueue
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
    onSaveQueueAsPlaylist: (Long, String) -> Unit,
    onRestorePreviousQueue: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<PlaybackQueue?>(null) }
    var showSavePlaylistDialog by remember { mutableStateOf<PlaybackQueue?>(null) }
    var inputName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Filas Independentes", color = MyatoWhite, fontWeight = FontWeight.Bold)
                        Text("${queues.size} de 20 filas ativas", color = MyatoGreen, fontSize = 12.sp)
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
                .padding(horizontal = 16.dp)
        ) {
            // New Queue button
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
                items(queues, key = { it.id }) { q ->
                    val isActive = q.id == activeQueueId
                    var showItemMenu by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSwitchQueue(q.id) },
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
