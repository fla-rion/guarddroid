package dev.guarddroid.core.update

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubUpdateChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val apiUrl = "https://api.github.com/repos/fla-rion/guarddroid/releases/latest"

    suspend fun checkForUpdate(): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val connection = URL(apiUrl).openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
                connectTimeout = 10_000
                readTimeout = 10_000
            }
            val responseCode = connection.responseCode
            if (responseCode != 200) {
                connection.disconnect()
                return@withContext UpdateCheckResult.Error("HTTP $responseCode")
            }
            val body = connection.inputStream.bufferedReader().readText()
            connection.disconnect()
            parseRelease(body)
        } catch (e: Exception) {
            UpdateCheckResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun parseRelease(json: String): UpdateCheckResult {
        return try {
            val obj = JSONObject(json)
            val tagName = obj.getString("tag_name")
            val htmlUrl = obj.getString("html_url")
            val body = obj.optString("body", "")
            val publishedAt = obj.optString("published_at", "")
            val versionName = tagName.removePrefix("v")
            val currentVersion = getCurrentVersion()
            val isNewer = isVersionNewer(versionName, currentVersion)
            val info = UpdateInfo(
                tagName = tagName,
                versionName = versionName,
                releaseUrl = htmlUrl,
                releaseNotes = body,
                publishedAt = publishedAt,
                isNewer = isNewer
            )
            if (isNewer) UpdateCheckResult.UpdateAvailable(info)
            else UpdateCheckResult.UpToDate
        } catch (e: Exception) {
            UpdateCheckResult.Error("Parse error: ${e.message}")
        }
    }

    private fun getCurrentVersion(): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "0.0.0"
        } catch (e: Exception) {
            "0.0.0"
        }
    }

    private fun isVersionNewer(remote: String, current: String): Boolean {
        return try {
            val r = remote.split(".").map { it.toIntOrNull() ?: 0 }
            val c = current.split(".").map { it.toIntOrNull() ?: 0 }
            val maxLen = maxOf(r.size, c.size)
            for (i in 0 until maxLen) {
                val rv = r.getOrElse(i) { 0 }
                val cv = c.getOrElse(i) { 0 }
                if (rv > cv) return true
                if (rv < cv) return false
            }
            false
        } catch (e: Exception) {
            false
        }
    }
}
