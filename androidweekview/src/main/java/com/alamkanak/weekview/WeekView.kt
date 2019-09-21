package com.alamkanak.weekview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.alamkanak.weekview.config.WeekViewConfig
import com.alamkanak.weekview.drawers.*
import com.alamkanak.weekview.listeners.*
import com.alamkanak.weekview.loaders.WeekLoader
import com.alamkanak.weekview.loaders.WeekViewLoader
import org.joda.time.LocalDate
import java.lang.Math.ceil
import java.lang.Math.min
import java.util.*
import java.util.Calendar.DATE
import java.util.Calendar.HOUR_OF_DAY

class WeekView<T>(
		context: Context,
		attrs: AttributeSet? = null
) :
		View(context, attrs),
		WeekViewGestureHandler.Listener,
		WeekViewViewState.UpdateListener {
	private val config: WeekViewConfig = WeekViewConfig(context, attrs)
	private val data: WeekViewData<T> = WeekViewData()

	private val viewState: WeekViewViewState = WeekViewViewState()
	private val gestureHandler: WeekViewGestureHandler<T>

	private val headerRowDrawer: HeaderRowDrawer
	private val dayLabelDrawer: DayLabelDrawer
	private val eventsDrawer: EventsDrawer<T>
	private val holidayDrawer: HolidayDrawer
	private val timeColumnDrawer: TimeColumnDrawer
	private val dayBackgroundDrawer: DayBackgroundDrawer
	private val backgroundGridDrawer: BackgroundGridDrawer
	private val nowLineDrawer: NowLineDrawer
	private val topLeftCornerDrawer: TopLeftCornerDrawer

	private val eventChipsProvider: EventChipProvider<T>

	// TODO: Move these setters entirely to WeekViewConfig
	var eventCornerRadius: Int
		get() = config.eventCornerRadius
		set(eventCornerRadius) {
			config.eventCornerRadius = eventCornerRadius
		}

	var eventTextBold: Boolean
		get() = config.drawConfig.eventTextPaint.typeface == Typeface.DEFAULT_BOLD
		set(bold) {
			config.drawConfig.eventTextPaint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
			invalidate()
		}

	var eventTextSize: Float
		get() = config.eventTextSize
		set(eventTextSize) {
			config.eventTextSize = eventTextSize
			invalidate()
		}

	var eventSecondaryTextCentered: Boolean
		get() = config.eventSecondaryTextCentered
		set(eventSecondaryTextCentered) {
			config.eventSecondaryTextCentered = eventSecondaryTextCentered
			invalidate()
		}

	var eventSecondaryTextSize: Float
		get() = config.eventSecondaryTextSize
		set(eventSecondaryTextSize) {
			config.eventSecondaryTextSize = eventSecondaryTextSize
			invalidate()
		}

	var eventTextColor: Int
		get() = config.eventTextColor
		set(eventTextColor) {
			config.eventTextColor = eventTextColor
			invalidate()
		}

	var columnGap: Int
		get() = config.columnGap
		set(columnGap) {
			config.columnGap = columnGap
			invalidate()
		}

	var overlappingEventGap: Int
		get() = config.overlappingEventGap
		set(overlappingEventGap) {
			config.overlappingEventGap = overlappingEventGap
			invalidate()
		}

	var hourHeight: Int
		get() = config.hourHeight
		set(hourHeight) {
			config.drawConfig.newHourHeight = hourHeight
			invalidate()
		}

	var nowLineColor: Int
		get() = config.nowLineColor
		set(nowLineColor) {
			config.nowLineColor = nowLineColor
			config.drawConfig.nowLinePaint.color = config.nowLineColor
			invalidate()
		}

	var numberOfVisibleDays: Int
		get() = config.numberOfVisibleDays
		set(value) {
			config.numberOfVisibleDays = value
		}

	//  Public methods

	fun setOnEventClickListener(listener: EventClickListener<T>) {
		gestureHandler.eventClickListener = listener
	}

	fun setOnCornerClickListener(listener: TopLeftCornerClickListener) {
		gestureHandler.topLeftCornerClickListener = listener
	}

	/**
	 * Event loaders define the interval after which the events are loaded in week view.
	 * For a MonthLoader events are loaded for every month.
	 * You can define your custom event loader by extending WeekViewLoader.
	 *
	 * @return The event loader.
	 */
	var weekViewLoader: WeekViewLoader<T>?
		get() = gestureHandler.weekViewLoader
		set(weekViewLoader) {
			gestureHandler.weekViewLoader = weekViewLoader
			eventChipsProvider.weekViewLoader = weekViewLoader
		}

	val eventLongPressListener: EventLongPressListener<*>?
		get() = gestureHandler.eventLongPressListener

	var emptyViewClickListener: EmptyViewClickListener?
		get() = gestureHandler.emptyViewClickListener
		set(emptyViewClickListener) {
			gestureHandler.emptyViewClickListener = emptyViewClickListener
		}

	var emptyViewLongPressListener: EmptyViewLongPressListener?
		get() = gestureHandler.emptyViewLongPressListener
		set(emptyViewLongPressListener) {
			gestureHandler.emptyViewLongPressListener = emptyViewLongPressListener
		}

	var scrollListener: ScrollListener?
		get() = gestureHandler.scrollListener
		set(scrollListener) {
			gestureHandler.scrollListener = scrollListener
		}

	var hourLines: IntArray
		get() = config.hourLines
		set(lines) {
			config.hourLines = lines
			invalidate()
		}

	var startTime: Int
		get() = config.startTime
		set(startTime) {
			config.startTime = startTime
			invalidate()
		}

	var endTime: Int
		get() = config.endTime
		set(endTime) {
			config.endTime = endTime
			invalidate()
		}

	// TODO: Re-do day offset calculation
	val currentDate: LocalDate
		get() {
			val weekOffset = (-config.drawConfig.currentOrigin.x / config.drawConfig.widthPerDay / config.numberOfVisibleDays.toFloat()).toInt()
			var offset = 0
			var today = DateUtils.today().get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
			if (today == 0)
				today = 7

			if (today - Calendar.MONDAY + 1 < config.numberOfVisibleDays)
				offset = today - Calendar.MONDAY + 1

			val localDate = LocalDate.now().plusWeeks(weekOffset).minusDays(offset)

			return localDate
		}

	init {
		gestureHandler = WeekViewGestureHandler(context, this, config, data)

		eventsDrawer = EventsDrawer(config)
		holidayDrawer = HolidayDrawer(config)
		timeColumnDrawer = TimeColumnDrawer(config)

		headerRowDrawer = HeaderRowDrawer(config)
		dayLabelDrawer = DayLabelDrawer(config)

		dayBackgroundDrawer = DayBackgroundDrawer(config)
		backgroundGridDrawer = BackgroundGridDrawer(config)
		nowLineDrawer = NowLineDrawer(config)
		topLeftCornerDrawer = TopLeftCornerDrawer(config)

		eventChipsProvider = EventChipProvider(config, data, viewState)
		eventChipsProvider.weekViewLoader = weekViewLoader
	}

	override fun onSaveInstanceState(): Parcelable {
		return SavedState(super.onSaveInstanceState()!!, config.numberOfVisibleDays)
	}

	override fun onRestoreInstanceState(state: Parcelable) {
		val savedState = state as SavedState
		super.onRestoreInstanceState(savedState.superState)
		config.numberOfVisibleDays = savedState.numberOfVisibleDays
	}

	override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
		super.onSizeChanged(width, height, oldWidth, oldHeight)
		viewState.areDimensionsInvalid = true

		viewWidth = width
		viewHeight = height
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)

		val isFirstDraw = viewState.isFirstDraw

		viewState.update(config, this)

		if (viewState.isFirstDraw) {
			viewState.isFirstDraw = false
			config.drawConfig.moveCurrentOriginIfFirstDraw(config)
		}

		config.drawConfig.refreshAfterZooming(config)
		config.drawConfig.updateVerticalOrigin(config)

		notifyScrollListeners()

		prepareEventDrawing(canvas)

		val drawingContext = DrawingContext.create(config, viewState)
		eventChipsProvider.loadEventsIfNecessary(this, drawingContext.dayRange)

		dayBackgroundDrawer.draw(drawingContext, canvas)
		backgroundGridDrawer.draw(drawingContext, canvas)

		eventsDrawer.drawEvents(data.eventChips, drawingContext, canvas)
		holidayDrawer.draw(drawingContext, canvas)
		nowLineDrawer.draw(drawingContext, canvas)

		headerRowDrawer.draw(drawingContext, canvas)
		dayLabelDrawer.draw(drawingContext, canvas)

		timeColumnDrawer.draw(drawingContext, canvas)

		topLeftCornerDrawer.draw(drawingContext, canvas)

		if (isFirstDraw) {
			// Temporary workaround to make sure that the events are actually being displayed
			invalidate()
		}
	}

	private fun notifyScrollListeners() {
		// Iterate through each day.
		val oldFirstVisibleDay = viewState.firstVisibleDay
		val today = DateUtils.today()

		(today.clone() as Calendar).let { firstVisibleDay ->
			viewState.firstVisibleDay = firstVisibleDay
			val totalDayWidth = config.totalDayWidth
			val delta = ceil((config.drawConfig.currentOrigin.x / totalDayWidth).toDouble()).toInt() * -1
			viewState.firstVisibleDay?.add(DATE, delta)

			val hasFirstVisibleDayChanged = viewState.firstVisibleDay != oldFirstVisibleDay
			if (hasFirstVisibleDayChanged) {
				scrollListener?.onFirstVisibleDayChanged(firstVisibleDay, oldFirstVisibleDay)
			}
		}
	}

	private fun prepareEventDrawing(canvas: Canvas) {
		// Clear the cache for event rectangles.
		data.clearEventChipsCache()
		canvas.save()
		clipEventsRect(canvas)
		calculateWidthPerDay()
	}

	private fun calculateWidthPerDay() {
		// Calculate the available width for each day
		config.drawConfig.widthPerDay = width - config.drawConfig.timeColumnWidth
		config.drawConfig.widthPerDay = config.drawConfig.widthPerDay / config.numberOfVisibleDays
	}

	private fun clipEventsRect(canvas: Canvas) {
		// Clip to event area.
		canvas.clipRect(config.drawConfig.timeColumnWidth, config.drawConfig.headerHeight, viewWidth.toFloat(), viewHeight.toFloat())
	}

	override fun onScaled() {
		invalidate()
	}

	override fun onScrolled() {
		invalidate()
		//ViewCompat.postInvalidateOnAnimation(this)
	}

	override fun invalidate() {
		super.invalidate()
		viewState.invalidate()
	}

	//  Functions related to scrolling

	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(event: MotionEvent): Boolean {
		return gestureHandler.onTouchEvent(event)
	}


	override fun computeScroll() {
		super.computeScroll()
		gestureHandler.computeScroll()
	}

	/**
	 * Show today on the week view.
	 */
	fun goToToday() {
		goToDate(DateUtils.today())
	}

	fun goToCurrentTime() {
		val today = Calendar.getInstance()
		val hour = today.get(HOUR_OF_DAY)
		goToDate(today)
		goToHour(hour)
	}

	/**
	 * Show a specific day on the week view.
	 *
	 * @param date The date to show.
	 */
	override fun goToDate(date: Calendar) {
		// TODO: If the date is in the future, selecting a weekend day will jump to the previous week. It works for past dates. This should be fixed to always jump to the next week when selecting a weekend.
		gestureHandler.forceScrollFinished()

		if (viewState.areDimensionsInvalid) {
			viewState.scrollToDay = date
			return
		}

		val diff = DateUtils.getDaysUntilDate(date)
		var actualDiff = DateUtils.getDisplayedDays(DateUtils.today(), diff, Calendar.MONDAY, Calendar.MONDAY + config.numberOfVisibleDays - 1).toDouble()

		val totalDayWidth = config.totalDayWidth

		if (config.startOnFirstDay)
			actualDiff /= config.numberOfVisibleDays.toDouble()

		val leftOriginCount = Math.floor(actualDiff).toInt()

		var nearestOrigin = 0
		nearestOrigin -= if (config.startOnFirstDay)
			(leftOriginCount.toFloat() * totalDayWidth * config.numberOfVisibleDays.toFloat()).toInt()
		else
			(leftOriginCount * totalDayWidth).toInt()

		if (nearestOrigin - config.drawConfig.currentOrigin.x != 0f) {
			gestureHandler.scroller.forceFinished(true)

			val startX = config.drawConfig.currentOrigin.x.toInt()

			val distanceX = (nearestOrigin - config.drawConfig.currentOrigin.x).toInt()

			val duration = config.scrollDuration

			gestureHandler.scroller.startScroll(startX, config.drawConfig.currentOrigin.y.toInt(), distanceX, 0, duration)
			gestureHandler.listener.onScrolled()
		}

		invalidate()
	}

	/**
	 * Refreshes the view and loads the events again.
	 */
	fun notifyDataSetChanged() {
		viewState.shouldRefreshEvents = true
		invalidate()
	}

	/**
	 * Vertically scroll to a specific hour in the week view.
	 *
	 * @param hour The hour to scroll to in 24-hour format. Supported values are 0-24.
	 */
	override fun goToHour(hour: Int) {
		if (viewState.areDimensionsInvalid) {
			viewState.scrollToHour = hour
			return
		}

		var verticalOffset = config.hourHeight * min(hour.toFloat(), config.hoursPerDay()).toInt()

		val dayHeight = config.totalDayHeight
		val viewHeight = height.toDouble()

		val desiredOffset = dayHeight - viewHeight
		verticalOffset = min(desiredOffset, verticalOffset.toDouble()).toInt()

		config.drawConfig.currentOrigin.y = (-verticalOffset).toFloat()
		invalidate()
	}

	//  Listeners

	fun setPeriodChangeListener(weekChangeListener: WeekViewLoader.PeriodChangeListener<T>) {
		val weekViewLoader = WeekLoader(weekChangeListener)
		gestureHandler.weekViewLoader = weekViewLoader
		eventChipsProvider.weekViewLoader = weekViewLoader
	}

	fun swipeRefreshAvailable(): Boolean {
		return (gestureHandler.currentFlingDirection == WeekViewGestureHandler.Direction.NONE
				&& gestureHandler.currentScrollDirection == WeekViewGestureHandler.Direction.NONE
				&& config.drawConfig.currentOrigin.y == 0f)
	}

	fun addHolidays(holidays: List<HolidayChip>) {
		holidayDrawer.holidayChips = holidays
	}

	class SavedState : BaseSavedState {
		val numberOfVisibleDays: Int

		internal constructor(superState: Parcelable, numberOfVisibleDays: Int) : super(superState) {
			this.numberOfVisibleDays = numberOfVisibleDays
		}

		private constructor(`in`: Parcel) : super(`in`) {
			numberOfVisibleDays = `in`.readInt()
		}

		override fun writeToParcel(destination: Parcel, flags: Int) {
			super.writeToParcel(destination, flags)
			destination.writeInt(numberOfVisibleDays)
		}

		companion object CREATOR : Parcelable.Creator<SavedState> {
			override fun createFromParcel(source: Parcel): SavedState {
				return SavedState(source)
			}

			override fun newArray(size: Int): Array<SavedState?> {
				return arrayOfNulls(size)
			}
		}
	}

	companion object {
		// TODO: Move to config or drawConfig
		var viewWidth: Int = 0
		var viewHeight: Int = 0
	}
}
