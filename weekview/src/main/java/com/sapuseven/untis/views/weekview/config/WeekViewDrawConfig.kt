package com.sapuseven.untis.views.weekview.config

import android.content.Context
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import com.sapuseven.untis.views.weekview.DateTimeInterpreter
import com.sapuseven.untis.views.weekview.DateUtils
import com.sapuseven.untis.views.weekview.WeekView
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import java.util.*
import kotlin.math.max
import kotlin.math.min

class WeekViewDrawConfig(context: Context) {
	var timeTextVisibility: Boolean = true
	val timeTextTopPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	val timeTextBottomPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	val timeCaptionPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	var timeTextWidth: Float = 0.0f
	var timeTextHeight: Float = 0.0f
	var timeCaptionHeight: Float = 0.0f
	var timeColumnWidth: Float = 0.0f

	val headerTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	var headerTextHeight: Float = 0.0f
	val headerSecondaryTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	var headerSecondaryTextHeight: Float = 0.0f
	var headerHeight: Float = 0.0f

	var currentOrigin: PointF = PointF(0f, 0f)
	val headerBackgroundPaint: Paint = Paint()
	var widthPerDay: Float = 0.0f
	val dayBackgroundPaint: Paint = Paint()
	val hourSeparatorPaint: Paint = Paint()
	val daySeparatorPaint: Paint = Paint()
	var headerMarginBottom: Float = 0.0f

	val timeColumnSeparatorPaint: Paint = Paint()
	val nowLinePaint: Paint = Paint()
	val todayHeaderTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	val todayHeaderSecondaryTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	val holidayTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	val eventTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	val eventTopPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	val eventBottomPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	val timeColumnBackgroundPaint: Paint = Paint()
	var newHourHeight: Int = -1
	val dateTimeInterpreter: DateTimeInterpreter = buildDefaultDateTimeInterpreter(context)
	val futureBackgroundPaint: Paint = Paint()
	val pastBackgroundPaint: Paint = Paint()

	companion object {
		private val TYPEFACE_SEMI_BOLD: Typeface = Typeface.create("sans-serif-light", Typeface.BOLD)
	}
	
	init {
		// Set additional permanent properties
		timeTextTopPaint.textAlign = Paint.Align.LEFT
		timeTextBottomPaint.textAlign = Paint.Align.RIGHT

		timeCaptionPaint.textAlign = Paint.Align.CENTER
		timeCaptionPaint.typeface = TYPEFACE_SEMI_BOLD

		headerTextPaint.textAlign = Paint.Align.CENTER
		headerTextPaint.typeface = TYPEFACE_SEMI_BOLD

		headerSecondaryTextPaint.textAlign = Paint.Align.CENTER
		headerSecondaryTextPaint.typeface = Typeface.DEFAULT

		hourSeparatorPaint.style = Paint.Style.STROKE

		daySeparatorPaint.style = Paint.Style.STROKE

		todayHeaderTextPaint.textAlign = Paint.Align.CENTER
		todayHeaderTextPaint.typeface = TYPEFACE_SEMI_BOLD

		todayHeaderSecondaryTextPaint.textAlign = Paint.Align.CENTER
		todayHeaderSecondaryTextPaint.typeface = Typeface.DEFAULT

		holidayTextPaint.typeface = Typeface.DEFAULT_BOLD

		eventTextPaint.textAlign = Paint.Align.CENTER
		eventTextPaint.typeface = Typeface.DEFAULT_BOLD

		eventTopPaint.style = Paint.Style.FILL
		eventTopPaint.strokeWidth = 4f

		eventBottomPaint.style = Paint.Style.FILL
		eventBottomPaint.strokeWidth = 4f
	}

	fun moveCurrentOriginIfFirstDraw(config: WeekViewConfig) {
		// If the week view is being drawn for the first time, then consider the first day of the week.
		val today = DateTime.now()
		val isWeekView = config.visibleDays >= 7
		val currentDayIsNotToday = today.dayOfWeek != config.firstDayOfWeek
		if (isWeekView && currentDayIsNotToday && config.showFirstDayOfWeekFirst) {
			val difference = today.dayOfWeek - config.firstDayOfWeek
			currentOrigin.x += widthPerDay/* + config.columnGap*/ * difference
		}
	}

	fun refreshAfterZooming(config: WeekViewConfig) {
		if (newHourHeight > 0) {
			if (newHourHeight < config.effectiveMinHourHeight) {
				newHourHeight = config.effectiveMinHourHeight
			} else if (newHourHeight > config.maxHourHeight) {
				newHourHeight = config.maxHourHeight
			}

			currentOrigin.y = currentOrigin.y / config.hourHeight * newHourHeight
			config.hourHeight = newHourHeight
			newHourHeight = -1
		}
	}

	fun updateVerticalOrigin(config: WeekViewConfig) {
		val height = WeekView.viewHeight

		// If the new currentOrigin.y is invalid, make it valid.
		val dayHeight = config.hourHeight * config.hoursPerDay()
		val potentialNewVerticalOrigin = height - (dayHeight + config.drawConfig.headerHeight)

		currentOrigin.y = max(currentOrigin.y, potentialNewVerticalOrigin)
		currentOrigin.y = min(currentOrigin.y, 0f)
	}

	private fun buildDefaultDateTimeInterpreter(context: Context): DateTimeInterpreter {
		return object : DateTimeInterpreter {
			private val datePattern = "EEE"
			private val secondaryDatePattern = "d. MMM"
			private val timePattern = DateUtils.getTimeFormat(context).toPattern()

			override fun interpretDate(date: DateTime) = date.toString(datePattern, Locale.getDefault()).toUpperCase(Locale.getDefault())

			override fun interpretSecondaryDate(date: DateTime) = date.toString(secondaryDatePattern, Locale.getDefault()).toUpperCase(Locale.getDefault())

			override fun interpretTime(minutes: Int) = DateTime()
					.withHourOfDay(minutes / DateTimeConstants.MINUTES_PER_HOUR)
					.withMinuteOfHour(minutes % DateTimeConstants.MINUTES_PER_HOUR)
					.toString(timePattern)
		}
	}

	internal fun calculateTimeTextHeight() {
		val rect = Rect()

		timeTextTopPaint.getTextBounds("00 PM", 0, "00 PM".length, rect)
		timeTextHeight = rect.height().toFloat()
	}

	internal fun calculateTimeCaptionHeight() {
		val rect = Rect()

		timeCaptionPaint.getTextBounds("10", 0, "10".length, rect)
		timeCaptionHeight = rect.height().toFloat()
	}

	internal fun calculateHeaderTextHeight() {
		val rect = Rect()

		headerTextPaint.getTextBounds("00 PM", 0, "00 PM".length, rect)
		headerTextHeight = rect.height().toFloat()
	}

	internal fun calculateHeaderSecondaryTextHeight() {
		val rect = Rect()

		headerSecondaryTextPaint.getTextBounds("00 PM", 0, "00 PM".length, rect)
		headerSecondaryTextHeight = rect.height().toFloat()
	}

	internal fun calculateHeaderHeight(config: WeekViewConfig) {
		var headerRowBottomLine = 0
		if (config.showHeaderRowBottomLine)
			headerRowBottomLine = config.headerRowBottomLineWidth

		headerHeight = headerTextHeight + headerSecondaryTextHeight + config.headerRowTextSpacing.toFloat() + headerRowBottomLine.toFloat() + config.headerRowPadding * 2
	}

	internal fun calculateTimeTextWidth() {
		timeTextWidth = timeTextTopPaint.measureText(dateTimeInterpreter.interpretTime(22 * 60 + 22))
	}

	internal fun calculateTimeColumnWidth(config: WeekViewConfig) {
		timeColumnWidth = timeTextWidth + config.timeColumnPadding * 2
	}
}
