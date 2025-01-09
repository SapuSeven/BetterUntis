package com.sapuseven.untis.activities

import android.app.Application
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.sapuseven.untis.helpers.analytics.initSentry
import com.sapuseven.untis.ui.pages.settings.GlobalSettingsRepository
import com.sapuseven.untis.workers.DailyWorker
import com.sapuseven.untis.workers.DailyWorker.Companion.TAG_DAILY_WORK
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {
	@Inject lateinit var globalSettingsRepository: GlobalSettingsRepository;

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
			.build()
}

private suspend fun <T> DataStore<Preferences>.loadPref(key: Preferences.Key<T>, default: T) =
	data.map { preferences ->
		preferences[key] ?: default
	}.first()
