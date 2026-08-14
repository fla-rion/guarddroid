package dev.guarddroid.core.update

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class UpdateCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val updateChecker: GitHubUpdateChecker,
    private val notificationManager: UpdateNotificationManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return when (val result = updateChecker.checkForUpdate()) {
            is UpdateCheckResult.UpdateAvailable -> {
                notificationManager.showUpdateNotification(result.info)
                Result.success()
            }
            is UpdateCheckResult.UpToDate -> Result.success()
            is UpdateCheckResult.Error -> Result.retry()
        }
    }
}
