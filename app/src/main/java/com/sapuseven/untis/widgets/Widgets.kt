package com.sapuseven.untis.widgets

class MessagesWidget : BaseWidget() {
	override fun getWidgetType(): Int = WidgetRemoteViewsFactory.WIDGET_TYPE_MESSAGES
}

class TimetableWidget : BaseWidget() {
	override fun getWidgetType(): Int = WidgetRemoteViewsFactory.WIDGET_TYPE_TIMETABLE
}
