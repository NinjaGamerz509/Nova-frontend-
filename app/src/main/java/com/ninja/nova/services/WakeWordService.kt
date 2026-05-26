package com.ninja.nova.services

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ninja.nova.ui.screens.MainActivity

class WakeWordService : Service() {
    companion object {
        const val CHANNEL_ID = "nova_wake_word"
        const val NOTIF_ID = 1
        const val ACTION_WAKE = "com.ninja.nova.WAKE_WORD_DETECTED"
        var isRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "nova", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val intent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("nova")
            .setContentText("Running in background")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(intent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() { super.onDestroy(); isRunning = false }
    override fun onBind(intent: Intent?): IBinder? = null
}
