package com.sapuseven.untis.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase

open class BaseWidget : AppWidgetProvider() {

    protected var user: UserDatabase.User? = null
    protected lateinit var views: RemoteViews

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

    open fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val userId = loadIdPref(context, appWidgetId)
        user = UserDatabase.createInstance(context).getUser(userId)
        views = RemoteViews(context.packageName, R.layout.daily_messages_widget)
        views.setTextViewText(R.id.textview_daily_messages_widget_account, user?.userData?.displayName)
        views.setTextViewText(R.id.textview_daily_messages_widget_school, user?.userData?.schoolName)

        val colorPrimary = when (context.getSharedPreferences("preferences_$userId", Context.MODE_PRIVATE).getString("preference_theme", null)) {
            "untis" -> R.color.colorPrimaryThemeUntis
            "blue" -> R.color.colorPrimaryThemeBlue
            "green" -> R.color.colorPrimaryThemeGreen
            "pink" -> R.color.colorPrimaryThemePink
            "cyan" -> R.color.colorPrimaryThemeCyan
            "pixel" -> R.color.colorPrimaryThemePixel
            else -> R.color.colorPrimary
        }
        views.setInt(R.id.linearlayout_daily_messages_widget_top_bar, "setBackgroundColor", context.resources.getColor(colorPrimary))
    }
}

private const val PREFS_NAME = "com.sapuseven.untis.widgets"
private const val PREF_PREFIX_KEY = "appwidget_"

internal fun saveIdPref(context: Context, appWidgetId: Int, userId: Long) {
    context.getSharedPreferences(PREFS_NAME, 0).edit().putLong(PREF_PREFIX_KEY + appWidgetId, userId).apply()
}

internal fun loadIdPref(context: Context, appWidgetId: Int): Long {
    return context.getSharedPreferences(PREFS_NAME, 0).getLong(PREF_PREFIX_KEY + appWidgetId, 0)
}

internal fun deleteIdPref(context: Context, appWidgetId: Int) {
    context.getSharedPreferences(PREFS_NAME, 0).edit().remove(PREF_PREFIX_KEY + appWidgetId).apply()
}