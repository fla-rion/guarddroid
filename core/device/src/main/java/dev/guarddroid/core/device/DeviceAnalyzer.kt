package dev.guarddroid.core.device

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PKG_GMS = "com.google.android.gms"
        private const val PKG_PLAY_STORE = "com.android.vending"
        private const val PKG_HMS = "com.huawei.hwid"
        private const val PKG_APP_GALLERY = "com.huawei.appmarket"
        private const val PKG_MIUI_HOME = "com.miui.home"
        private const val PKG_ONE_UI_HOME = "com.sec.android.app.launcher"
    }

    fun analyze(): DeviceInfo {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, "dev.guarddroid.app.receiver.GuardDroidAdminReceiver")

        return DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            device = Build.DEVICE,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            buildId = Build.DISPLAY,
            securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else null,
            systemUi = detectSystemUi(),
            hasGms = isPackageInstalled(PKG_GMS),
            hasPlayStore = isPackageInstalled(PKG_PLAY_STORE),
            hasHms = isPackageInstalled(PKG_HMS),
            hasAppGallery = isPackageInstalled(PKG_APP_GALLERY),
            isDeviceAdmin = try { dpm.isAdminActive(adminComponent) } catch (e: Exception) { false },
            isDeviceOwner = try { dpm.isDeviceOwnerApp(context.packageName) } catch (e: Exception) { false }
        )
    }

    private fun detectSystemUi(): SystemUi {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()

        return when {
            isHarmonyOs() -> SystemUi.HARMONY_OS
            isEMUI() -> SystemUi.EMUI
            isOneUi() -> SystemUi.ONE_UI
            isMIUI() -> SystemUi.MIUI_HYPER_OS
            manufacturer.contains("huawei") || brand.contains("huawei") || brand.contains("honor") -> SystemUi.EMUI
            manufacturer.contains("samsung") -> SystemUi.ONE_UI
            manufacturer.contains("xiaomi") || manufacturer.contains("poco") || brand.contains("redmi") -> SystemUi.MIUI_HYPER_OS
            else -> SystemUi.STOCK
        }
    }

    private fun isHarmonyOs(): Boolean = try {
        Class.forName("com.huawei.system.BuildEx")
        val method = Class.forName("com.huawei.system.BuildEx").getMethod("getOsBrand")
        method.invoke(null)?.toString()?.lowercase()?.contains("harmony") == true
    } catch (e: Exception) {
        false
    }

    private fun isEMUI(): Boolean = try {
        val prop = System.getProperty("ro.build.version.emui")
        !prop.isNullOrEmpty()
    } catch (e: Exception) {
        false
    }

    private fun isOneUi(): Boolean = try {
        val prop = System.getProperty("ro.build.version.oneui")
        !prop.isNullOrEmpty() || isPackageInstalled(PKG_ONE_UI_HOME)
    } catch (e: Exception) {
        isPackageInstalled(PKG_ONE_UI_HOME)
    }

    private fun isMIUI(): Boolean = try {
        val prop = System.getProperty("ro.miui.ui.version.name")
        !prop.isNullOrEmpty() || isPackageInstalled(PKG_MIUI_HOME)
    } catch (e: Exception) {
        isPackageInstalled(PKG_MIUI_HOME)
    }

    private fun isPackageInstalled(packageName: String): Boolean = try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}
