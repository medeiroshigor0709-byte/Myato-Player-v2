package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.downloads.ParsedMediaInfo
import com.example.model.DownloadItem
import com.example.model.DownloadStatus
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    downloads: List<DownloadItem>,
    inspectedMedia: ParsedMediaInfo?,
    isInspecting: Boolean,
    onInspectUrl: (String) -> Unit,
    onStartDownload: (ParsedMediaInfo) -> Unit,
    onClearInspected: () -> Unit,
    onRetryDownload: (Long) -> Unit,
    onCancelDownload: (Long) -> Unit,
    onCreatePlaylistFromYouTube: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var inputUrl by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MyatoBackground)
            .padding(horizontal = 16.dp)
            .testTag("downloads_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Downloads & Importação",
                color = MyatoWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Importe áudios diretos autorizados e consulte metadados de mídias",
                color = MyatoGrayMedium,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // URL Input Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MyatoCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Inserir Link de Áudio ou Playlist",
                        color = MyatoWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        placeholder = { Text("Cole o link aqui (.mp3, .wav, YouTube...)", color = MyatoGrayDark, fontSize = 13.sp) },
                        singleLine = true,
                        trailingIcon = {
                            if (inputUrl.isNotEmpty()) {
                                IconButton(onClick = { inputUrl = "" }) {
                                    Icon(Icons.Default.Close, null, tint = MyatoGrayLight)
                                }
                            } else {
                                IconButton(onClick = {
                                    val clip = clipboardManager.getText()?.text
                                    if (!clip.isNullOrEmpty()) inputUrl = clip
                                }) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = "Colar", tint = MyatoGreen)
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
                            .testTag("download_url_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onInspectUrl(inputUrl) },
                            enabled = inputUrl.isNotBlank() && !isInspecting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MyatoGreen,
                                contentColor = MyatoBackground
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("inspect_link_btn")
                        ) {
                            if (isInspecting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MyatoBackground,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Inspecionando...")
                            } else {
                                Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Inspecionar Link", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Inspected Media Card Result
        if (inspectedMedia != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MyatoSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("inspected_media_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (inspectedMedia.isYouTube) Color(0xFFFF0000).copy(alpha = 0.2f) else MyatoGreen.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (inspectedMedia.isYouTube) Icons.Default.PlayCircle else Icons.Default.AudioFile,
                                        contentDescription = null,
                                        tint = if (inspectedMedia.isYouTube) Color(0xFFFF4444) else MyatoGreen,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = inspectedMedia.title,
                                        color = MyatoWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${inspectedMedia.author} • ${inspectedMedia.format}",
                                        color = MyatoGrayLight,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            IconButton(onClick = onClearInspected) {
                                Icon(Icons.Default.Close, null, tint = MyatoGrayLight)
                            }
                        }

                        if (!inspectedMedia.notice.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MyatoCard),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, null, tint = MyatoInfo, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = inspectedMedia.notice,
                                        color = MyatoGrayLight,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons based on type
                        if (inspectedMedia.isDirectDownloadable) {
                            Button(
                                onClick = { onStartDownload(inspectedMedia) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MyatoGreen,
                                    contentColor = MyatoBackground
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("confirm_download_btn")
                            ) {
                                Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Baixar Arquivo (${inspectedMedia.format})", fontWeight = FontWeight.Bold)
                            }
                        } else if (inspectedMedia.isYouTube) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(inspectedMedia.originalUrl))
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFF0000),
                                        contentColor = MyatoWhite
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Abrir Conteúdo no YouTube", fontWeight = FontWeight.Bold)
                                }

                                if (inspectedMedia.isPlaylist) {
                                    OutlinedButton(
                                        onClick = { onCreatePlaylistFromYouTube(inspectedMedia.title) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MyatoGreen),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.PlaylistAdd, null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Importar como Playlist de Referência")
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Section: Gerenciador de Downloads
        item {
            Text(
                text = "Histórico & Gerenciador de Downloads",
                color = MyatoWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (downloads.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhum download registrado", color = MyatoGrayMedium, fontSize = 13.sp)
                }
            }
        } else {
            items(downloads) { dl ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MyatoCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dl.title,
                                    color = MyatoWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${dl.artist} • ${dl.format} • ${dl.source}",
                                    color = MyatoGrayMedium,
                                    fontSize = 11.sp
                                )
                            }

                            // Status Chip
                            val statusColor = when (dl.status) {
                                DownloadStatus.COMPLETED -> MyatoGreen
                                DownloadStatus.DOWNLOADING -> MyatoInfo
                                DownloadStatus.FAILED -> MyatoError
                                else -> MyatoGrayMedium
                            }
                            val statusText = when (dl.status) {
                                DownloadStatus.COMPLETED -> "Concluído"
                                DownloadStatus.DOWNLOADING -> "${dl.progress}%"
                                DownloadStatus.FAILED -> "Falha"
                                DownloadStatus.QUEUED -> "Na fila"
                                DownloadStatus.PAUSED -> "Pausado"
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = statusColor.copy(alpha = 0.15f),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = statusText,
                                    color = statusColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (dl.status == DownloadStatus.DOWNLOADING) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { (dl.progress / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                color = MyatoGreen,
                                trackColor = MyatoBorder
                            )
                        }

                        if (dl.status == DownloadStatus.FAILED && !dl.errorMsg.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = dl.errorMsg, color = MyatoError, fontSize = 11.sp)
                        }

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (dl.status == DownloadStatus.DOWNLOADING) {
                                TextButton(onClick = { onCancelDownload(dl.id) }) {
                                    Text("Cancelar", color = MyatoError, fontSize = 12.sp)
                                }
                            } else if (dl.status == DownloadStatus.FAILED) {
                                TextButton(onClick = { onRetryDownload(dl.id) }) {
                                    Text("Tentar Novamente", color = MyatoGreen, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
