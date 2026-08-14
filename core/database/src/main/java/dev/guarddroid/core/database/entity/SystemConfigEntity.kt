package dev.guarddroid.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_config")
data class SystemConfigEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)

object ConfigKeys {
    const val RESTRICT_INSTALL = "restrict_install"
    const val RESTRICT_UNKNOWN_SOURCES = "restrict_unknown_sources"
    const val RESTRICT_PLAY_STORE = "restrict_play_store"
    const val RESTRICT_SETTINGS = "restrict_settings"
    const val RESTRICT_DEVELOPER_OPTIONS = "restrict_developer_options"
    const val RESTRICT_USB_DEBUG = "restrict_usb_debug"
    const val RESTRICT_SYSTEM_APPS = "restrict_system_apps"
    const val RESTRICT_BROWSER = "restrict_browser"
    const val RESTRICT_FILE_MANAGER = "restrict_file_manager"
    const val ADMIN_TIMEOUT_MINUTES = "admin_timeout_minutes"
    const val SETUP_COMPLETE = "setup_complete"
}
