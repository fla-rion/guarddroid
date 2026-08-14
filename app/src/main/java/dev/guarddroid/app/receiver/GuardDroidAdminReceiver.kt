package dev.guarddroid.app.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dev.guarddroid.app.R

class GuardDroidAdminReceiver : DeviceAdminReceiver() {

    companion object {
        private const val TAG = "GuardDroidAdmin"
    }

    override fun onEnabled(context: Context, intent: Intent) {
        Log.i(TAG, "Device Admin enabled")
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return context.getString(R.string.admin_disable_warning)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Log.w(TAG, "Device Admin disabled")
    }

}
