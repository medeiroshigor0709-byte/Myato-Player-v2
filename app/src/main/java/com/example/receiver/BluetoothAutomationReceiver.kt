package com.example.receiver

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.example.player.PlayerController

class BluetoothAutomationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val playerController = PlayerController.getInstance(context)

        when (intent.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                if (playerController.playOnBluetoothConnect.value) {
                    playerController.play()
                }
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                if (playerController.pauseOnBluetoothDisconnect.value) {
                    playerController.pause()
                }
            }
            AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                // Audio cable unplugged or Bluetooth audio device turned off
                playerController.pause()
            }
        }
    }
}
