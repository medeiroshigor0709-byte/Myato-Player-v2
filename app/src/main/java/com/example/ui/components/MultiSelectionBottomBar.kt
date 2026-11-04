package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun MultiSelectionBottomBar(
    isVisible: Boolean,
    selectedCount: Int,
    onOptionsClick: () -> Unit,
    onAdvanceClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("multi_selection_bottom_bar"),
            shape = RoundedCornerShape(20.dp),
            color = MyatoSurfaceVariant,
            tonalElevation = 10.dp,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MyatoGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$selectedCount selecionada${if (selectedCount == 1) "" else "s"}",
                            color = MyatoWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    TextButton(
                        onClick = onCancelClick,
                        modifier = Modifier.testTag("selection_cancel_btn")
                    ) {
                        Text("Cancelar", color = MyatoGrayLight, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // The 3 required buttons: Opções, Selecionar avançar, Cancelar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botão 1: Opções
                    OutlinedButton(
                        onClick = onOptionsClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("selection_options_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MyatoWhite),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MyatoGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Opções", fontSize = 13.sp)
                    }

                    // Botão 2: Selecionar avançar (Ações em massa)
                    Button(
                        onClick = onAdvanceClick,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("selection_advance_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MyatoGreen,
                            contentColor = MyatoBackground
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Ações ($selectedCount)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionOptionsMenuSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onSelectRange: () -> Unit,
    onInvertSelection: () -> Unit
) {
    if (!isVisible) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MyatoCard,
        contentColor = MyatoWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Opções de Seleção Múltipla",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MyatoGreen,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ListItem(
                headlineContent = { Text("1. Selecionar todas a partir desta lista", color = MyatoWhite) },
                leadingContent = { Icon(Icons.Default.SelectAll, null, tint = MyatoGreen) },
                modifier = Modifier
                    .testTag("opt_select_all")
                    .clickable {
                        onSelectAll()
                        onDismiss()
                    },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            HorizontalDivider(color = MyatoBorder)

            ListItem(
                headlineContent = { Text("2. Desmarcar todas a partir desta lista", color = MyatoWhite) },
                leadingContent = { Icon(Icons.Default.Deselect, null, tint = MyatoGrayLight) },
                modifier = Modifier
                    .testTag("opt_deselect_all")
                    .clickable {
                        onDeselectAll()
                        onDismiss()
                    },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            HorizontalDivider(color = MyatoBorder)

            ListItem(
                headlineContent = { Text("3. Selecionar músicas entre a primeira e a última", color = MyatoWhite) },
                leadingContent = { Icon(Icons.Default.LinearScale, null, tint = MyatoGreen) },
                modifier = Modifier
                    .testTag("opt_select_range")
                    .clickable {
                        onSelectRange()
                        onDismiss()
                    },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            HorizontalDivider(color = MyatoBorder)

            ListItem(
                headlineContent = { Text("4. Inverter seleção", color = MyatoWhite) },
                leadingContent = { Icon(Icons.Default.SwapCalls, null, tint = MyatoGreen) },
                modifier = Modifier
                    .testTag("opt_invert_selection")
                    .clickable {
                        onInvertSelection()
                        onDismiss()
                    },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchActionsSheet(
    isVisible: Boolean,
    selectedCount: Int,
    onDismiss: () -> Unit,
    onPlaySelected: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onCreatePlaylist: () -> Unit,
    onFavoriteAll: () -> Unit,
    onDeleteSelected: () -> Unit
) {
    if (!isVisible) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MyatoCard,
        contentColor = MyatoWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Ações para $selectedCount músicas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MyatoWhite,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ListItem(
                headlineContent = { Text("Reproduzir Agora", color = MyatoWhite) },
                leadingContent = { Icon(Icons.Default.PlayArrow, null, tint = MyatoGreen) },
                modifier = Modifier
                    .testTag("action_play_selected")
                    .clickable {
                        onPlaySelected()
                        onDismiss()
                    },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            ListItem(
                headlineContent = { Text("Adicionar à Fila", color = MyatoWhite) },
                leadingContent = { Icon(Icons.Default.QueueMusic, null, tint = MyatoGreen) },
                modifier = Modifier
                    .testTag("action_queue_selected")
                    .clickable {
                        onAddToQueue()
                        onDismiss()
                    },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            ListItem(
                headlineContent = { Text("Adicionar a uma Playlist Existente", color = MyatoWhite) },
                leadingContent = { Icon(Icons.Default.PlaylistAdd, null, tint = MyatoGreen) },
                modifier = Modifier
                    .testTag("action_playlist_selected")
                    .clickable {
                        onAddToPlaylist()
                        onDismiss()
                    },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            ListItem(
                headlineContent = { Text("Criar Nova Playlist com a Seleção", color = MyatoWhite) },
                leadingContent = { Icon(Icons.Default.Add, null, tint = MyatoGreen) },
                modifier = Modifier
                    .testTag("action_new_playlist_selected")
                    .clickable {
                        onCreatePlaylist()
                        onDismiss()
                    },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            ListItem(
                headlineContent = { Text("Adicionar às Favoritas", color = MyatoWhite) },
                leadingContent = { Icon(Icons.Default.Favorite, null, tint = MyatoGreen) },
                modifier = Modifier
                    .testTag("action_fav_selected")
                    .clickable {
                        onFavoriteAll()
                        onDismiss()
                    },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            ListItem(
                headlineContent = { Text("Excluir Selecionadas da Biblioteca", color = MyatoError) },
                leadingContent = { Icon(Icons.Default.Delete, null, tint = MyatoError) },
                modifier = Modifier
                    .testTag("action_delete_selected")
                    .clickable {
                        onDeleteSelected()
                        onDismiss()
                    },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
