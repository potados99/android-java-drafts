package org.potados.workmanagerpractice

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class SomeWorkingClass {
    fun doYourJob(context: Context) {
        val galleryWorkRequest = OneTimeWorkRequestBuilder<GalleryWorker>().build()

        WorkManager.getInstance(context).enqueue(galleryWorkRequest)
    }
}