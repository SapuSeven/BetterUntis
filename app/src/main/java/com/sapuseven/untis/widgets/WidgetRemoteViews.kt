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
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.helpers.SerializationUtils
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.helpers.timetable.TimetableLoader
import com.sapuseven.untis.interfaces.TimetableDisplay
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.params.MessageParams
import com.sapuseven.untis.models.untis.response.MessageResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.lang.ref.WeakReference


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

	private fun reloadWidget() = AppWidgetManager.getInstance(applicationContext)
			.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listview_widget_base_content)

	private fun loadItems(intent: Intent) {
		when (intent.getIntExtra(EXTRA_INT_WIDGET_TYPE, 0)) {
			WIDGET_TYPE_MESSAGES -> user?.let { loadMessages(it) }
			WIDGET_TYPE_TIMETABLE -> loadTimetable()
			else -> items = emptyList()
		}
	}

	private fun loadMessages(user: UserDatabase.User) = GlobalScope.launch(Dispatchers.IO) {
		val query = UntisRequest.UntisRequestQuery(user)

		query.data.method = UntisApiConstants.METHOD_GET_MESSAGES
		query.proxyHost = applicationContext.getSharedPreferences("preferences_${user.id}", Context.MODE_PRIVATE).getString("preference_connectivity_proxy_host", null)
		query.data.params = listOf(MessageParams(
				UntisDate.fromLocalDate(LocalDate.now()),
				auth = UntisAuthentication.createAuthObject(user)
		))

		val result = UntisRequest().request(query)
		items = result.fold({ data ->
			val untisResponse = SerializationUtils.getJSON().parse(MessageResponse.serializer(), data)

			untisResponse.result?.messages?.map {
				WidgetListItem(it.id.toLong(), it.subject, it.body)
			}
		}, {
			listOf(WidgetListItem(0, "Failed to load messages", "Tap to retry")) // TODO: Extract string resources
		})
		reloadWidget()
	}

	private fun loadTimetable() {
		val timeFormatter: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
		val timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, user?.id
				?: return)
		val timetableLoader = TimetableLoader(WeakReference(applicationContext), user = user, timetableDatabaseInterface = timetableDatabaseInterface,
				timetableDisplay = object : TimetableDisplay {
					override fun addTimetableItems(items: List<TimegridItem>, startDate: UntisDate, endDate: UntisDate, timestamp: Long) {
						this@WidgetRemoteViewsFactory.items = items.map {
							WidgetListItem(
									it.id,
									"${it.startDateTime.toString(timeFormatter)} - ${it.endDateTime.toString(timeFormatter)} | ${it.title}",
									"${it.top}, ${it.bottom}"
							)
						}
						reloadWidget()
					}

					override fun onTimetableLoadingError(requestId: Int, code: Int?, message: String?) {
						items = listOf(WidgetListItem(0, "Failed to load timetable", "Tap to retry")) // TODO: Extract string resources
					}
				})

		val today = UntisDate.fromLocalDate(LocalDate.now())
		timetableLoader.load(TimetableLoader.TimetableLoaderTarget(
				today,
				today,
				user.userData.elemId,
				user.userData.elemType ?: ""
		), TimetableLoader.FLAG_LOAD_SERVER)
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

	override fun getCount(): Int = items?.size ?: 0

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
