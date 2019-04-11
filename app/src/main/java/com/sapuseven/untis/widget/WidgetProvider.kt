package com.sapuseven.untis.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context


class WidgetProvider : AppWidgetProvider() {
	override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
		val n = appWidgetIds.size

		// Perform this loop procedure for each App Widget that belongs to this provider
		for (i in 0 until n) {
			/*val appWidgetId = appWidgetIds[i]

			// Create an Intent to launch ExampleActivity
			val intent = Intent(context, ExampleActivity::class.java)
			val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

			// Get the layout for the App Widget and attach an on-click listener
			// to the button
			val views = RemoteViews(context.getPackageName(), R.layout.appwidget_provider_layout)
			views.setOnClickPendingIntent(R.id.button, pendingIntent)

			// Tell the AppWidgetManager to perform an update on the current app widget
			appWidgetManager.updateAppWidget(appWidgetId, views)*/
		}
	}
}