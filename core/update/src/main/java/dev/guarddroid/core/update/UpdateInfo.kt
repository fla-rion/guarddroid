package dev.guarddroid.core.update

data class UpdateInfo(
    val tagName: String,
    val versionName: String,
    val releaseUrl: String,
    val releaseNotes: String,
    val publishedAt: String,
    val isNewer: Boolean
)

sealed class UpdateCheckResult {
    data class UpdateAvailable(val info: UpdateInfo) : UpdateCheckResult()
    object UpToDate : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}
