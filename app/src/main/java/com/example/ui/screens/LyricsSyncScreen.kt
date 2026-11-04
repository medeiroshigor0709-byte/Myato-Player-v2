package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ABRepeatState
import com.example.model.LyricLine
import com.example.model.Song
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsSyncScreen(
    song: Song?,
    lyrics: List<LyricLine>,
    currentPositionMs: Long,
    isPlaying: Boolean,
    abRepeatState: ABRepeatState,
    onBack: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSaveLyrics: (Long, String) -> Unit,
    onSetPointA: (Long) -> Unit,
    onSetPointB: (Long) -> Unit,
    onClearABRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var isEditMode by remember { mutableStateOf(false) }
    var editableLyricsText by remember { mutableStateOf(song?.lyrics ?: "") }

    // Find active lyric index
    val activeIndex = remember(currentPositionMs, lyrics) {
        val idx = lyrics.indexOfLast { it.timestampMs <= currentPositionMs }
        if (idx >= 0) idx else 0
    }

    // Auto-scroll to active lyric line
    LaunchedEffect(activeIndex) {
        if (!isEditMode && lyrics.isNotEmpty() && activeIndex in lyrics.indices) {
            coroutineScope.launch {
                listState.animateScrollToItem(activeIndex.coerceAtLeast(0))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Letras Sincronizadas",
                            color = MyatoWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = song?.title ?: "Myato-Player",
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
                        onClick = {
                            val clipMgr = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Lyrics", song?.lyrics ?: "")
                            clipMgr.setPrimaryClip(clip)
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar Letras", tint = MyatoGrayLight)
                    }
                    IconButton(
                        onClick = {
                            if (isEditMode) {
                                if (song != null) {
                                    onSaveLyrics(song.id, editableLyricsText)
                                }
                                isEditMode = false
                            } else {
                                editableLyricsText = song?.lyrics ?: ""
                                isEditMode = true
                            }
                        },
                        modifier = Modifier.testTag("toggle_edit_lyrics_btn")
                    ) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditMode) "Salvar" else "Editar",
                            tint = MyatoGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MyatoBackground)
            )
        },
        containerColor = MyatoBackground,
        modifier = modifier.testTag("lyrics_sync_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // A-B Repeat Loop Bar (Section 5)
            Card(
                colors = CardDefaults.cardColors(containerColor = MyatoCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Repeat, null, tint = if (abRepeatState.isEnabled) MyatoGreen else MyatoGrayMedium, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Repetição A-B (Loop de Trecho)", color = MyatoWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            val aStr = abRepeatState.pointA?.let { Song(0, "", durationMs = it).formattedDuration } ?: "--:--"
                            val bStr = abRepeatState.pointB?.let { Song(0, "", durationMs = it).formattedDuration } ?: "--:--"
                            Text("Ponto A: $aStr • Ponto B: $bStr", color = if (abRepeatState.isEnabled) MyatoGreen else MyatoGrayMedium, fontSize = 11.sp)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilledTonalButton(
                            onClick = { onSetPointA(currentPositionMs) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = MyatoSurfaceVariant)
                        ) {
                            Text("Ponto A", fontSize = 11.sp)
                        }
                        FilledTonalButton(
                            onClick = { onSetPointB(currentPositionMs) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = MyatoSurfaceVariant)
                        ) {
                            Text("Ponto B", fontSize = 11.sp)
                        }
                        if (abRepeatState.isEnabled) {
                            IconButton(onClick = onClearABRepeat, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Close, null, tint = MyatoError, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isEditMode) {
                // LRC Raw Editor
                Text(
                    text = "Editor LRC: Ajuste os tempos [mm:ss.xx] ou toque em 'Carimbar Posição Atual'",
                    color = MyatoGrayLight,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Button(
                    onClick = {
                        val curSec = currentPositionMs / 1000
                        val min = curSec / 60
                        val sec = curSec % 60
                        val ms = (currentPositionMs % 1000) / 10
                        val stamp = String.format("[%02d:%02d.%02d] Nova Linha\n", min, sec, ms)
                        editableLyricsText = editableLyricsText + "\n" + stamp
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MyatoSurfaceVariant, contentColor = MyatoGreen),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Carimbar Posição Atual (${Song(0, "", durationMs = currentPositionMs).formattedDuration})")
                }

                OutlinedTextField(
                    value = editableLyricsText,
                    onValueChange = { editableLyricsText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MyatoGreen,
                        focusedTextColor = MyatoWhite,
                        unfocusedTextColor = MyatoWhite
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (song != null) onSaveLyrics(song.id, editableLyricsText)
                        isEditMode = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Salvar Letras (.LRC)", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                // Synchronized Lines Display
                if (lyrics.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.MicNone, null, tint = MyatoGrayDark, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Nenhuma letra disponível para esta música", color = MyatoGrayMedium, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { isEditMode = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MyatoGreen)
                            ) {
                                Text("Adicionar Letras / LRC")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        itemsIndexed(lyrics) { index, line ->
                            val isActive = index == activeIndex

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isActive) MyatoGreen.copy(alpha = 0.18f) else Color.Transparent)
                                    .clickable {
                                        if (line.timestampMs >= 0) {
                                            onSeekTo(line.timestampMs)
                                        }
                                    }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = line.text,
                                    color = if (isActive) MyatoGreen else MyatoGrayMedium,
                                    fontSize = if (isActive) 18.sp else 15.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
