package com.sapuseven.untis.helpers.analytics

import android.app.Application
import android.util.Log
import com.sapuseven.untis.BuildConfig
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
fun Application.initSentry(
	enable: Boolean = false,
	enableBreadcrumbs: Boolean = false
) {
	if (!enable) {
		Log.i("Sentry", "Sentry is not enabled")
	}

	SentryAndroid.init(this) { options ->
		Log.i("Sentry", "Sentry is enabled with [breadcrumbs=$enableBreadcrumbs]")

		with(options) {
			dsn = BuildConfig.SENTRY_DSN
			tracesSampleRate = if (enableBreadcrumbs) 1.0 else null
			isEnableUserInteractionTracing = enableBreadcrumbs
			enableAllAutoBreadcrumbs(enableBreadcrumbs)
			setBeforeBreadcrumb { breadcrumb, _ -> if (enableBreadcrumbs) breadcrumb else null }
			environment = if (BuildConfig.DEBUG) "development" else "production"
		}
	}
}
