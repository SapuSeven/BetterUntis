package com.sapuseven.untis.activities

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.sapuseven.untis.helpers.analytics.initSentry
import com.sapuseven.untis.data.repository.GlobalSettingsRepository
import com.sapuseven.untis.workers.DailyWorker
import com.sapuseven.untis.workers.DailyWorker.Companion.TAG_DAILY_WORK
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {
	@Inject lateinit var globalSettingsRepository: GlobalSettingsRepository;

	@Inject lateinit var workerFactory: HiltWorkerFactory;

	private val ioScope: CoroutineScope = CoroutineScope(Dispatchers.IO + Job())

	@OptIn(DelicateCoroutinesApi::class)
	override fun onCreate() {
		super.onCreate()

		ioScope.launch {
			val settings = globalSettingsRepository.getSettings().first()
			initSentry(
				settings.errorReportingEnable,
				settings.errorReportingEnableBreadcrumbs
			)
		}

		GlobalScope.launch {
			WorkManager.getInstance(applicationContext).apply {
				cancelAllWorkByTag(TAG_DAILY_WORK)
				enqueue(OneTimeWorkRequestBuilder<DailyWorker>().build())
			}
		}
	}

	override val workManagerConfiguration: Configuration
		get() = Configuration.Builder()
			.setMinimumLoggingLevel(Log.VERBOSE)
			.setWorkerFactory(workerFactory)
			.build()
}
