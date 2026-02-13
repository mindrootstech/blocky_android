package com.example.parentalcontrol.utils

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

data class AppGroup(
    val name: String,
    val packageNames: Set<String>,
    val startTime: String, // format "HH:mm"
    val endTime: String,   // format "HH:mm"
    val isEnabled: Boolean = true
)

data class BlockEvent(
    val packageName: String,
    val timestamp: Long
)

data class DetailedSession(
    val startTime: Long,
    val endTime: Long
) {
    val durationMs: Long get() = endTime - startTime
}

data class CapturedNotification(
    val packageName: String,
    val title: String,
    val content: String,
    val timestamp: Long
)

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("parental_prefs", Context.MODE_PRIVATE)
    private val ownPackageName = context.packageName

    companion object {
        private const val KEY_IS_LOCKED = "is_locked"
        private const val KEY_UNLOCK_EXPIRATION = "unlock_expiration"
        private const val KEY_NFC_ID = "authorized_nfc_id"
        private const val KEY_RESTRICTED_APPS = "restricted_apps"
        private const val KEY_STRICT_MODE = "strict_mode"
        private const val KEY_APP_GROUPS = "app_groups"
        private const val KEY_BLOCK_HISTORY = "block_history"
        private const val KEY_SERVICE_SESSIONS = "service_sessions"
        private const val KEY_LAST_SERVICE_START = "last_service_start"
        private const val KEY_CAPTURED_NOTIFICATIONS = "captured_notifications"
        private const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PASSWORD = "user_password"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    var isLocked: Boolean
        get() = prefs.getBoolean(KEY_IS_LOCKED, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOCKED, value).apply()

    var unlockExpiration: Long
        get() = prefs.getLong(KEY_UNLOCK_EXPIRATION, 0L)
        set(value) = prefs.edit().putLong(KEY_UNLOCK_EXPIRATION, value).apply()

    var authorizedNfcId: String?
        get() = prefs.getString(KEY_NFC_ID, "DEFAULT_UID")
        set(value) = prefs.edit().putString(KEY_NFC_ID, value).apply()

    var restrictedApps: Set<String>
        get() = prefs.getStringSet(KEY_RESTRICTED_APPS, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_RESTRICTED_APPS, value).apply()

    var isStrictMode: Boolean
        get() = prefs.getBoolean(KEY_STRICT_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_STRICT_MODE, value).apply()

    var lastServiceStartTime: Long
        get() = prefs.getLong(KEY_LAST_SERVICE_START, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_SERVICE_START, value).apply()

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_IS_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_FIRST_LAUNCH, value).apply()

    var userEmail: String?
        get() = prefs.getString(KEY_USER_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var userPassword: String?
        get() = prefs.getString(KEY_USER_PASSWORD, null)
        set(value) = prefs.edit().putString(KEY_USER_PASSWORD, value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var appGroups: List<AppGroup>
        get() {
            val json = prefs.getString(KEY_APP_GROUPS, "[]") ?: "[]"
            val array = JSONArray(json)
            val groups = mutableListOf<AppGroup>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val pkgs = obj.getJSONArray("packages")
                val pkgSet = mutableSetOf<String>()
                for (j in 0 until pkgs.length()) pkgSet.add(pkgs.getString(j))
                
                groups.add(AppGroup(
                    obj.getString("name"),
                    pkgSet,
                    obj.getString("start"),
                    obj.getString("end"),
                    obj.optBoolean("enabled", true)
                ))
            }
            return groups
        }
        set(value) {
            val array = JSONArray()
            value.forEach { group ->
                val obj = JSONObject()
                obj.put("name", group.name)
                obj.put("start", group.startTime)
                obj.put("end", group.endTime)
                obj.put("enabled", group.isEnabled)
                val pkgs = JSONArray()
                group.packageNames.forEach { pkgs.put(it) }
                obj.put("packages", pkgs)
                array.put(obj)
            }
            prefs.edit().putString(KEY_APP_GROUPS, array.toString()).apply()
        }

    var blockHistory: List<BlockEvent>
        get() {
            val json = prefs.getString(KEY_BLOCK_HISTORY, "[]") ?: "[]"
            val array = JSONArray(json)
            val history = mutableListOf<BlockEvent>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                history.add(BlockEvent(obj.getString("pkg"), obj.getLong("time")))
            }
            return history
        }
        set(value) {
            val array = JSONArray()
            val list = if (value.size > 500) value.takeLast(500) else value
            list.forEach { event ->
                val obj = JSONObject()
                obj.put("pkg", event.packageName)
                obj.put("time", event.timestamp)
                array.put(obj)
            }
            prefs.edit().putString(KEY_BLOCK_HISTORY, array.toString()).apply()
        }

    var detailedSessions: List<DetailedSession>
        get() {
            val json = prefs.getString(KEY_SERVICE_SESSIONS, "[]") ?: "[]"
            val array = JSONArray(json)
            val sessions = mutableListOf<DetailedSession>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                sessions.add(DetailedSession(obj.getLong("start"), obj.getLong("end")))
            }
            return sessions
        }
        set(value) {
            val array = JSONArray()
            value.forEach { session ->
                val obj = JSONObject()
                obj.put("start", session.startTime)
                obj.put("end", session.endTime)
                array.put(obj)
            }
            prefs.edit().putString(KEY_SERVICE_SESSIONS, array.toString()).apply()
        }

    var capturedNotifications: List<CapturedNotification>
        get() {
            val json = prefs.getString(KEY_CAPTURED_NOTIFICATIONS, "[]") ?: "[]"
            val array = JSONArray(json)
            val notifications = mutableListOf<CapturedNotification>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                notifications.add(CapturedNotification(
                    obj.getString("pkg"),
                    obj.getString("title"),
                    obj.getString("content"),
                    obj.getLong("time")
                ))
            }
            return notifications
        }
        set(value) {
            val array = JSONArray()
            val list = if (value.size > 200) value.takeLast(200) else value
            list.forEach { notification ->
                val obj = JSONObject()
                obj.put("pkg", notification.packageName)
                obj.put("title", notification.title)
                obj.put("content", notification.content)
                obj.put("time", notification.timestamp)
                array.put(obj)
            }
            prefs.edit().putString(KEY_CAPTURED_NOTIFICATIONS, array.toString()).apply()
        }

    fun addCapturedNotification(packageName: String, title: String, content: String) {
        val current = capturedNotifications.toMutableList()
        
        // Anti-duplicate check: Ignore if same content from same app was received in last 3 seconds
        val lastNotif = current.lastOrNull()
        if (lastNotif != null && 
            lastNotif.packageName == packageName && 
            lastNotif.title == title && 
            lastNotif.content == content && 
            System.currentTimeMillis() - lastNotif.timestamp < 3000) {
            return
        }

        current.add(CapturedNotification(packageName, title, content, System.currentTimeMillis()))
        capturedNotifications = current
    }

    fun recordSession(startTime: Long, endTime: Long) {
        val current = detailedSessions.toMutableList()
        current.add(DetailedSession(startTime, endTime))
        // Keep last 1000 sessions
        detailedSessions = if (current.size > 1000) current.takeLast(1000) else current
    }

    fun addBlockEvent(packageName: String) {
        val currentHistory = blockHistory.toMutableList()
        val lastEvent = currentHistory.lastOrNull { it.packageName == packageName }
        if (lastEvent == null || System.currentTimeMillis() - lastEvent.timestamp > 60000) {
            currentHistory.add(BlockEvent(packageName, System.currentTimeMillis()))
            blockHistory = currentHistory
        }
    }

    fun isCurrentlyUnlocked(): Boolean {
        return !isLocked || System.currentTimeMillis() < unlockExpiration
    }

    fun isAppRestricted(packageName: String): Boolean {
        // SAFEGUARD: Never restrict the parental control app itself
        if (packageName == ownPackageName) return false
        
        if (restrictedApps.contains(packageName)) return true
        val currentTime = Calendar.getInstance()
        val currentMinutes = currentTime.get(Calendar.HOUR_OF_DAY) * 60 + currentTime.get(Calendar.MINUTE)
        for (group in appGroups) {
            if (!group.isEnabled) continue
            if (group.packageNames.contains(packageName)) {
                val startParts = group.startTime.split(":")
                val endParts = group.endTime.split(":")
                if (startParts.size == 2 && endParts.size == 2) {
                    val startMin = startParts[0].toInt() * 60 + startParts[1].toInt()
                    val endMin = endParts[0].toInt() * 60 + endParts[1].toInt()
                    if (startMin < endMin) {
                        if (currentMinutes in startMin..endMin) return true
                    } else {
                        if (currentMinutes >= startMin || currentMinutes <= endMin) return true
                    }
                }
            }
        }
        return false
    }

    fun toggleAppRestriction(packageName: String) {
        val currentSet = restrictedApps.toMutableSet()
        if (currentSet.contains(packageName)) {
            currentSet.remove(packageName)
        } else {
            currentSet.add(packageName)
        }
        restrictedApps = currentSet
    }
}
