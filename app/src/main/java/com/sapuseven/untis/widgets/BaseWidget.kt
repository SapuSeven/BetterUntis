package com.sapuseven.untis.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase

open class BaseWidget : AppWidgetProvider() {

    protected var user: UserDatabase.User? = null
    protected lateinit var userDatabase: UserDatabase
    protected lateinit var context: Context
    protected lateinit var appWidgetManager: AppWidgetManager

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        this.context = context
        this.appWidgetManager = appWidgetManager
        userDatabase = UserDatabase.createInstance(context)

        for (appWidgetId in appWidgetIds) {
            user = userDatabase.getUser(loadIdPref(context, appWidgetId))
            if (user != null) updateAppWidget(appWidgetId)
            else appWidgetManager.updateAppWidget(appWidgetId, loadBaseLayout(user))
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            deleteIdPref(context, appWidgetId)
        }
    }

    open fun updateAppWidget(appWidgetId: Int) {}

    fun loadBaseLayout(user: UserDatabase.User?): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.base_widget)
        if (user == null) {
            remoteViews.setTextViewText(R.id.textview_base_widget_account, context.resources.getString(R.string.all_error))
            remoteViews.setTextViewText(R.id.textview_base_widget_school, context.resources.getString(R.string.all_error))
            remoteViews.setTextViewText(R.id.textview_base_widget_content, context.resources.getString(R.string.all_error))
        } else {
            remoteViews.setTextViewText(R.id.textview_base_widget_account, user.userData.displayName)
            remoteViews.setTextViewText(R.id.textview_base_widget_school, user.userData.schoolName)

            val colorPrimary = when (context.getSharedPreferences("preferences_${user.id}", Context.MODE_PRIVATE).getString("preference_theme", null)) {
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