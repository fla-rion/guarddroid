package dev.guarddroid.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import dev.guarddroid.app.activity.BlockedAppActivity

class GuardDroidAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "GuardA11y"
        var instance: GuardDroidAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        instance = this
        Log.i(TAG, "Accessibility Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            if (packageName == this.packageName) return
            // Window state changed - a new app came to foreground
            // The actual blocking logic is handled by AppMonitorService via UsageStats
            // This service provides a secondary signal and can perform back/home navigation
            Log.d(TAG, "Window state changed: $packageName")
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility Service interrupted")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    fun performGlobalBack() {
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun performGlobalHome() {
        performGlobalAction(GLOBAL_ACTION_HOME)
    }
}
