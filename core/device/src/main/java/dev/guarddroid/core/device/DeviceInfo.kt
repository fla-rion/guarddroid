package dev.guarddroid.core.device

data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val device: String,
    val androidVersion: String,
    val apiLevel: Int,
    val buildId: String,
    val securityPatch: String?,
    val systemUi: SystemUi,
    val hasGms: Boolean,
    val hasPlayStore: Boolean,
    val hasHms: Boolean,
    val hasAppGallery: Boolean,
    val isDeviceAdmin: Boolean,
    val isDeviceOwner: Boolean
)

enum class SystemUi {
    STOCK,
    EMUI,
    HARMONY_OS,
    ONE_UI,
    MIUI_HYPER_OS,
    OTHER
}
