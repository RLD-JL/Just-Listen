package com.rld.justlisten.workers

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.rld.justlisten.datalayer.repositories.SyncRepository
import com.rld.justlisten.datalayer.repositories.SyncRetryScheduler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

private const val FAVORITE_SYNC_WORK = "FavoriteSync"

class AndroidSyncRetryScheduler(context: Context) : SyncRetryScheduler {
    private val workManager = WorkManager.getInstance(context)

    override fun scheduleRetry(delayMs: Long) {
        val request = OneTimeWorkRequestBuilder<FavoriteSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInitialDelay(delayMs.coerceAtLeast(0L), TimeUnit.MILLISECONDS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniqueWork(
            FAVORITE_SYNC_WORK,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    override fun cancelRetry() {
        workManager.cancelUniqueWork(FAVORITE_SYNC_WORK)
    }
}

class FavoriteSyncWorker(
    context: Context,
    parameters: WorkerParameters,
) : CoroutineWorker(context, parameters), KoinComponent {
    private val syncRepository: SyncRepository by inject()

    override suspend fun doWork(): Result =
        if (syncRepository.runPendingSync()) Result.success() else Result.retry()
}
