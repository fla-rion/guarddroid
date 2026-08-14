package dev.guarddroid.core.management

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import dev.guarddroid.core.common.ManagementOperation

class DeviceAdminProvider(private val context: Context) : ManagementProvider {

    private val dpm: DevicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComponent: ComponentName =
        ComponentName(context, "dev.guarddroid.app.receiver.GuardDroidAdminReceiver")

    override fun getProviderName() = "DeviceAdminProvider"

    override fun getSupportedOperations(): Set<ManagementOperation> = setOf(
        ManagementOperation.PREVENT_UNINSTALL
    )

    override fun blockApp(packageName: String): Result<Unit> = Result.failure(
        UnsupportedOperationException("App blocking requires Device Owner. Current level: Device Admin.")
    )

    override fun unblockApp(packageName: String): Result<Unit> = Result.failure(
        UnsupportedOperationException("App unblocking requires Device Owner.")
    )

    override fun hideApp(packageName: String): Result<Unit> = Result.failure(
        UnsupportedOperationException("App hiding requires Device Owner.")
    )

    override fun showApp(packageName: String): Result<Unit> = Result.failure(
        UnsupportedOperationException("App showing requires Device Owner.")
    )

    override fun preventUninstall(packageName: String): Result<Unit> = runCatching {
        require(isActive()) { "Device Admin not active" }
        dpm.setUninstallBlocked(adminComponent, packageName, true)
    }

    override fun allowUninstall(packageName: String): Result<Unit> = runCatching {
        require(isActive()) { "Device Admin not active" }
        dpm.setUninstallBlocked(adminComponent, packageName, false)
    }

    override fun restrictInstallation(): Result<Unit> = Result.failure(
        UnsupportedOperationException("Installation restriction requires Device Owner.")
    )

    override fun allowInstallation(): Result<Unit> = Result.failure(
        UnsupportedOperationException("Installation control requires Device Owner.")
    )

    override fun restrictUnknownSources(): Result<Unit> = Result.failure(
        UnsupportedOperationException("Unknown sources restriction requires Device Owner.")
    )

    override fun allowUnknownSources(): Result<Unit> = Result.failure(
        UnsupportedOperationException("Unknown sources control requires Device Owner.")
    )

    override fun restrictSettings(): Result<Unit> = Result.failure(
        UnsupportedOperationException("Settings restriction requires Device Owner.")
    )

    override fun allowSettings(): Result<Unit> = Result.failure(
        UnsupportedOperationException("Settings control requires Device Owner.")
    )

    override fun enterRestrictedMode(): Result<Unit> = runCatching {
        require(isActive()) { "Device Admin not active" }
        // Limited restricted mode with DeviceAdmin only
    }

    private fun isActive(): Boolean = try {
        dpm.isAdminActive(adminComponent)
    } catch (e: Exception) {
        false
    }
}
