package com.example.parentalcontrol.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.parentalcontrol.utils.PreferenceManager

class NotificationBlockerService : NotificationListenerService() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate() {
        super.onCreate()
        preferenceManager = PreferenceManager(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        
        // NEVER block notifications from our own app
        if (packageName == this.packageName) return

        // Check if the app is restricted
        if (preferenceManager.isAppRestricted(packageName)) {
            val title = sbn.notification.extras.getString("android.title") ?: "No Title"
            val text = sbn.notification.extras.getCharSequence("android.text")?.toString() ?: "No Content"

            // Save for parent to see later
            preferenceManager.addCapturedNotification(packageName, title, text)

            // Cancel (block) the notification
            cancelNotification(sbn.key)
        }
    }
}
