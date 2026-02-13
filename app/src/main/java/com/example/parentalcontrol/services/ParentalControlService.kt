package com.example.parentalcontrol.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.parentalcontrol.R
import com.example.parentalcontrol.utils.PreferenceManager

class ParentalControlService : Service() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate() {
        super.onCreate()
        preferenceManager = PreferenceManager(this)
        
        // Start foreground immediately as required by Android
        startForeground(1, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // If the service is started but the flag is off, stop itself.
        // This handles cases where the system might restart the service automatically.
        if (!preferenceManager.isServiceRunning) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "parental_control_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Parental Control Active",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Parental Control is Active")
            .setContentText("Monitoring and protection are running.")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
