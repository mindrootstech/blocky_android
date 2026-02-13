package com.example.parentalcontrol.services

import android.accessibilityservice.AccessibilityService
import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.parentalcontrol.MainActivity
import com.example.parentalcontrol.receivers.AdminReceiver
import com.example.parentalcontrol.utils.PreferenceManager

class AppBlockerService : AccessibilityService() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onServiceConnected() {
        super.onServiceConnected()
        preferenceManager = PreferenceManager(this)
        preferenceManager.lastServiceStartTime = System.currentTimeMillis()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return

        // 1. ALWAYS ALLOW OUR OWN APP
        if (packageName == this.packageName) return

        // 2. SETUP MODE BYPASS
        // If the app is not fully setup, we allow everything (especially Settings)
        // so the user can grant permissions without being blocked.
        if (!areAllPermissionsGranted()) return

        // 3. UNLOCK BYPASS
        // If the parent has temporarily unlocked the app (e.g., via NFC), bypass blocking.
        if (preferenceManager.isCurrentlyUnlocked()) return

        // --- STRICT MODE: UNINSTALL PROTECTION ---
        if (preferenceManager.isStrictMode) {
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                val uiText = findAllText(rootNode).lowercase()
                
                // Identify if the UI is trying to uninstall or deactivate this specific app
                val mentionsOurApp = uiText.contains("parental control") || 
                                    uiText.contains("com.example.parentalcontrol")

                val deactivationIntent = uiText.contains("deactivate") || 
                                        uiText.contains("uninstall") || 
                                        uiText.contains("force stop") ||
                                        uiText.contains("clear data") ||
                                        uiText.contains("delete") ||
                                        uiText.contains("remove")

                if (mentionsOurApp && deactivationIntent) {
                    // Check if we are still in setup mode - if so, don't trigger HOME
                    if (!areAllPermissionsGranted()) return

                    performGlobalAction(GLOBAL_ACTION_HOME)
                    preferenceManager.addBlockEvent("Uninstall Protection")
                    launchLockScreen(packageName)
                    return
                }
            }
        }

        // --- STANDARD APP BLOCKING ---
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || 
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            
            if (shouldBlockApp(packageName)) {
                preferenceManager.addBlockEvent(packageName)
                launchLockScreen(packageName)
            }
        }
    }

    private fun findAllText(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""
        val sb = StringBuilder()
        sb.append(node.text ?: "")
        sb.append(node.contentDescription ?: "")
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                sb.append(" ").append(findAllText(child))
                child.recycle()
            }
        }
        return sb.toString()
    }

    private fun shouldBlockApp(packageName: String): Boolean {
        if (packageName == this.packageName) return false
        if (packageName == "com.android.systemui") return false
        
        // Don't block settings while setup is being finalized
        if (packageName == "com.android.settings" && !areAllPermissionsGranted()) return false

        if (preferenceManager.isStrictMode) {
            if (packageName == "com.android.settings" || 
                packageName == "com.android.packageinstaller" || 
                packageName == "com.google.android.packageinstaller") {
                return true
            }
        }

        return preferenceManager.isAppRestricted(packageName)
    }

    private fun areAllPermissionsGranted(): Boolean {
        val accessibility = isAccessibilityServiceEnabled(this, AppBlockerService::class.java)
        val overlay = Settings.canDrawOverlays(this)
        val usage = isUsageStatsPermissionGranted()
        val notifications = isNotificationServiceEnabled(this)
        val admin = isAdminActive()
        return accessibility && overlay && usage && notifications && admin
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
        val expectedServiceName = ComponentName(context, service).flattenToString()
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return enabledServices?.contains(expectedServiceName) == true
    }

    private fun isNotificationServiceEnabled(context: Context): Boolean {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return flat?.contains(context.packageName) == true
    }

    private fun isAdminActive(): Boolean {
        val dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, AdminReceiver::class.java)
        return dpm.isAdminActive(adminComponent)
    }

    private fun isUsageStatsPermissionGranted(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun launchLockScreen(blockedPackage: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            // Removed CLEAR_TOP to prevent clearing the task when launching the lock screen
            putExtra("EXTRA_LOCKED", true)
            putExtra("BLOCKED_PACKAGE", blockedPackage)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}
}
