package com.sapuseven.untis.core.data.system

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build
import androidx.annotation.RequiresPermission
import kotlinx.datetime.Instant

@RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM, conditional = true)
fun AlarmManager.setBest(time: Instant, pendingIntent: PendingIntent) {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && canScheduleExactAlarms()) {
		setExact(
			AlarmManager.RTC_WAKEUP,
			time.toEpochMilliseconds(),
			pendingIntent
		)
	} else {
		setWindow(
			AlarmManager.RTC_WAKEUP,
			time.toEpochMilliseconds(),
			600_000, // 10 minutes
			pendingIntent
		)
	}
}
