package com.example.ui.screens

import android.content.Context
import android.media.AudioManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RepeatMode
import com.example.model.Song
import com.example.ui.theme.*

@Composable
fun PlayerScreen(
    song: Song?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    repeatMode: RepeatMode,
    isShuffle: Boolean,
    activeQueueName: String,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenQueuesManager: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenSpeedPitch: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenTagEditor: (Song) -> Unit,
    onShareSong: (Song) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVol = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat() }
    var currentVol by remember {
        mutableStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat())
    }

    var isAlbumArtZoomed by remember { mutableStateOf(false) }

    // Vinyl rotation animation when playing
    val infiniteTransition = rememberInfiniteTransition(label = "player_rot")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulse scale for visualizer beats
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MyatoSurfaceVariant,
                        MyatoBackground,
                        MyatoBackground
                    )
                )
            )
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
            .testTag("player_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Bar: Dismiss down arrow, Queue Title, Tag Edit & More
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose, modifier = Modifier.testTag("player_close_btn")) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Fechar", tint = MyatoWhite, modifier = Modifier.size(32.dp))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onOpenQueuesManager() }
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = activeQueueName.uppercase(),
                        color = MyatoGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.SwapHoriz, null, tint = MyatoGreen, modifier = Modifier.size(14.dp))
                }
                Text(
                    text = song?.album ?: "Myato-Player 2.1",
                    color = MyatoGrayLight,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row {
                IconButton(onClick = { song?.let { onOpenTagEditor(it) } }) {
                    Icon(Icons.Default.EditNote, contentDescription = "Editor de Tags", tint = MyatoWhite)
                }
                IconButton(onClick = onOpenQueue, modifier = Modifier.testTag("player_queue_btn")) {
                    Icon(Icons.Default.QueueMusic, contentDescription = "Fila", tint = MyatoWhite)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Album Art in Vinyl Frame with Tap to Zoom (Section 18)
        Box(
            modifier = Modifier
                .size(if (isAlbumArtZoomed) 300.dp else 240.dp)
                .scale(if (isPlaying) pulse else 1f)
                .clip(CircleShape)
                .clickable { isAlbumArtZoomed = !isAlbumArtZoomed }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MyatoSurfaceVariant,
                            MyatoCard,
                            MyatoBackground
                        )
                    )
                )
                .rotate(if (isPlaying) rotation else 0f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(if (isAlbumArtZoomed) 280.dp else 220.dp)
                    .clip(CircleShape)
                    .background(MyatoCard),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(MyatoGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = MyatoGreen,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Animated Audio Waveform Visualizer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val barHeights = listOf(10.dp, 20.dp, 16.dp, 24.dp, 12.dp, 22.dp, 18.dp, 14.dp, 20.dp, 8.dp)
            barHeights.forEachIndexed { idx, ht ->
                val animatedHeight by infiniteTransition.animateFloat(
                    initialValue = 4f,
                    targetValue = ht.value,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 350 + (idx * 50), easing = LinearEasing),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                    ),
                    label = "waveform_$idx"
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .width(4.dp)
                        .height(if (isPlaying) animatedHeight.dp else 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MyatoGreen)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Song Title, Artist and Favorite Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song?.title ?: "Nenhuma música",
                    color = MyatoWhite,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${song?.artist ?: "Selecione uma faixa"} • Disco ${song?.discNumber ?: "1"}",
                    color = MyatoGrayMedium,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row {
                IconButton(onClick = { song?.let { onShareSong(it) } }) {
                    Icon(Icons.Default.Share, contentDescription = "Compartilhar", tint = MyatoGrayLight, modifier = Modifier.size(22.dp))
                }
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.testTag("player_favorite_btn")
                ) {
                    Icon(
                        imageVector = if (song?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favoritar",
                        tint = if (song?.isFavorite == true) MyatoGreen else MyatoGrayLight,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress Slider & Timestamps
        val progressRatio = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
        var isSeeking by remember { mutableStateOf(false) }
        var seekPosition by remember { mutableFloatStateOf(0f) }

        Slider(
            value = if (isSeeking) seekPosition else progressRatio,
            onValueChange = {
                isSeeking = true
                seekPosition = it
            },
            onValueChangeFinished = {
                isSeeking = false
                val newPos = (seekPosition * durationMs).toLong()
                onSeek(newPos)
            },
            colors = SliderDefaults.colors(
                thumbColor = MyatoGreen,
                activeTrackColor = MyatoGreen,
                inactiveTrackColor = MyatoBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("player_progress_slider")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val curSec = currentPositionMs / 1000
            val durSec = durationMs / 1000
            Text(
                text = "%d:%02d".format(curSec / 60, curSec % 60),
                color = MyatoGrayMedium,
                fontSize = 12.sp
            )
            Text(
                text = "%d:%02d".format(durSec / 60, durSec % 60),
                color = MyatoGrayMedium,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Controls: Shuffle, Previous, Play/Pause, Next, Repeat
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleShuffle, modifier = Modifier.testTag("player_shuffle_btn")) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Aleatório",
                    tint = if (isShuffle) MyatoGreen else MyatoGrayLight
                )
            }

            IconButton(
                onClick = onPrevious,
                modifier = Modifier
                    .size(46.dp)
                    .testTag("player_previous_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Anterior",
                    tint = MyatoWhite,
                    modifier = Modifier.size(34.dp)
                )
            }

            FilledIconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(64.dp)
                    .testTag("player_main_play_pause"),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MyatoGreen,
                    contentColor = MyatoBackground
                )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier
                    .size(46.dp)
                    .testTag("player_next_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Próxima",
                    tint = MyatoWhite,
                    modifier = Modifier.size(34.dp)
                )
            }

            IconButton(onClick = onToggleRepeat, modifier = Modifier.testTag("player_repeat_btn")) {
                Icon(
                    imageVector = when (repeatMode) {
                        RepeatMode.OFF -> Icons.Default.Repeat
                        RepeatMode.ALL -> Icons.Default.Repeat
                        RepeatMode.ONE -> Icons.Default.RepeatOne
                    },
                    contentDescription = "Repetir",
                    tint = if (repeatMode != RepeatMode.OFF) MyatoGreen else MyatoGrayLight
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Volume control slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.VolumeDown, null, tint = MyatoGrayMedium, modifier = Modifier.size(18.dp))
            Slider(
                value = currentVol,
                onValueChange = {
                    currentVol = it
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, it.toInt(), 0)
                },
                valueRange = 0f..maxVol,
                colors = SliderDefaults.colors(
                    thumbColor = MyatoGrayLight,
                    activeTrackColor = MyatoGrayMedium,
                    inactiveTrackColor = MyatoBorder
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
            Icon(Icons.Default.VolumeUp, null, tint = MyatoGrayMedium, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Musicolet Advanced Toolbelt: Letras, Marcadores, Velocidade/Tom, Filas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                selected = false,
                onClick = onOpenLyrics,
                leadingIcon = { Icon(Icons.Default.Mic, null, tint = MyatoGreen, modifier = Modifier.size(16.dp)) },
                label = { Text("Letras", fontSize = 11.sp, color = MyatoWhite) },
                colors = FilterChipDefaults.filterChipColors(containerColor = MyatoCard)
            )

            FilterChip(
                selected = false,
                onClick = onOpenBookmarks,
                leadingIcon = { Icon(Icons.Default.Bookmark, null, tint = MyatoGreen, modifier = Modifier.size(16.dp)) },
                label = { Text("Marcadores", fontSize = 11.sp, color = MyatoWhite) },
                colors = FilterChipDefaults.filterChipColors(containerColor = MyatoCard)
            )

            FilterChip(
                selected = false,
                onClick = onOpenSpeedPitch,
                leadingIcon = { Icon(Icons.Default.Speed, null, tint = MyatoGreen, modifier = Modifier.size(16.dp)) },
                label = { Text("Velocidade", fontSize = 11.sp, color = MyatoWhite) },
                colors = FilterChipDefaults.filterChipColors(containerColor = MyatoCard)
            )

            FilterChip(
                selected = false,
                onClick = onOpenQueuesManager,
                leadingIcon = { Icon(Icons.Default.Layers, null, tint = MyatoGreen, modifier = Modifier.size(16.dp)) },
                label = { Text("20 Filas", fontSize = 11.sp, color = MyatoWhite) },
                colors = FilterChipDefaults.filterChipColors(containerColor = MyatoCard)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom row: Equalizer and Sleep Timer shortcuts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = onOpenEqualizer,
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MyatoWhite),
                modifier = Modifier.testTag("player_equalizer_shortcut")
            ) {
                Icon(Icons.Default.Tune, null, tint = MyatoGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Equalizador", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onOpenSleepTimer,
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MyatoWhite),
                modifier = Modifier.testTag("player_timer_shortcut")
            ) {
                Icon(Icons.Default.Timer, null, tint = MyatoGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Temporizador", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
