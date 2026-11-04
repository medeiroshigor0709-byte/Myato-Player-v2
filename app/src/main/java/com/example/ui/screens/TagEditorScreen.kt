package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Song
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagEditorScreen(
    song: Song?,
    batchSongIds: List<Long>,
    onBack: () -> Unit,
    onSaveSongTags: (Song) -> Unit,
    onBatchSaveTags: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isBatchMode = batchSongIds.isNotEmpty() && song == null

    var title by remember { mutableStateOf(song?.title ?: "") }
    var artist by remember { mutableStateOf(song?.artist ?: "") }
    var albumArtist by remember { mutableStateOf(song?.albumArtist ?: "") }
    var album by remember { mutableStateOf(song?.album ?: "") }
    var genre by remember { mutableStateOf(song?.genre ?: "") }
    var year by remember { mutableStateOf(song?.year ?: "") }
    var trackNumber by remember { mutableStateOf(song?.trackNumber ?: "") }
    var discNumber by remember { mutableStateOf(song?.discNumber ?: "1") }
    var composer by remember { mutableStateOf(song?.composer ?: "") }
    var comments by remember { mutableStateOf(song?.comments ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isBatchMode) "Edição em Lote (${batchSongIds.size} músicas)" else "Editor de Metadados",
                            color = MyatoWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBatchMode) "Altere campos em comum" else (song?.title ?: ""),
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
                    TextButton(
                        onClick = {
                            if (isBatchMode) {
                                onBatchSaveTags(artist, album, genre)
                            } else if (song != null) {
                                val updated = song.copy(
                                    title = title.trim().ifEmpty { song.title },
                                    artist = artist.trim().ifEmpty { song.artist },
                                    albumArtist = albumArtist.trim(),
                                    album = album.trim().ifEmpty { song.album },
                                    genre = genre.trim().ifEmpty { "Geral" },
                                    year = year.trim(),
                                    trackNumber = trackNumber.trim(),
                                    discNumber = discNumber.trim().ifEmpty { "1" },
                                    composer = composer.trim(),
                                    comments = comments.trim()
                                )
                                onSaveSongTags(updated)
                            }
                        },
                        modifier = Modifier.testTag("save_tags_btn")
                    ) {
                        Text("SALVAR", color = MyatoGreen, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MyatoBackground)
            )
        },
        containerColor = MyatoBackground,
        modifier = modifier.testTag("tag_editor_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Notice Box
            Card(
                colors = CardDefaults.cardColors(containerColor = MyatoSurfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EditNote, null, tint = MyatoGreen, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBatchMode)
                            "Os campos preenchidos serão aplicados simultaneamente a todas as ${batchSongIds.size} músicas selecionadas."
                        else "As alterações são sincronizadas e salvas com segurança no banco de dados da sua biblioteca.",
                        color = MyatoWhite,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isBatchMode) {
                TagInputField(label = "Título da Música", value = title, onValueChange = { title = it })
                Spacer(modifier = Modifier.height(10.dp))
            }

            TagInputField(label = "Artista", value = artist, onValueChange = { artist = it })
            Spacer(modifier = Modifier.height(10.dp))

            TagInputField(label = "Álbum", value = album, onValueChange = { album = it })
            Spacer(modifier = Modifier.height(10.dp))

            TagInputField(label = "Artista do Álbum", value = albumArtist, onValueChange = { albumArtist = it })
            Spacer(modifier = Modifier.height(10.dp))

            TagInputField(label = "Gênero Musical", value = genre, onValueChange = { genre = it })
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TagInputField(label = "Ano", value = year, onValueChange = { year = it }, modifier = Modifier.weight(1f))
                TagInputField(label = "Nº Faixa", value = trackNumber, onValueChange = { trackNumber = it }, modifier = Modifier.weight(1f))
                TagInputField(label = "Nº Disco", value = discNumber, onValueChange = { discNumber = it }, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))

            TagInputField(label = "Compositor", value = composer, onValueChange = { composer = it })
            Spacer(modifier = Modifier.height(10.dp))

            TagInputField(label = "Comentários", value = comments, onValueChange = { comments = it })
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isBatchMode) {
                        onBatchSaveTags(artist, album, genre)
                    } else if (song != null) {
                        val updated = song.copy(
                            title = title.trim().ifEmpty { song.title },
                            artist = artist.trim().ifEmpty { song.artist },
                            albumArtist = albumArtist.trim(),
                            album = album.trim().ifEmpty { song.album },
                            genre = genre.trim().ifEmpty { "Geral" },
                            year = year.trim(),
                            trackNumber = trackNumber.trim(),
                            discNumber = discNumber.trim().ifEmpty { "1" },
                            composer = composer.trim(),
                            comments = comments.trim()
                        )
                        onSaveSongTags(updated)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("save_tags_bottom_btn")
            ) {
                Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salvar Alterações de Metadados", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun TagInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = MyatoGrayLight) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MyatoGreen,
            focusedLabelColor = MyatoGreen,
            unfocusedBorderColor = MyatoBorder,
            focusedTextColor = MyatoWhite,
            unfocusedTextColor = MyatoWhite
        ),
        modifier = modifier.fillMaxWidth()
    )
}
