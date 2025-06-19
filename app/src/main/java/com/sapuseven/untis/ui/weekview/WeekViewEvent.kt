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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.sapuseven.untis.ui.common.ifNotNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

sealed class EventColor(
	private val colorForScheme: (ColorScheme) -> Color,
	private val textColorForScheme: (ColorScheme) -> Color
) {
	object Debug : EventColor({ Color.Magenta }, { Color.Black }) // Used for debugging to highlight potential issues
	object ThemePrimary : EventColor({ it.primary }, { it.onPrimary })
	object ThemeSecondary : EventColor({ it.secondary }, { it.onSecondary })
	object ThemeTertiary : EventColor({ it.tertiary }, { it.onTertiary })
	object ThemeError : EventColor({ it.error }, { it.onError })

	data class Custom(val color: Color, val textColor: Color? = null) : EventColor(
		colorForScheme = { color },
		textColorForScheme = {
			textColor ?: if (ColorUtils.calculateLuminance(color.toArgb()) < 0.5) Color.White else Color.Black
		}
	)

	@Composable
	fun color() = colorForScheme(MaterialTheme.colorScheme)

	@Composable
	fun pastColor() = color().copy(alpha = .7f)

	@Composable
	fun textColor() = textColorForScheme(MaterialTheme.colorScheme)
}

data class Event<T>(
	var title: CharSequence,
	var top: CharSequence = "",
	var bottom: CharSequence = "",
	var colorScheme: EventColor,
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
	var colorScheme: EventColor,
	var textColor: Color,
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
	val eventStyle = LocalWeekViewEventStyle.current
	val outerPadding = eventStyle.padding.dp

	val color = event.colorScheme.color()
	val pastColor = event.colorScheme.pastColor()
	val textColor = event.colorScheme.textColor()

	Box(
		modifier = modifier
			.fillMaxSize()
			.padding(outerPadding)
			.clip(RoundedCornerShape(eventStyle.cornerRadius.dp))
			.drawBehind {
				drawVerticalSplitRect(
					pastColor,
					color,
					topLeft = Offset(-outerPadding.toPx(), -outerPadding.toPx()),
					size = Size(size.width + outerPadding.toPx() * 2, size.height + outerPadding.toPx() * 2),
					division = ((currentTime.seconds() - event.start.seconds()).toFloat()
						/ (event.end.seconds() - event.start.seconds()).toFloat())
						.coerceIn(0f, 1f)
				)
			}
			.ifNotNull(onClick) {
				clickable(onClick = it)
			}
			.padding(horizontal = innerPadding)
	) {
		Text(
			text = event.top.asAnnotatedString(),
			style = MaterialTheme.typography.bodySmall + eventStyle.lessonInfoStyle,
			textAlign = if (eventStyle.lessonInfoCentered) TextAlign.Center else TextAlign.Start,
			maxLines = 1,
			color = textColor,
			modifier = Modifier
				.fillMaxWidth()
				.align(Alignment.TopCenter)
		)

		Text(
			text = event.title.asAnnotatedString(),
			style = MaterialTheme.typography.bodyLarge + eventStyle.lessonNameStyle,
			textAlign = TextAlign.Center,
			maxLines = 1,
			color = textColor,
			modifier = Modifier.align(Alignment.Center)
		)

		Text(
			text = event.bottom.asAnnotatedString(),
			style = MaterialTheme.typography.bodySmall + eventStyle.lessonInfoStyle,
			textAlign = if (eventStyle.lessonInfoCentered) TextAlign.Center else TextAlign.End,
			maxLines = 1,
			color = textColor,
			modifier = Modifier
				.fillMaxWidth()
				.align(Alignment.BottomCenter)
		)
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
				colorScheme = EventColor.ThemePrimary,
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
				colorScheme = EventColor.ThemePrimary,
				start = LocalDateTime.parse("2021-05-18T09:00:00"),
				end = LocalDateTime.parse("2021-05-18T11:00:00"),
				top = "This is a",
				bottom = "event"
			),
			modifier = Modifier.sizeIn(maxHeight = 64.dp, maxWidth = 72.dp)
		)
	}
}
