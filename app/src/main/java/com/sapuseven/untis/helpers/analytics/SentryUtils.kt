package com.sapuseven.untis.helpers.analytics

import android.app.Application
import android.util.Log
import com.sapuseven.untis.BuildConfig
import io.sentry.android.core.SentryAndroid

fun Application.initSentry(
	enable: Boolean = false,
	enableBreadcrumbs: Boolean = false
) {
	if (enable) {
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
	} else {
		Log.i("Sentry", "Sentry is not enabled")
	}
}
