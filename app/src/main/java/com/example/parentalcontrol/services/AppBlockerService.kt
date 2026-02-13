package com.example.parentalcontrol.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.parentalcontrol.MainActivity
import com.example.parentalcontrol.utils.PreferenceManager

class AppBlockerService : AccessibilityService() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onServiceConnected() {
        super.onServiceConnected()
        preferenceManager = PreferenceManager(this)
        Log.d("AppBlockerService", "Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // IMPORTANT: We only process events and block apps IF the user 
        // has explicitly enabled "Start Protection" from the Home Screen.
        // Granting the permission alone will not trigger any blocking.
        if (!::preferenceManager.isInitialized || !preferenceManager.isServiceRunning) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Skip blocking if our own app is in foreground
            if (packageName == this.packageName) return

            // Skip blocking if the device is currently temporarily unlocked via NFC
            if (preferenceManager.isCurrentlyUnlocked()) return

            if (preferenceManager.isAppRestricted(packageName)) {
                redirectToLockScreen()
            }
        }
    }

    private fun redirectToLockScreen() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("EXTRA_LOCKED", true)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}
}
