package dev.guarddroid.feature.apps

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.guarddroid.core.common.AppStatus
import dev.guarddroid.core.database.dao.AppRuleDao
import dev.guarddroid.core.database.entity.AppRuleEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppItem(
    val packageName: String,
    val appName: String,
    val status: AppStatus,
    val isSystem: Boolean
)

@HiltViewModel
class AppListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appRuleDao: AppRuleDao
) : ViewModel() {

    val allowedApps: StateFlow<List<AppItem>> = appRuleDao
        .getRulesByStatuses(listOf(AppStatus.ALWAYS_ALLOWED))
        .map { rules -> rules.mapNotNull { rule -> rule.toAppItem() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allManagedApps: StateFlow<List<AppItem>> = appRuleDao
        .getAllRules()
        .map { rules -> rules.mapNotNull { it.toAppItem() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateAppStatus(packageName: String, status: AppStatus) {
        viewModelScope.launch {
            val existing = appRuleDao.getRuleByPackage(packageName)
            if (existing != null) {
                appRuleDao.insertOrUpdate(existing.copy(status = status))
            }
        }
    }

    private fun AppRuleEntity.toAppItem(): AppItem? {
        return try {
            AppItem(packageName, appName.ifEmpty { packageName }, status, isSystemApp)
        } catch (e: Exception) {
            null
        }
    }
}
