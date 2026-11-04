package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.database.MyatoDatabase
import com.example.player.service.MediaPlaybackService

class MyatoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Pre-initialize database instance
        MyatoDatabase.getInstance(this)

        // Create foreground media channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MediaPlaybackService.CHANNEL_ID,
                "Reprodução de Música",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificação de reprodução do Myato-Player"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
