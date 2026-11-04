package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothScreen(
    playOnConnectEnabled: Boolean,
    pauseOnDisconnectEnabled: Boolean,
    onTogglePlayOnConnect: (Boolean) -> Unit,
    onTogglePauseOnDisconnect: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Automação Bluetooth", color = MyatoWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MyatoWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MyatoBackground)
            )
        },
        containerColor = MyatoBackground,
        modifier = modifier.testTag("bluetooth_automation_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Security & Consent Notice Box
            Card(
                colors = CardDefaults.cardColors(containerColor = MyatoSurfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, null, tint = MyatoGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "A reprodução automática respeita sua privacidade e só é ativada se você conceder permissão expressa nas opções abaixo.",
                        color = MyatoWhite,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Comportamento de Conexão",
                color = MyatoWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Option 1: Play on connect
            Card(
                colors = CardDefaults.cardColors(containerColor = MyatoCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reproduzir ao Conectar Dispositivo",
                            color = MyatoWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Inicia a última faixa automaticamente ao parear fone ou rádio do carro",
                            color = MyatoGrayMedium,
                            fontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = playOnConnectEnabled,
                        onCheckedChange = onTogglePlayOnConnect,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MyatoBackground,
                            checkedTrackColor = MyatoGreen
                        ),
                        modifier = Modifier.testTag("switch_play_on_connect")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Option 2: Pause on disconnect
            Card(
                colors = CardDefaults.cardColors(containerColor = MyatoCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pausar ao Desconectar",
                            color = MyatoWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Interrompe imediatamente o áudio ao desconectar fones ou saída Bluetooth",
                            color = MyatoGrayMedium,
                            fontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = pauseOnDisconnectEnabled,
                        onCheckedChange = onTogglePauseOnDisconnect,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MyatoBackground,
                            checkedTrackColor = MyatoGreen
                        ),
                        modifier = Modifier.testTag("switch_pause_on_disconnect")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Info Card about Media Buttons & Headset support
            Card(
                colors = CardDefaults.cardColors(containerColor = MyatoCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Headphones, null, tint = MyatoGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Controles Físicos do Fone", color = MyatoWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• 1 toque: Tocar / Pausar\n• 2 toques: Avançar para a próxima música\n• 3 toques: Voltar para a música anterior",
                        color = MyatoGrayLight,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
