package dev.guarddroid.core.management

import android.content.Context
import dev.guarddroid.core.device.CapabilityEngine
import dev.guarddroid.core.device.DeviceAnalyzer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManagementFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceAnalyzer: DeviceAnalyzer,
    private val capabilityEngine: CapabilityEngine
) {
    fun getBestProvider(): ManagementProvider {
        val info = deviceAnalyzer.analyze()
        return when {
            info.isDeviceOwner -> DeviceOwnerProvider(context)
            info.isDeviceAdmin -> DeviceAdminProvider(context)
            else -> AccessibilityProvider(context)
        }
    }

    fun getProviderDescription(): String {
        val info = deviceAnalyzer.analyze()
        return when {
            info.isDeviceOwner -> "Device Owner - Vollständige Verwaltung verfügbar"
            info.isDeviceAdmin -> "Device Admin - Eingeschränkte Verwaltung (Deinstallationsschutz)"
            else -> "Kein Verwaltungsmodus - Nur Überwachung via Accessibility Service"
        }
    }
}
