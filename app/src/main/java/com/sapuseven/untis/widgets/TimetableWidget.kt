package com.sapuseven.untis.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import com.sapuseven.untis.R
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.helpers.TimetableListSorter.formatItems
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.helpers.timetable.TimetableLoader
import com.sapuseven.untis.interfaces.TimetableDisplay
import com.sapuseven.untis.models.untis.UntisDate
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.lang.ref.WeakReference

class TimetableWidget : BaseWidget(), TimetableDisplay {

    private lateinit var timetableDatabaseInterface: TimetableDatabaseInterface
    private lateinit var timetableLoader: TimetableLoader

    override fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        super.updateAppWidget(context, appWidgetManager, appWidgetId)

        timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, userId)
        timetableLoader = TimetableLoader(WeakReference(context), this, user, timetableDatabaseInterface)
        loadTimetable()

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun addTimetableItems(items: List<TimegridItem>, startDate: UntisDate, endDate: UntisDate, timestamp: Long) {
        val newViews = loadBaseLayout()
        val formattedItems = formatItems(items)
        val fmt: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
        var time: String
        var text = ""
        formattedItems.forEach {
            time = it.startDateTime.toString(fmt) + " - " + it.endDateTime.toString(fmt)
            text += "$time\n${it.title}"
            if (it.top != "") text += ", ${it.top}"
            if (it.bottom != "") text += ", ${it.bottom}"
            text += "\n\n"
        }
        newViews.setTextViewText(R.id.textview_daily_messages_widget_content, text)
        updateViews(newViews)
    }

    override fun onTimetableLoadingError(requestId: Int, code: Int?, message: String?) {
        Log.e("BetterUntis", message)
    }

    private fun loadTimetable(force: Boolean = false) {
        val today = UntisDate.fromLocalDate(LocalDate.now())
        val flags = if (force) TimetableLoader.FLAG_LOAD_SERVER else TimetableLoader.FLAG_LOAD_CACHE
        timetableLoader.load(TimetableLoader.TimetableLoaderTarget(today, today, user.userData.elemId, user.userData.elemType ?: ""), flags)
    }
}