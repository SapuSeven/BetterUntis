package com.sapuseven.untis.ui.weekview

import android.text.format.DateFormat
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import org.joda.time.Minutes
import org.joda.time.format.DateTimeFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

data class Event(
	val name: String,
	val color: Color,
	val start: LocalDateTime,
	val end: LocalDateTime,
	val description: String? = null,
)

val eventTimeFormat = DateTimeFormat.forPattern("h:mm a")

@Composable
fun WeekViewEvent(
	event: Event,
	modifier: Modifier = Modifier,
) {
	Column(
		modifier = modifier
			.fillMaxSize()
			.padding(end = 2.dp, bottom = 2.dp)
			.background(event.color, shape = RoundedCornerShape(4.dp))
			.padding(4.dp)
	) {
		Text(
			text = "${eventTimeFormat.print(event.start)} - ${eventTimeFormat.print(event.end)}"
		)

		Text(
			text = event.name,
			fontWeight = FontWeight.Bold,
		)

		if (event.description != null) {
			Text(
				text = event.description,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
		}
	}
}


@Preview(showBackground = true)
@Composable
fun EventPreview() {
	WeekViewEvent(
		event = Event(
			name = "Test event",
			color = Color(0xFFAFBBF2),
			start = LocalDateTime.parse("2021-05-18T09:00:00"),
			end = LocalDateTime.parse("2021-05-18T11:00:00"),
			description = "This is an example event.",
		), modifier = Modifier.sizeIn(maxHeight = 64.dp)
	)
}

private class EventDataModifier(
	val event: Event,
) : ParentDataModifier {
	override fun Density.modifyParentData(parentData: Any?) = event
}

private fun Modifier.eventData(event: Event) = this.then(EventDataModifier(event))

private val dayNameFormat = DateTimeFormat.forPattern("EEE")
private val dayDateFormat = DateTimeFormat.forPattern("d. MMM")
private val timeFormat12h = DateTimeFormat.forPattern("h:mm a")
private val timeFormat24h = DateTimeFormat.forPattern("H:mm")

@Composable
fun WeekViewHeaderDay(
	day: LocalDate,
	modifier: Modifier = Modifier,
) {
	val isToday = LocalDate.now().equals(day)

	Column(
		modifier = Modifier
			.padding(4.dp)
	) {
		Text(
			text = dayNameFormat.print(day),
			textAlign = TextAlign.Center,
			fontSize = 20.sp,
			fontWeight = FontWeight.Medium,
			color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
			modifier = modifier
				.fillMaxWidth()
		)
		Text(
			text = dayDateFormat.print(day),
			textAlign = TextAlign.Center,
			fontSize = 14.sp,
			color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = modifier
				.fillMaxWidth()
		)
	}
}

@Preview(showBackground = true)
@Composable
fun WeekViewHeaderDayPreview() {
	WeekViewHeaderDay(day = LocalDate.now())
}

@Composable
fun WeekViewHeader(
	startDate: LocalDate,
	numDays: Int,
	modifier: Modifier = Modifier,
	dayHeader: @Composable (day: LocalDate) -> Unit = { WeekViewHeaderDay(day = it) },
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
	) {
		repeat(numDays) { i ->
			Box(modifier = Modifier.weight(1f)) {
				dayHeader(startDate.plusDays(i))
			}
		}
	}
}

@Preview(showBackground = true)
@Composable
fun WeekViewHeaderPreview() {
	WeekViewHeader(
		startDate = LocalDate.now(),
		numDays = 5
	)
}

@Composable
fun WeekViewSidebarLabel(
	hour: WeekViewHour,
	modifier: Modifier = Modifier,
) {
	val timeFormat = if (DateFormat.is24HourFormat(LocalContext.current)) timeFormat24h else timeFormat12h

	Column(
		verticalArrangement = Arrangement.SpaceBetween,
		modifier = modifier
			.padding(horizontal = 4.dp)
			.fillMaxHeight()
	) {
		Text(
			text = timeFormat.print(hour.startTime),
			textAlign = TextAlign.Left,
			fontSize = 12.sp,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier
				.fillMaxWidth()
				.padding(end = 4.dp)
		)
		Text(
			text = hour.label,
			textAlign = TextAlign.Center,
			fontSize = 16.sp,
			fontWeight = FontWeight.Medium,
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier
				.fillMaxWidth()
		)
		Text(
			text = timeFormat.print(hour.endTime),
			textAlign = TextAlign.Right,
			fontSize = 12.sp,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier
				.fillMaxWidth()
				.padding(start = 4.dp)
		)
	}
}

/*@Preview(showBackground = true)
@Composable
fun BasicSidebarLabelPreview() {
	WeekViewSidebarLabel(time = LocalTime.MIDNIGHT, Modifier.sizeIn(maxHeight = 64.dp))
}*/

@Composable
fun WeekViewSidebar(
	hourHeight: Dp,
	bottomPadding: Dp,
	modifier: Modifier = Modifier,
	hourList: List<WeekViewHour>,
	label: @Composable (hour: WeekViewHour) -> Unit = { WeekViewSidebarLabel(hour = it) },
) {
	// TODO: This implementation is prone to alignment issues due to rounding errors. Maybe use a Box with absolute padding instead (like the hour lines).
	Column(modifier = modifier
		.width(IntrinsicSize.Max)
		.padding(bottom = bottomPadding)
	) {
		var lastEndTime: LocalTime? = null
		hourList.forEach { hour ->
			val totalHourHeight = hourHeight *
					(Minutes.minutesBetween(hour.startTime, hour.endTime).minutes / 60f)

			val topPadding = lastEndTime?.let {
				hourHeight *
						(Minutes.minutesBetween(lastEndTime, hour.startTime).minutes / 60f)
			} ?: 0.dp

			Box(
				modifier = Modifier
					.padding(top = topPadding)
					.height(totalHourHeight)
					.fillMaxWidth()
			) {
				label(hour)
			}

			lastEndTime = hour.endTime
		}
	}
}

/*@Preview(showBackground = true)
@Composable
fun WeekViewSidebarPreview() {
	WeekViewSidebar(
		hourHeight = 64.dp,
		startTime = LocalTime.MIDNIGHT.plusHours(5),
		endTime = LocalTime.MIDNIGHT.plusHours(18)
	)
}*/

@Composable
fun WeekViewContent(
	events: List<Event>,
	modifier: Modifier = Modifier,
	eventContent: @Composable (event: Event) -> Unit = { WeekViewEvent(event = it) },
	startDate: LocalDate,
	numDays: Int = 5,
	startTime: LocalTime,
	endTime: LocalTime,
	endTimeOffset: Float,
	hourHeight: Dp,
	hourList: List<WeekViewHour>,
	dividerColor: Color = MaterialTheme.colorScheme.outline,
	dividerWidth: Float = Stroke.HairlineWidth,
) {
	Layout(
		content = {
			events.sortedBy(Event::start).forEach { event ->
				Box(modifier = Modifier.eventData(event)) {
					eventContent(event)
				}
			}
		},
		modifier = modifier
			.drawBehind {
				val hours = hourList.map { listOf(it.startTime, it.endTime) }.flatten().toSet()

				hours.forEach {
					val yPos =
						Minutes.minutesBetween(startTime, it).minutes / 60f * hourHeight.toPx()

					if (yPos == 0f || yPos == size.height) return@forEach

					drawLine(
						dividerColor,
						start = Offset(0f, yPos),
						end = Offset(size.width, yPos),
						strokeWidth = dividerWidth
					)
				}
				repeat(numDays - 1) {
					val xPos = (it + 1) * (size.width / numDays)
					drawLine(
						dividerColor,
						start = Offset(xPos, 0f),
						end = Offset(xPos, size.height),
						strokeWidth = dividerWidth
					)
				}
				drawLine(
					dividerColor,
					start = Offset(size.width + dividerWidth, 0f),
					end = Offset(size.width + dividerWidth, size.height),
					strokeWidth = dividerWidth
				)
			}
	) { measureables, constraints ->
		val height = (Minutes.minutesBetween(startTime, endTime).minutes / 60f * hourHeight.toPx()
				+ endTimeOffset).roundToInt()
		val width = constraints.maxWidth
		val placeablesWithEvents = measureables.map { measurable ->
			val event = measurable.parentData as Event
			val eventDurationMinutes = Minutes.minutesBetween(event.start, event.end).minutes
			val eventHeight = ((eventDurationMinutes / 60f) * hourHeight.toPx()).roundToInt()
			val placeable = measurable.measure(
				constraints.copy(
					minHeight = eventHeight,
					maxHeight = eventHeight
				)
			)
			Pair(placeable, event)
		}
		layout(width, height) {
			placeablesWithEvents.forEach { (placeable, event) ->
				val eventOffsetMinutes =
					Minutes.minutesBetween(LocalTime.MIDNIGHT, event.start.toLocalTime()).minutes
				val eventY = ((eventOffsetMinutes / 60f) * hourHeight.toPx()).roundToInt()
				val eventOffsetDays =
					Minutes.minutesBetween(startDate, event.start.toLocalDate()).minutes
				val eventX = eventOffsetDays * (constraints.maxWidth / numDays)
				placeable.place(eventX, eventY)
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeekViewCompose(
	events: List<Event> = emptyList(),
	modifier: Modifier = Modifier,
	eventContent: @Composable (event: Event) -> Unit = { WeekViewEvent(event = it) },
	dayHeader: @Composable (day: LocalDate) -> Unit = { WeekViewHeaderDay(day = it) },
	startDate: LocalDate = LocalDate.now(),
	preferences: WeekViewPreferences
) {
	val dividerWidth = Stroke.HairlineWidth

	val verticalScrollState = rememberScrollState()
	var sidebarWidth by remember { mutableStateOf(0) }
	var headerHeight by remember { mutableStateOf(0) }
	var contentHeight by remember { mutableStateOf(0) }

	val startPage = Int.MAX_VALUE / 2
	val pagerState = rememberPagerState(initialPage = startPage)

	Row(modifier = modifier) {
		WeekViewSidebar(
			hourHeight = preferences.hourHeight,
			bottomPadding = with(LocalDensity.current) { preferences.endTimeOffset.toDp() },
			hourList = preferences.hourList,
			modifier = Modifier
				.padding(top = with(LocalDensity.current) { headerHeight.toDp() })
				.onGloballyPositioned { sidebarWidth = it.size.width }
				.verticalScroll(verticalScrollState)
		)

		HorizontalPager(
			state = pagerState,
			pageCount = Int.MAX_VALUE,
			pageSpacing = with(LocalDensity.current) { dividerWidth.toDp() }
		) { index ->
			val pageOffset = index - startPage
			val visibleStartDate =
				startDate.withDayOfWeek(1).plusWeeks(pageOffset) // 1 = Monday, 7 = Sunday

			Column {
				WeekViewHeader(
					startDate = visibleStartDate,
					numDays = 5,
					dayHeader = dayHeader,
					modifier = Modifier
						.onGloballyPositioned { headerHeight = it.size.height }
				)

				WeekViewContent(
					events = events,
					eventContent = eventContent,
					startDate = visibleStartDate,
					numDays = 5,
					startTime = preferences.startTime,
					endTime = preferences.endTime,
					endTimeOffset = preferences.endTimeOffset,
					hourHeight = preferences.hourHeight,
					hourList = preferences.hourList,
					dividerWidth = preferences.dividerWidth,
					modifier = Modifier
						.weight(1f)
						.onGloballyPositioned { contentHeight = it.size.height }
						.verticalScroll(verticalScrollState)
				)
			}
		}
	}
}

@Preview(showBackground = true)
@Composable
fun WeekViewPreview() {
	WeekViewCompose(
		preferences = rememberWeekViewPreferences()
	)
}

@Composable
fun rememberWeekViewPreferences(
	hourHeight: Dp = 72.dp,
	hourList: List<WeekViewHour> = emptyList(),
	dividerColor: Color = MaterialTheme.colorScheme.outline,
	dividerWidth: Float = Stroke.HairlineWidth,
	startTime: LocalTime = hourList.firstOrNull()?.startTime ?: LocalTime.MIDNIGHT.plusHours(6),
	endTime: LocalTime = hourList.lastOrNull()?.endTime ?: LocalTime.MIDNIGHT.plusHours(18),
	endTimeOffset: Float = 0f,
) = remember(
	hourHeight,
	hourList,
	dividerColor,
	dividerWidth,
	startTime,
	endTime,
	endTimeOffset,
) {
	WeekViewPreferences(
		hourHeight = hourHeight,
		hourList = hourList,
		dividerColor = dividerColor,
		dividerWidth = dividerWidth,
		startTime = startTime,
		endTime = endTime,
		endTimeOffset = endTimeOffset,
	)
}

data class WeekViewPreferences(
	var hourHeight: Dp,
	var hourList: List<WeekViewHour>,
	var dividerColor: Color,
	var dividerWidth: Float,
	var startTime: LocalTime,
	var endTime: LocalTime,
	var endTimeOffset: Float,
)

data class WeekViewHour(
	val startTime: LocalTime,
	val endTime: LocalTime,
	val label: String
)
