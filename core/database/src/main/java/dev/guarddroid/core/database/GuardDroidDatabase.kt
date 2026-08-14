package dev.guarddroid.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import dev.guarddroid.core.common.AppStatus
import dev.guarddroid.core.database.dao.AppRuleDao
import dev.guarddroid.core.database.dao.ScheduleDao
import dev.guarddroid.core.database.dao.SystemConfigDao
import dev.guarddroid.core.database.entity.AppRuleEntity
import dev.guarddroid.core.database.entity.ScheduleEntity
import dev.guarddroid.core.database.entity.SystemConfigEntity

@Database(
    entities = [AppRuleEntity::class, ScheduleEntity::class, SystemConfigEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class GuardDroidDatabase : RoomDatabase() {
    abstract fun appRuleDao(): AppRuleDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun systemConfigDao(): SystemConfigDao
}

class DatabaseConverters {
    @TypeConverter
    fun fromAppStatus(status: AppStatus): String = status.name

    @TypeConverter
    fun toAppStatus(name: String): AppStatus = AppStatus.valueOf(name)
}
