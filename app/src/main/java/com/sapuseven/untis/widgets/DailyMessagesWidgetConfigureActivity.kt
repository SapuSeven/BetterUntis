package com.sapuseven.untis.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R
import com.sapuseven.untis.activities.BaseActivity
import com.sapuseven.untis.adapters.ProfileListAdapter
import com.sapuseven.untis.data.databases.UserDatabase

class DailyMessagesWidgetConfigureActivity : BaseActivity() {

    private lateinit var profileListAdapter: ProfileListAdapter
    private lateinit var userList: RecyclerView
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private val onClickListener = View.OnClickListener {
        val context = this@DailyMessagesWidgetConfigureActivity

        val userId = profileListAdapter.itemAt(userList.getChildLayoutPosition(it)).id ?: 0
        saveIdPref(context, appWidgetId, userId)

        updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
        finish()
    }
    private val onLongClickListener = View.OnLongClickListener { true }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        setContentView(R.layout.daily_messages_widget_configure)

        userList = findViewById(R.id.recyclerview_daily_messages_widget_configure_profile_list)
        profileListAdapter = ProfileListAdapter(this, UserDatabase.createInstance(this).getAllUsers().toMutableList(), onClickListener, onLongClickListener)
        userList.layoutManager = LinearLayoutManager(this)
        userList.adapter = profileListAdapter

        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
    }

}

private const val PREFS_NAME = "com.sapuseven.untis.widgets.DailyMessagesWidget"
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