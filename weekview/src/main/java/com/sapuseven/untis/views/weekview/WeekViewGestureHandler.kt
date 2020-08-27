package com.sapuseven.untis.views.weekview

import android.content.Context
import android.view.*
import android.view.MotionEvent.ACTION_UP
import android.widget.OverScroller
import com.sapuseven.untis.views.weekview.config.WeekViewConfig
import com.sapuseven.untis.views.weekview.config.WeekViewDrawConfig
import com.sapuseven.untis.views.weekview.listeners.*
import com.sapuseven.untis.views.weekview.loaders.WeekViewLoader
import kotlin.math.*

internal class WeekViewGestureHandler<T>(
		context: Context,
		view: View,
		private val config: WeekViewConfig,
		private val data: WeekViewData<T>
) : GestureDetector.SimpleOnGestureListener() {
	var listener: Listener
	var scroller: OverScroller
	internal val drawConfig: WeekViewDrawConfig
	private val touchHandler: WeekViewTouchHandler
	var currentScrollDirection = Direction.NONE
	var currentFlingDirection = Direction.NONE
	private val gestureDetector: GestureDetector
	private val scaleDetector: ScaleGestureDetector
	internal var isZooming: Boolean = false
	private val minimumFlingVelocity: Int
	private val scaledTouchSlop: Int
	var eventClickListener: EventClickListener<T>? = null
	var eventLongPressListener: EventLongPressListener<T>? = null
	var emptyViewClickListener: EmptyViewClickListener? = null
	var emptyViewLongPressListener: EmptyViewLongPressListener? = null
	var topLeftCornerClickListener: TopLeftCornerClickListener? = null
	var weekViewLoader: WeekViewLoader<T>? = null
	var scrollListener: ScrollListener? = null
	var scaleListener: ScaleListener? = null

	init {
		this.listener = view as Listener
		this.drawConfig = config.drawConfig

		touchHandler = WeekViewTouchHandler(config)
		gestureDetector = GestureDetector(context, this)
		scroller = OverScroller(context)

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
				drawConfig.newHourHeight = (hourHeight * detector.scaleFactor).roundToInt()
				listener.onScaled()
				return true
			}
		})
	}

	override fun onDown(e: MotionEvent): Boolean {
		goToNearestOrigin()
		return true
	}

	override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
		if (isZooming) return true

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
		if (isZooming) return true

		if (currentFlingDirection == Direction.LEFT && !config.horizontalFlingEnabled ||
				currentFlingDirection == Direction.RIGHT && !config.horizontalFlingEnabled ||
				currentFlingDirection == Direction.VERTICAL && !config.verticalFlingEnabled)
			return true

		//scroller.forceFinished(true)

		currentFlingDirection = currentScrollDirection
		when (currentFlingDirection) {
			Direction.LEFT, Direction.RIGHT -> if (config.horizontalFlingEnabled) onFlingHorizontal(velocityX)
			Direction.VERTICAL -> if (config.verticalFlingEnabled) onFlingVertical(velocityY)
			Direction.NONE -> {
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

		if (e.x > timeColumnWidth && e.y > drawConfig.headerHeight)
			emptyViewClickListener?.let { listener ->
				touchHandler.getTimeFromPoint(e)?.let { listener.onEmptyViewClicked(it) }
			}

		if (e.x <= timeColumnWidth && e.y <= drawConfig.headerHeight)
			topLeftCornerClickListener?.onCornerClick()

		return super.onSingleTapUp(e)
	}

	override fun onLongPress(e: MotionEvent) {
		super.onLongPress(e)

		val eventChip = findHitEvent(e)
		if (eventChip != null && eventLongPressListener != null) {
			val data = eventChip.originalEvent.data
			if (data != null) {
				eventLongPressListener?.onEventLongPress(data, eventChip.rect ?: return)
			} else {
				throw WeekViewException("No data to show. Did you pass the original object into the constructor of WeekViewEvent?")
			}
		}

		val timeColumnWidth = drawConfig.timeColumnWidth

		if (e.x > timeColumnWidth && e.y > drawConfig.headerHeight)
			emptyViewLongPressListener?.let { listener ->
				touchHandler.getTimeFromPoint(e)?.let {
					listener.onEmptyViewLongPress(it)
				}
			}

		if (e.x <= timeColumnWidth && e.y <= drawConfig.headerHeight)
			topLeftCornerClickListener?.onCornerLongClick()
	}

	private fun findHitEvent(e: MotionEvent): EventChip<T>? {
		data.eventChips.forEach {
			if (it.isHit(e))
				return it
		}
		return null
	}

	internal fun goToNearestOrigin() {
		val totalDayWidth = config.totalDayWidth
		var leftDays = drawConfig.currentOrigin.x.toDouble()
		leftDays /= if (config.snapToWeek)
			(totalDayWidth * config.weekLength).toDouble()
		else
			totalDayWidth.toDouble()

		leftDays = when (currentScrollDirection) {
			Direction.LEFT -> // snap to last day
				floor(leftDays)
			Direction.RIGHT -> // snap to next day
				ceil(leftDays)
			else -> // snap to nearest day
				round(leftDays)
		}

		var nearestOrigin = drawConfig.currentOrigin.x.toInt()
		nearestOrigin -= if (config.snapToWeek)
			(leftDays * totalDayWidth.toDouble() * config.weekLength.toDouble()).toInt()
		else
			(leftDays * totalDayWidth).toInt()

		if (nearestOrigin != 0) {
			scroller.forceFinished(true)

			scroller.startScroll(
					drawConfig.currentOrigin.x.toInt(),
					drawConfig.currentOrigin.y.toInt(),
					-nearestOrigin,
					0,
					(abs(nearestOrigin) / drawConfig.widthPerDay * config.scrollDuration).toInt())

			listener.onScrolled()
		}

		currentFlingDirection = Direction.NONE
		currentScrollDirection = Direction.NONE
	}

	fun onTouchEvent(event: MotionEvent): Boolean {
		scaleDetector.onTouchEvent(event)
		val value = gestureDetector.onTouchEvent(event)

		if (event.action == ACTION_UP && !isZooming && currentFlingDirection == Direction.NONE) {
			if (currentScrollDirection == Direction.RIGHT || currentScrollDirection == Direction.LEFT) {
				goToNearestOrigin()
			}
			currentScrollDirection = Direction.NONE
		}

		return value
	}

	fun forceScrollFinished() {
		scroller.forceFinished(true)
		currentFlingDirection = Direction.NONE
		currentScrollDirection = currentFlingDirection
	}

	fun computeScroll() {
		if (scroller.isFinished && currentFlingDirection != Direction.NONE) {
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
	private fun shouldForceFinishScroll() = scroller.currVelocity <= minimumFlingVelocity

	internal enum class Direction {
		NONE, LEFT, RIGHT, VERTICAL
	}

	internal interface Listener {
		fun onScaled()

		fun onScrolled()
	}
}
