package com.vidhya.focuslock

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "focus_lock_prefs"
        private const val KEY_IS_LOCKED = "is_locked"
        private const val KEY_SESSION_END_TIME = "session_end_time"
        private const val KEY_SESSION_DURATION = "session_duration"
        private const val KEY_BLOCKED_PACKAGES = "blocked_packages"
        private const val KEY_EMERGENCY_PASS_ACTIVE = "emergency_pass_active"
        private const val KEY_EMERGENCY_EXPIRES = "emergency_expires"
    }

    var isLocked: Boolean
        get() = prefs.getBoolean(KEY_IS_LOCKED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOCKED, value).apply()

    var sessionEndTime: Long
        get() = prefs.getLong(KEY_SESSION_END_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_SESSION_END_TIME, value).apply()

    var sessionDurationMinutes: Int
        get() = prefs.getInt(KEY_SESSION_DURATION, 25)
        set(value) = prefs.edit().putInt(KEY_SESSION_DURATION, value).apply()

    var blockedPackages: Set<String>
        get() = prefs.getStringSet(KEY_BLOCKED_PACKAGES, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_BLOCKED_PACKAGES, value).apply()

    fun startSession(durationMinutes: Int) {
        val now = System.currentTimeMillis()
        val durationMs = durationMinutes * 60 * 1000L
        prefs.edit()
            .putBoolean(KEY_IS_LOCKED, true)
            .putLong(KEY_SESSION_END_TIME, now + durationMs)
            .putInt(KEY_SESSION_DURATION, durationMinutes)
            .putBoolean(KEY_EMERGENCY_PASS_ACTIVE, false)
            .apply()
    }

    fun endSession() {
        prefs.edit()
            .putBoolean(KEY_IS_LOCKED, false)
            .putLong(KEY_SESSION_END_TIME, 0L)
            .putBoolean(KEY_EMERGENCY_PASS_ACTIVE, false)
            .apply()
    }

    fun isSessionActive(): Boolean {
        if (!isLocked) return false
        val now = System.currentTimeMillis()
        if (now >= sessionEndTime) {
            endSession()
            return false
        }
        return true
    }

    fun getRemainingTimeMs(): Long {
        if (!isSessionActive()) return 0L
        return (sessionEndTime - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    fun grantEmergencyPass(durationMinutes: Int = 2) {
        val expires = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        prefs.edit()
            .putBoolean(KEY_EMERGENCY_PASS_ACTIVE, true)
            .putLong(KEY_EMERGENCY_EXPIRES, expires)
            .apply()
    }

    fun isEmergencyPassActive(): Boolean {
        val active = prefs.getBoolean(KEY_EMERGENCY_PASS_ACTIVE, false)
        val expires = prefs.getLong(KEY_EMERGENCY_EXPIRES, 0L)
        if (active && System.currentTimeMillis() < expires) {
            return true
        }
        if (active) {
            prefs.edit().putBoolean(KEY_EMERGENCY_PASS_ACTIVE, false).apply()
        }
        return false
    }

    fun isPackageBlocked(packageName: String): Boolean {
        if (!isSessionActive()) return false
        if (isEmergencyPassActive()) return false
        return blockedPackages.contains(packageName)
    }
}
