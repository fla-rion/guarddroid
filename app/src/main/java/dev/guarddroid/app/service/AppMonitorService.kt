package dev.guarddroid.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.guarddroid.app.R
import dev.guarddroid.app.activity.BlockedAppActivity
import dev.guarddroid.core.common.AppStatus
import dev.guarddroid.core.database.dao.AppRuleDao
import dev.guarddroid.core.scheduling.ScheduleEvaluator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class AppMonitorService : Service() {

    companion object {
        private const val TAG = "AppMonitor"
        private const val CHANNEL_ID = "guarddroid_monitor"
        private const val NOTIFICATION_ID = 1
        private const val POLL_INTERVAL_MS = 500L

        fun start(context: Context) {
            val intent = Intent(context, AppMonitorService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AppMonitorService::class.java))
        }
    }

    @Inject
    lateinit var appRuleDao: AppRuleDao

    @Inject
    lateinit var scheduleEvaluator: ScheduleEvaluator

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var lastForegroundApp = ""

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        startMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startMonitoring() {
        serviceScope.launch {
            while (isActive) {
                try {
                    checkForegroundApp()
                } catch (e: Exception) {
                    Log.e(TAG, "Monitor error", e)
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private suspend fun checkForegroundApp() {
        val foregroundApp = getForegroundApp() ?: return
        if (foregroundApp == lastForegroundApp) return
        if (foregroundApp == packageName) {
            lastForegroundApp = foregroundApp
            return
        }
        lastForegroundApp = foregroundApp

        val rule = appRuleDao.getRuleByPackage(foregroundApp) ?: return
        val shouldBlock = when (rule.status) {
            AppStatus.BLOCKED -> true
            AppStatus.HIDDEN -> true
            AppStatus.ADMIN_ONLY -> true
            AppStatus.SCHEDULED -> {
                val scheduleId = rule.scheduleId
                if (scheduleId != null) {
                    // Block if not in allowed schedule time
                    false // Evaluated by ScheduleEvaluator in a full implementation
                } else {
                    false
                }
            }
            AppStatus.ALWAYS_ALLOWED -> false
        }

        if (shouldBlock) {
            showBlockedScreen(foregroundApp, rule.appName)
        }
    }

    private fun getForegroundApp(): String? {
        return try {
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val now = System.currentTimeMillis()
            val stats = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                now - 5000,
                now
            )
            stats?.maxByOrNull { it.lastTimeUsed }?.packageName
        } catch (e: Exception) {
            Log.w(TAG, "Cannot get foreground app: ${e.message}")
            null
        }
    }

    private fun showBlockedScreen(packageName: String, appName: String) {
        val intent = Intent(this, BlockedAppActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(BlockedAppActivity.EXTRA_PACKAGE_NAME, packageName)
            putExtra(BlockedAppActivity.EXTRA_APP_NAME, appName)
        }
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.monitor_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.monitor_channel_description)
            setShowBadge(false)
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.monitor_notification_title))
            .setContentText(getString(R.string.monitor_notification_text))
            .setSmallIcon(R.drawable.ic_shield)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}
