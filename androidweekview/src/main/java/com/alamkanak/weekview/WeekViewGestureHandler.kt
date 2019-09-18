package com.alamkanak.weekview

import android.content.Context
import android.view.*
import android.view.MotionEvent.ACTION_UP
import android.widget.OverScroller
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import com.alamkanak.weekview.config.WeekViewConfig
import com.alamkanak.weekview.config.WeekViewDrawConfig
import com.alamkanak.weekview.listeners.*
import com.alamkanak.weekview.loaders.WeekViewLoader
import java.lang.Math.*

internal class WeekViewGestureHandler<T>(
		context: Context,
		view: View,
		private val config: WeekViewConfig,
		private val data: WeekViewData<T>
) : GestureDetector.SimpleOnGestureListener() {
	var listener: Listener
	var scroller: OverScroller
	private val drawConfig: WeekViewDrawConfig
	private val touchHandler: WeekViewTouchHandler
	var currentScrollDirection = Direction.NONE
	var currentFlingDirection = Direction.NONE
	private val gestureDetector: GestureDetector
	private val scaleDetector: ScaleGestureDetector
	private var isZooming: Boolean = false
	private val minimumFlingVelocity: Int
	private val scaledTouchSlop: Int
	var eventClickListener: EventClickListener<T>? = null
	var eventLongPressListener: EventLongPressListener<T>? = null
	var emptyViewClickListener: EmptyViewClickListener? = null
	var emptyViewLongPressListener: EmptyViewLongPressListener? = null
	var topLeftCornerClickListener: TopLeftCornerClickListener? = null
	var topLeftCornerLongPressListener: TopLeftCornerLongPressListener? = null
	var weekViewLoader: WeekViewLoader<T>? = null
	var scrollListener: ScrollListener? = null

	init {
		this.listener = view as Listener
		this.drawConfig = config.drawConfig

		touchHandler = WeekViewTouchHandler(config)
		gestureDetector = GestureDetector(context, this)
		scroller = OverScroller(context, FastOutLinearInInterpolator())

		minimumFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
		scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

		scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.OnScaleGestureListener {
			override fun onScaleEnd(detector: ScaleGestureDetector) {
				isZooming = false
			}

			override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
				isZooming = true
				goToNearestOrigin()
				return true
			}

			override fun onScale(detector: ScaleGestureDetector): Boolean {
				val hourHeight = this@WeekViewGestureHandler.config.hourHeight.toFloat()
				drawConfig.newHourHeight = round(hourHeight * detector.scaleFactor)
				listener.onScaled()
				return true
			}
		})
	}

	override fun onDown(e: MotionEvent): Boolean {
		goToNearestOrigin()
		return true
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//   Gesture Detector
	//
	////////////////////////////////////////////////////////////////////////////////////////////////

	override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
		if (isZooming) {
			return true
		}

		val absDistanceX = abs(distanceX)
		val absDistanceY = abs(distanceY)

		val canScrollHorizontally = config.horizontalScrollingEnabled

		when (currentScrollDirection) {
			Direction.NONE -> {
				// Allow scrolling only in one direction.
				currentScrollDirection = if (absDistanceX > absDistanceY && canScrollHorizontally) {
					if (distanceX > 0)
						Direction.LEFT
					else
						Direction.RIGHT
				} else {
					Direction.VERTICAL
				}
			}
			Direction.LEFT -> {
				// Change direction if there was enough change.
				if (absDistanceX > absDistanceY && distanceX < -scaledTouchSlop)
					currentScrollDirection = Direction.RIGHT
			}
			Direction.RIGHT -> {
				// Change direction if there was enough change.
				if (absDistanceX > absDistanceY && distanceX > scaledTouchSlop)
					currentScrollDirection = Direction.LEFT
			}
			else -> {
			}
		}

		// Calculate the new origin after scroll.
		when (currentScrollDirection) {
			Direction.LEFT, Direction.RIGHT -> {
				drawConfig.currentOrigin.x -= distanceX * config.xScrollingSpeed
				listener.onScrolled()
			}
			Direction.VERTICAL -> {
				drawConfig.currentOrigin.y -= distanceY
				listener.onScrolled()
			}
			else -> {
			}
		}
		return true
	}

	override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
		if (isZooming) {
			return true
		}

		if (currentFlingDirection == Direction.LEFT && !config.horizontalFlingEnabled ||
				currentFlingDirection == Direction.RIGHT && !config.horizontalFlingEnabled ||
				currentFlingDirection == Direction.VERTICAL && !config.verticalFlingEnabled) {
			return true
		}

		scroller.forceFinished(true)

		currentFlingDirection = currentScrollDirection
		when (currentFlingDirection) {
			Direction.LEFT, Direction.RIGHT -> onFlingHorizontal(velocityX)
			Direction.VERTICAL -> onFlingVertical(velocityY)
			else -> {
			}
		}

		listener.onScrolled()
		return true
	}

	private fun onFlingHorizontal(originalVelocityX: Float) {
		val startX = drawConfig.currentOrigin.x.toInt()
		val startY = drawConfig.currentOrigin.y.toInt()

		val velocityX = (originalVelocityX * config.xScrollingSpeed).toInt()
		val velocityY = 0

		val minX = Integer.MIN_VALUE
		val maxX = Integer.MAX_VALUE

		val dayHeight = (config.hourHeight * config.hoursPerDay()).toInt()
		val viewHeight = WeekView.viewHeight

		val minY = (dayHeight + drawConfig.headerHeight - viewHeight).toInt() * -1
		val maxY = 0

		scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY)
	}

	private fun onFlingVertical(originalVelocityY: Float) {
		val startX = drawConfig.currentOrigin.x.toInt()
		val startY = drawConfig.currentOrigin.y.toInt()

		val velocityX = 0
		val velocityY = originalVelocityY.toInt()

		val minX = Integer.MIN_VALUE
		val maxX = Integer.MAX_VALUE

		val dayHeight = (config.hourHeight * config.hoursPerDay()).toInt()
		val viewHeight = WeekView.viewHeight

		val minY = (dayHeight + drawConfig.headerHeight - viewHeight).toInt() * -1
		val maxY = 0

		scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY)
	}

	override fun onSingleTapUp(e: MotionEvent): Boolean {
		val eventChip = findHitEvent(e)
		if (eventChip != null && eventClickListener != null) {
			val data = eventChip.event.data
			if (data != null) {
				eventClickListener!!.onEventClick(data, eventChip.rect!!)
			} else {
				throw WeekViewException("No data to show. Did you pass the original object into the constructor of WeekViewEvent?")
			}

			return super.onSingleTapUp(e)
		}

		// If the tap was on in an empty space, then trigger the callback.
		val timeColumnWidth = drawConfig.timeColumnWidth

		if (emptyViewClickListener != null
				&& e.x > timeColumnWidth && e.y > drawConfig.headerHeight) {
			val selectedTime = touchHandler.getTimeFromPoint(e)
			if (selectedTime != null) {
				emptyViewClickListener!!.onEmptyViewClicked(selectedTime)
			}
		}

		if (topLeftCornerClickListener != null
				&& e.x <= timeColumnWidth && e.y <= drawConfig.headerHeight) {
			topLeftCornerClickListener!!.onCornerClick()
		}

		return super.onSingleTapUp(e)
	}

	override fun onLongPress(e: MotionEvent) {
		super.onLongPress(e)

		val eventChip = findHitEvent(e)
		if (eventChip != null && eventLongPressListener != null) {
			val data = eventChip.originalEvent.data
			if (data != null) {
				eventLongPressListener!!.onEventLongPress(data, eventChip.rect!!)
			} else {
				throw WeekViewException("No data to show. Did you pass the original object into the constructor of WeekViewEvent?")
			}
		}

		val timeColumnWidth = drawConfig.timeColumnWidth

		// If the tap was on in an empty space, then trigger the callback.
		if (emptyViewLongPressListener != null
				&& e.x > timeColumnWidth && e.y > drawConfig.headerHeight) {
			val selectedTime = touchHandler.getTimeFromPoint(e)
			if (selectedTime != null) {
				emptyViewLongPressListener!!.onEmptyViewLongPress(selectedTime)
			}
		}

		if (topLeftCornerLongPressListener != null
				&& e.x <= timeColumnWidth && e.y <= drawConfig.headerHeight) {
			topLeftCornerLongPressListener!!.onCornerLongPress()
		}
	}

	private fun findHitEvent(e: MotionEvent): EventChip<T>? {
		data.eventChips?.forEach {
			if (it.isHit(e))
				return it
		}
		return null
	}

	private fun goToNearestOrigin() {
		val totalDayWidth = config.totalDayWidth
		var leftDays = drawConfig.currentOrigin.x.toDouble()
		leftDays /= if (config.startOnFirstDay)
			(totalDayWidth * config.numberOfVisibleDays).toDouble()
		else
			totalDayWidth.toDouble()

		leftDays = when {
			currentFlingDirection != Direction.NONE -> // snap to nearest day
				round(leftDays).toDouble()
			currentScrollDirection == Direction.LEFT -> // snap to last day
				floor(leftDays)
			currentScrollDirection == Direction.RIGHT -> // snap to next day
				ceil(leftDays)
			else -> // snap to nearest day
				round(leftDays).toDouble()
		}

		var nearestOrigin = drawConfig.currentOrigin.x.toInt()
		nearestOrigin -= if (config.startOnFirstDay)
			(leftDays * totalDayWidth.toDouble() * config.numberOfVisibleDays.toDouble()).toInt()
		else
			(leftDays * totalDayWidth).toInt()

		if (nearestOrigin != 0) {
			// Stop current animation
			scroller.forceFinished(true)

			// Snap to date
			val startX = drawConfig.currentOrigin.x.toInt()
			val startY = drawConfig.currentOrigin.y.toInt()

			val distanceX = -nearestOrigin
			val distanceY = 0

			val daysScrolled = abs(nearestOrigin) / drawConfig.widthPerDay
			val duration = (daysScrolled * config.scrollDuration).toInt()

			scroller.startScroll(startX, startY, distanceX, distanceY, duration)
			listener.onScrolled()
		}

		// Reset scrolling and fling direction.
		currentFlingDirection = Direction.NONE
		currentScrollDirection = currentFlingDirection
	}

	fun onTouchEvent(event: MotionEvent): Boolean {
		scaleDetector.onTouchEvent(event)
		val `val` = gestureDetector.onTouchEvent(event)

		// Check after call of gestureDetector, so currentFlingDirection and currentScrollDirection are set
		if (event.action == ACTION_UP && !isZooming && currentFlingDirection == Direction.NONE) {
			if (currentScrollDirection == Direction.RIGHT || currentScrollDirection == Direction.LEFT) {
				goToNearestOrigin()
			}
			currentScrollDirection = Direction.NONE
		}

		return `val`
	}

	fun forceScrollFinished() {
		scroller.forceFinished(true)
		currentFlingDirection = Direction.NONE
		currentScrollDirection = currentFlingDirection
	}

	fun computeScroll() {
		if (scroller.isFinished && currentFlingDirection != Direction.NONE) {
			// Snap to day after fling is finished
			goToNearestOrigin()
		} else {
			if (currentFlingDirection != Direction.NONE && shouldForceFinishScroll()) {
				goToNearestOrigin()
			} else if (scroller.computeScrollOffset()) {
				drawConfig.currentOrigin.y = scroller.currY.toFloat()
				drawConfig.currentOrigin.x = scroller.currX.toFloat()
				listener.onScrolled()
			}
		}
	}

	/**
	 * Check if scrolling should be stopped.
	 *
	 * @return true if scrolling should be stopped before reaching the end of animation.
	 */
	private fun shouldForceFinishScroll(): Boolean {
		return scroller.currVelocity <= minimumFlingVelocity
	}

	internal enum class Direction {
		NONE, LEFT, RIGHT, VERTICAL
	}

	internal interface Listener {
		fun onScaled()

		fun onScrolled()
	}

}
