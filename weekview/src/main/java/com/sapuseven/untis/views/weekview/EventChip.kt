package com.sapuseven.untis.views.weekview

import android.graphics.Canvas
import android.graphics.Paint
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.CharacterStyle
import android.text.style.StrikethroughSpan
import android.view.MotionEvent
import com.sapuseven.untis.views.weekview.config.WeekViewConfig


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
	internal var left: Float = 0.0f
	internal var width: Float = 0.0f
	internal var top: Float = 0.0f
	internal var bottom: Float = 0.0f

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

		rect!!.drawTo(canvas, cornerRadius, cornerRadius, pastBackgroundPaint, backgroundPaint)

		drawTitle(config, canvas)
	}

	private fun drawTitle(config: WeekViewConfig, canvas: Canvas) {
		val availableHeight = (rect!!.bottom - rect!!.top - (config.eventPadding * 2).toFloat()).toInt()
		val availableWidth = (rect!!.right - rect!!.left - (config.eventPadding * 2).toFloat()).toInt()

		if (availableHeight < 0 || availableWidth < 0) return

		val topPaint = config.drawConfig.eventTopPaint
		val titlePaint = config.drawConfig.eventTextPaint
		val bottomPaint = config.drawConfig.eventBottomPaint
		val indicatorRadius = topPaint.textSize / 4f

		val eventTop = restoreSpanned(event.top, TextUtils.ellipsize(event.top, topPaint, availableWidth.toFloat(), TextUtils.TruncateAt.END))
		val eventTitle = restoreSpanned(event.title, TextUtils.ellipsize(event.title, titlePaint, availableWidth.toFloat(), TextUtils.TruncateAt.END))
		val eventBottom = restoreSpanned(event.bottom, TextUtils.ellipsize(event.bottom, bottomPaint, availableWidth.toFloat(), TextUtils.TruncateAt.END))

		canvas.save()
		canvas.translate(rect!!.left + config.eventPadding, rect!!.top + config.eventPadding)

		if (config.eventSecondaryTextCentered) {
			canvas.drawSpannableString(eventTop, availableWidth / 2.0f, -(topPaint.ascent() + topPaint.descent()), topPaint)
			canvas.drawSpannableString(eventBottom, availableWidth / 2.0f, availableHeight.toFloat(), bottomPaint)
		} else {
			canvas.drawSpannableString(eventTop, 0f, -(topPaint.ascent() + topPaint.descent()), topPaint)
			canvas.drawSpannableString(eventBottom, availableWidth.toFloat(), availableHeight.toFloat(), bottomPaint)
		}

		if (event.hasIndicator) canvas.drawCircle(availableWidth - indicatorRadius, -(topPaint.ascent() + topPaint.descent()) - indicatorRadius * 2, indicatorRadius, topPaint)
		canvas.drawText(eventTitle.toString(), availableWidth / 2.0f, availableHeight / 2.0f - (titlePaint.descent() + titlePaint.ascent()) / 2, titlePaint)
		canvas.restore()
	}

	private fun restoreSpanned(original: CharSequence, target: CharSequence): CharSequence {
		if (original !is SpannableString) return target

		val targetSpannable = SpannableString.valueOf(target)
		var next: Int
		var i = 0
		while (i < target.length) {
			next = original.nextSpanTransition(i, target.length, CharacterStyle::class.java)
			val spans: Array<StrikethroughSpan> = original.getSpans(i, next, StrikethroughSpan::class.java)
			if (spans.isNotEmpty())
				targetSpannable.setSpan(StrikethroughSpan(), i, next, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
			i = next
		}
		return targetSpannable
	}

	internal fun isHit(e: MotionEvent): Boolean {
		return rect != null && e.x > rect!!.left && e.x < rect!!.right && e.y > rect!!.top && e.y < rect!!.bottom
	}
}

/**
 * Draws a SpannableString correctly to a canvas.
 * Currently only [StrikethroughSpan] is supported, other spans are ignored.
 *
 * @param text The text to be drawn.
 * @param x The x-coordinate of the origin of the text being drawn.
 * @param y The x-coordinate of the baseline of the text being drawn.
 * @param paint The paint used for the text. It's `lineWidth` property will be used for strike through width.
 */
fun Canvas.drawSpannableString(text: CharSequence, x: Float, y: Float, paint: Paint) {
	val originalAlign = paint.textAlign
	paint.textAlign = Paint.Align.LEFT

	var next: Int
	var xEnd: Float
	var xStart = when (originalAlign) {
		Paint.Align.RIGHT -> x - paint.measureText(text.toString())
		Paint.Align.CENTER -> x - paint.measureText(text.toString()) / 2
		else -> x
	}

	var i = 0
	while (i < text.length) {
		next = if (text is SpannableString) text.nextSpanTransition(i, text.length, CharacterStyle::class.java) else text.length

		xEnd = xStart + paint.measureText(text, i, next)

		val spans: Array<StrikethroughSpan> = if (text is SpannableString) text.getSpans(i, next, StrikethroughSpan::class.java) else emptyArray()
		if (spans.isNotEmpty()) {
			drawText(text, i, next, xStart, y, paint)
			drawLine(xStart, y + (paint.ascent() + paint.descent()) / 2, xEnd, y + (paint.ascent() + paint.descent()) / 2, paint)
		} else {
			drawText(text, i, next, xStart, y, paint)
		}
		xStart = xEnd
		i = next
	}

	paint.textAlign = originalAlign
}
