package com.ninja.nova.services

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ninja.nova.ui.screens.MainActivity
import com.ninja.nova.utils.Constants

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
        startListening()
    }

    private fun startListening() {
        // Porcupine wake word engine would go here
        // For now we use a placeholder - real impl needs Porcupine API key
        Log.d("WakeWordService", "Listening for: ${Constants.WAKE_WORD}")

        // TODO: Replace with actual Porcupine init:
        // val porcupine = Porcupine.Builder()
        //     .setAccessKey("YOUR_PICOVOICE_KEY")
        //     .setKeyword(Porcupine.BuiltInKeyword.HEY_SIRI) // closest to "Hey Nova"
        //     .build(applicationContext)
        // Then start AudioRecord loop
    }

    private fun onWakeWordDetected() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            action = ACTION_WAKE
        }
        startActivity(intent)
        sendBroadcast(Intent(ACTION_WAKE))
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "nova Wake Word", NotificationManager.IMPORTANCE_LOW
        ).apply { description = "nova is listening for Hey Nova" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val intent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("nova")
            .setContentText("Listening for Hey Nova...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(intent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
