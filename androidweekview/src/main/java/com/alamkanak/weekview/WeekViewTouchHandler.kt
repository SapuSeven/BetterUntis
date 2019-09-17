package com.alamkanak.weekview

import android.view.MotionEvent
import com.alamkanak.weekview.DateUtils.today
import com.alamkanak.weekview.config.WeekViewConfig
import com.alamkanak.weekview.config.WeekViewDrawConfig
import java.lang.Math.max
import java.util.*

internal class WeekViewTouchHandler(private val config: WeekViewConfig) {
	private val drawConfig: WeekViewDrawConfig = config.drawConfig

	/**
	 * Returns the time and date where the user clicked on.
	 *
	 * @param event The [MotionEvent] of the touch event.
	 * @return The time and date at the clicked position.
	 */
	fun getTimeFromPoint(event: MotionEvent): Calendar? {
		val touchX = event.x
		val touchY = event.y

		val widthPerDay = drawConfig.widthPerDay
		val originX = drawConfig.currentOrigin.x
		val timeColumnWidth = drawConfig.timeColumnWidth

		val leftDaysWithGaps = (Math.ceil((originX / widthPerDay).toDouble()) * -1).toInt()
		var startPixel = originX + widthPerDay * leftDaysWithGaps + timeColumnWidth

		val begin = leftDaysWithGaps + 1
		val end = leftDaysWithGaps + config.numberOfVisibleDays + 1

		for (dayNumber in begin..end) {
			val start = max(startPixel, timeColumnWidth)

			val isVisibleHorizontally = startPixel + widthPerDay - start > 0
			val isWithinDay = (touchX > start) and (touchX < startPixel + widthPerDay)

			if (isVisibleHorizontally && isWithinDay) {
				val day = today()
				day.add(Calendar.DATE, dayNumber - 1)

				val originY = drawConfig.currentOrigin.y
				val hourHeight = config.hourHeight.toFloat()

				val pixelsFromZero = touchY - originY - drawConfig.headerHeight
				val hour = (pixelsFromZero / hourHeight).toInt()
				val minute = (60 * (pixelsFromZero - hour * hourHeight) / hourHeight).toInt()
				day.add(Calendar.HOUR, hour)
				day.set(Calendar.MINUTE, minute)
				return day
			}

			startPixel += widthPerDay
		}

		return null
	}
}
