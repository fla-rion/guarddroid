package dev.guarddroid.feature.restrictions

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.guarddroid.core.database.dao.SystemConfigDao
import dev.guarddroid.core.database.entity.ConfigKeys
import dev.guarddroid.core.database.entity.SystemConfigEntity
import dev.guarddroid.core.management.ManagementProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class RestrictionConfig(
    val restrictInstall: Boolean = false,
    val restrictUnknownSources: Boolean = false,
    val restrictPlayStore: Boolean = false,
    val restrictSettings: Boolean = false,
    val restrictDeveloperOptions: Boolean = false,
    val restrictUsbDebug: Boolean = false,
    val restrictBrowser: Boolean = false,
    val restrictFileManager: Boolean = false
)

@Singleton
class RestrictionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val systemConfigDao: SystemConfigDao,
    private val managementProvider: ManagementProvider
) {
    fun getRestrictionConfig(): Flow<RestrictionConfig> =
        systemConfigDao.getAllConfigs().map { configs ->
            val map = configs.associateBy { it.key }
            RestrictionConfig(
                restrictInstall = map[ConfigKeys.RESTRICT_INSTALL]?.value == "true",
                restrictUnknownSources = map[ConfigKeys.RESTRICT_UNKNOWN_SOURCES]?.value == "true",
                restrictPlayStore = map[ConfigKeys.RESTRICT_PLAY_STORE]?.value == "true",
                restrictSettings = map[ConfigKeys.RESTRICT_SETTINGS]?.value == "true",
                restrictDeveloperOptions = map[ConfigKeys.RESTRICT_DEVELOPER_OPTIONS]?.value == "true",
                restrictUsbDebug = map[ConfigKeys.RESTRICT_USB_DEBUG]?.value == "true",
                restrictBrowser = map[ConfigKeys.RESTRICT_BROWSER]?.value == "true",
                restrictFileManager = map[ConfigKeys.RESTRICT_FILE_MANAGER]?.value == "true"
            )
        }

    suspend fun applyRestrictions(config: RestrictionConfig) {
        saveConfig(ConfigKeys.RESTRICT_INSTALL, config.restrictInstall)
        saveConfig(ConfigKeys.RESTRICT_UNKNOWN_SOURCES, config.restrictUnknownSources)
        saveConfig(ConfigKeys.RESTRICT_SETTINGS, config.restrictSettings)
        saveConfig(ConfigKeys.RESTRICT_USB_DEBUG, config.restrictUsbDebug)

        if (config.restrictInstall) managementProvider.restrictInstallation()
        else managementProvider.allowInstallation()

        if (config.restrictUnknownSources) managementProvider.restrictUnknownSources()
        else managementProvider.allowUnknownSources()

        if (config.restrictSettings) managementProvider.restrictSettings()
        else managementProvider.allowSettings()
    }

    private suspend fun saveConfig(key: String, value: Boolean) {
        systemConfigDao.setConfig(SystemConfigEntity(key, value.toString()))
    }
}
