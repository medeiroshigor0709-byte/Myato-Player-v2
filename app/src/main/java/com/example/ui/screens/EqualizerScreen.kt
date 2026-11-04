package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EqualizerConfig
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    config: EqualizerConfig,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectPreset: (String) -> Unit,
    onBassBoostChange: (Int) -> Unit,
    onVirtualizerChange: (Int) -> Unit,
    onBandChange: (Int, Int) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        "Rock",
        "Pop",
        "Clássico",
        "Jazz",
        "Voz",
        "Grave Reforçado",
        "Agudos",
        "Normal"
    )

    val bandFrequencies = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Equalizador de Áudio", color = MyatoWhite, fontWeight = FontWeight.Bold)
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
            Spacer(modifier = Modifier.height(8.dp))

            // Presets Horizontal Carousel
            Text(
                text = "Predefinições",
                color = MyatoWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(presets) { preset ->
                    val isSelected = config.presetName == preset
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectPreset(preset) },
                        label = {
                            Text(
                                text = preset,
                                color = if (isSelected) MyatoBackground else MyatoWhite,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MyatoGreen,
                            selectedLabelColor = MyatoBackground,
                            containerColor = MyatoCard
                        ),
                        modifier = Modifier.testTag("preset_$preset")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                            Text("Reforço de Graves (Bass Boost)", color = MyatoWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
                            Text("Surround / Virtualizador 3D", color = MyatoWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5 Band Equalizer Sliders
            Card(
                colors = CardDefaults.cardColors(containerColor = MyatoCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ajuste Fino de Frequências",
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
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = freqName,
                                color = MyatoGrayLight,
                                fontSize = 12.sp,
                                modifier = Modifier.width(55.dp)
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
                                color = if (currentLevel > 0) MyatoGreen else MyatoGrayMedium,
                                fontSize = 11.sp,
                                modifier = Modifier.width(45.dp)
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
                Text("Restaurar Padrão")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
