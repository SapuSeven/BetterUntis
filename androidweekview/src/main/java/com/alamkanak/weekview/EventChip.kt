package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.MotionEvent

import com.alamkanak.weekview.config.WeekViewConfig

/**
 * A class to hold reference to the events and their visual representation. An EventRect is
 * actually the rectangle that is drawn on the calendar for a given event. There may be more
 * than one rectangle for a single event (an event that expands more than one day). In that
 * case two instances of the EventRect will be used for a single event. The given event will be
 * stored in "originalEvent". But the event that corresponds to rectangle the rectangle
 * instance will be stored in "event".
 */
class EventChip<T>

/**
 * Create a new instance of event rect. An EventRect is actually the rectangle that is drawn
 * on the calendar for a given event. There may be more than one rectangle for a single
 * event (an event that expands more than one day). In that case two instances of the
 * EventRect will be used for a single event. The given event will be stored in
 * "originalEvent". But the event that corresponds to rectangle the rectangle instance will
 * be stored in "event".
 *
 * @param event         Represents the event which this instance of rectangle represents.
 * @param originalEvent The original event that was passed by the user.
 * @param rect          The rectangle.
 */
internal constructor(var event: WeekViewEvent<T>, var originalEvent: WeekViewEvent<T>, var rect: SplitRect?) {
	internal var left: Float = 0.toFloat()
	internal var width: Float = 0.toFloat()
	internal var top: Float = 0.toFloat()
	internal var bottom: Float = 0.toFloat()

	private val backgroundPaint: Paint
		get() {
			val paint = Paint(Paint.ANTI_ALIAS_FLAG)
			paint.color = event.colorOrDefault
			return paint
		}

	private val pastBackgroundPaint: Paint
		get() {
			val paint = Paint(Paint.ANTI_ALIAS_FLAG)
			paint.color = event.pastColorOrDefault
			return paint
		}

	fun draw(config: WeekViewConfig, canvas: Canvas) {
		val cornerRadius = config.eventCornerRadius.toFloat()
		val backgroundPaint = backgroundPaint
		val pastBackgroundPaint = pastBackgroundPaint

		rect!!.drawTo(canvas, cornerRadius, cornerRadius, pastBackgroundPaint, backgroundPaint)

		drawTitle(config, canvas)
	}

	private fun drawTitle(config: WeekViewConfig, canvas: Canvas) {
		val negativeWidth = rect!!.right - rect!!.left - (config.eventPadding * 2).toFloat() < 0
		val negativeHeight = rect!!.bottom - rect!!.top - (config.eventPadding * 2).toFloat() < 0

		if (negativeWidth || negativeHeight) return

		// Prepare the name of the event.
		val topBuilder = SpannableStringBuilder()
		val titleBuilder = SpannableStringBuilder()
		val bottomBuilder = SpannableStringBuilder()

		topBuilder.append(event.top)
		titleBuilder.append(event.title)
		bottomBuilder.append(event.bottom)

		val availableHeight = (rect!!.bottom - rect!!.top - (config.eventPadding * 2).toFloat()).toInt()
		val availableWidth = (rect!!.right - rect!!.left - (config.eventPadding * 2).toFloat()).toInt()

		val topPaint = config.drawConfig.eventTopPaint
		val titlePaint = config.drawConfig.eventTextPaint
		val bottomPaint = config.drawConfig.eventBottomPaint

		val eventTop = TextUtils.ellipsize(topBuilder, topPaint, availableWidth.toFloat(), TextUtils.TruncateAt.END)
		val eventTitle = TextUtils.ellipsize(titleBuilder, titlePaint, availableWidth.toFloat(), TextUtils.TruncateAt.END)
		val eventBottom = TextUtils.ellipsize(bottomBuilder, bottomPaint, availableWidth.toFloat(), TextUtils.TruncateAt.END)

		canvas.save()
		canvas.translate(rect!!.left + config.eventPadding, rect!!.top + config.eventPadding)

		if (config.eventSecondaryTextCentered) {
			canvas.drawText(eventTop.toString(), availableWidth / 2.0f, -topPaint.ascent(), topPaint)
			canvas.drawText(eventBottom.toString(), availableWidth / 2.0f, availableHeight.toFloat(), bottomPaint)
		} else {
			canvas.drawText(eventTop.toString(), 0f, -topPaint.ascent(), topPaint)
			canvas.drawText(eventBottom.toString(), availableWidth.toFloat(), availableHeight.toFloat(), bottomPaint)
		}

		canvas.drawText(eventTitle.toString(), availableWidth / 2.0f, availableHeight / 2.0f - (titlePaint.descent() + titlePaint.ascent()) / 2, titlePaint)
		canvas.restore()
	}

	internal fun isHit(e: MotionEvent): Boolean {
		return rect != null && e.x > rect!!.left && e.x < rect!!.right && e.y > rect!!.top && e.y < rect!!.bottom
	}
}
