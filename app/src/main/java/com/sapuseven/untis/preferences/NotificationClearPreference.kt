package com.sapuseven.untis.preferences

import android.app.NotificationManager
import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference

class NotificationClearPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {
	override fun onClick() {
		(context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
		super.onClick()
	}
}
