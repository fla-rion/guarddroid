package dev.guarddroid.core.management

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.UserManager
import dev.guarddroid.core.common.ManagementOperation

class DeviceOwnerProvider(private val context: Context) : ManagementProvider {

    private val dpm: DevicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComponent: ComponentName =
        ComponentName(context, "dev.guarddroid.app.receiver.GuardDroidAdminReceiver")

    override fun getProviderName() = "DeviceOwnerProvider"

    override fun getSupportedOperations(): Set<ManagementOperation> =
        ManagementOperation.values().toSet()

    override fun blockApp(packageName: String): Result<Unit> = runCatching {
        require(isActive()) { "Device Owner not active" }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dpm.setPackagesSuspended(adminComponent, arrayOf(packageName), true)
        }
    }

    override fun unblockApp(packageName: String): Result<Unit> = runCatching {
        require(isActive()) { "Device Owner not active" }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dpm.setPackagesSuspended(adminComponent, arrayOf(packageName), false)
        }
    }

    override fun hideApp(packageName: String): Result<Unit> = runCatching {
        require(isActive()) { "Device Owner not active" }
        dpm.setApplicationHidden(adminComponent, packageName, true)
    }

    override fun showApp(packageName: String): Result<Unit> = runCatching {
        require(isActive()) { "Device Owner not active" }
        dpm.setApplicationHidden(adminComponent, packageName, false)
    }

    override fun preventUninstall(packageName: String): Result<Unit> = runCatching {
        require(isActive()) { "Device Owner not active" }
        dpm.setUninstallBlocked(adminComponent, packageName, true)
    }

    override fun allowUninstall(packageName: String): Result<Unit> = runCatching {
        require(isActive()) { "Device Owner not active" }
        dpm.setUninstallBlocked(adminComponent, packageName, false)
    }

    override fun restrictInstallation(): Result<Unit> = runCatching {
        require(isActive()) { "Device Owner not active" }
        dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_INSTALL_APPS)
        dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES)
    }

    override fun allowInstallation(): Result<Unit> = runCatching {
        require(isActive()) { "Device Owner not active" }
        dpm.clearUserRestriction(adminComponent, UserManager.DISALLOW_INSTALL_APPS)
    }

    override fun restrictUnknownSources(): Result<Unit> = runCatching {
        require(isActive()) { "Device Owner not active" }
        dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY)
        }
    }

    override fun allowUnknownSources(): Result<Unit> = runCatching {
        require(isActive()) { "Device Owner not active" }
        dpm.clearUserRestriction(adminComponent, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dpm.clearUserRestriction(adminComponent, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY)
        }
    }

    override fun restrictSettings(): Result<Unit> = runCatching {
        require(isActive()) { "Device Owner not active" }
        dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_CONFIG_WIFI)
        dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_FACTORY_RESET)
        dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_ADD_USER)
        dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_DEBUGGING_FEATURES)
    }

    override fun allowSettings(): Result<Unit> = runCatching {
        require(isActive()) { "Device Owner not active" }
        dpm.clearUserRestriction(adminComponent, UserManager.DISALLOW_CONFIG_WIFI)
        dpm.clearUserRestriction(adminComponent, UserManager.DISALLOW_FACTORY_RESET)
        dpm.clearUserRestriction(adminComponent, UserManager.DISALLOW_ADD_USER)
        dpm.clearUserRestriction(adminComponent, UserManager.DISALLOW_DEBUGGING_FEATURES)
    }

    override fun enterRestrictedMode(): Result<Unit> = runCatching {
        require(isActive()) { "Device Owner not active" }
        // Restricted mode: apply all configured restrictions
    }

    private fun isActive(): Boolean = try {
        dpm.isDeviceOwnerApp(context.packageName)
    } catch (e: Exception) {
        false
    }
}
