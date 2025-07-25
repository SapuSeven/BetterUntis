package com.sapuseven.untis.ui.weekview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.sapuseven.untis.ui.common.ifNotNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

sealed class EventStyle(
	private val colorForScheme: (ColorScheme) -> Color,
	private val textStyleForScheme: (ColorScheme) -> TextStyle,
	private var textStyleOverride: TextStyle? = null
) {
	object Transparent : EventStyle({ Color.Transparent }, { TextStyle(color = it.onSurface) })
	object ThemePrimary : EventStyle({ it.primary }, { TextStyle(color = it.onPrimary) })
	object ThemeSecondary : EventStyle({ it.secondary }, { TextStyle(color = it.onSecondary) })
	object ThemeTertiary : EventStyle({ it.tertiary }, { TextStyle(color = it.onTertiary) })
	object ThemeError : EventStyle({ it.error }, { TextStyle(color = it.onError) })

	data class Custom(val color: Color, val textStyle: TextStyle? = null) : EventStyle(
		colorForScheme = { color },
		textStyleForScheme = {
			textStyle ?: TextStyle(
				color = if (
					ColorUtils.calculateContrast(Color.Black.toArgb(), color.toArgb()) >
					ColorUtils.calculateContrast(Color.White.toArgb(), color.toArgb())
				) Color.Black else Color.White
			)
		}
	)

	fun withTextStyle(textStyle: TextStyle): EventStyle {
		textStyleOverride = textStyle
		return this
	}

	@Composable
	fun color() = colorForScheme(MaterialTheme.colorScheme)

	@Composable
	fun pastColor() = color().copy(alpha = color().alpha * .7f)

	@Composable
	fun textStyle() = textStyleForScheme(MaterialTheme.colorScheme) + (textStyleOverride ?: TextStyle())
}

data class Event<T>(
	var title: CharSequence,
	var top: CharSequence = "",
	var bottom: CharSequence = "",
	var eventStyle: EventStyle,
	var start: LocalDateTime,
	var end: LocalDateTime,
	val data: T? = null
) {
	var numSimultaneous: Int = 1 // relative width is determined by 1/x
	var offsetSteps: Int = 0 // x-offset in multiples of width
	var simultaneousEvents = mutableSetOf<Event<T>>()
}

data class Holiday(
	var title: CharSequence,
	var colorScheme: EventStyle,
	var start: LocalDate,
	var end: LocalDate,
)

@Composable
fun <T> WeekViewEvent(
	event: Event<T>,
	modifier: Modifier = Modifier,
	currentTime: LocalDateTime = LocalDateTime.now(),
	innerPadding: Dp = 2.dp,
	onClick: (() -> Unit)? = null,
) {
	val textMeasurer = rememberTextMeasurer()
	val eventStyle = LocalWeekViewEventStyle.current
	val outerPadding = eventStyle.padding.dp

	val color = event.eventStyle.color()
	val pastColor = event.eventStyle.pastColor()
	val textStyle = event.eventStyle.textStyle()

	val isFullDayEvent = event.data == null

	val holidayTextStyle = MaterialTheme.typography.bodyLarge + eventStyle.lessonNameStyle

	Box(
		modifier = modifier
			.fillMaxSize()
			.padding(outerPadding)
			.clip(RoundedCornerShape(eventStyle.cornerRadius.dp))
			.drawBehind {
				if (!isFullDayEvent) {
					drawVerticalSplitRect(
						pastColor,
						color,
						topLeft = Offset(-outerPadding.toPx(), -outerPadding.toPx()),
						size = Size(size.width + outerPadding.toPx() * 2, size.height + outerPadding.toPx() * 2),
						division = ((currentTime.seconds() - event.start.seconds()).toFloat() /
								(event.end.seconds() - event.start.seconds()).toFloat()).coerceIn(0f, 1f)
					)
				}
			}
			.drawWithContent {
				drawContent()
				if (isFullDayEvent) {
					val textSize = textMeasurer.measure(event.title.asAnnotatedString(), holidayTextStyle)

					rotate(90f, Offset(0f, 0f)) {
						translate((size.width - textSize.size.height) / 2, -(size.width + textSize.size.height) / 2) {
							drawText(
								textMeasurer,
								text = event.title.asAnnotatedString(),
								style = holidayTextStyle + textStyle,
								overflow = TextOverflow.Visible,
								softWrap = false
							)
						}
					}
				}
			}
			.ifNotNull(onClick) {
				if (event.data != null) {
					clickable(onClick = it)
				} else this
			}
			.padding(horizontal = innerPadding)
	) {
		if (!isFullDayEvent) {
			Text(
				text = event.top.asAnnotatedString(),
				style = MaterialTheme.typography.bodySmall + eventStyle.lessonInfoStyle + textStyle,
				textAlign = if (eventStyle.lessonInfoCentered) TextAlign.Center else TextAlign.Start,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier
					.fillMaxWidth()
					.align(Alignment.TopCenter)
			)

			Text(
				text = event.title.asAnnotatedString(),
				style = MaterialTheme.typography.bodyLarge + eventStyle.lessonNameStyle + textStyle,
				textAlign = TextAlign.Center,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.align(Alignment.Center)
			)

			Text(
				text = event.bottom.asAnnotatedString(),
				style = MaterialTheme.typography.bodySmall + eventStyle.lessonInfoStyle + textStyle,
				textAlign = if (eventStyle.lessonInfoCentered) TextAlign.Center else TextAlign.End,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier
					.fillMaxWidth()
					.align(Alignment.BottomCenter)
			)
		}
	}
}

private fun CharSequence.asAnnotatedString(): AnnotatedString = let {
	it as? AnnotatedString ?: AnnotatedString(it.toString())
}

private fun LocalDateTime.seconds() = atZone(ZoneId.systemDefault()).toEpochSecond()

@Preview(showBackground = true)
@Composable
fun EventPreview() {
	WeekViewStyle {
		WeekViewEvent(
			event = Event<Nothing>(
				title = "Test",
				eventStyle = EventStyle.ThemePrimary,
				start = LocalDateTime.parse("2021-05-18T09:00:00"),
				end = LocalDateTime.parse("2021-05-18T11:00:00"),
				top = "This is a",
				bottom = "event"
			),
			modifier = Modifier.sizeIn(maxHeight = 64.dp, maxWidth = 72.dp)
		)
	}
}

@Preview(showBackground = true)
@Composable
fun EventStyledPreview() {
	WeekViewStyle(
		weekViewEventStyle = WeekViewEventStyle(
			padding = 4,
			cornerRadius = 8,
			lessonNameStyle = MaterialTheme.typography.bodyLarge,
			lessonInfoStyle = MaterialTheme.typography.bodySmall,
			lessonInfoCentered = true,
		)
	) {
		WeekViewEvent(
			event = Event<Nothing>(
				title = "Styled",
				eventStyle = EventStyle.ThemePrimary,
				start = LocalDateTime.parse("2021-05-18T09:00:00"),
				end = LocalDateTime.parse("2021-05-18T11:00:00"),
				top = "This is a",
				bottom = "event"
			),
			modifier = Modifier.sizeIn(maxHeight = 64.dp, maxWidth = 72.dp)
		)
	}
}

@Preview(showBackground = true)
@Composable
fun HolidayEventPreview() {
	WeekViewStyle {
		WeekViewEvent(
			event = Event<Nothing>(
				title = "Test Holiday",
				eventStyle = EventStyle.Transparent,
				start = LocalDateTime.parse("2021-05-18T00:00:00"),
				end = LocalDateTime.parse("2021-05-18T23:59:59"),
			),
			modifier = Modifier.sizeIn(maxHeight = 192.dp, maxWidth = 72.dp)
		)
	}
}
