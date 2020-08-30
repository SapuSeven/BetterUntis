package com.sapuseven.untis.widgets

class MessagesWidget : BaseWidget() {
    override fun getWidgetType(): Int = WidgetRemoteViewsFactory.WIDGET_TYPE_MESSAGES
}

class TimetableWidget : BaseWidget() {
    override fun getWidgetType(): Int = WidgetRemoteViewsFactory.WIDGET_TYPE_TIMETABLE

    // TODO: Properly implement timetable loading in WidgetRemoteViews.kt
    /*private lateinit var timetableDatabaseInterface: TimetableDatabaseInterface
    private lateinit var timetableLoader: TimetableLoader

    override fun updateAppWidget(appWidgetId: Int) {
        super.updateAppWidget(appWidgetId)
        timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, user?.id ?: return)
        timetableLoader = TimetableLoader(WeakReference(context), TimetableWidgetTimetableDisplay(appWidgetManager, appWidgetId, user), user ?: return, timetableDatabaseInterface)
        loadTimetable()
    }

    private fun loadTimetable() {
        val today = UntisDate.fromLocalDate(LocalDate.now())
        timetableLoader.load(TimetableLoader.TimetableLoaderTarget(
                today,
                today,
                user?.userData?.elemId ?: return,
                user?.userData?.elemType ?: ""
        ), TimetableLoader.FLAG_LOAD_SERVER)
    }

    inner class TimetableWidgetTimetableDisplay(
            private val appWidgetManager: AppWidgetManager,
            private val appWidgetId: Int,
            private val user: UserDatabase.User?
    ) : TimetableDisplay {

        override fun addTimetableItems(items: List<TimegridItem>, startDate: UntisDate, endDate: UntisDate, timestamp: Long) {
            val newViews = loadBaseLayout(user)
            val formattedItems = formatItems(items)
            val fmt: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
            val text = SpannableStringBuilder()
            var firstLine: SpannableString
            var secondLine: String
            formattedItems.forEach {
                firstLine = SpannableString("${it.startDateTime.toString(fmt)} - ${it.endDateTime.toString(fmt)} | ${it.title}\n")
                firstLine.setSpan(ForegroundColorSpan(context.resources.getColor(android.R.color.primary_text_light)), 0, firstLine.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                secondLine = "${it.top}, ${it.bottom}"
                text.append(firstLine)
                text.append(secondLine)
                text.append("\n\n")
            }
            if (text.isEmpty()) text.append(context.resources.getString(R.string.widget_timetable_empty))
            appWidgetManager.updateAppWidget(appWidgetId, newViews)
        }

        override fun onTimetableLoadingError(requestId: Int, code: Int?, message: String?) {
            val newViews = loadBaseLayout(user)
            appWidgetManager.updateAppWidget(appWidgetId, newViews)
        }
    }*/
}