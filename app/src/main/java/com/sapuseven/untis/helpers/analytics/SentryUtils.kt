package com.sapuseven.untis.helpers.analytics

import android.app.Application
import com.sapuseven.untis.BuildConfig
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
fun Application.initSentry(enableDetailedInfo: Boolean = false) {
	SentryAndroid.init(this) { options ->
		with(options) {
			dsn = BuildConfig.SENTRY_DSN
			tracesSampleRate = if (enableDetailedInfo) 1.0 else null
			isEnableUserInteractionTracing = enableDetailedInfo
			enableAllAutoBreadcrumbs(enableDetailedInfo)
			setBeforeBreadcrumb { breadcrumb, _ -> if (enableDetailedInfo) breadcrumb else null }
		}
	}
}
