package com.sapuseven.untis.helpers.analytics

import android.app.Application
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sapuseven.untis.BuildConfig
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
fun Application.initSentry(enableDetailedInfo: Boolean = false) {
	SentryAndroid.init(this) { options ->
		with(options) {
			dsn = BuildConfig.SENTRY_DSN
			tracesSampleRate = if (enableDetailedInfo) 1.0 else null
			isEnableUserInteractionTracing = enableDetailedInfo
			enableAllAutoBreadcrumbs(enableDetailedInfo)
			setBeforeBreadcrumb { breadcrumb, _ -> if (enableDetailedInfo) breadcrumb else null }
			environment = if (BuildConfig.DEBUG) "development" else "production"
		}
	}
}
