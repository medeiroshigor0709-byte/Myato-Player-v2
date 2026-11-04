package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlaybackQueue
import com.example.ui.theme.*

@Composable
fun MultiSelectionBottomBar(
    isVisible: Boolean,
    selectedCount: Int,
    closeSelectionOnAction: Boolean,
    onToggleCloseOnAction: (Boolean) -> Unit,
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
                // Header status & close
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

                // Checkbox: "Fechar processo de seleção após uma opção ser selecionada"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleCloseOnAction(!closeSelectionOnAction) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = closeSelectionOnAction,
                        onCheckedChange = { onToggleCloseOnAction(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MyatoGreen,
                            checkmarkColor = MyatoBackground,
                            uncheckedColor = MyatoGrayMedium
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Fechar seleção após executar ação",
                        color = MyatoGrayLight,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // The 2 primary trigger buttons: Opções de seleção & Ações em lote
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botão 1: Seleção Inteligente
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
                        Text("Seleção", fontSize = 13.sp)
                    }

                    // Botão 2: Ações em massa
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
    onInvertSelection: () -> Unit,
    onSelectSameArtist: () -> Unit,
    onSelectSameAlbum: () -> Unit
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
                text = "Seleção Inteligente",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MyatoGreen,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            ListItem(
                headlineContent = { Text("1. Selecionar todas da lista", color = MyatoWhite) },
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
                headlineContent = { Text("2. Desmarcar todas da lista", color = MyatoWhite) },
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
                headlineContent = { Text("3. Selecionar intervalo (entre a primeira e última)", color = MyatoWhite) },
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

            HorizontalDivider(color = MyatoBorder)

            ListItem(
                headlineContent = { Text("5. Selecionar músicas do mesmo artista", color = MyatoWhite) },
                leadingContent = { Icon(Icons.Default.Person, null, tint = MyatoGreen) },
                modifier = Modifier
                    .clickable {
                        onSelectSameArtist()
                        onDismiss()
                    },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            HorizontalDivider(color = MyatoBorder)

            ListItem(
                headlineContent = { Text("6. Selecionar músicas do mesmo álbum", color = MyatoWhite) },
                leadingContent = { Icon(Icons.Default.Album, null, tint = MyatoGreen) },
                modifier = Modifier
                    .clickable {
                        onSelectSameAlbum()
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
    queues: List<PlaybackQueue>,
    onDismiss: () -> Unit,
    onPlaySelected: () -> Unit,
    onShufflePlaySelected: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToCurrentQueue: () -> Unit,
    onAddToSpecificQueue: (Long) -> Unit,
    onRemoveFromQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onFavoriteAll: () -> Unit,
    onUnfavoriteAll: () -> Unit,
    onBatchEditTags: () -> Unit,
    onMoveToFolder: () -> Unit,
    onCopyToFolder: () -> Unit,
    onSpeedPitch: () -> Unit,
    onShare: () -> Unit,
    onClearHistory: () -> Unit,
    onDeleteSelected: () -> Unit
) {
    if (!isVisible) return

    var showQueuePicker by remember { mutableStateOf(false) }

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
            Text(
                text = "Ações para $selectedCount músicas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MyatoGreen,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // 1. Tocar músicas selecionadas
                item {
                    BatchActionItem("Tocar Músicas Selecionadas", Icons.Default.PlayArrow, MyatoGreen) {
                        onPlaySelected()
                        onDismiss()
                    }
                }
                // 2. Colocar em ordem aleatória e tocar
                item {
                    BatchActionItem("Ordem Aleatória e Tocar", Icons.Default.Shuffle, MyatoGreen) {
                        onShufflePlaySelected()
                        onDismiss()
                    }
                }
                // 3. Tocar depois da música atual
                item {
                    BatchActionItem("Tocar Depois da Atual", Icons.Default.SkipNext, MyatoGreen) {
                        onPlayNext()
                        onDismiss()
                    }
                }
                // 4. Adicionar à fila que está tocando agora
                item {
                    BatchActionItem("Adicionar à Fila Atual", Icons.Default.QueueMusic, MyatoGreen) {
                        onAddToCurrentQueue()
                        onDismiss()
                    }
                }
                // 5. Adicionar a uma fila específica
                item {
                    BatchActionItem("Adicionar a uma Fila Específica (1..20)", Icons.Default.Layers, MyatoGreen) {
                        showQueuePicker = true
                    }
                }
                // 6. Remover dessa fila
                item {
                    BatchActionItem("Remover da Fila", Icons.Default.DeleteSweep, MyatoError) {
                        onRemoveFromQueue()
                        onDismiss()
                    }
                }
                // 7. Adicionar às playlists
                item {
                    BatchActionItem("Adicionar às Playlists", Icons.Default.PlaylistAdd, MyatoWhite) {
                        onAddToPlaylist()
                        onDismiss()
                    }
                }
                // 8. Adicionar às favoritas
                item {
                    BatchActionItem("Adicionar às Favoritas", Icons.Default.Favorite, MyatoGreen) {
                        onFavoriteAll()
                        onDismiss()
                    }
                }
                // 9. Remover das favoritas
                item {
                    BatchActionItem("Remover das Favoritas", Icons.Default.FavoriteBorder, MyatoGrayLight) {
                        onUnfavoriteAll()
                        onDismiss()
                    }
                }
                // 10. Editar tags
                item {
                    BatchActionItem("Editar Tags em Lote", Icons.Default.EditNote, MyatoWhite) {
                        onBatchEditTags()
                        onDismiss()
                    }
                }
                // 11. Mover para uma pasta
                item {
                    BatchActionItem("Mover para uma Pasta", Icons.Default.DriveFileMove, MyatoWhite) {
                        onMoveToFolder()
                        onDismiss()
                    }
                }
                // 12. Copiar para uma pasta
                item {
                    BatchActionItem("Copiar para uma Pasta", Icons.Default.ContentCopy, MyatoWhite) {
                        onCopyToFolder()
                        onDismiss()
                    }
                }
                // 13. Velocidade e tom
                item {
                    BatchActionItem("Velocidade e Tom de Reprodução", Icons.Default.Speed, MyatoWhite) {
                        onSpeedPitch()
                        onDismiss()
                    }
                }
                // 14. Compartilhar
                item {
                    BatchActionItem("Compartilhar", Icons.Default.Share, MyatoWhite) {
                        onShare()
                        onDismiss()
                    }
                }
                // 15. Limpar histórico de reprodução
                item {
                    BatchActionItem("Limpar do Histórico", Icons.Default.History, MyatoWhite) {
                        onClearHistory()
                        onDismiss()
                    }
                }
                // 16. Apagar permanentemente
                item {
                    BatchActionItem("Apagar Permanentemente (Excluir Arquivos)", Icons.Default.DeleteForever, MyatoError) {
                        onDeleteSelected()
                        onDismiss()
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showQueuePicker) {
        AlertDialog(
            onDismissRequest = { showQueuePicker = false },
            containerColor = MyatoCard,
            title = { Text("Escolha a fila de destino", color = MyatoWhite, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp)) {
                    items(queues.size) { idx ->
                        val q = queues[idx]
                        ListItem(
                            headlineContent = { Text(q.name, color = MyatoWhite) },
                            supportingContent = { Text("${q.songs.size} faixas", color = MyatoGrayMedium) },
                            leadingContent = { Icon(Icons.Default.QueueMusic, null, tint = MyatoGreen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAddToSpecificQueue(q.id)
                                    showQueuePicker = false
                                    onDismiss()
                                },
                            colors = ListItemDefaults.colors(containerColor = MyatoCard)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showQueuePicker = false }) {
                    Text("Cancelar", color = MyatoGrayLight)
                }
            }
        )
    }
}

@Composable
private fun BatchActionItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title, color = MyatoWhite, fontSize = 14.sp) },
        leadingContent = { Icon(icon, null, tint = tint) },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
    HorizontalDivider(color = MyatoBorder.copy(alpha = 0.5f))
}
