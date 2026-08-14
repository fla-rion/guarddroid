package dev.guarddroid.feature.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.guarddroid.core.common.AppStatus
import dev.guarddroid.core.common.Capability
import dev.guarddroid.core.common.CapabilityResult
import dev.guarddroid.core.database.dao.AppRuleDao
import dev.guarddroid.core.database.dao.SystemConfigDao
import dev.guarddroid.core.database.entity.AppRuleEntity
import dev.guarddroid.core.database.entity.ConfigKeys
import dev.guarddroid.core.database.entity.SystemConfigEntity
import dev.guarddroid.core.device.CapabilityEngine
import dev.guarddroid.core.device.DeviceAnalyzer
import dev.guarddroid.core.device.DeviceInfo
import dev.guarddroid.core.security.SecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val deviceAnalyzer: DeviceAnalyzer,
    private val capabilityEngine: CapabilityEngine,
    private val securityManager: SecurityManager,
    private val appRuleDao: AppRuleDao,
    private val systemConfigDao: SystemConfigDao
) : ViewModel() {

    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
    val deviceInfo: StateFlow<DeviceInfo?> = _deviceInfo

    private val _capabilities = MutableStateFlow<Map<Capability, CapabilityResult>>(emptyMap())
    val capabilities: StateFlow<Map<Capability, CapabilityResult>> = _capabilities

    private val _setupComplete = MutableStateFlow(false)
    val setupComplete: StateFlow<Boolean> = _setupComplete

    init {
        analyzeDevice()
    }

    fun analyzeDevice() {
        viewModelScope.launch {
            _deviceInfo.value = deviceAnalyzer.analyze()
            _capabilities.value = capabilityEngine.checkAll()
        }
    }

    fun setMasterCode(code: String): Result<Unit> =
        securityManager.setMasterCode(code)

    fun updateAppRule(packageName: String, appName: String, status: AppStatus, isSystem: Boolean) {
        viewModelScope.launch {
            appRuleDao.insertOrUpdate(
                AppRuleEntity(packageName, status, appName, null, isSystem)
            )
        }
    }

    fun setSystemConfig(key: String, value: String) {
        viewModelScope.launch {
            systemConfigDao.setConfig(SystemConfigEntity(key, value))
        }
    }

    fun completeSetup() {
        viewModelScope.launch {
            systemConfigDao.setConfig(SystemConfigEntity(ConfigKeys.SETUP_COMPLETE, "true"))
            _setupComplete.value = true
        }
    }
}
