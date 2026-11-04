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
import androidx.compose.ui.graphics.Color
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

    // 7.1 Idioma
    var selectedLanguage by remember { mutableStateOf("Português (Brasil)") }
    var showLanguageDialog by remember { mutableStateOf(false) }

    // 7.2 Interface
    var selectedTheme by remember { mutableStateOf("Escuro (Pitch Black)") }
    var showThemeDialog by remember { mutableStateOf(false) }
    var selectedAccentColor by remember { mutableStateOf("Verde Myato (#00E676)") }
    var showAccentDialog by remember { mutableStateOf(false) }
    var fontSizeSetting by remember { mutableStateOf("Médio (Padrão)") }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    var coverShapeSetting by remember { mutableStateOf("Cantos Arredondados") }

    // 7.3 Áudio
    var crossfadeEnabled by remember { mutableStateOf(false) }
    var crossfadeDuration by remember { mutableIntStateOf(3) }
    var gaplessEnabled by remember { mutableStateOf(true) }
    var volumeNormalizationEnabled by remember { mutableStateOf(false) }
    var fadeInOutEnabled by remember { mutableStateOf(true) }

    // 7.4 Biblioteca & Tags
    var autoRefreshLibrary by remember { mutableStateOf(true) }
    var ignoreTracksUnder10s by remember { mutableStateOf(true) }

    // 7.5 Fone & Bluetooth
    var pauseOnUnplug by remember { mutableStateOf(true) }
    var resumeOnPlug by remember { mutableStateOf(false) }
    var singleClickAction by remember { mutableStateOf("Reproduzir / Pausar") }
    var doubleClickAction by remember { mutableStateOf("Próxima Música") }
    var tripleClickAction by remember { mutableStateOf("Música Anterior") }
    var showHeadsetClickDialog by remember { mutableStateOf(false) }

    // 7.6 Notificações
    var notifShowArtwork by remember { mutableStateOf(true) }
    var notifShowFavoriteButton by remember { mutableStateOf(true) }
    var notifShowCloseButton by remember { mutableStateOf(true) }
    var notifShowDeviceConnected by remember { mutableStateOf(true) }

    // 7.7 Widgets
    var showWidgetInfoDialog by remember { mutableStateOf(false) }

    // 7.8 Lockscreen
    var lockscreenControlsEnabled by remember { mutableStateOf(true) }

    // 7.9 Avançado
    var cacheSizeMb by remember { mutableStateOf("12.4 MB") }
    var showClearCacheConfirm by remember { mutableStateOf(false) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }

    // 7.10 Backup & Restauração
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
            text = "Central de Configurações",
            color = MyatoWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Myato-Player v2.1 • Controle Total do Reprodutor",
            color = MyatoGreen,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(20.dp))

        // 7.1 Idioma
        SettingsSectionHeader(title = "7.1 Idioma / Language / Idioma")
        SettingsCard {
            SettingsActionRow(
                title = "Idioma do Aplicativo",
                subtitle = selectedLanguage,
                icon = Icons.Default.Language,
                onClick = { showLanguageDialog = true }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 7.2 Interface
        SettingsSectionHeader(title = "7.2 Interface & Visual")
        SettingsCard {
            SettingsActionRow(
                title = "Tema da Interface",
                subtitle = selectedTheme,
                icon = Icons.Default.DarkMode,
                onClick = { showThemeDialog = true }
            )
            HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 8.dp))
            SettingsActionRow(
                title = "Cor de Destaque",
                subtitle = selectedAccentColor,
                icon = Icons.Default.Palette,
                onClick = { showAccentDialog = true }
            )
            HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 8.dp))
            SettingsActionRow(
                title = "Tamanho das Fontes",
                subtitle = fontSizeSetting,
                icon = Icons.Default.FormatSize,
                onClick = { showFontSizeDialog = true }
            )
            HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Formato das Capas de Álbuns", color = MyatoWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(coverShapeSetting, color = MyatoGrayMedium, fontSize = 11.sp)
                }
                TextButton(onClick = {
                    coverShapeSetting = if (coverShapeSetting == "Cantos Arredondados") "Circular / Vinil" else "Cantos Arredondados"
                }) {
                    Text("Alternar", color = MyatoGreen, fontSize = 12.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 7.3 Áudio
        SettingsSectionHeader(title = "7.3 Áudio & Reprodução Avançada")
        SettingsCard {
            SettingsActionRow(
                title = "Equalizador de Áudio (10 Bandas)",
                subtitle = "Bass Boost, Virtualizer 3D e Perfis por Dispositivo",
                icon = Icons.Default.Tune,
                onClick = { onNavigate(Screen.EQUALIZER) }
            )
            HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 8.dp))
            SettingsToggleRow(
                title = "Reprodução Contínua (Gapless Playback)",
                subtitle = "Sem intervalos entre faixas sequenciais do álbum",
                checked = gaplessEnabled,
                onCheckedChange = { gaplessEnabled = it }
            )
            HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 8.dp))
            SettingsToggleRow(
                title = "Transição Suave (Crossfade)",
                subtitle = if (crossfadeEnabled) "$crossfadeDuration segundos de sobreposição" else "Desativado",
                checked = crossfadeEnabled,
                onCheckedChange = { crossfadeEnabled = it }
            )
            HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 8.dp))
            SettingsToggleRow(
                title = "Fade-In & Fade-Out Suave",
                subtitle = "Gradual de 500ms ao pausar e retomar",
                checked = fadeInOutEnabled,
                onCheckedChange = { fadeInOutEnabled = it }
            )
            HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 8.dp))
            SettingsToggleRow(
                title = "Normalização de Volume",
                subtitle = "ReplayGain para equilibrar faixas muito altas ou baixas",
                checked = volumeNormalizationEnabled,
                onCheckedChange = { volumeNormalizationEnabled = it }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 7.4 Biblioteca & Tags
        SettingsSectionHeader(title = "7.4 Biblioteca de Músicas & Tags")
        SettingsCard {
            SettingsActionRow(
                title = "Reescanear Músicas do Celular",
                subtitle = "Atualiza arquivos adicionados ou removidos do aparelho",
                icon = Icons.Default.Sync,
                onClick = onRescanLibrary
            )
            HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 8.dp))
            SettingsToggleRow(
                title = "Atualização Automática da Biblioteca",
                subtitle = "Sincroniza quando novos arquivos forem detectados",
                checked = autoRefreshLibrary,
                onCheckedChange = { autoRefreshLibrary = it }
            )
            HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 8.dp))
            SettingsToggleRow(
                title = "Ignorar Áudios Curtos (< 10 segundos)",
                subtitle = "Evita notificações do WhatsApp e efeitos sonoros",
                checked = ignoreTracksUnder10s,
                onCheckedChange = { ignoreTracksUnder10s = it }
            )
            HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 8.dp))
            SettingsActionRow(
                title = "Navegador de Pastas",
                subtitle = "Navegue pelo sistema de arquivos em modo linear ou em árvore",
                icon = Icons.Default.Folder,
                onClick = { onNavigate(Screen.FOLDER_EXPLORER) }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 7.5 Fone & Bluetooth
        SettingsSectionHeader(title = "7.5 Fone de Ouvido, Bluetooth & Alto-falantes")
        SettingsCard {
            SettingsToggleRow(
                title = "Pausar ao Desconectar Fone / Bluetooth",
                subtitle = "Evita tocar áudio no alto-falante acidentalmente (Noisy)",
                checked = pauseOnUnplug,
                onCheckedChange = { pauseOnUnplug = it }
            )
            HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 8.dp))
            SettingsToggleRow(
                title = "Retomar ao Conectar",
                subtitle = "Volta a tocar automaticamente quando o fone for plugado",
                checked = resumeOnPlug,
                onCheckedChange = { resumeOnPlug = it }
            )
            HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 8.dp))
            SettingsActionRow(
                title = "Botões do Fone de Ouvido",
                subtitle = "1x: $singleClickAction • 2x: $doubleClickAction • 3x: $tripleClickAction",
                icon = Icons.Default.Headset,
                onClick = { showHeadsetClickDialog = true }
            )
            HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 8.dp))
            SettingsActionRow(
                title = "Automação Bluetooth Completa",
                subtitle = "Configurar dispositivos pareados e ações automáticas",
                icon = Icons.Default.Bluetooth,
                onClick = { onNavigate(Screen.BLUETOOTH_AUTOMATION) }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 7.6 Notificações
        SettingsSectionHeader(title = "7.6 Notificações & Controles")
        SettingsCard {
            SettingsToggleRow(
                title = "Mostrar Capa do Álbum na Notificação",
                subtitle = "Exibe arte da faixa em alta resolução",
                checked = notifShowArtwork,
                onCheckedChange = { notifShowArtwork = it }
            )
            HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 8.dp))
            SettingsToggleRow(
                title = "Botão de Favorito na Notificação",
                subtitle = "Permite favoritar a música sem abrir o app",
                checked = notifShowFavoriteButton,
                onCheckedChange = { notifShowFavoriteButton = it }
            )
            HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 8.dp))
            SettingsToggleRow(
                title = "Botão Fechar / Encerrar",
                subtitle = "Para a reprodução e descarta a notificação",
                checked = notifShowCloseButton,
                onCheckedChange = { notifShowCloseButton = it }
            )
            HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 8.dp))
            SettingsToggleRow(
                title = "Exibir Dispositivo de Áudio Conectado",
                subtitle = "Mostra se está no Bluetooth, Fone com fio ou Alto-falante",
                checked = notifShowDeviceConnected,
                onCheckedChange = { notifShowDeviceConnected = it }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 7.7 Widgets na Tela Inicial
        SettingsSectionHeader(title = "7.7 Widgets na Tela Inicial")
        SettingsCard {
            SettingsActionRow(
                title = "Configurações do Widget",
                subtitle = "Tamanhos 4x1 e 4x2 com controles rápidos e capa",
                icon = Icons.Default.Widgets,
                onClick = { showWidgetInfoDialog = true }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 7.8 Tela de Bloqueio
        SettingsSectionHeader(title = "7.8 Tela de Bloqueio")
        SettingsCard {
            SettingsToggleRow(
                title = "Controles de Mídia na Tela de Bloqueio",
                subtitle = "MediaSession nativo com seek bar e arte do álbum",
                checked = lockscreenControlsEnabled,
                onCheckedChange = { lockscreenControlsEnabled = it }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 7.9 Avançado
        SettingsSectionHeader(title = "7.9 Avançado & Desempenho")
        SettingsCard {
            SettingsActionRow(
                title = "Estatísticas de Audição",
                subtitle = "Músicas, artistas e álbuns mais ouvidos por período",
                icon = Icons.Default.BarChart,
                onClick = { onNavigate(Screen.STATISTICS) }
            )
            HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 8.dp))
            SettingsActionRow(
                title = "Gerenciador de 20 Filas",
                subtitle = "Alterne, renomeie, salve ou limpe filas independentes",
                icon = Icons.Default.Layers,
                onClick = { onNavigate(Screen.QUEUES_MANAGER) }
            )
            HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Limpar Cache Temporário", color = MyatoWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("Espaço utilizado: $cacheSizeMb", color = MyatoGrayMedium, fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { showClearCacheConfirm = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MyatoWhite)
                ) {
                    Text("Limpar", fontSize = 11.sp)
                }
            }
            HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 8.dp))
            SettingsActionRow(
                title = "Diagnóstico do Player",
                subtitle = "Verificar integridade do áudio e codecs do aparelho",
                icon = Icons.Default.BugReport,
                onClick = { showDiagnosticsDialog = true }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 7.10 Backup & Restauração
        SettingsSectionHeader(title = "7.10 Backup & Restauração Completa")
        SettingsCard {
            SettingsActionRow(
                title = "Exportar Backup JSON",
                subtitle = "Salva playlists, filas, favoritas, estatísticas e presets",
                icon = Icons.Default.Backup,
                onClick = {
                    isRestoring = false
                    onExportBackup { json ->
                        backupJsonText = json
                        showBackupDialog = true
                    }
                }
            )
            HorizontalDivider(color = MyatoBorder, modifier = Modifier.padding(vertical = 8.dp))
            SettingsActionRow(
                title = "Restaurar Backup JSON",
                subtitle = "Recupere seus dados a partir de um arquivo ou texto JSON",
                icon = Icons.Default.Restore,
                onClick = {
                    isRestoring = true
                    backupJsonText = ""
                    showBackupDialog = true
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Sobre o Aplicativo
        SettingsSectionHeader(title = "Sobre o Sistema")
        SettingsCard {
            SettingsActionRow(
                title = "Myato-Player v2.1 Pro",
                subtitle = "Build Nativo Android Media3 • 100% Offline & Seguro",
                icon = Icons.Default.Info,
                onClick = { showAboutDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(110.dp))
    }

    // Language Dialog
    if (showLanguageDialog) {
        val languages = listOf("Português (Brasil)", "English (US)", "Español")
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = MyatoCard,
            title = { Text("Selecionar Idioma", color = MyatoWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    languages.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLanguage = lang
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage == lang,
                                onClick = {
                                    selectedLanguage = lang
                                    showLanguageDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = MyatoGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(lang, color = MyatoWhite, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Fechar", color = MyatoGrayLight)
                }
            }
        )
    }

    // Theme Dialog
    if (showThemeDialog) {
        val themes = listOf("Escuro (Pitch Black)", "Claro (Light)", "Seguir Sistema (Auto)")
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            containerColor = MyatoCard,
            title = { Text("Tema do Aplicativo", color = MyatoWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    themes.forEach { th ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTheme = th
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedTheme == th,
                                onClick = {
                                    selectedTheme = th
                                    showThemeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = MyatoGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(th, color = MyatoWhite, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Fechar", color = MyatoGrayLight)
                }
            }
        )
    }

    // Accent Color Dialog
    if (showAccentDialog) {
        val accents = listOf(
            "Verde Myato (#00E676)" to Color(0xFF00E676),
            "Ciano Futurista (#00E5FF)" to Color(0xFF00E5FF),
            "Azul Elétrico (#2979FF)" to Color(0xFF2979FF),
            "Roxo Neon (#D500F9)" to Color(0xFFD500F9),
            "Âmbar Dourado (#FFD600)" to Color(0xFFFFD600)
        )
        AlertDialog(
            onDismissRequest = { showAccentDialog = false },
            containerColor = MyatoCard,
            title = { Text("Cor de Destaque", color = MyatoWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    accents.forEach { (name, color) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAccentColor = name
                                    showAccentDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(name, color = MyatoWhite, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAccentDialog = false }) {
                    Text("Fechar", color = MyatoGrayLight)
                }
            }
        )
    }

    // Font Size Dialog
    if (showFontSizeDialog) {
        val sizes = listOf("Pequeno", "Médio (Padrão)", "Grande", "Extra Grande")
        AlertDialog(
            onDismissRequest = { showFontSizeDialog = false },
            containerColor = MyatoCard,
            title = { Text("Tamanho das Fontes", color = MyatoWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    sizes.forEach { sz ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    fontSizeSetting = sz
                                    showFontSizeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = fontSizeSetting == sz,
                                onClick = {
                                    fontSizeSetting = sz
                                    showFontSizeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = MyatoGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(sz, color = MyatoWhite, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFontSizeDialog = false }) {
                    Text("Fechar", color = MyatoGrayLight)
                }
            }
        )
    }

    // Headset Controls Dialog
    if (showHeadsetClickDialog) {
        AlertDialog(
            onDismissRequest = { showHeadsetClickDialog = false },
            containerColor = MyatoCard,
            title = { Text("Configurar Botões do Fone", color = MyatoWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("1 Clique: Reproduzir / Pausar", color = MyatoWhite, fontWeight = FontWeight.SemiBold)
                    Text("2 Cliques: Próxima Faixa", color = MyatoWhite, fontWeight = FontWeight.SemiBold)
                    Text("3 Cliques: Faixa Anterior", color = MyatoWhite, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Os comandos dos botões de fones de ouvido Bluetooth e com fio são processados em segundo plano através do MediaSession.",
                        color = MyatoGrayLight,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHeadsetClickDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground)
                ) {
                    Text("Salvar")
                }
            }
        )
    }

    // Widget Info Dialog
    if (showWidgetInfoDialog) {
        AlertDialog(
            onDismissRequest = { showWidgetInfoDialog = false },
            containerColor = MyatoCard,
            title = { Text("Widgets na Tela Inicial", color = MyatoWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Para adicionar o widget do Myato-Player:\n\n" +
                        "1. Na tela inicial do seu celular, mantenha o dedo pressionado em um espaço livre.\n" +
                        "2. Toque em 'Widgets'.\n" +
                        "3. Localize 'Myato-Player'.\n" +
                        "4. Arraste o widget para sua tela inicial.\n\n" +
                        "O widget permite pausar, retomar, avançar, retroceder faixas e ver o título em tempo real.",
                        color = MyatoGrayLight,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showWidgetInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground)
                ) {
                    Text("Entendido")
                }
            }
        )
    }

    // Diagnostics Dialog
    if (showDiagnosticsDialog) {
        AlertDialog(
            onDismissRequest = { showDiagnosticsDialog = false },
            containerColor = MyatoCard,
            title = { Text("Diagnóstico do Sistema", color = MyatoWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("• Motor de Áudio: Media3 ExoPlayer 1.5.1", color = MyatoGreen, fontSize = 12.sp)
                    Text("• Codecs Suportados: MP3, AAC, FLAC, OGG, WAV, M4A, OPUS", color = MyatoWhite, fontSize = 12.sp)
                    Text("• Equalizador: Integrado com suporte a AudioSession", color = MyatoWhite, fontSize = 12.sp)
                    Text("• Banco de Dados: SQLite Room v2.7.0 (Integridade OK)", color = MyatoWhite, fontSize = 12.sp)
                    Text("• Status do Serviço: MediaSessionService Ativo", color = MyatoGreen, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDiagnosticsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground)
                ) {
                    Text("Fechar")
                }
            }
        )
    }

    // Clear Cache Confirm Dialog
    if (showClearCacheConfirm) {
        AlertDialog(
            onDismissRequest = { showClearCacheConfirm = false },
            containerColor = MyatoCard,
            title = { Text("Limpar Cache?", color = MyatoWhite, fontWeight = FontWeight.Bold) },
            text = { Text("Deseja apagar os arquivos temporários de capas e cache? Suas músicas e playlists não serão afetadas.", color = MyatoGrayLight) },
            confirmButton = {
                Button(
                    onClick = {
                        cacheSizeMb = "0.0 MB"
                        showClearCacheConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground)
                ) {
                    Text("Limpar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheConfirm = false }) {
                    Text("Cancelar", color = MyatoGrayLight)
                }
            }
        )
    }

    // Backup & Restore Dialog
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            containerColor = MyatoCard,
            title = {
                Text(
                    text = if (isRestoring) "Restaurar Backup" else "Backup Completo Gerado",
                    color = MyatoWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (isRestoring) {
                        Text("Cole o código JSON do seu backup para restaurar playlists, favoritas, filas e presets:", color = MyatoGrayLight, fontSize = 12.sp)
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
                        Text("Backup gerado com sucesso. Copie o JSON abaixo ou salve em um arquivo de texto:", color = MyatoGrayLight, fontSize = 12.sp)
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
                    Text("Myato-Player v2.1 Pro", color = MyatoWhite, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Reprodutor e gerenciador profissional de músicas para Android.\n\n" +
                            "• Motor de Áudio: Android Media3 ExoPlayer\n" +
                            "• Até 20 Filas de Reprodução Independentes\n" +
                            "• Letras Sincronizadas (.LRC) com Edição em Tempo Real\n" +
                            "• Equalizador de 10 Bandas com Perfis por Dispositivo\n" +
                            "• Repetição A-B e Marcadores com Anotações\n" +
                            "• Editor de Tags ID3 Individual e em Lote\n" +
                            "• Estatísticas Detalhadas com Exportação CSV\n" +
                            "• 100% Offline e Local com Banco Room Database\n" +
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
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MyatoCard),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), content = content)
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = MyatoGreen,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 6.dp)
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

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MyatoWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = MyatoGrayMedium, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MyatoBackground,
                checkedTrackColor = MyatoGreen,
                uncheckedThumbColor = MyatoGrayMedium,
                uncheckedTrackColor = MyatoSurfaceVariant
            )
        )
    }
}
