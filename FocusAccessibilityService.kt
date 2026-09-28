package com.vidhya.focuslock

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class FocusAccessibilityService : AccessibilityService() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            // Avoid blocking our own app or launcher
            if (packageName == packageName) return
            if (packageName == "com.android.launcher" || packageName.contains("launcher")) return

            // Check if focus lock is active and app is blacklisted
            if (sessionManager.isSessionActive() && sessionManager.isPackageBlocked(packageName)) {
                // Launch strict overlay blocker
                val intent = Intent(this, BlockActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra(BlockActivity.EXTRA_BLOCKED_PACKAGE, packageName)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {
        // Service interrupted
    }
}
