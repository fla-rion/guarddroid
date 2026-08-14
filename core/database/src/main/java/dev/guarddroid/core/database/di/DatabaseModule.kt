package dev.guarddroid.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.guarddroid.core.database.GuardDroidDatabase
import dev.guarddroid.core.database.dao.AppRuleDao
import dev.guarddroid.core.database.dao.ScheduleDao
import dev.guarddroid.core.database.dao.SystemConfigDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GuardDroidDatabase =
        Room.databaseBuilder(context, GuardDroidDatabase::class.java, "guarddroid.db")
            .build()

    @Provides
    fun provideAppRuleDao(db: GuardDroidDatabase): AppRuleDao = db.appRuleDao()

    @Provides
    fun provideScheduleDao(db: GuardDroidDatabase): ScheduleDao = db.scheduleDao()

    @Provides
    fun provideSystemConfigDao(db: GuardDroidDatabase): SystemConfigDao = db.systemConfigDao()
}
