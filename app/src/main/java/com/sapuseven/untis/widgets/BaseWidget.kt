package com.sapuseven.untis.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase

open class BaseWidget : AppWidgetProvider() {

    protected var userId: Long = 0
    protected var user: UserDatabase.User? = null
    protected lateinit var userDatabase: UserDatabase
    protected lateinit var views: RemoteViews
    protected lateinit var context: Context
    protected lateinit var appWidgetManager: AppWidgetManager
    protected lateinit var appWidgetIds: IntArray

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        this.context = context
        this.appWidgetManager = appWidgetManager
        this.appWidgetIds = appWidgetIds

        for (appWidgetId in appWidgetIds) {
            userDatabase = UserDatabase.createInstance(context)
            userId = loadIdPref(context, appWidgetId)
            user = userDatabase.getUser(userId)
            views = loadBaseLayout()
            if (user != null) updateAppWidget(context, appWidgetManager, appWidgetId)
            else appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            deleteIdPref(context, appWidgetId)
        }
    }

    open fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) { }

    fun loadBaseLayout(): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.base_widget)
        if (user == null) {
            remoteViews.setTextViewText(R.id.textview_base_widget_account, context.resources.getString(R.string.all_error))
            remoteViews.setTextViewText(R.id.textview_base_widget_school, context.resources.getString(R.string.all_error))
            remoteViews.setTextViewText(R.id.textview_base_widget_content, context.resources.getString(R.string.all_error))
        } else {
            remoteViews.setTextViewText(R.id.textview_base_widget_account, user?.userData?.displayName)
            remoteViews.setTextViewText(R.id.textview_base_widget_school, user?.userData?.schoolName)

            val colorPrimary = when (context.getSharedPreferences("preferences_$userId", Context.MODE_PRIVATE).getString("preference_theme", null)) {
                "untis" -> R.color.colorPrimaryThemeUntis
                "blue" -> R.color.colorPrimaryThemeBlue
                "green" -> R.color.colorPrimaryThemeGreen
                "pink" -> R.color.colorPrimaryThemePink
                "cyan" -> R.color.colorPrimaryThemeCyan
                "pixel" -> R.color.colorPrimaryThemePixel
                else -> R.color.colorPrimary
            }
            remoteViews.setInt(R.id.linearlayout_base_widget_top_bar, "setBackgroundColor", context.resources.getColor(colorPrimary))
        }

        return remoteViews
    }

    fun updateViews(remoteViews: RemoteViews) {
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
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