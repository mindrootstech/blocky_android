package com.example.parentalcontrol.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import com.example.parentalcontrol.model.Schedule
import com.example.parentalcontrol.receivers.ScheduleReceiver
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
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

open class PreferenceManager(private val context: Context?) {
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
            return try {
                gson.fromJson<List<Mode>>(json, type)
            } catch (e: Exception) {
                Log.e("PreferenceManager", "Error loading modes", e)
                emptyList()
            }
        }
        set(value) {
            val json = gson.toJson(value)
            prefs?.edit()?.putString("modes", json)?.apply()
        }

    var appGroups: List<AppGroup>
        get() {
            val json = prefs?.getString("app_groups", null) ?: return emptyList()
            val type = object : TypeToken<List<AppGroup>>() {}.type
            return try {
                gson.fromJson(json, type)
            } catch (e: Exception) {
                Log.e("PreferenceManager", "Error loading app groups", e)
                emptyList()
            }
        }
        set(value) {
            val json = gson.toJson(value)
            prefs?.edit()?.putString("app_groups", json)?.apply()
        }

    var blockHistory: List<BlockEvent>
        get() {
            val json = prefs?.getString("block_history", null) ?: return emptyList()
            val type = object : TypeToken<List<BlockEvent>>() {}.type
            return try {
                gson.fromJson(json, type)
            } catch (e: Exception) {
                emptyList()
            }
        }
        set(value) {
            val json = gson.toJson(value)
            prefs?.edit()?.putString("block_history", json)?.apply()
        }

    var capturedNotifications: List<CapturedNotification>
        get() {
            val json = prefs?.getString("captured_notifications", null) ?: return emptyList()
            val type = object : TypeToken<List<CapturedNotification>>() {}.type
            return try {
                gson.fromJson(json, type)
            } catch (e: Exception) {
                emptyList()
            }
        }
        set(value) {
            val json = gson.toJson(value)
            prefs?.edit()?.putString("captured_notifications", json)?.apply()
        }

    var detailedSessions: List<DetailedSession>
        get() {
            val json = prefs?.getString("detailed_sessions", null) ?: return emptyList()
            val type = object : TypeToken<List<DetailedSession>>() {}.type
            return try {
                gson.fromJson(json, type)
            } catch (e: Exception) {
                emptyList()
            }
        }
        set(value) {
            val json = gson.toJson(value)
            prefs?.edit()?.putString("detailed_sessions", json)?.apply()
        }

    var schedules: List<Schedule>
        get() {
            val json = prefs?.getString("schedules", null) ?: return emptyList()
            val type = object : TypeToken<List<Schedule>>() {}.type
            return try {
                gson.fromJson<List<Schedule>>(json, type)
            } catch (e: Exception) {
                Log.e("PreferenceManager", "Error loading schedules", e)
                emptyList()
            }
        }
        set(value) {
            val json = gson.toJson(value)
            prefs?.edit()?.putString("schedules", json)?.apply()
            updateAlarms(value)
        }

    fun updateAlarms(currentSchedules: List<Schedule> = schedules) {
        val context = this.context ?: return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Find next transition time
        val nextTransitionTime = findNextScheduleTransition(currentSchedules) ?: return
        
        val intent = Intent(context, ScheduleReceiver::class.java).apply {
            action = "com.example.parentalcontrol.ACTION_SCHEDULE_ALARM"
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context, 
            1001, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextTransitionTime,
            pendingIntent
        )
        Log.i("PreferenceManager", "Next schedule alarm set for: ${Date(nextTransitionTime)}")
    }

    private fun findNextScheduleTransition(currentSchedules: List<Schedule>): Long? {
        val now = Calendar.getInstance()
        val currentMillis = now.timeInMillis
        var soonestTransition: Long? = null

        currentSchedules.filter { it.isEnabled }.forEach { schedule ->
            // Check start and end times for the next 7 days
            for (i in 0..7) {
                val dayCheck = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, i) }
                val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(dayCheck.time).uppercase()
                
                if (schedule.days.contains(dayName)) {
                    val startRef = Calendar.getInstance().apply { timeInMillis = schedule.startTimeMs }
                    val endRef = Calendar.getInstance().apply { timeInMillis = schedule.endTimeMs }

                    // Check start
                    val startCal = (dayCheck.clone() as Calendar).apply {
                        set(Calendar.HOUR_OF_DAY, startRef.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, startRef.get(Calendar.MINUTE))
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    if (startCal.timeInMillis > currentMillis) {
                        soonestTransition = minOf(soonestTransition ?: Long.MAX_VALUE, startCal.timeInMillis)
                    }

                    // Check end
                    val endCal = (dayCheck.clone() as Calendar).apply {
                        set(Calendar.HOUR_OF_DAY, endRef.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, endRef.get(Calendar.MINUTE))
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    if (endCal.timeInMillis > currentMillis) {
                        soonestTransition = minOf(soonestTransition ?: Long.MAX_VALUE, endCal.timeInMillis)
                    }
                }
            }
        }
        return soonestTransition
    }

    fun addDetailedSession(name: String, type: String, startTime: Long) {
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        if (duration < 1000) return // Ignore sessions shorter than 1 second for testing

        val session = DetailedSession(name, type, startTime, endTime, duration)
        val current = detailedSessions.toMutableList()
        current.add(session)
        detailedSessions = current
        Log.d("PreferenceManager", "Session Added: $name, Duration: ${duration}ms")
    }

    fun getActiveSchedule(): Schedule? {
        val now = Calendar.getInstance()
        val currentDay = SimpleDateFormat("EEEE", Locale.getDefault()).format(now.time).uppercase()
        
        return schedules.find { schedule ->
            if (!schedule.isEnabled) return@find false
            if (!schedule.days.contains(currentDay)) return@find false
            
            val start = Calendar.getInstance().apply { timeInMillis = schedule.startTimeMs }
            val end = Calendar.getInstance().apply { timeInMillis = schedule.endTimeMs }
            
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
        val currentModes = modes
        val activeMode = currentModes.find { it.isEnabled }
        val activeSchedule = getActiveSchedule()
        
        // 1. Manual Protection
        val isManualBlocked = isServiceRunning && activeMode?.packageNames?.contains(packageName) == true
        
        // 2. Scheduled Protection
        val isScheduledBlocked = activeSchedule != null && activeSchedule.mode.packageNames.contains(packageName)

        // 3. Specific Apps or Groups
        val isOtherRestricted = restrictedApps.contains(packageName) || 
               appGroups.any { group -> group.isEnabled && group.packageNames.contains(packageName) }

        return isManualBlocked || isScheduledBlocked || isOtherRestricted
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
