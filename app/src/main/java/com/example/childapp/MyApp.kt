package com.example.childapp

import android.app.Application
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val myWorker = OneTimeWorkRequest.Builder(WatchDogWorker::class.java)
            .setInitialDelay(15, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(this).enqueue(myWorker)
    }
}