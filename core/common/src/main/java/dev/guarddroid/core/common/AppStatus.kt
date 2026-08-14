package dev.guarddroid.core.common

enum class AppStatus {
    ALWAYS_ALLOWED,
    SCHEDULED,
    ADMIN_ONLY,
    BLOCKED,
    HIDDEN
}

enum class CapabilityStatus {
    SUPPORTED,
    UNSUPPORTED,
    REQUIRES_PERMISSION,
    REQUIRES_DEVICE_OWNER,
    REQUIRES_SETUP,
    UNKNOWN
}

enum class Capability {
    APP_BLOCKING,
    APP_HIDING,
    APP_INSTALL_RESTRICTION,
    APP_UNINSTALL_RESTRICTION,
    LOCK_TASK,
    DEVICE_OWNER,
    DEVICE_ADMIN,
    USAGE_ACCESS,
    ACCESSIBILITY_SERVICE,
    UNKNOWN_SOURCES_RESTRICTION,
    SETTINGS_RESTRICTION
}

data class CapabilityResult(
    val capability: Capability,
    val status: CapabilityStatus,
    val reason: String = ""
)

enum class ManagementOperation {
    BLOCK_APP,
    HIDE_APP,
    PREVENT_UNINSTALL,
    RESTRICT_INSTALLATION,
    RESTRICT_UNKNOWN_SOURCES,
    RESTRICT_SETTINGS,
    LOCK_TASK
}
