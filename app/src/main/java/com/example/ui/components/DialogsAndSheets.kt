package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Playlist
import com.example.model.SleepTimerConfig
import com.example.model.Song
import com.example.ui.theme.*

@Composable
fun AddToPlaylistDialog(
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (Long) -> Unit,
    onCreateNewPlaylist: (String) -> Unit
) {
    var newPlaylistName by remember { mutableStateOf("") }
    var showCreateField by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MyatoCard,
        title = {
            Text(
                text = "Adicionar à Playlist",
                color = MyatoWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (showCreateField) {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("Nome da Nova Playlist") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MyatoGreen,
                            focusedLabelColor = MyatoGreen,
                            unfocusedBorderColor = MyatoBorder,
                            focusedTextColor = MyatoWhite,
                            unfocusedTextColor = MyatoWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("new_playlist_name_input")
                    )

                    Button(
                        onClick = {
                            if (newPlaylistName.isNotBlank()) {
                                onCreateNewPlaylist(newPlaylistName.trim())
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground),
                        modifier = Modifier.fillMaxWidth().testTag("confirm_create_playlist_btn")
                    ) {
                        Text("Criar e Adicionar")
                    }
                } else {
                    OutlinedButton(
                        onClick = { showCreateField = true },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MyatoGreen),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Nova Playlist")
                    }

                    if (playlists.isEmpty()) {
                        Text(
                            text = "Nenhuma playlist criada ainda.",
                            color = MyatoGrayMedium,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                            itemsIndexed(playlists) { _, playlist ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            onSelectPlaylist(playlist.id)
                                            onDismiss()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QueueMusic,
                                        contentDescription = null,
                                        tint = MyatoGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = playlist.name,
                                        color = MyatoWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${playlist.songCount} músicas",
                                        color = MyatoGrayDark,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MyatoGrayLight)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomSheet(
    queue: List<Song>,
    currentIndex: Int,
    onDismiss: () -> Unit,
    onSelectIndex: (Int) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onClearQueue: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MyatoCard,
        contentColor = MyatoWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Fila de Reprodução",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MyatoWhite
                    )
                    Text(
                        text = "${queue.size} faixas na fila",
                        color = MyatoGrayMedium,
                        fontSize = 12.sp
                    )
                }

                if (queue.isNotEmpty()) {
                    TextButton(onClick = onClearQueue) {
                        Text("Limpar Fila", color = MyatoError, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (queue.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("A fila está vazia", color = MyatoGrayMedium)
                }
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 380.dp)) {
                    itemsIndexed(queue) { index, song ->
                        val isCurrent = index == currentIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrent) MyatoSurfaceVariant else Color.Transparent)
                                .clickable { onSelectIndex(index) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = if (isCurrent) MyatoGreen else MyatoGrayDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(28.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    color = if (isCurrent) MyatoGreen else MyatoWhite,
                                    fontSize = 14.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = song.artist,
                                    color = MyatoGrayMedium,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = { onRemoveItem(index) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remover",
                                    tint = MyatoGrayDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SleepTimerDialog(
    sleepTimerState: SleepTimerConfig,
    onDismiss: () -> Unit,
    onStartTimer: (Int) -> Unit,
    onSetStopAfterTrack: (Boolean) -> Unit,
    onCancelTimer: () -> Unit
) {
    val presets = listOf(5, 10, 15, 30, 45, 60)
    var customMinutesText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MyatoCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = MyatoGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Temporizador de Desligamento",
                    color = MyatoWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (sleepTimerState.isRunning) {
                    val remSeconds = sleepTimerState.remainingSeconds
                    val mins = remSeconds / 60
                    val secs = remSeconds % 60
                    val displayText = if (sleepTimerState.stopAfterCurrentTrack) {
                        "Desligando ao terminar a música atual"
                    } else {
                        "Desligando em %02d:%02d".format(mins, secs)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MyatoSurfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.HourglassBottom, null, tint = MyatoGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = displayText,
                                color = MyatoWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onCancelTimer()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MyatoError),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar Temporizador")
                    }
                } else {
                    Text(
                        text = "Selecione o tempo para pausar a reprodução:",
                        color = MyatoGrayLight,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Presets chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            presets.take(3).forEach { m ->
                                SuggestionChip(
                                    onClick = {
                                        onStartTimer(m)
                                        onDismiss()
                                    },
                                    label = { Text("${m} min", color = MyatoWhite) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            presets.drop(3).forEach { m ->
                                SuggestionChip(
                                    onClick = {
                                        onStartTimer(m)
                                        onDismiss()
                                    },
                                    label = { Text("${m} min", color = MyatoWhite) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stop after track option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onSetStopAfterTrack(true)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.StopCircle, null, tint = MyatoGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Parar após a música atual",
                            color = MyatoWhite,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Custom minutes input
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customMinutesText,
                            onValueChange = { customMinutesText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Minutos") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MyatoGreen,
                                focusedTextColor = MyatoWhite,
                                unfocusedTextColor = MyatoWhite
                            )
                        )
                        Button(
                            onClick = {
                                val mins = customMinutesText.toIntOrNull()
                                if (mins != null && mins > 0) {
                                    onStartTimer(mins)
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground)
                        ) {
                            Text("Iniciar")
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = MyatoGrayLight)
            }
        }
    )
}

// Musicolet Bookmarks Bottom Sheet (Section 6)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksBottomSheet(
    bookmarks: List<com.example.model.SongBookmark>,
    currentPositionMs: Long,
    onDismiss: () -> Unit,
    onJumpTo: (Long) -> Unit,
    onAddBookmark: (String, String, Long) -> Unit,
    onDeleteBookmark: (Long) -> Unit
) {
    var showAddFields by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MyatoCard,
        contentColor = MyatoWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Marcadores e Anotações", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MyatoWhite)
                    Text("Posições salvas nesta faixa", color = MyatoGrayMedium, fontSize = 12.sp)
                }

                IconButton(onClick = { showAddFields = !showAddFields }) {
                    Icon(
                        imageVector = if (showAddFields) Icons.Default.Close else Icons.Default.BookmarkAdd,
                        contentDescription = "Novo Marcador",
                        tint = MyatoGreen
                    )
                }
            }

            if (showAddFields) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MyatoSurfaceVariant)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Novo Marcador em ${Song(0, "", durationMs = currentPositionMs).formattedDuration}",
                            color = MyatoGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            placeholder = { Text("Nome (ex: Solo de Guitarra)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MyatoGreen,
                                focusedTextColor = MyatoWhite,
                                unfocusedTextColor = MyatoWhite
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = noteInput,
                            onValueChange = { noteInput = it },
                            placeholder = { Text("Anotação / Observação (opcional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MyatoGreen,
                                focusedTextColor = MyatoWhite,
                                unfocusedTextColor = MyatoWhite
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onAddBookmark(nameInput, noteInput, currentPositionMs)
                                nameInput = ""
                                noteInput = ""
                                showAddFields = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Salvar Marcador")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (bookmarks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhum marcador salvo ainda nesta música", color = MyatoGrayMedium, fontSize = 13.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                    itemsIndexed(bookmarks) { _, b ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onJumpTo(b.positionMs)
                                    onDismiss()
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MyatoGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = b.formattedPosition,
                                    color = MyatoGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(b.name, color = MyatoWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                if (b.note.isNotBlank()) {
                                    Text(b.note, color = MyatoGrayMedium, fontSize = 11.sp)
                                }
                            }
                            IconButton(onClick = { onDeleteBookmark(b.id) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, null, tint = MyatoError, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Musicolet Speed & Pitch Dialog (Section 9)
@Composable
fun SpeedPitchDialog(
    speed: Float,
    pitch: Float,
    onDismiss: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onReset: () -> Unit
) {
    val speedPresets = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MyatoCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Speed, null, tint = MyatoGreen, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Velocidade e Tom (Pitch)", color = MyatoWhite, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Speed Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Velocidade:", color = MyatoWhite, fontSize = 13.sp)
                    Text(String.format("%.2fx", speed), color = MyatoGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Slider(
                    value = speed,
                    onValueChange = onSpeedChange,
                    valueRange = 0.25f..3.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = MyatoGreen,
                        activeTrackColor = MyatoGreen,
                        inactiveTrackColor = MyatoBorder
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    speedPresets.forEach { sp ->
                        FilledTonalButton(
                            onClick = { onSpeedChange(sp) },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (speed == sp) MyatoGreen else MyatoSurfaceVariant,
                                contentColor = if (speed == sp) MyatoBackground else MyatoWhite
                            )
                        ) {
                            Text("${sp}x", fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pitch Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tom (Pitch):", color = MyatoWhite, fontSize = 13.sp)
                    Text(String.format("%.2fx", pitch), color = MyatoGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Slider(
                    value = pitch,
                    onValueChange = onPitchChange,
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = MyatoGreen,
                        activeTrackColor = MyatoGreen,
                        inactiveTrackColor = MyatoBorder
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MyatoWhite),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Restaurar Velocidade Normal (1.0x)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground)
            ) {
                Text("OK")
            }
        }
    )
}

// Musicolet Share Card Dialog (Section 16)
@Composable
fun ShareNowPlayingDialog(
    song: Song?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    if (song == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MyatoCard,
        title = {
            Text("Compartilhar Mídia", color = MyatoWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MyatoSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MyatoGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.GraphicEq, null, tint = MyatoGreen, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(song.title, color = MyatoWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(song.artist, color = MyatoGrayLight, fontSize = 13.sp)
                        Text(song.album, color = MyatoGrayMedium, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ouvindo no Myato-Player 2.1 🎵", color = MyatoGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val shareText = "🎵 Ouvindo \"${song.title}\" por ${song.artist} (${song.album}) no Myato-Player 2.1!\nBaixe o app oficial em: https://github.com/myatodevolper-lab/Myato-Player"
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Ouvindo no Myato-Player")
                        putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, "Compartilhar Mídia"))
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground)
            ) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Compartilhar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = MyatoGrayLight)
            }
        }
    )
}

