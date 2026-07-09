package com.example.parentalcontrol.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.parentalcontrol.LockScreenActivity
import com.example.parentalcontrol.utils.PreferenceManager
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

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

    private var lastServiceStartTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        try {
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
                
                // Skip common system packages
                val isSystemPackage = (packageName == this.packageName || 
                    packageName == "com.android.systemui" || 
                    packageName == "com.android.launcher" || 
                    packageName == "com.mi.android.globallauncher" ||
                    packageName == "com.google.android.apps.nexuslauncher" ||
                    packageName == "com.android.settings" || 
                    packageName == "android")
                
                if (isSystemPackage) return

                // Skip if device is currently in a temporary "NFC Unlock" grace period
                if (preferenceManager.isCurrentlyUnlocked()) return

                // CRITICAL: Re-check restriction status from storage every time
                if (preferenceManager.isAppRestricted(packageName)) {
                    Log.i("AppBlockerService", "!! ACTION REQUIRED: Blocking $packageName")
                    redirectToLockScreen()
                } else {
                    // Periodically ensure foreground service is running if active
                    val now = System.currentTimeMillis()
                    if (now - lastServiceStartTime > 15000) { // Every 15 seconds
                        val isActiveSchedule = preferenceManager.getActiveSchedule() != null
                        if (isActiveSchedule || preferenceManager.isServiceRunning) {
                            val serviceIntent = Intent(this, ParentalControlService::class.java)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(serviceIntent)
                            } else {
                                startService(serviceIntent)
                            }
                        }
                        lastServiceStartTime = now
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AppBlockerService", "Error in onAccessibilityEvent", e)
        }
    }

    private fun redirectToLockScreen() {
        Log.d("AppBlockerService", "Attempting to redirect to LockScreenActivity")
        val intent = Intent(this, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or 
                     Intent.FLAG_ACTIVITY_SINGLE_TOP or 
                     Intent.FLAG_ACTIVITY_CLEAR_TOP or
                     Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                     Intent.FLAG_ACTIVITY_NO_ANIMATION)
            putExtra("EXTRA_LOCKED", true)
        }
        try {
            // On some devices, we might need to use a PendingIntent or start activity with options
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("AppBlockerService", "Failed to start LockScreenActivity", e)
        }
    }


    override fun onInterrupt() {}
}
