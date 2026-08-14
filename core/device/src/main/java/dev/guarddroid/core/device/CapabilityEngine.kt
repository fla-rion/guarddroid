package dev.guarddroid.core.device

import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dev.guarddroid.core.common.Capability
import dev.guarddroid.core.common.CapabilityResult
import dev.guarddroid.core.common.CapabilityStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CapabilityEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceAnalyzer: DeviceAnalyzer
) {
    private val deviceInfo: DeviceInfo by lazy { deviceAnalyzer.analyze() }
    private val dpm: DevicePolicyManager by lazy {
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }
    private val adminComponent: ComponentName by lazy {
        ComponentName(context, "dev.guarddroid.app.receiver.GuardDroidAdminReceiver")
    }

    fun checkAll(): Map<Capability, CapabilityResult> =
        Capability.values().associateWith { check(it) }

    fun check(capability: Capability): CapabilityResult = when (capability) {
        Capability.DEVICE_OWNER -> checkDeviceOwner()
        Capability.DEVICE_ADMIN -> checkDeviceAdmin()
        Capability.APP_BLOCKING -> checkAppBlocking()
        Capability.APP_HIDING -> checkAppHiding()
        Capability.APP_INSTALL_RESTRICTION -> checkInstallRestriction()
        Capability.APP_UNINSTALL_RESTRICTION -> checkUninstallRestriction()
        Capability.LOCK_TASK -> checkLockTask()
        Capability.USAGE_ACCESS -> checkUsageAccess()
        Capability.ACCESSIBILITY_SERVICE -> checkAccessibilityService()
        Capability.UNKNOWN_SOURCES_RESTRICTION -> checkUnknownSourcesRestriction()
        Capability.SETTINGS_RESTRICTION -> checkSettingsRestriction()
    }

    private fun checkDeviceOwner(): CapabilityResult {
        return if (deviceInfo.isDeviceOwner) {
            CapabilityResult(Capability.DEVICE_OWNER, CapabilityStatus.SUPPORTED)
        } else {
            CapabilityResult(
                Capability.DEVICE_OWNER,
                CapabilityStatus.REQUIRES_SETUP,
                "Device Owner requires ADB setup or NFC/QR provisioning: " +
                    "adb shell dpm set-device-owner dev.guarddroid.app/.receiver.GuardDroidAdminReceiver"
            )
        }
    }

    private fun checkDeviceAdmin(): CapabilityResult {
        return if (deviceInfo.isDeviceAdmin) {
            CapabilityResult(Capability.DEVICE_ADMIN, CapabilityStatus.SUPPORTED)
        } else {
            CapabilityResult(
                Capability.DEVICE_ADMIN,
                CapabilityStatus.REQUIRES_SETUP,
                "Device Admin must be activated in Settings > Security > Device Administrators"
            )
        }
    }

    private fun checkAppBlocking(): CapabilityResult {
        return when {
            deviceInfo.isDeviceOwner -> CapabilityResult(
                Capability.APP_BLOCKING,
                CapabilityStatus.SUPPORTED,
                "Full app suspension via DevicePolicyManager"
            )
            deviceInfo.isDeviceAdmin -> CapabilityResult(
                Capability.APP_BLOCKING,
                CapabilityStatus.REQUIRES_DEVICE_OWNER,
                "setPackagesSuspended requires Device Owner. Basic blocking via AccessibilityService available."
            )
            hasUsageStatsPermission() -> CapabilityResult(
                Capability.APP_BLOCKING,
                CapabilityStatus.REQUIRES_SETUP,
                "Limited blocking via AccessibilityService monitoring"
            )
            else -> CapabilityResult(
                Capability.APP_BLOCKING,
                CapabilityStatus.REQUIRES_PERMISSION,
                "Requires Usage Stats permission and/or Device Admin"
            )
        }
    }

    private fun checkAppHiding(): CapabilityResult {
        return if (deviceInfo.isDeviceOwner) {
            CapabilityResult(Capability.APP_HIDING, CapabilityStatus.SUPPORTED)
        } else {
            CapabilityResult(
                Capability.APP_HIDING,
                CapabilityStatus.REQUIRES_DEVICE_OWNER,
                "setApplicationHidden() requires Device Owner"
            )
        }
    }

    private fun checkInstallRestriction(): CapabilityResult {
        return when {
            deviceInfo.isDeviceOwner -> CapabilityResult(
                Capability.APP_INSTALL_RESTRICTION,
                CapabilityStatus.SUPPORTED,
                "Full restriction via addUserRestriction(DISALLOW_INSTALL_APPS)"
            )
            deviceInfo.isDeviceAdmin -> CapabilityResult(
                Capability.APP_INSTALL_RESTRICTION,
                CapabilityStatus.REQUIRES_DEVICE_OWNER,
                "DISALLOW_INSTALL_APPS requires Device Owner"
            )
            else -> CapabilityResult(
                Capability.APP_INSTALL_RESTRICTION,
                CapabilityStatus.REQUIRES_DEVICE_OWNER,
                "Installation restriction requires Device Owner"
            )
        }
    }

    private fun checkUninstallRestriction(): CapabilityResult {
        return if (deviceInfo.isDeviceOwner || deviceInfo.isDeviceAdmin) {
            CapabilityResult(
                Capability.APP_UNINSTALL_RESTRICTION,
                CapabilityStatus.SUPPORTED,
                "setUninstallBlocked() available"
            )
        } else {
            CapabilityResult(
                Capability.APP_UNINSTALL_RESTRICTION,
                CapabilityStatus.REQUIRES_DEVICE_OWNER,
                "setUninstallBlocked() requires Device Admin or Device Owner"
            )
        }
    }

    private fun checkLockTask(): CapabilityResult {
        return if (deviceInfo.isDeviceOwner) {
            CapabilityResult(Capability.LOCK_TASK, CapabilityStatus.SUPPORTED)
        } else {
            CapabilityResult(
                Capability.LOCK_TASK,
                CapabilityStatus.REQUIRES_DEVICE_OWNER,
                "Lock Task mode requires Device Owner"
            )
        }
    }

    private fun checkUsageAccess(): CapabilityResult {
        return if (hasUsageStatsPermission()) {
            CapabilityResult(Capability.USAGE_ACCESS, CapabilityStatus.SUPPORTED)
        } else {
            CapabilityResult(
                Capability.USAGE_ACCESS,
                CapabilityStatus.REQUIRES_PERMISSION,
                "Requires Usage Access permission in Settings > Apps > Special App Access"
            )
        }
    }

    private fun checkAccessibilityService(): CapabilityResult {
        return if (isAccessibilityServiceEnabled()) {
            CapabilityResult(Capability.ACCESSIBILITY_SERVICE, CapabilityStatus.SUPPORTED)
        } else {
            CapabilityResult(
                Capability.ACCESSIBILITY_SERVICE,
                CapabilityStatus.REQUIRES_SETUP,
                "Enable GuardDroid in Settings > Accessibility"
            )
        }
    }

    private fun checkUnknownSourcesRestriction(): CapabilityResult {
        return if (deviceInfo.isDeviceOwner) {
            CapabilityResult(
                Capability.UNKNOWN_SOURCES_RESTRICTION,
                CapabilityStatus.SUPPORTED,
                "DISALLOW_INSTALL_UNKNOWN_SOURCES via DevicePolicyManager"
            )
        } else {
            CapabilityResult(
                Capability.UNKNOWN_SOURCES_RESTRICTION,
                CapabilityStatus.REQUIRES_DEVICE_OWNER,
                "Requires Device Owner to restrict unknown sources system-wide"
            )
        }
    }

    private fun checkSettingsRestriction(): CapabilityResult {
        return when {
            deviceInfo.isDeviceOwner -> CapabilityResult(
                Capability.SETTINGS_RESTRICTION,
                CapabilityStatus.SUPPORTED,
                "Multiple settings restrictions available via addUserRestriction()"
            )
            deviceInfo.isDeviceAdmin -> CapabilityResult(
                Capability.SETTINGS_RESTRICTION,
                CapabilityStatus.REQUIRES_DEVICE_OWNER,
                "Only limited settings restrictions without Device Owner"
            )
            else -> CapabilityResult(
                Capability.SETTINGS_RESTRICTION,
                CapabilityStatus.REQUIRES_DEVICE_OWNER,
                "Settings restrictions require Device Owner"
            )
        }
    }

    private fun hasUsageStatsPermission(): Boolean = try {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        mode == AppOpsManager.MODE_ALLOWED
    } catch (e: Exception) {
        false
    }

    private fun isAccessibilityServiceEnabled(): Boolean = try {
        val serviceName = "${context.packageName}/dev.guarddroid.app.service.GuardDroidAccessibilityService"
        val enabledServices = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""
        enabledServices.contains(serviceName)
    } catch (e: Exception) {
        false
    }
}
