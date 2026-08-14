package dev.guarddroid.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.guarddroid.core.device.CapabilityEngine
import dev.guarddroid.core.device.DeviceAnalyzer
import dev.guarddroid.core.management.ManagementFactory
import dev.guarddroid.core.management.ManagementProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideManagementProvider(
        @ApplicationContext context: Context,
        factory: ManagementFactory
    ): ManagementProvider = factory.getBestProvider()
}
