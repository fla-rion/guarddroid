package dev.guarddroid.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.guarddroid.core.common.AppStatus

@Entity(tableName = "app_rules")
data class AppRuleEntity(
    @PrimaryKey val packageName: String,
    val status: AppStatus = AppStatus.ALWAYS_ALLOWED,
    val appName: String = "",
    val scheduleId: Long? = null,
    val isSystemApp: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
