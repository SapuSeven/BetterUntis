package com.sapuseven.untis.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.config.PreferenceManager
import com.sapuseven.untis.helpers.config.PreferenceUtils

class DailyMessagesWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            deleteIdPref(context, appWidgetId)
        }
    }
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val userId = loadIdPref(context, appWidgetId)
    val user = UserDatabase.createInstance(context).getUser(userId)

    val views = RemoteViews(context.packageName, R.layout.daily_messages_widget)
    views.setTextViewText(R.id.textview_daily_messages_widget_account, user?.userData?.displayName)
    views.setTextViewText(R.id.textview_daily_messages_widget_school, user?.userData?.schoolName)
    views.setTextViewText(R.id.textview_daily_messages_widget_content, "TODO: Get and format messages")

    val colorPrimary = when (context.getSharedPreferences("preferences_${userId}", Context.MODE_PRIVATE).getString("preference_theme", null)) {
        "untis" -> R.color.colorPrimaryThemeUntis
        "blue" -> R.color.colorPrimaryThemeBlue
        "green" -> R.color.colorPrimaryThemeGreen
        "pink" -> R.color.colorPrimaryThemePink
        "cyan" -> R.color.colorPrimaryThemeCyan
        "pixel" -> R.color.colorPrimaryThemePixel
        else -> R.color.colorPrimary
    }
    views.setInt(R.id.linearlayout_daily_messages_widget_top_bar, "setBackgroundColor", context.resources.getColor(colorPrimary))

    appWidgetManager.updateAppWidget(appWidgetId, views)
}