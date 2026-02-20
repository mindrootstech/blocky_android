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
        if (!::preferenceManager.isInitialized) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Skip blocking if our own app is in foreground
            if (packageName == this.packageName) return

            // Skip blocking if the device is currently temporarily unlocked via NFC
            if (preferenceManager.isCurrentlyUnlocked()) return

            // Check if the current app should be restricted (manual or scheduled)
            if (preferenceManager.isAppRestricted(packageName)) {
                Log.d("AppBlockerService", "Restricting access to: $packageName")
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
