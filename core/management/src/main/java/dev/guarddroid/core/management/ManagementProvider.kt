package dev.guarddroid.core.management

import dev.guarddroid.core.common.ManagementOperation

interface ManagementProvider {
    fun blockApp(packageName: String): Result<Unit>
    fun unblockApp(packageName: String): Result<Unit>
    fun hideApp(packageName: String): Result<Unit>
    fun showApp(packageName: String): Result<Unit>
    fun preventUninstall(packageName: String): Result<Unit>
    fun allowUninstall(packageName: String): Result<Unit>
    fun restrictInstallation(): Result<Unit>
    fun allowInstallation(): Result<Unit>
    fun restrictUnknownSources(): Result<Unit>
    fun allowUnknownSources(): Result<Unit>
    fun restrictSettings(): Result<Unit>
    fun allowSettings(): Result<Unit>
    fun enterRestrictedMode(): Result<Unit>
    fun getSupportedOperations(): Set<ManagementOperation>
    fun getProviderName(): String
}
