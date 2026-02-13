package com.example.parentalcontrol.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class AppGroup(
    val name: String,
    val packageNames: Set<String>,
    val startTime: String,
    val endTime: String,
    val isEnabled: Boolean = true
)

data class BlockEvent(
    val packageName: String,
    val timestamp: Long
)

data class CapturedNotification(
    val packageName: String,
    val title: String,
    val content: String,
    val timestamp: Long
)

data class DetailedSession(
    val startTime: Long,
    val endTime: Long,
    val durationMs: Long
)

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("parental_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean("is_first_launch", true)
        set(value) = prefs.edit().putBoolean("is_first_launch", value).apply()

    var isLocked: Boolean
        get() = prefs.getBoolean("is_locked", true)
        set(value) = prefs.edit().putBoolean("is_locked", value).apply()

    var unlockExpiration: Long
        get() = prefs.getLong("unlock_expiration", 0L)
        set(value) = prefs.edit().putLong("unlock_expiration", value).apply()

    var isServiceRunning: Boolean
        get() = prefs.getBoolean("is_service_running", false)
        set(value) = prefs.edit().putBoolean("is_service_running", value).apply()

    var lastServiceStartTime: Long
        get() = prefs.getLong("last_service_start_time", 0L)
        set(value) = prefs.edit().putLong("last_service_start_time", value).apply()

    var isStrictMode: Boolean
        get() = prefs.getBoolean("is_strict_mode", false)
        set(value) = prefs.edit().putBoolean("is_strict_mode", value).apply()

    var restrictedApps: Set<String>
        get() = prefs.getStringSet("restricted_apps", emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet("restricted_apps", value).apply()

    fun toggleAppRestriction(packageName: String) {
        val current = restrictedApps.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        restrictedApps = current
    }

    var appGroups: List<AppGroup>
        get() {
            val json = prefs.getString("app_groups", null) ?: return emptyList()
            val type = object : TypeToken<List<AppGroup>>() {}.type
            return gson.fromJson(json, type)
        }
        set(value) {
            val json = gson.toJson(value)
            prefs.edit().putString("app_groups", json).apply()
        }

    var blockHistory: List<BlockEvent>
        get() {
            val json = prefs.getString("block_history", null) ?: return emptyList()
            val type = object : TypeToken<List<BlockEvent>>() {}.type
            return gson.fromJson(json, type)
        }
        set(value) {
            val json = gson.toJson(value)
            prefs.edit().putString("block_history", json).apply()
        }

    var capturedNotifications: List<CapturedNotification>
        get() {
            val json = prefs.getString("captured_notifications", null) ?: return emptyList()
            val type = object : TypeToken<List<CapturedNotification>>() {}.type
            return gson.fromJson(json, type)
        }
        set(value) {
            val json = gson.toJson(value)
            prefs.edit().putString("captured_notifications", json).apply()
        }

    var detailedSessions: List<DetailedSession>
        get() {
            val json = prefs.getString("detailed_sessions", null) ?: return emptyList()
            val type = object : TypeToken<List<DetailedSession>>() {}.type
            return gson.fromJson(json, type)
        }
        set(value) {
            val json = gson.toJson(value)
            prefs.edit().putString("detailed_sessions", json).apply()
        }

    fun isAppRestricted(packageName: String): Boolean {
        return restrictedApps.contains(packageName) || appGroups.any { group -> group.isEnabled && group.packageNames.contains(packageName) }
    }

    fun addCapturedNotification(packageName: String, title: String, content: String) {
        val notification = CapturedNotification(packageName, title, content, System.currentTimeMillis())
        val current = capturedNotifications.toMutableList()
        current.add(notification)
        capturedNotifications = current
    }

    fun isCurrentlyUnlocked(): Boolean {
        return System.currentTimeMillis() < unlockExpiration
    }
}
