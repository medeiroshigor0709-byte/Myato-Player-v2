package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Song
import com.example.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongListItem(
    song: Song,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    isPlayingCurrent: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDelete: () -> Unit,
    onEditTags: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MyatoSelectionBg
            isPlayingCurrent -> MyatoSurfaceVariant.copy(alpha = 0.7f)
            else -> Color.Transparent
        },
        label = "item_bg"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("song_item_${song.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection Checkbox or Leading Visualizer/Album Art
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MyatoGreen,
                    checkmarkColor = MyatoBackground,
                    uncheckedColor = MyatoGrayMedium
                ),
                modifier = Modifier.padding(end = 6.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isPlayingCurrent) MyatoGreen.copy(alpha = 0.2f) else MyatoCard),
                contentAlignment = Alignment.Center
            ) {
                if (isPlayingCurrent) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "Tocando",
                        tint = MyatoGreen,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MyatoGrayLight,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        // Title and details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = if (isPlayingCurrent) MyatoGreen else MyatoWhite,
                fontSize = 15.sp,
                fontWeight = if (isPlayingCurrent) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = song.artist,
                    color = MyatoGrayMedium,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    text = " • ${song.formattedDuration}",
                    color = MyatoGrayDark,
                    fontSize = 12.sp
                )
            }
        }

        // Actions
        if (!isSelectionMode) {
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favoritar",
                    tint = if (song.isFavorite) MyatoGreen else MyatoGrayDark,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Mais opções",
                        tint = MyatoGrayMedium,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MyatoCard)
                ) {
                    DropdownMenuItem(
                        text = { Text("Adicionar à Fila", color = MyatoWhite) },
                        leadingIcon = { Icon(Icons.Default.QueueMusic, null, tint = MyatoGreen) },
                        onClick = {
                            showMenu = false
                            onAddToQueue()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Adicionar à Playlist", color = MyatoWhite) },
                        leadingIcon = { Icon(Icons.Default.PlaylistAdd, null, tint = MyatoGreen) },
                        onClick = {
                            showMenu = false
                            onAddToPlaylist()
                        }
                    )
                    onEditTags?.let { editTagsAction ->
                        DropdownMenuItem(
                            text = { Text("Editar Tags (ID3)", color = MyatoWhite) },
                            leadingIcon = { Icon(Icons.Default.Edit, null, tint = MyatoGreen) },
                            onClick = {
                                showMenu = false
                                editTagsAction()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Excluir Mídia", color = MyatoError) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MyatoError) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
