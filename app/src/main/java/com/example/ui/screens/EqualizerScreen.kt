package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.model.AudioOutputDevice
import com.example.model.EqualizerConfig
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    config: EqualizerConfig,
    currentDevice: AudioOutputDevice,
    onSelectDeviceProfile: (AudioOutputDevice) -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectPreset: (String) -> Unit,
    onSaveCustomPreset: (String) -> Unit,
    onDeleteCustomPreset: (String) -> Unit,
    onBassBoostChange: (Int) -> Unit,
    onVirtualizerChange: (Int) -> Unit,
    onBandChange: (Int, Int) -> Unit,
    onChannelBalanceChange: (Float) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }

    val standardPresets = listOf(
        "Normal",
        "Bass Boost",
        "Reduced Bass",
        "Rock",
        "Pop",
        "Clássico",
        "Jazz",
        "Eletrônica",
        "Hip-Hop",
        "Vocal",
        "Personalizado"
    )

    val allPresets = remember(config.customPresets) {
        val customs = config.customPresets.keys.toList()
        (standardPresets + customs).distinct()
    }

    // 10 Frequency Bands requested in Section 5 (Image 5 reference)
    val bandFrequencies = listOf(
        "31,5 Hz",
        "63 Hz",
        "125 Hz",
        "250 Hz",
        "500 Hz",
        "1 kHz",
        "2 kHz",
        "4 kHz",
        "8 kHz",
        "16 kHz"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Equalizador de Áudio", color = MyatoWhite, fontWeight = FontWeight.Bold)
                        Text("Perfis independentes por dispositivo", color = MyatoGreen, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MyatoWhite)
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (config.isEnabled) "ATIVO" else "DESATIVADO",
                            color = if (config.isEnabled) MyatoGreen else MyatoGrayMedium,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = config.isEnabled,
                            onCheckedChange = onToggleEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MyatoBackground,
                                checkedTrackColor = MyatoGreen,
                                uncheckedThumbColor = MyatoGrayMedium,
                                uncheckedTrackColor = MyatoSurfaceVariant
                            ),
                            modifier = Modifier.testTag("equalizer_switch")
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MyatoBackground)
            )
        },
        containerColor = MyatoBackground,
        modifier = modifier.testTag("equalizer_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Device Profile Switcher
            Text(
                text = "Perfil do Dispositivo de Saída",
                color = MyatoWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AudioOutputDevice.values().forEach { dev ->
                    val isConnected = dev == currentDevice
                    val chipLabel = when (dev) {
                        AudioOutputDevice.SPEAKER -> "Alto-falante"
                        AudioOutputDevice.WIRED_HEADSET -> "Fone c/ Fio"
                        AudioOutputDevice.BLUETOOTH -> "Bluetooth"
                        AudioOutputDevice.USB -> "DAC / USB"
                    }

                    FilterChip(
                        selected = isConnected,
                        onClick = { onSelectDeviceProfile(dev) },
                        label = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = chipLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isConnected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isConnected) {
                                    Text("● CONECTADO", fontSize = 8.sp, color = MyatoGreen)
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MyatoSurfaceVariant,
                            selectedLabelColor = MyatoGreen,
                            containerColor = MyatoCard
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Presets Horizontal Carousel + Save Preset Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Predefinições de Áudio",
                    color = MyatoWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = {
                    newPresetName = "Meu Preset ${config.customPresets.size + 1}"
                    showSavePresetDialog = true
                }) {
                    Icon(Icons.Default.Save, null, tint = MyatoGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Salvar Atual", color = MyatoGreen, fontSize = 12.sp)
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(allPresets) { preset ->
                    val isSelected = config.presetName == preset
                    val isCustom = config.customPresets.containsKey(preset)

                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectPreset(preset) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = preset,
                                    color = if (isSelected) MyatoBackground else MyatoWhite,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isCustom) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Excluir preset",
                                        tint = if (isSelected) MyatoBackground else MyatoError,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { onDeleteCustomPreset(preset) }
                                    )
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MyatoGreen,
                            selectedLabelColor = MyatoBackground,
                            containerColor = MyatoCard
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bass Boost & Virtualizer 3D Cards
            Card(
                colors = CardDefaults.cardColors(containerColor = MyatoCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Bass Boost
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speaker, null, tint = MyatoGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reforço de Graves (Bass Boost)", color = MyatoWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text("${config.bassBoost / 10}%", color = MyatoGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = config.bassBoost.toFloat(),
                        onValueChange = { onBassBoostChange(it.toInt()) },
                        valueRange = 0f..1000f,
                        enabled = config.isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = MyatoGreen,
                            activeTrackColor = MyatoGreen,
                            inactiveTrackColor = MyatoBorder
                        ),
                        modifier = Modifier.testTag("bass_boost_slider")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Virtualizer 3D
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SurroundSound, null, tint = MyatoGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Virtualizador 3D Surround", color = MyatoWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text("${config.virtualizer / 10}%", color = MyatoGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = config.virtualizer.toFloat(),
                        onValueChange = { onVirtualizerChange(it.toInt()) },
                        valueRange = 0f..1000f,
                        enabled = config.isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = MyatoGreen,
                            activeTrackColor = MyatoGreen,
                            inactiveTrackColor = MyatoBorder
                        ),
                        modifier = Modifier.testTag("virtualizer_slider")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stereo Channel Balance (L / R)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GraphicEq, null, tint = MyatoGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Balanço Estéreo (E / D)", color = MyatoWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            text = when {
                                config.channelBalance < -0.1f -> "E ${(-config.channelBalance * 100).toInt()}%"
                                config.channelBalance > 0.1f -> "D ${(config.channelBalance * 100).toInt()}%"
                                else -> "Centro"
                            },
                            color = MyatoGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = config.channelBalance,
                        onValueChange = { onChannelBalanceChange(it) },
                        valueRange = -1.0f..1.0f,
                        enabled = config.isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = MyatoGreen,
                            activeTrackColor = MyatoGreen,
                            inactiveTrackColor = MyatoBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 10 Band Equalizer Sliders
            Card(
                colors = CardDefaults.cardColors(containerColor = MyatoCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Equalizador de 10 Bandas",
                        color = MyatoWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    bandFrequencies.forEachIndexed { bandIndex, freqName ->
                        val currentLevel = config.bandLevels[bandIndex] ?: 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = freqName,
                                color = MyatoGrayLight,
                                fontSize = 12.sp,
                                modifier = Modifier.width(62.dp)
                            )

                            Slider(
                                value = currentLevel.toFloat(),
                                onValueChange = { onBandChange(bandIndex, it.toInt()) },
                                valueRange = -1200f..1200f,
                                enabled = config.isEnabled,
                                colors = SliderDefaults.colors(
                                    thumbColor = MyatoGreen,
                                    activeTrackColor = MyatoGreen,
                                    inactiveTrackColor = MyatoBorder
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = "${currentLevel / 100} dB",
                                color = if (currentLevel > 0) MyatoGreen else if (currentLevel < 0) MyatoError else MyatoGrayMedium,
                                fontSize = 11.sp,
                                modifier = Modifier.width(48.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reset Button
            OutlinedButton(
                onClick = onReset,
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MyatoWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("equalizer_reset_btn")
            ) {
                Icon(Icons.Default.Refresh, null, tint = MyatoGrayLight, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Restaurar Padrão do Perfil")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Save Custom Preset Dialog
    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            containerColor = MyatoCard,
            title = { Text("Salvar Predefinição", color = MyatoWhite, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPresetName,
                    onValueChange = { newPresetName = it },
                    label = { Text("Nome do Preset") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MyatoGreen,
                        focusedTextColor = MyatoWhite,
                        unfocusedTextColor = MyatoWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            onSaveCustomPreset(newPresetName.trim())
                        }
                        showSavePresetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MyatoGreen, contentColor = MyatoBackground)
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text("Cancelar", color = MyatoGrayLight)
                }
            }
        )
    }
}
