package com.sapuseven.untis.activities

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R
import com.sapuseven.untis.adapters.ProfileListAdapter
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.widgets.DailyMessagesWidget
import com.sapuseven.untis.widgets.TimetableWidget
import com.sapuseven.untis.widgets.saveIdPref

class BaseWidgetConfigureActivity : BaseActivity() {

    private lateinit var profileListAdapter: ProfileListAdapter
    private lateinit var userList: RecyclerView
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private val onClickListener = View.OnClickListener {
        val context = this@BaseWidgetConfigureActivity

        val userId = profileListAdapter.itemAt(userList.getChildLayoutPosition(it)).id ?: 0
        saveIdPref(context, appWidgetId, userId)

        val intent = Intent(context, DailyMessagesWidget::class.java).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, DailyMessagesWidget::class.java))
        if (ids != null && ids.isNotEmpty()) {
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
        val intent2 = Intent(context, TimetableWidget::class.java).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        val ids2 = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, TimetableWidget::class.java))
        if (ids2 != null && ids2.isNotEmpty()) {
            intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids2)
            context.sendBroadcast(intent2)
        }


        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
        finish()
    }
    private val onLongClickListener = View.OnLongClickListener { true }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        setContentView(R.layout.base_widget_configure)

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