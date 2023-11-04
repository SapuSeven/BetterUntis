package com.sapuseven.untis.activities

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.sapuseven.untis.helpers.analytics.initSentry
import com.sapuseven.untis.workers.DailyWorker
import com.sapuseven.untis.workers.DailyWorker.Companion.TAG_DAILY_WORK
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.reportsDataStore: DataStore<Preferences> by preferencesDataStore(name = "reports")
val reportsDataStoreBreadcrumbsEnable = Pair(booleanPreferencesKey("reportBreadcrumbsEnable"), true)

class App : Application(), Configuration.Provider {
	override fun getWorkManagerConfiguration() =
		Configuration.Builder()
			.setMinimumLoggingLevel(Log.VERBOSE)
			.build()

	@OptIn(DelicateCoroutinesApi::class)
	override fun onCreate() {
		super.onCreate()

		GlobalScope.launch {
			val reportBreadcrumbsEnbaled = reportsDataStore.loadPref(reportsDataStoreBreadcrumbsEnable.first, reportsDataStoreBreadcrumbsEnable.second)
			Log.d("Sentry", "Breadcrumbs enabled: $reportBreadcrumbsEnbaled")
			initSentry(reportBreadcrumbsEnbaled)
		}

		GlobalScope.launch {
			WorkManager.getInstance(applicationContext).apply {
				cancelAllWorkByTag(TAG_DAILY_WORK)
				enqueue(OneTimeWorkRequestBuilder<DailyWorker>().build())
			}
		}
	}
}

private suspend fun <T> DataStore<Preferences>.loadPref(key: Preferences.Key<T>, default: T) =
	data.map { preferences ->
		preferences[key] ?: default
	}.first()
