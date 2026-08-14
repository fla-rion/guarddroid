package dev.guarddroid.core.database.dao

import androidx.room.*
import dev.guarddroid.core.common.AppStatus
import dev.guarddroid.core.database.entity.AppRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppRuleDao {
    @Query("SELECT * FROM app_rules ORDER BY appName ASC")
    fun getAllRules(): Flow<List<AppRuleEntity>>

    @Query("SELECT * FROM app_rules WHERE packageName = :packageName")
    suspend fun getRuleByPackage(packageName: String): AppRuleEntity?

    @Query("SELECT * FROM app_rules WHERE status = :status")
    fun getRulesByStatus(status: AppStatus): Flow<List<AppRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(rule: AppRuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(rules: List<AppRuleEntity>)

    @Update
    suspend fun update(rule: AppRuleEntity)

    @Delete
    suspend fun delete(rule: AppRuleEntity)

    @Query("DELETE FROM app_rules WHERE packageName = :packageName")
    suspend fun deleteByPackage(packageName: String)

    @Query("SELECT * FROM app_rules WHERE status IN (:statuses)")
    fun getRulesByStatuses(statuses: List<AppStatus>): Flow<List<AppRuleEntity>>
}
