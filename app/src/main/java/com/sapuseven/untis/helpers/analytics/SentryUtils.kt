package com.sapuseven.untis.helpers.analytics

import com.sapuseven.untis.BuildConfig
import com.sapuseven.untis.activities.BaseComposeActivity
import io.sentry.android.core.SentryAndroid

fun BaseComposeActivity.initSentry(enableUsageStats: Boolean = false) {
	SentryAndroid.init(this) { options ->
		with(options) {
			dsn = BuildConfig.SENTRY_DSN
			tracesSampleRate = 1.0
			isEnableUserInteractionTracing = true
			isEnableUserInteractionBreadcrumbs = true
		}
	}
}
