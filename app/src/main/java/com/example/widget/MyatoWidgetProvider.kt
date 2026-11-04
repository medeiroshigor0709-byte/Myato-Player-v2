package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.player.service.MediaPlaybackService

class MyatoWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_WIDGET_PLAY_PAUSE = "com.example.ACTION_WIDGET_PLAY_PAUSE"
        const val ACTION_WIDGET_PREV = "com.example.ACTION_WIDGET_PREV"
        const val ACTION_WIDGET_NEXT = "com.example.ACTION_WIDGET_NEXT"
        const val ACTION_WIDGET_FAV = "com.example.ACTION_WIDGET_FAV"

        fun updateWidget(
            context: Context,
            title: String = "Myato-Player 2.1",
            artist: String = "Toque para reproduzir",
            isPlaying: Boolean = false
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, MyatoWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (widgetId in allWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_myato_player)
                views.setTextViewText(R.id.widget_title, title)
                views.setTextViewText(R.id.widget_artist, artist)

                val playPauseRes = if (isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
                views.setImageViewResource(R.id.widget_btn_play_pause, playPauseRes)

                // Root Click -> Open Main Activity
                val appIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val appPendingIntent = PendingIntent.getActivity(
                    context, 0, appIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, appPendingIntent)

                // Play / Pause Click
                val playPauseIntent = Intent(context, MediaPlaybackService::class.java).apply {
                    action = ACTION_WIDGET_PLAY_PAUSE
                }
                val playPausePendingIntent = PendingIntent.getService(
                    context, 1, playPauseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_play_pause, playPausePendingIntent)

                // Prev Click
                val prevIntent = Intent(context, MediaPlaybackService::class.java).apply {
                    action = ACTION_WIDGET_PREV
                }
                val prevPendingIntent = PendingIntent.getService(
                    context, 2, prevIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_prev, prevPendingIntent)

                // Next Click
                val nextIntent = Intent(context, MediaPlaybackService::class.java).apply {
                    action = ACTION_WIDGET_NEXT
                }
                val nextPendingIntent = PendingIntent.getService(
                    context, 3, nextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_next, nextPendingIntent)

                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWidget(context)
    }
}
