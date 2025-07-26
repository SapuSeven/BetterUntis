package com.sapuseven.untis.glance.receiver

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.sapuseven.untis.glance.widget.TimetableWidget

class TimetableWidgetReceiver : GlanceAppWidgetReceiver() {
	override val glanceAppWidget: GlanceAppWidget = TimetableWidget()
}
