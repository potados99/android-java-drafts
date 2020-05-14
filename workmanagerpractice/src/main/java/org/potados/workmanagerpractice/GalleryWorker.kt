package org.potados.workmanagerpractice

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters

class GalleryWorker(private val appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    override fun doWork(): Result {

        for (i: Int in 0..100) {
            reportProgress(i)
            Thread.sleep(100)
        }

        return Result.success()
    }

    private fun reportProgress(progress: Int) {
        val intent = Intent("GALLERY_PROGRESS_REPORT").apply {
            putExtra("PROGRESS", progress)
        }

        appContext.sendBroadcast(intent)
    }
}