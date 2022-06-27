package com.rld.justlisten.android.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlin.system.exitProcess

class SleepWorker(val context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters)  {
    override suspend fun doWork(): Result {
       exitProcess(0)
    }
}