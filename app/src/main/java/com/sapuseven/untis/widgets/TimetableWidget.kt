package com.sapuseven.untis.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import com.sapuseven.untis.R

class TimetableWidget : BaseWidget() {
    override fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        super.updateAppWidget(context, appWidgetManager, appWidgetId)
        views.setTextViewText(R.id.textview_daily_messages_widget_content, "TODO: Display timetable for current day")
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}