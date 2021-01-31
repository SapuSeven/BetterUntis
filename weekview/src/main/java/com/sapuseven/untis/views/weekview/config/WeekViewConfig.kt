package com.sapuseven.untis.views.weekview.config

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import com.sapuseven.untis.views.weekview.R
import org.joda.time.DateTimeConstants

/**
 * This class contains all attributes that can be specified using XML.
 *
 * Setting any value will update the drawConfig accordingly as well.
 */
class WeekViewConfig(context: Context, attrs: AttributeSet?) {
	val drawConfig: WeekViewDrawConfig = WeekViewDrawConfig(context)

	// Calendar configuration
	var firstDayOfWeek: Int
	var snapToWeek: Boolean
	var visibleDays: Int = 0
	var weekLength: Int = 0
	var showFirstDayOfWeekFirst: Boolean

	// Time column
	var timeColumnVisibility: Boolean = true
		set(value) {
			field = value
			drawConfig.timeTextVisibility = value
		}
	var timeColumnTextColor: Int = 0
		set(value) {
			field = value
			drawConfig.timeTextTopPaint.color = value
			drawConfig.timeTextBottomPaint.color = value
		}
	var timeColumnCaptionColor: Int
		get() = drawConfig.timeCaptionPaint.color
		set(value) {
			drawConfig.timeCaptionPaint.color = value
		}
	var timeColumnBackgroundColor: Int
		get() = drawConfig.timeColumnBackgroundPaint.color
		set(value) {
			drawConfig.timeColumnBackgroundPaint.color = value
		}
	var timeColumnPadding: Int = 0
		set(value) {
			field = value
			drawConfig.calculateTimeTextWidth()
		}
	var timeColumnTextSize: Float
		get() = drawConfig.timeTextTopPaint.textSize
		set(value) {
			drawConfig.timeTextTopPaint.textSize = value
			drawConfig.timeTextBottomPaint.textSize = value
			drawConfig.calculateTimeTextWidth()
			drawConfig.calculateTimeTextHeight()
		}
	var timeColumnCaptionSize: Float
		get() = drawConfig.timeCaptionPaint.textSize
		set(value) {
			drawConfig.timeCaptionPaint.textSize = value
			drawConfig.calculateTimeCaptionHeight()
		}
	// Time column separator
	var showTimeColumnSeparator: Boolean
	var timeColumnSeparatorColor: Int
		get() = drawConfig.timeColumnSeparatorPaint.color
		set(value) {
			drawConfig.timeColumnSeparatorPaint.color = value
		}
	var timeColumnSeparatorStrokeWidth: Float
	var hourIndexOffset: Int = 0

	// Header row
	var headerRowTextColor: Int
		get() = drawConfig.headerTextPaint.color
		set(value) {
			drawConfig.headerTextPaint.color = value
			drawConfig.todayHeaderTextPaint.color = value
		}
	var headerRowTextSize: Float
		get() = drawConfig.headerTextPaint.textSize
		set(value) {
			drawConfig.headerTextPaint.textSize = value
			drawConfig.todayHeaderTextPaint.textSize = value
			drawConfig.calculateHeaderTextHeight()
		}
	var headerRowSecondaryTextColor: Int
		get() = drawConfig.headerSecondaryTextPaint.color
		set(value) {
			drawConfig.headerSecondaryTextPaint.color = value
			drawConfig.todayHeaderSecondaryTextPaint.color = value
		}
	var headerRowSecondaryTextSize: Float
		get() = drawConfig.headerSecondaryTextPaint.textSize
		set(value) {
			drawConfig.headerSecondaryTextPaint.textSize = value
			drawConfig.todayHeaderSecondaryTextPaint.textSize = value
			drawConfig.calculateHeaderSecondaryTextHeight()
		}
	var headerRowBackgroundColor: Int
		get() = drawConfig.headerBackgroundPaint.color
		set(value) {
			drawConfig.headerBackgroundPaint.color = value
		}
	var headerRowPadding: Int = 0
		set(value) {
			field = value
			drawConfig.calculateHeaderHeight(this)
		}
	var headerRowTextSpacing: Int
	var todayHeaderTextColor: Int
		get() = drawConfig.todayHeaderTextPaint.color
		set(value) {
			drawConfig.todayHeaderTextPaint.color = value
			drawConfig.todayHeaderSecondaryTextPaint.color = value
		}

	// Header bottom line
	var showHeaderRowBottomLine: Boolean
	var headerRowBottomLineColor: Int
	var headerRowBottomLineWidth: Int = 0
		set(value) {
			field = value
			drawConfig.calculateHeaderHeight(this)
		}

	// Event chips
	var allDayEventHeight: Int
	var eventCornerRadius: Int
	var eventTextSize: Float
		get() = drawConfig.eventTextPaint.textSize
		set(value) {
			drawConfig.eventTextPaint.textSize = value
		}
	var eventSecondaryTextSize: Float
		get() = drawConfig.eventTopPaint.textSize
		set(value) {
			drawConfig.eventTopPaint.textSize = value
			drawConfig.eventBottomPaint.textSize = value
		}
	var eventSecondaryTextCentered: Boolean = false
		set(value) {
			field = value
			drawConfig.eventTopPaint.textAlign = if (value) Paint.Align.CENTER else Paint.Align.LEFT
			drawConfig.eventBottomPaint.textAlign = if (value) Paint.Align.CENTER else Paint.Align.RIGHT
		}
	var eventTextColor: Int
		get() = drawConfig.eventTextPaint.color
		set(value) {
			drawConfig.eventTextPaint.color = value
			drawConfig.eventTopPaint.color = value
			drawConfig.eventBottomPaint.color = value
		}
	var eventPadding: Int
	var defaultEventColor: Int

	// Holidays and free days
	var holidayTextColor: Int
		get() = drawConfig.holidayTextPaint.color
		set(value) {
			drawConfig.holidayTextPaint.color = value
		}
	var holidayTextSize: Float
		get() = drawConfig.holidayTextPaint.textSize
		set(value) {
			drawConfig.holidayTextPaint.textSize = value
		}

	// Event margins
	var columnGap: Int
	var overlappingEventGap: Int
	var eventMarginVertical: Int
	var eventMarginHorizontal: Int

	// Colors
	var dayBackgroundColor: Int
		get() = drawConfig.dayBackgroundPaint.color
		set(value) {
			drawConfig.dayBackgroundPaint.color = value
		}
	var pastBackgroundColor: Int
		get() = drawConfig.pastBackgroundPaint.color
		set(value) {
			drawConfig.pastBackgroundPaint.color = value
		}
	var futureBackgroundColor: Int
		get() = drawConfig.futureBackgroundPaint.color
		set(value) {
			drawConfig.futureBackgroundPaint.color = value
		}

	// Hour height
	var hourHeight: Int
	var minHourHeight: Int
	var maxHourHeight: Int
	var effectiveMinHourHeight: Int

	// Now line
	var showNowLine: Boolean
	var nowLineColor: Int
		get() = drawConfig.nowLinePaint.color
		set(value) {
			drawConfig.nowLinePaint.color = value
		}
	var nowLineStrokeWidth: Float
		get() = drawConfig.nowLinePaint.strokeWidth
		set(value) {
			drawConfig.nowLinePaint.strokeWidth = value
		}

	// Hour separators
	var showHourSeparator: Boolean
	var hourSeparatorColor: Int
		get() = drawConfig.hourSeparatorPaint.color
		set(value) {
			drawConfig.hourSeparatorPaint.color = value
		}
	var hourSeparatorStrokeWidth: Float
		get() = drawConfig.hourSeparatorPaint.strokeWidth
		set(value) {
			drawConfig.hourSeparatorPaint.strokeWidth = value
		}

	// Day separators
	var showDaySeparator: Boolean
	var daySeparatorColor: Int
		get() = drawConfig.daySeparatorPaint.color
		set(value) {
			drawConfig.daySeparatorPaint.color = value
		}
	var daySeparatorStrokeWidth: Float
		get() = drawConfig.daySeparatorPaint.strokeWidth
		set(value) {
			drawConfig.daySeparatorPaint.strokeWidth = value
		}

	// Scrolling
	var xScrollingSpeed: Float
	var verticalFlingEnabled: Boolean
	var horizontalFlingEnabled: Boolean
	var horizontalScrollingEnabled: Boolean
	var scrollDuration: Int

	// Top left corner
	var topLeftCornerDrawable: Drawable? = null
	var topLeftCornerPadding: Int

	// Custom properties
	var startTime: Int = 0 // in minutes
	var endTime: Int = 0 // in minutes
	var hourLines: IntArray = IntArray(0) // in minutes
	lateinit var hourLabels: Array<String>
	// Calculated values

	val timeColumnWidth: Float
		get() = drawConfig.timeTextWidth + timeColumnPadding * 2

	val totalDayWidth: Float
		get() = drawConfig.widthPerDay

	val totalDayHeight: Float
		get() {
			val dayHeight = hourHeight * hoursPerDay()
			val totalHeaderPadding = (headerRowPadding * 2).toFloat()
			val headerBottomMargin = drawConfig.headerMarginBottom
			return dayHeight + drawConfig.headerHeight + totalHeaderPadding + headerBottomMargin
		}

	val isSingleDay: Boolean
		get() = visibleDays == 1

	init {
		val a = context.theme.obtainStyledAttributes(attrs, R.styleable.WeekView, 0, 0)
		try {
			// Calendar configuration
			firstDayOfWeek = a.getInteger(R.styleable.WeekView_firstDayOfWeek, DateTimeConstants.MONDAY)
			snapToWeek = a.getBoolean(R.styleable.WeekView_snapToWeek, true)
			visibleDays = a.getInteger(R.styleable.WeekView_numberOfVisibleDays, 3)
			showFirstDayOfWeekFirst = a.getBoolean(R.styleable.WeekView_showFirstDayOfWeekFirst, false)

			// Header bottom line
			showHeaderRowBottomLine = a.getBoolean(R.styleable.WeekView_showHeaderRowBottomLine, false)
			headerRowBottomLineColor = a.getColor(R.styleable.WeekView_headerRowBottomLineColor, Color.rgb(102, 102, 102))
			headerRowBottomLineWidth = a.getDimensionPixelSize(R.styleable.WeekView_headerRowBottomLineWidth, 1)

			// Time column
			timeColumnTextColor = a.getColor(R.styleable.WeekView_timeColumnTextColor, Color.BLACK)
			timeColumnCaptionColor = a.getColor(R.styleable.WeekView_timeColumnCaptionColor, Color.BLACK)
			timeColumnBackgroundColor = a.getColor(R.styleable.WeekView_timeColumnBackground, Color.WHITE)
			timeColumnPadding = a.getDimensionPixelSize(R.styleable.WeekView_timeColumnPadding, 10)
			timeColumnTextSize = a.getDimensionPixelSize(R.styleable.WeekView_timeColumnTextSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12.0f, context.resources.displayMetrics).toInt()).toFloat()
			timeColumnCaptionSize = a.getDimensionPixelSize(R.styleable.WeekView_timeColumnCaptionSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14.0f, context.resources.displayMetrics).toInt()).toFloat()

			// Time column separator
			showTimeColumnSeparator = a.getBoolean(R.styleable.WeekView_showTimeColumnSeparator, false)
			timeColumnSeparatorColor = a.getColor(R.styleable.WeekView_timeColumnSeparatorColor, Color.rgb(102, 102, 102))
			timeColumnSeparatorStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_timeColumnSeparatorStrokeWidth, 1).toFloat()

			// Header row
			headerRowBackgroundColor = a.getColor(R.styleable.WeekView_headerRowBackgroundColor, Color.WHITE)
			headerRowTextColor = a.getColor(R.styleable.WeekView_headerRowTextColor, Color.BLACK)
			headerRowTextSize = a.getDimensionPixelSize(R.styleable.WeekView_headerRowTextSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12.0f, context.resources.displayMetrics).toInt()).toFloat()
			headerRowSecondaryTextColor = a.getColor(R.styleable.WeekView_headerRowSecondaryTextColor, Color.BLACK)
			headerRowSecondaryTextSize = a.getDimensionPixelSize(R.styleable.WeekView_headerRowSecondaryTextSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12.0f, context.resources.displayMetrics).toInt()).toFloat()
			headerRowPadding = a.getDimensionPixelSize(R.styleable.WeekView_headerRowPadding, 12)
			headerRowTextSpacing = a.getDimensionPixelSize(R.styleable.WeekView_headerRowTextSpacing, 12)
			todayHeaderTextColor = a.getColor(R.styleable.WeekView_todayHeaderTextColor, Color.rgb(39, 137, 228))

			// Event chips
			allDayEventHeight = a.getDimensionPixelSize(R.styleable.WeekView_allDayEventHeight, 100)
			eventCornerRadius = a.getDimensionPixelSize(R.styleable.WeekView_eventCornerRadius, 4)
			eventTextSize = a.getDimensionPixelSize(R.styleable.WeekView_eventTextSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14.0f, context.resources.displayMetrics).toInt()).toFloat()
			eventSecondaryTextSize = a.getDimensionPixelSize(R.styleable.WeekView_eventSecondaryTextSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12.0f, context.resources.displayMetrics).toInt()).toFloat()
			eventSecondaryTextCentered = a.getBoolean(R.styleable.WeekView_eventSecondaryTextCentered, false)
			eventTextColor = a.getColor(R.styleable.WeekView_eventTextColor, Color.BLACK)
			eventPadding = a.getDimensionPixelSize(R.styleable.WeekView_eventPadding, 8)
			defaultEventColor = a.getColor(R.styleable.WeekView_defaultEventColor, Color.parseColor("#9fc6e7"))

			// Event margins
			columnGap = a.getDimensionPixelSize(R.styleable.WeekView_columnGap, 10)
			overlappingEventGap = a.getDimensionPixelSize(R.styleable.WeekView_overlappingEventGap, 4)
			eventMarginVertical = a.getDimensionPixelSize(R.styleable.WeekView_eventMarginVertical, 4)
			eventMarginHorizontal = a.getDimensionPixelSize(R.styleable.WeekView_singleDayHorizontalMargin, 4)

			// Holidays and free days
			holidayTextColor = a.getColor(R.styleable.WeekView_holidayTextColor, Color.BLACK)
			holidayTextSize = a.getDimensionPixelSize(R.styleable.WeekView_holidayTextSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12.0f, context.resources.displayMetrics).toInt()).toFloat()

			// Colors
			dayBackgroundColor = a.getColor(R.styleable.WeekView_dayBackgroundColor, Color.WHITE)
			pastBackgroundColor = a.getColor(R.styleable.WeekView_pastBackgroundColor, Color.rgb(227, 227, 227))
			futureBackgroundColor = a.getColor(R.styleable.WeekView_futureBackgroundColor, Color.rgb(245, 245, 245))

			// Hour height
			hourHeight = a.getDimensionPixelSize(R.styleable.WeekView_hourHeight, 50)
			minHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_minHourHeight, 0) // no minimum specified (will be dynamic, based on screen)
			maxHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_maxHourHeight, 500)
			effectiveMinHourHeight = minHourHeight

			// Now line
			showNowLine = a.getBoolean(R.styleable.WeekView_showNowLine, false)
			nowLineColor = a.getColor(R.styleable.WeekView_nowLineColor, Color.rgb(102, 102, 102))
			nowLineStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_nowLineStrokeWidth, 5).toFloat()

			// Hour separators
			showHourSeparator = a.getBoolean(R.styleable.WeekView_showHourSeparator, true)
			hourSeparatorColor = a.getColor(R.styleable.WeekView_hourSeparatorColor, Color.rgb(230, 230, 230))
			hourSeparatorStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_hourSeparatorStrokeWidth, 2).toFloat()

			// Day separators
			showDaySeparator = a.getBoolean(R.styleable.WeekView_showDaySeparator, true)
			daySeparatorColor = a.getColor(R.styleable.WeekView_daySeparatorColor, Color.rgb(230, 230, 230))
			daySeparatorStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_daySeparatorStrokeWidth, 2).toFloat()

			// Scrolling
			xScrollingSpeed = a.getFloat(R.styleable.WeekView_xScrollingSpeed, 1.0f)
			horizontalFlingEnabled = a.getBoolean(R.styleable.WeekView_horizontalFlingEnabled, true)
			horizontalScrollingEnabled = a.getBoolean(R.styleable.WeekView_horizontalScrollingEnabled, true)
			verticalFlingEnabled = a.getBoolean(R.styleable.WeekView_verticalFlingEnabled, true)
			scrollDuration = a.getInt(R.styleable.WeekView_scrollDuration, 150)

			// Top left corner
			topLeftCornerDrawable = a.getDrawable(R.styleable.WeekView_topLeftCornerDrawable)
			topLeftCornerPadding = a.getDimensionPixelSize(R.styleable.WeekView_topLeftCornerPadding, 0)
		} finally {
			a.recycle()
		}

		drawConfig.calculateTimeColumnWidth(this)
		drawConfig.calculateHeaderHeight(this)
	}

	fun hoursPerDay(): Float {
		return (endTime - startTime) / 60.0f
	}

	fun minutesPerDay(): Float {
		return hoursPerDay() * 60
	}
}
