package dev.guarddroid.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.guarddroid.core.common.AppStatus
import dev.guarddroid.core.database.dao.AppRuleDao
import dev.guarddroid.core.database.dao.SystemConfigDao
import dev.guarddroid.core.database.entity.AppRuleEntity
import dev.guarddroid.core.management.ManagementProvider
import dev.guarddroid.core.security.SecurityManager
import dev.guarddroid.core.update.GitHubUpdateChecker
import dev.guarddroid.core.update.UpdateCheckResult
import dev.guarddroid.core.update.UpdateInfo
import dev.guarddroid.core.update.UpdateNotificationManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminUiState {
    object Idle : AdminUiState()
    object Authenticated : AdminUiState()
    data class Error(val message: String) : AdminUiState()
    data class LockedOut(val remainingSeconds: Long) : AdminUiState()
}

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class Available(val info: UpdateInfo) : UpdateState()
    object UpToDate : UpdateState()
    data class Error(val message: String) : UpdateState()
}

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val securityManager: SecurityManager,
    private val managementProvider: ManagementProvider,
    private val appRuleDao: AppRuleDao,
    private val systemConfigDao: SystemConfigDao,
    private val updateChecker: GitHubUpdateChecker,
    private val updateNotificationManager: UpdateNotificationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Idle)
    val uiState: StateFlow<AdminUiState> = _uiState

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    val allApps: StateFlow<List<AppRuleEntity>> = appRuleDao.getAllRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun verifyCode(code: String) {
        viewModelScope.launch {
            if (securityManager.isLockedOut()) {
                _uiState.value = AdminUiState.LockedOut(securityManager.getLockoutRemainingSeconds())
                return@launch
            }
            if (securityManager.verifyMasterCode(code)) {
                _uiState.value = AdminUiState.Authenticated
            } else {
                val attempts = securityManager.getFailedAttempts()
                _uiState.value = AdminUiState.Error("Wrong code ($attempts failed attempts)")
            }
        }
    }

    fun updateAppStatus(packageName: String, status: AppStatus) {
        viewModelScope.launch {
            val existing = appRuleDao.getRuleByPackage(packageName) ?: return@launch
            val updated = existing.copy(status = status)
            appRuleDao.insertOrUpdate(updated)
            when (status) {
                AppStatus.HIDDEN -> managementProvider.hideApp(packageName)
                AppStatus.BLOCKED -> managementProvider.blockApp(packageName)
                AppStatus.ALWAYS_ALLOWED -> {
                    managementProvider.showApp(packageName)
                    managementProvider.unblockApp(packageName)
                }
                else -> {}
            }
        }
    }

    fun blockApp(packageName: String) = updateAppStatus(packageName, AppStatus.BLOCKED)
    fun unblockApp(packageName: String) = updateAppStatus(packageName, AppStatus.ALWAYS_ALLOWED)
    fun hideApp(packageName: String) = updateAppStatus(packageName, AppStatus.HIDDEN)
    fun showApp(packageName: String) = updateAppStatus(packageName, AppStatus.ALWAYS_ALLOWED)

    fun preventUninstall(packageName: String) {
        viewModelScope.launch {
            managementProvider.preventUninstall(packageName)
        }
    }

    fun checkForUpdate() {
        viewModelScope.launch {
            _updateState.value = UpdateState.Checking
            _updateState.value = when (val result = updateChecker.checkForUpdate()) {
                is UpdateCheckResult.UpdateAvailable -> {
                    updateNotificationManager.showUpdateNotification(result.info)
                    UpdateState.Available(result.info)
                }
                is UpdateCheckResult.UpToDate -> UpdateState.UpToDate
                is UpdateCheckResult.Error -> UpdateState.Error(result.message)
            }
        }
    }
}
