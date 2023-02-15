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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.analyticsDataStore: DataStore<Preferences> by preferencesDataStore(name = "analytics")
val analyticsDataStoreEnable = Pair(booleanPreferencesKey("analyticsEnable"), false)

class App : Application(), Configuration.Provider {
	override fun getWorkManagerConfiguration() =
		Configuration.Builder()
			.setMinimumLoggingLevel(Log.VERBOSE)
			.build()

	@OptIn(DelicateCoroutinesApi::class)
	override fun onCreate() {
		super.onCreate()

		GlobalScope.launch {
			val analyticsEnbaled = analyticsDataStore.loadPref(analyticsDataStoreEnable.first, false)
			Log.d("Sentry", "Analytics enabled: $analyticsEnbaled")
			initSentry(analyticsEnbaled)
		}

		WorkManager.getInstance(applicationContext).enqueue(OneTimeWorkRequestBuilder<DailyWorker>().build())
	}
}

private suspend fun <T> DataStore<Preferences>.loadPref(key: Preferences.Key<T>, default: T) =
	data.map { preferences ->
		preferences[key] ?: default
	}.first()
