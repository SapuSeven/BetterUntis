package com.alamkanak.weekview.config

import android.content.Context
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import com.alamkanak.weekview.DateTimeInterpreter
import com.alamkanak.weekview.DateUtils
import com.alamkanak.weekview.WeekView
import java.lang.Math.max
import java.lang.Math.min
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.DAY_OF_WEEK

class WeekViewDrawConfig(context: Context) {
	val timeTextTopPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	val timeTextBottomPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	val timeCaptionPaint: Paint = Paint()
	var timeTextWidth: Float = 0.0f
	var timeTextHeight: Float = 0.0f
	var timeColumnWidth: Float = 0.0f

	val headerTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	var headerTextHeight: Float = 0.0f
	val headerSecondaryTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	var headerSecondaryTextHeight: Float = 0.0f
	var headerHeight: Float = 0.0f

	var currentOrigin = PointF(0f, 0f)
	val headerBackgroundPaint: Paint = Paint()
	var widthPerDay: Float = 0.0f
	val dayBackgroundPaint: Paint = Paint()
	val hourSeparatorPaint: Paint = Paint()
	val daySeparatorPaint: Paint = Paint()
	var headerMarginBottom: Float = 0.0f

	val todayBackgroundPaint: Paint = Paint()
	val timeColumnSeparatorPaint: Paint = Paint()
	val nowLinePaint: Paint = Paint()
	val todayHeaderTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	val todayHeaderSecondaryTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	val holidayTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	val eventTextPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	val eventTopPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	val eventBottomPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
	val timeColumnBackgroundPaint: Paint = Paint()
	var newHourHeight = -1
	val dateTimeInterpreter: DateTimeInterpreter = buildDefaultDateTimeInterpreter(context)
	val futureBackgroundPaint: Paint = Paint()
	val pastBackgroundPaint: Paint = Paint()

	init {
		// Set additional permanent properties
		timeTextTopPaint.textAlign = Paint.Align.LEFT
		timeTextBottomPaint.textAlign = Paint.Align.RIGHT

		timeCaptionPaint.textAlign = Paint.Align.CENTER
		timeCaptionPaint.typeface = Typeface.DEFAULT_BOLD

		headerTextPaint.textAlign = Paint.Align.CENTER
		headerTextPaint.typeface = Typeface.DEFAULT_BOLD

		headerSecondaryTextPaint.textAlign = Paint.Align.CENTER
		headerSecondaryTextPaint.typeface = Typeface.DEFAULT

		hourSeparatorPaint.style = Paint.Style.STROKE

		daySeparatorPaint.style = Paint.Style.STROKE

		todayHeaderTextPaint.textAlign = Paint.Align.CENTER
		todayHeaderTextPaint.typeface = Typeface.DEFAULT_BOLD

		todayHeaderSecondaryTextPaint.textAlign = Paint.Align.CENTER
		todayHeaderSecondaryTextPaint.typeface = Typeface.DEFAULT

		holidayTextPaint.typeface = Typeface.DEFAULT_BOLD

		eventTextPaint.textAlign = Paint.Align.CENTER
		eventTextPaint.typeface = Typeface.DEFAULT_BOLD

		eventTopPaint.style = Paint.Style.FILL

		eventBottomPaint.style = Paint.Style.FILL
	}

	fun moveCurrentOriginIfFirstDraw(config: WeekViewConfig) {
		// If the week view is being drawn for the first time, then consider the first day of the week.
		val today = DateUtils.today()
		val isWeekView = config.numberOfVisibleDays >= 7
		val currentDayIsNotToday = today.get(DAY_OF_WEEK) != config.firstDayOfWeek
		if (isWeekView && currentDayIsNotToday && config.showFirstDayOfWeekFirst) {
			val difference = today.get(DAY_OF_WEEK) - config.firstDayOfWeek
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

	fun resetOrigin() {
		currentOrigin = PointF(0f, 0f)
	}

	fun getTodayBackgroundPaint(isToday: Boolean): Paint {
		return if (isToday) todayBackgroundPaint else dayBackgroundPaint
	}

	private fun buildDefaultDateTimeInterpreter(context: Context): DateTimeInterpreter {
		return object : DateTimeInterpreter {
			private val sdfDate = SimpleDateFormat("EEE", Locale.getDefault())
			private val sdfSecondaryDate = SimpleDateFormat("d. MMM", Locale.getDefault())
			private val sdfTime = DateUtils.getTimeFormat(context)
			private val calendar = Calendar.getInstance()

			override fun interpretDate(date: Calendar): String {
				return try {
					sdfDate.format(date.time).toUpperCase()
				} catch (e: Exception) {
					e.printStackTrace()
					""
				}

			}

			override fun interpretSecondaryDate(date: Calendar): String {
				return try {
					sdfSecondaryDate.format(date.time).toUpperCase()
				} catch (e: Exception) {
					e.printStackTrace()
					""
				}

			}

			override fun interpretTime(minutes: Int): String {
				calendar.clear()
				calendar.add(Calendar.MINUTE, minutes)

				return try {
					sdfTime.format(calendar.time)
				} catch (e: Exception) {
					e.printStackTrace()
					""
				}
			}
		}
	}

	internal fun calculateTimeTextHeight() {
		val rect = Rect()

		timeTextTopPaint.getTextBounds("00 PM", 0, "00 PM".length, rect)
		timeTextHeight = rect.height().toFloat()
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
