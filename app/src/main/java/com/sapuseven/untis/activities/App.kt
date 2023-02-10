package com.sapuseven.untis.activities

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.sapuseven.untis.workers.DailyWorker

class App : Application(), Configuration.Provider {
	override fun getWorkManagerConfiguration() =
		Configuration.Builder()
			.setMinimumLoggingLevel(Log.VERBOSE)
			.build()

	override fun onCreate() {
		super.onCreate()
		WorkManager.getInstance(applicationContext).enqueue(OneTimeWorkRequestBuilder<DailyWorker>().build())
	}
}
