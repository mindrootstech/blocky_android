package com.example.parentalcontrol.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.parentalcontrol.LockScreenActivity
import com.example.parentalcontrol.utils.PreferenceManager

class AppBlockerService : AccessibilityService() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate() {
        super.onCreate()
        preferenceManager = PreferenceManager(this)
        Log.d("AppBlockerService", "Accessibility Service Created")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Ensure preferenceManager is initialized even if onCreate wasn't called (unlikely)
        if (!::preferenceManager.isInitialized) {
            preferenceManager = PreferenceManager(this)
        }
        Log.d("AppBlockerService", "Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!::preferenceManager.isInitialized) {
            preferenceManager = PreferenceManager(this)
        }

        val eventType = event.eventType
        val packageName = event.packageName?.toString() ?: return

        // We listen for multiple event types to ensure we catch all app transitions
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || 
            eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED ||
            eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED ||
            eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            
            // Log for debugging (Important to see if events arrive after termination)
            Log.i("AppBlockerService", "Process alive check. Package: $packageName")

            // Skip common system packages
            if (packageName == this.packageName || 
                packageName == "com.android.systemui" || 
                packageName == "com.android.launcher" || 
                packageName == "com.google.android.apps.nexuslauncher" ||
                packageName == "com.android.settings" || 
                packageName == "android") return

            // Skip if device is currently in a temporary "NFC Unlock" grace period
            if (preferenceManager.isCurrentlyUnlocked()) return

            // CRITICAL: Re-check restriction status from storage every time
            if (preferenceManager.isAppRestricted(packageName)) {
                Log.i("AppBlockerService", "!! ACTION REQUIRED: Blocking $packageName")
                redirectToLockScreen()
            }
        }
    }

    private fun redirectToLockScreen() {
        val intent = Intent(this, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or 
                     Intent.FLAG_ACTIVITY_SINGLE_TOP or 
                     Intent.FLAG_ACTIVITY_CLEAR_TOP or
                     Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                     Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("AppBlockerService", "Failed to start LockScreenActivity", e)
        }
    }


    override fun onInterrupt() {}
}
