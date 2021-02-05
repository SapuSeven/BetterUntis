package com.sapuseven.untis.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.widgets.WidgetRemoteViewsFactory.Companion.WIDGET_TYPE_UNKNOWN


open class BaseWidget : AppWidgetProvider() {
	private var user: UserDatabase.User? = null
	private lateinit var userDatabase: UserDatabase
	private lateinit var context: Context
	private lateinit var appWidgetManager: AppWidgetManager

	open fun getWidgetType(): Int = WIDGET_TYPE_UNKNOWN

	companion object {
		const val EXTRA_INT_RELOAD = "com.sapuseven.widgets.reload"
	}

	override fun onReceive(context: Context, intent: Intent) {
		if (intent.getBooleanExtra(EXTRA_INT_RELOAD, true))
			super.onReceive(context, intent)
	}

	override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
		Log.d("Widgets", "Provider onUpdate() for ${appWidgetIds.joinToString(",")}")
		this.context = context
		this.appWidgetManager = appWidgetManager
		userDatabase = UserDatabase.createInstance(context)

		for (appWidgetId in appWidgetIds) {
			user = userDatabase.getUser(loadIdPref(context, appWidgetId))
			if (user != null) updateData(appWidgetId)
			else appWidgetManager.updateAppWidget(appWidgetId, loadBaseLayout(user))
		}
	}

	override fun onDeleted(context: Context, appWidgetIds: IntArray) {
		for (appWidgetId in appWidgetIds) {
			deleteIdPref(context, appWidgetId)
		}
	}

	private fun updateData(appWidgetId: Int) {
		val remoteViews = loadBaseLayout(user)
		val intent = Intent(context, WidgetRemoteViewsService::class.java)
				.putExtra(WidgetRemoteViewsFactory.EXTRA_INT_WIDGET_TYPE, getWidgetType())
				.putExtra(WidgetRemoteViewsFactory.EXTRA_INT_WIDGET_ID, appWidgetId)
				.setData(Uri.fromParts("content", appWidgetId.toString(), System.currentTimeMillis().toString()))

		remoteViews.setRemoteAdapter(R.id.listview_widget_base_content, intent)
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
	}

	private fun loadBaseLayout(user: UserDatabase.User?): RemoteViews {
		val remoteViews = RemoteViews(context.packageName, R.layout.widget_base)
		if (user == null) {
			remoteViews.setTextViewText(R.id.textview_base_widget_account, context.resources.getString(R.string.all_error))
			remoteViews.setTextViewText(R.id.textview_base_widget_school, context.resources.getString(R.string.all_error))
		} else {
			remoteViews.setTextViewText(R.id.textview_base_widget_account, user.getDisplayedName(context))
			remoteViews.setTextViewText(R.id.textview_base_widget_school, user.userData.schoolName)

			val primaryColor = when (context.getSharedPreferences("preferences_${user.id}", Context.MODE_PRIVATE).getString("preference_theme", null)) {
				"untis" -> R.color.colorPrimaryThemeUntis
				"blue" -> R.color.colorPrimaryThemeBlue
				"green" -> R.color.colorPrimaryThemeGreen
				"pink" -> R.color.colorPrimaryThemePink
				"cyan" -> R.color.colorPrimaryThemeCyan
				"pixel" -> R.color.colorPrimaryThemePixel
				else -> R.color.colorPrimary
			}
			remoteViews.setInt(R.id.linearlayout_base_widget_top_bar, "setBackgroundColor", ContextCompat.getColor(context, primaryColor))
		}

		val updateIntent = Intent(context, this.javaClass)
				.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
		val pendingIntent = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT)
		remoteViews.setPendingIntentTemplate(R.id.listview_widget_base_content, pendingIntent)

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
