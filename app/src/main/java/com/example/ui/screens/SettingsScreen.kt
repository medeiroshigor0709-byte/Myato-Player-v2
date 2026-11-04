package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Screen
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigate: (Screen) -> Unit,
    onRescanLibrary: () -> Unit,
    onExportBackup: ((String) -> Unit) -> Unit,
    onRestoreBackup: (String, (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showBackupDialog by remember { mutableStateOf(false) }
    var backupJsonText by remember { mutableStateOf("") }
    var isRestoring by remember { mutableStateOf(false) }

    var showAboutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MyatoBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("settings_screen")
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Configurações",
            color = MyatoWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Personalize o Myato-Player de acordo com sua preferência",
            color = MyatoGrayMedium,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Seção: Aparência
        SettingsSectionHeader(title = "Aparência & Interface")
        Card(
            colors = CardDefaults.cardColors(containerColor = MyatoCard),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Tema Dark Puro (Pitch Black)", color = MyatoWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Economiza bateria em telas AMOLED", color = MyatoGrayMedium, fontSize = 11.sp)
                    }
                    Switch(
                        checked = true,
                        onCheckedChange = {},
                        enabled = false,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MyatoBackground,
                            checkedTrackColor = MyatoGreen
                        )
                    )
                }

                HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Destaque Visual", color = MyatoWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Verde Neon Neon Pro (Padrão Myato)", color = MyatoGreen, fontSize = 11.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MyatoGreen)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Seção: Biblioteca e Mídia
        SettingsSectionHeader(title = "Biblioteca & Escaneamento")
        Card(
            colors = CardDefaults.cardColors(containerColor = MyatoCard),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SettingsActionRow(
                    title = "Reescanear Músicas do Dispositivo",
                    subtitle = "Atualiza novos arquivos adicionados ao armazenamento",
                    icon = Icons.Default.Sync,
                    onClick = onRescanLibrary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Seção: Áudio & Automação
        SettingsSectionHeader(title = "Áudio & Conexões")
        Card(
            colors = CardDefaults.cardColors(containerColor = MyatoCard),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SettingsActionRow(
                    title = "Equalizador de Áudio",
                    subtitle = "Bass Boost, Virtualizer 3D e bandas de frequência",
                    icon = Icons.Default.Tune,
                    onClick = { onNavigate(Screen.EQUALIZER) }
                )

                HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 10.dp))

                SettingsActionRow(
                    title = "Automação Bluetooth & Fones",
                    subtitle = "Comportamento ao conectar e desconectar dispositivos",
                    icon = Icons.Default.Bluetooth,
                    onClick = { onNavigate(Screen.BLUETOOTH_AUTOMATION) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Seção: Backup & Restauração (Section 18)
        SettingsSectionHeader(title = "Backup & Restauração")
        Card(
            colors = CardDefaults.cardColors(containerColor = MyatoCard),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SettingsActionRow(
                    title = "Exportar Backup JSON",
                    subtitle = "Gera uma cópia de segurança de playlists e favoritas",
                    icon = Icons.Default.Backup,
                    onClick = {
                        isRestoring = false
                        onExportBackup { json ->
                            backupJsonText = json
                            showBackupDialog = true
                        }
                    }
                )

                HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 10.dp))

                SettingsActionRow(
                    title = "Restaurar Backup JSON",
                    subtitle = "Recupere suas playlists a partir de um código JSON",
                    icon = Icons.Default.Restore,
                    onClick = {
                        isRestoring = true
                        backupJsonText = ""
                        showBackupDialog = true
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Seção: Sobre
        SettingsSectionHeader(title = "Sobre o Sistema")
        Card(
            colors = CardDefaults.cardColors(containerColor = MyatoCard),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SettingsActionRow(
                    title = "Myato-Player 2.0 Pro",
                    subtitle = "Versão 2.0.0 (Build Nativo Android Media3)",
                    icon = Icons.Default.Info,
                    onClick = { showAboutDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }

    // Backup & Restore Dialog
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            containerColor = MyatoCard,
            title = {
                Text(
                    text = if (isRestoring) "Restaurar Backup" else "Backup de Playlists",
                    color = MyatoWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (isRestoring) {
                        Text("Cole o conteúdo JSON do backup abaixo:", color = MyatoGrayLight, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = backupJsonText,
                            onValueChange = { backupJsonText = it },
                            placeholder = { Text("Cole o JSON aqui...", color = MyatoGrayDark) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MyatoGreen,
                                focusedTextColor = MyatoWhite,
                                unfocusedTextColor = MyatoWhite
                            )
                        )
                    } else {
                        Text("Seu backup foi gerado. Copie o código abaixo:", color = MyatoGrayLight, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = backupJsonText,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MyatoGreen,
                                focusedTextColor = MyatoWhite,
                                unfocusedTextColor = MyatoWhite
                            )
                        )
                    }
                }
            },
            confirmButton = {
                if (isRestoring) {
                    Button(
                        onClick = {
                            onRestoreBackup(backupJsonText) {
                                showBackupDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground)
                    ) {
                        Text("Restaurar")
                    }
                } else {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("MyatoBackup", backupJsonText)
                            clipboard.setPrimaryClip(clip)
                            showBackupDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground)
                    ) {
                        Text("Copiar JSON")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Fechar", color = MyatoGrayLight)
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = MyatoCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.GraphicEq, null, tint = MyatoGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Myato-Player 2.0", color = MyatoWhite, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Aplicativo profissional de reprodução musical para Android.\n\n" +
                            "• Motor de Áudio: Android Media3 ExoPlayer\n" +
                            "• Equalizador nativo com Bass Boost e Virtualizer\n" +
                            "• Suporte a reprodução em segundo plano com Foreground Service\n" +
                            "• Seleção múltipla avançada com range e inversão\n" +
                            "• Banco de dados Room 100% local e seguro\n" +
                            "• Gestor de downloads de fontes autorizadas\n" +
                            "• Desenvolvido com Jetpack Compose & Material Design 3",
                    color = MyatoGrayLight,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground)
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = MyatoGreen,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MyatoSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = MyatoGreen, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, color = MyatoWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = MyatoGrayMedium, fontSize = 11.sp)
            }
        }
        Icon(Icons.Default.ChevronRight, null, tint = MyatoGrayMedium, modifier = Modifier.size(20.dp))
    }
}
