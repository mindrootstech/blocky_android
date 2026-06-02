package com.example.parentalcontrol.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.parentalcontrol.model.Schedule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

data class AppGroup(
    val name: String,
    val packageNames: Set<String>,
    val startTime: String,
    val endTime: String,
    val isEnabled: Boolean = true
)

data class Mode(
    val name: String,
    val packageNames: Set<String>,
    val isEnabled: Boolean = false,
    val durationMinutes: Int = 15
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
    val name: String,
    val type: String, // "MODE" or "SCHEDULE"
    val startTime: Long,
    val endTime: Long,
    val durationMs: Long
)

open class PreferenceManager(context: Context?) {
    private val prefs: SharedPreferences? = context?.getSharedPreferences("parental_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    var isFirstLaunch: Boolean
        get() = prefs?.getBoolean("is_first_launch", true) ?: true
        set(value) { prefs?.edit()?.putBoolean("is_first_launch", value)?.apply() }

    var isPermissionOnboarded: Boolean
        get() = prefs?.getBoolean("is_permission_onboarded", false) ?: false
        set(value) { prefs?.edit()?.putBoolean("is_permission_onboarded", value)?.apply() }

    var isLocked: Boolean
        get() = prefs?.getBoolean("is_locked", true) ?: true
        set(value) { prefs?.edit()?.putBoolean("is_locked", value)?.apply() }

    var unlockExpiration: Long
        get() = prefs?.getLong("unlock_expiration", 0L) ?: 0L
        set(value) { prefs?.edit()?.putLong("unlock_expiration", value)?.apply() }

    var isServiceRunning: Boolean
        get() = prefs?.getBoolean("is_service_running", false) ?: false
        set(value) { prefs?.edit()?.putBoolean("is_service_running", value)?.apply() }

    var lastServiceStartTime: Long
        get() = prefs?.getLong("last_service_start_time", 0L) ?: 0L
        set(value) { prefs?.edit()?.putLong("last_service_start_time", value)?.apply() }

    var emergencyCount: Int
        get() = prefs?.getInt("emergency_count", 5) ?: 5
        set(value) { prefs?.edit()?.putInt("emergency_count", value)?.apply() }

    companion object {
        const val NFC_VERIFICATION_VALUE = "toggle_bool_variable"
    }

    var isStrictMode: Boolean
        get() = prefs?.getBoolean("is_strict_mode", false) ?: false
        set(value) { prefs?.edit()?.putBoolean("is_strict_mode", value)?.apply() }

    var restrictedApps: Set<String>
        get() = prefs?.getStringSet("restricted_apps", emptySet()) ?: emptySet()
        set(value) { prefs?.edit()?.putStringSet("restricted_apps", value)?.apply() }

    fun toggleAppRestriction(packageName: String) {
        val current = restrictedApps.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        restrictedApps = current
    }

    var scheduledApps: Set<String>
        get() = prefs?.getStringSet("scheduled_apps", emptySet()) ?: emptySet()
        set(value) { prefs?.edit()?.putStringSet("scheduled_apps", value)?.apply() }

    var modes: List<Mode>
        get() {
            val json = prefs?.getString("modes", null) ?: return emptyList()
            val type = object : TypeToken<List<Mode>>() {}.type
            return gson.fromJson(json, type)
        }
        set(value) {
            val json = gson.toJson(value)
            prefs?.edit()?.putString("modes", json)?.apply()
        }

    open var appGroups: List<AppGroup>
        get() {
            val json = prefs?.getString("app_groups", null) ?: return emptyList()
            val type = object : TypeToken<List<AppGroup>>() {}.type
            return gson.fromJson(json, type)
        }
        set(value) {
            val json = gson.toJson(value)
            prefs?.edit()?.putString("app_groups", json)?.apply()
        }

    var blockHistory: List<BlockEvent>
        get() {
            val json = prefs?.getString("block_history", null) ?: return emptyList()
            val type = object : TypeToken<List<BlockEvent>>() {}.type
            return gson.fromJson(json, type)
        }
        set(value) {
            val json = gson.toJson(value)
            prefs?.edit()?.putString("block_history", json)?.apply()
        }

    var capturedNotifications: List<CapturedNotification>
        get() {
            val json = prefs?.getString("captured_notifications", null) ?: return emptyList()
            val type = object : TypeToken<List<CapturedNotification>>() {}.type
            return gson.fromJson(json, type)
        }
        set(value) {
            val json = gson.toJson(value)
            prefs?.edit()?.putString("captured_notifications", json)?.apply()
        }

    var detailedSessions: List<DetailedSession>
        get() {
            val json = prefs?.getString("detailed_sessions", null) ?: return emptyList()
            val type = object : TypeToken<List<DetailedSession>>() {}.type
            return gson.fromJson(json, type)
        }
        set(value) {
            val json = gson.toJson(value)
            prefs?.edit()?.putString("detailed_sessions", json)?.apply()
        }

    var schedules: List<Schedule>
        get() {
            val json = prefs?.getString("schedules", null) ?: return emptyList()
            val type = object : TypeToken<List<Schedule>>() {}.type
            return gson.fromJson(json, type)
        }
        set(value) {
            val json = gson.toJson(value)
            prefs?.edit()?.putString("schedules", json)?.apply()
        }

    fun addDetailedSession(name: String, type: String, startTime: Long) {
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        if (duration < 5000) return // Ignore sessions shorter than 5 seconds

        val session = DetailedSession(name, type, startTime, endTime, duration)
        val current = detailedSessions.toMutableList()
        current.add(session)
        detailedSessions = current
    }

    fun getActiveSchedule(): Schedule? {
        val now = Calendar.getInstance()
        val currentDay = SimpleDateFormat("EEEE", Locale.getDefault()).format(now.time).uppercase()
        
        return schedules.find { schedule ->
            if (!schedule.isEnabled) return@find false
            if (!schedule.days.contains(currentDay)) return@find false
            
            val start = schedule.startTime
            val end = schedule.endTime
            
            val nowTime = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            val startTime = start.get(Calendar.HOUR_OF_DAY) * 60 + start.get(Calendar.MINUTE)
            val endTime = end.get(Calendar.HOUR_OF_DAY) * 60 + end.get(Calendar.MINUTE)
            
            if (startTime < endTime) {
                nowTime in startTime until endTime
            } else if (startTime > endTime) {
                nowTime >= startTime || nowTime < endTime
            } else {
                schedule.isEnabled
            }
        }
    }

    fun isAppRestricted(packageName: String): Boolean {
        val inActiveMode = modes.any { it.isEnabled && it.packageNames.contains(packageName) }
        val activeSchedule = getActiveSchedule()
        val inActiveSchedule = activeSchedule?.mode?.packageNames?.contains(packageName) ?: false

        return restrictedApps.contains(packageName) || 
               appGroups.any { group -> group.isEnabled && group.packageNames.contains(packageName) } ||
               inActiveMode ||
               inActiveSchedule
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
