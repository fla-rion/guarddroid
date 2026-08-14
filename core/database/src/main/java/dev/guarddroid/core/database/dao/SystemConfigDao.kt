package dev.guarddroid.core.database.dao

import androidx.room.*
import dev.guarddroid.core.database.entity.SystemConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SystemConfigDao {
    @Query("SELECT * FROM system_config WHERE `key` = :key")
    suspend fun getConfig(key: String): SystemConfigEntity?

    @Query("SELECT * FROM system_config")
    fun getAllConfigs(): Flow<List<SystemConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(config: SystemConfigEntity)

    @Query("DELETE FROM system_config WHERE `key` = :key")
    suspend fun deleteConfig(key: String)
}
