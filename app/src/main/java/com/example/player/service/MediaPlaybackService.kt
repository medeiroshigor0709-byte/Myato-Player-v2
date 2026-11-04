package com.example.player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.MainActivity
import com.example.R
import com.example.model.AudioOutputDevice
import com.example.player.PlayerController
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(UnstableApi::class)
class MediaPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var notificationJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        val playerController = PlayerController.getInstance(this)
        val player = playerController.getExoPlayer()

        val sessionActivityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            sessionActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()

        createNotificationChannel()
        observePlayerStateAndNotify(playerController)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reprodução Myato-Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controles de reprodução de áudio, capa e dispositivo conectado"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun observePlayerStateAndNotify(playerController: PlayerController) {
        notificationJob?.cancel()
        notificationJob = serviceScope.launch {
            // Combine current song, playing state, and current device
            launch {
                playerController.currentSong.collectLatest { song ->
                    updateNotification()
                }
            }
            launch {
                playerController.isPlaying.collectLatest { isPlaying ->
                    updateNotification()
                }
            }
            launch {
                playerController.currentDevice.collectLatest { dev ->
                    updateNotification()
                }
            }
        }
    }

    private fun updateNotification() {
        val playerController = PlayerController.getInstance(this)
        val song = playerController.currentSong.value
        val isPlaying = playerController.isPlaying.value
        val currentDevice = playerController.currentDevice.value

        if (song == null) {
            stopForeground(STOP_FOREGROUND_DETACH)
            return
        }

        val deviceLabel = when (currentDevice) {
            AudioOutputDevice.SPEAKER -> "Alto-falante do Celular"
            AudioOutputDevice.WIRED_HEADSET -> "Fone de Ouvido Conectado"
            AudioOutputDevice.BLUETOOTH -> "Bluetooth Conectado"
            AudioOutputDevice.USB -> "Áudio Digital USB / DAC"
        }

        // Open App Intent
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Previous Action Intent
        val prevPendingIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Play/Pause Action Intent
        val playPausePendingIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_PLAY_PAUSE },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Next Action Intent
        val nextPendingIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Favorite Action Intent
        val favPendingIntent = PendingIntent.getService(
            this,
            4,
            Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_TOGGLE_FAVORITE },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Close / Dismiss Action Intent
        val closePendingIntent = PendingIntent.getService(
            this,
            5,
            Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_STOP_SERVICE },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val albumArtBitmap = generatePlaceholderArtwork(song.title)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(albumArtBitmap)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setSubText(deviceLabel)
            .setContentIntent(contentIntent)
            .setDeleteIntent(closePendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_media_previous,
                "Anterior",
                prevPendingIntent
            )
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Pausar" else "Reproduzir",
                playPausePendingIntent
            )
            .addAction(
                android.R.drawable.ic_media_next,
                "Próxima",
                nextPendingIntent
            )
            .addAction(
                if (song.isFavorite) android.R.drawable.star_on else android.R.drawable.star_off,
                if (song.isFavorite) "Desfavoritar" else "Favoritar",
                favPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Fechar",
                closePendingIntent
            )

        val durSec = (song.durationMs / 1000).toInt()
        val posSec = (playerController.currentPosition.value / 1000).toInt()
        if (durSec > 0) {
            notificationBuilder.setProgress(durSec, posSec, false)
        }

        try {
            startForeground(NOTIFICATION_ID, notificationBuilder.build())
        } catch (e: Exception) {
            e.printStackTrace()
            val nm = NotificationManagerCompat.from(this)
            nm.notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    private fun generatePlaceholderArtwork(title: String): Bitmap {
        val size = 256
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = Color.rgb(18, 18, 18)
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)

        paint.color = Color.rgb(0, 230, 118) // Myato Green
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f
        canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 20f, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 96f
        paint.textAlign = Paint.Align.CENTER
        val initial = title.firstOrNull()?.uppercaseChar()?.toString() ?: "M"
        val yPos = (size / 2f - (paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(initial, size / 2f, yPos, paint)

        return bitmap
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val playerController = PlayerController.getInstance(this)
        when (intent?.action) {
            ACTION_PLAY_PAUSE, "com.example.ACTION_WIDGET_PLAY_PAUSE" -> playerController.togglePlayPause()
            ACTION_PREV, "com.example.ACTION_WIDGET_PREV" -> playerController.previous()
            ACTION_NEXT, "com.example.ACTION_WIDGET_NEXT" -> playerController.next()
            ACTION_TOGGLE_FAVORITE -> {
                val current = playerController.currentSong.value
                if (current != null) {
                    playerController.onSongPlayed?.let {
                        // Toggle favorite via callback or background coroutine
                        serviceScope.launch {
                            val db = com.example.database.MyatoDatabase.getInstance(applicationContext)
                            val repo = com.example.repository.MusicRepository(applicationContext, db)
                            repo.toggleFavorite(current.id)
                        }
                    }
                }
            }
            ACTION_STOP_SERVICE -> {
                playerController.pause()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        updateNotification()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        notificationJob?.cancel()
        serviceScope.cancel()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "myato_playback_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY_PAUSE = "com.example.ACTION_NOTIFICATION_PLAY_PAUSE"
        const val ACTION_PREV = "com.example.ACTION_NOTIFICATION_PREV"
        const val ACTION_NEXT = "com.example.ACTION_NOTIFICATION_NEXT"
        const val ACTION_TOGGLE_FAVORITE = "com.example.ACTION_NOTIFICATION_TOGGLE_FAVORITE"
        const val ACTION_STOP_SERVICE = "com.example.ACTION_NOTIFICATION_STOP_SERVICE"
    }
}
