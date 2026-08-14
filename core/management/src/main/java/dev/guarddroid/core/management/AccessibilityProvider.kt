package dev.guarddroid.core.management

import android.content.Context
import dev.guarddroid.core.common.ManagementOperation

class AccessibilityProvider(private val context: Context) : ManagementProvider {

    override fun getProviderName() = "AccessibilityProvider"

    override fun getSupportedOperations(): Set<ManagementOperation> = emptySet()

    override fun blockApp(packageName: String): Result<Unit> = Result.failure(
        UnsupportedOperationException(
            "This device cannot block apps without Device Owner. " +
                "The Accessibility Service monitors app usage but cannot force-close apps reliably."
        )
    )

    override fun unblockApp(packageName: String): Result<Unit> = Result.failure(
        UnsupportedOperationException("App blocking not available on this device.")
    )

    override fun hideApp(packageName: String): Result<Unit> = Result.failure(
        UnsupportedOperationException("App hiding requires Device Owner.")
    )

    override fun showApp(packageName: String): Result<Unit> = Result.failure(
        UnsupportedOperationException("App hiding not available on this device.")
    )

    override fun preventUninstall(packageName: String): Result<Unit> = Result.failure(
        UnsupportedOperationException("Uninstall protection requires Device Admin or Device Owner.")
    )

    override fun allowUninstall(packageName: String): Result<Unit> = Result.failure(
        UnsupportedOperationException("Uninstall control not available on this device.")
    )

    override fun restrictInstallation(): Result<Unit> = Result.failure(
        UnsupportedOperationException("Installation restriction requires Device Owner.")
    )

    override fun allowInstallation(): Result<Unit> = Result.failure(
        UnsupportedOperationException("Installation control requires Device Owner.")
    )

    override fun restrictUnknownSources(): Result<Unit> = Result.failure(
        UnsupportedOperationException("Unknown sources restriction requires Device Owner.")
    )

    override fun allowUnknownSources(): Result<Unit> = Result.failure(
        UnsupportedOperationException("Unknown sources control requires Device Owner.")
    )

    override fun restrictSettings(): Result<Unit> = Result.failure(
        UnsupportedOperationException("Settings restriction requires Device Owner.")
    )

    override fun allowSettings(): Result<Unit> = Result.failure(
        UnsupportedOperationException("Settings control requires Device Owner.")
    )

    override fun enterRestrictedMode(): Result<Unit> = Result.failure(
        UnsupportedOperationException(
            "No management mode available. Please activate Device Admin in Settings > Security > Device Administrators."
        )
    )
}
