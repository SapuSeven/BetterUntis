package com.sapuseven.untis.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.widget.RemoteViewsService.RemoteViewsFactory
import com.sapuseven.untis.R
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisAuthentication
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.SerializationUtils
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.params.MessageParams
import com.sapuseven.untis.models.untis.response.MessageResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.joda.time.LocalDate


class WidgetRemoteViewsFactory(private val applicationContext: Context, intent: Intent) : RemoteViewsFactory {
	companion object {
		const val EXTRA_INT_WIDGET_ID = "com.sapuseven.widgets.id"
		const val EXTRA_INT_WIDGET_TYPE = "com.sapuseven.widgets.type"

		const val WIDGET_TYPE_UNKNOWN = 0
		const val WIDGET_TYPE_MESSAGES = 1
		const val WIDGET_TYPE_TIMETABLE = 2
	}

	private val appWidgetId = intent.getIntExtra(EXTRA_INT_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
	private val userDatabase = UserDatabase.createInstance(applicationContext)
	private val user = userDatabase.getUser(loadIdPref(applicationContext, appWidgetId))
	private var items: List<WidgetListItem>? = null

	init {
		loadItems(intent)
	}

	private fun loadItems(intent: Intent) = GlobalScope.launch(Dispatchers.Main) {
		items = when (intent.getIntExtra(EXTRA_INT_WIDGET_TYPE, 0)) {
			WIDGET_TYPE_MESSAGES -> user?.let { loadMessages(it) }
			WIDGET_TYPE_TIMETABLE -> loadTimetable()
			else -> emptyList()
		}

		AppWidgetManager.getInstance(applicationContext)
				.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listview_widget_base_content)
	}

	private suspend fun loadMessages(user: UserDatabase.User): List<WidgetListItem> {
		val query = UntisRequest.UntisRequestQuery(user)

		query.data.method = UntisApiConstants.METHOD_GET_MESSAGES
		query.proxyHost = applicationContext.getSharedPreferences("preferences_${user.id}", Context.MODE_PRIVATE).getString("preference_connectivity_proxy_host", null)
		query.data.params = listOf(MessageParams(
				UntisDate.fromLocalDate(LocalDate.now()),
				auth = UntisAuthentication.createAuthObject(user)
		))

		val result = UntisRequest().request(query)
		return result.fold({ data ->
			val untisResponse = SerializationUtils.getJSON().parse(MessageResponse.serializer(), data)

			untisResponse.result?.messages?.map {
				WidgetListItem(it.id.toLong(), it.subject, it.body)
			}
			/*listOf(
					WidgetListItem(1, "Message 1", "Text 1"),
					WidgetListItem(2, "Message 2", "Text 2"),
					WidgetListItem(3, "Message 3", "Text 3"),
					WidgetListItem(4, "Message 4", "Text 4"),
					WidgetListItem(5, "Message 5", "Text 5"),
					WidgetListItem(6, "Message 6", "Text 6")
			)*/
		}, { null }) ?: emptyList()
	}

	private suspend fun loadTimetable(): List<WidgetListItem> {
		/*val today = UntisDate.fromLocalDate(LocalDate.now())
		timetableLoader.load(TimetableLoader.TimetableLoaderTarget(
				today,
				today,
				user?.userData?.elemId ?: return,
				user?.userData?.elemType ?: ""
		), TimetableLoader.FLAG_LOAD_SERVER)*/
		return listOf(WidgetListItem(1, "Not yet implemented", ""))
	}

	override fun onCreate() {}

	override fun onDataSetChanged() {}

	override fun onDestroy() {}

	override fun getViewAt(position: Int): RemoteViews {
		while (items == null) Thread.sleep(100)

		return RemoteViews(applicationContext.packageName, R.layout.widget_base_item).apply {
			items?.get(position)?.let { item: WidgetListItem ->
				setTextViewText(R.id.textview_listitem_line1, item.firstLine)
				setTextViewText(R.id.textview_listitem_line2, item.secondLine)
			}
		}
	}

	override fun getCount(): Int = items?.size ?: 1

	override fun getLoadingView(): RemoteViews? = null

	override fun getViewTypeCount(): Int = 1

	override fun getItemId(position: Int): Long = items?.get(position)?.id ?: position.toLong()

	override fun hasStableIds(): Boolean = true

	data class WidgetListItem(
			val id: Long,
			val firstLine: String,
			val secondLine: String
	)
}

class WidgetRemoteViewsService : RemoteViewsService() {
	override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
		return WidgetRemoteViewsFactory(this.applicationContext, intent)
	}
}
