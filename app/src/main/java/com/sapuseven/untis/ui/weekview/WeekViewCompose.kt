package com.sapuseven.untis.ui.weekview

import android.text.format.DateFormat
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.sapuseven.untis.R
import com.sapuseven.untis.data.timetable.PeriodData
import com.sapuseven.untis.ui.common.ifNotNull
import com.sapuseven.untis.ui.dialogs.DatePickerDialog
import kotlinx.coroutines.launch
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import org.joda.time.Minutes
import org.joda.time.format.DateTimeFormat
import kotlin.math.roundToInt

data class Event(
	val title: String,
	val top: String = "",
	val bottom: String = "",
	val color: Color,
	val pastColor: Color,
	val textColor: Color,
	val start: LocalDateTime,
	val end: LocalDateTime,
	val periodData: PeriodData? = null
) {
	var numSimultaneous: Int = 1 // relative width is determined by 1/x
	var offsetSteps: Int = 0 // x-offset in multiples of width
	var simultaneousEvents = mutableSetOf<Event>()

	// temp
	var leftX = 0
	var rightX = 0
}

val eventTimeFormat = DateTimeFormat.forPattern("h:mm a")

@Composable
fun WeekViewEvent(
	event: Event,
	modifier: Modifier = Modifier,
	currentTime: LocalDateTime = LocalDateTime.now(),
	onClick: (() -> Unit)? = null,
) {
	Box(
		modifier = modifier
			.fillMaxSize()
			.padding(2.dp) // Outer padding
			.clip(RoundedCornerShape(4.dp))
			.drawBehind {
				drawVerticalSplitRect(
					event.pastColor,
					event.color,
					size = Size(size.width, size.height),
					division = ((currentTime.toDateTime().millis - event.start.toDateTime().millis).toFloat()
						/ (event.end.toDateTime().millis - event.start.toDateTime().millis).toFloat())
						.coerceIn(0f, 1f)
				)
			}
			.padding(horizontal = 2.dp) // Inner padding
			.ifNotNull(onClick) {
				clickable(onClick = it)
			}
	) {
		Text(
			text = event.top,
			fontSize = 10.sp,
			textAlign = TextAlign.Start,
			maxLines = 1,
			color = event.textColor,
			modifier = Modifier
				.align(Alignment.TopStart)
		)

		Text(
			text = event.title,
			fontWeight = FontWeight.Bold,
			textAlign = TextAlign.Center,
			maxLines = 1,
			color = event.textColor,
			modifier = Modifier
				.align(Alignment.Center)
		)

		Text(
			text = event.bottom,
			fontSize = 10.sp,
			textAlign = TextAlign.End,
			maxLines = 1,
			color = event.textColor,
			modifier = Modifier
				.align(Alignment.BottomEnd)
		)
	}
}


@Preview(showBackground = true)
@Composable
fun EventPreview() {
	WeekViewEvent(
		event = Event(
			title = "Test",
			color = Color(0xFFAFBBF2),
			pastColor = Color(0xFFAFBBF2),
			textColor = Color(0xFF000000),
			start = LocalDateTime.parse("2021-05-18T09:00:00"),
			end = LocalDateTime.parse("2021-05-18T11:00:00"),
			top = "This is a",
			bottom = "event"
		),
		modifier = Modifier.sizeIn(maxHeight = 64.dp, maxWidth = 72.dp)
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
	WeekViewHeaderDay(
		day = LocalDate.now(),
		modifier = Modifier.sizeIn(maxWidth = 72.dp)
	)
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
	val timeFormat =
		if (DateFormat.is24HourFormat(LocalContext.current)) timeFormat24h else timeFormat12h

	Box(
		modifier = modifier
			.padding(horizontal = 4.dp)
			.fillMaxSize()
	) {
		Text(
			text = timeFormat.print(hour.startTime),
			fontSize = 12.sp,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier
				.align(Alignment.TopStart)
				.padding(end = 4.dp)
		)
		Text(
			text = hour.label,
			fontSize = 16.sp,
			fontWeight = FontWeight.Medium,
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier
				.align(Alignment.Center)
		)
		Text(
			text = timeFormat.print(hour.endTime),
			fontSize = 12.sp,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier
				.align(Alignment.BottomEnd)
				.padding(start = 4.dp)
		)
	}
}

@Preview(showBackground = true)
@Composable
fun BasicSidebarLabelPreview() {
	WeekViewSidebarLabel(
		hour = WeekViewHour(
			LocalTime(9, 45),
			LocalTime(10, 30),
			"1"
		),
		Modifier.sizeIn(maxHeight = 64.dp, maxWidth = 72.dp)
	)
}

@Preview(showBackground = true)
@Composable
fun CompactSidebarLabelPreview() {
	WeekViewSidebarLabel(
		hour = WeekViewHour(
			LocalTime(9, 45),
			LocalTime(10, 30),
			"1"
		),
		Modifier.sizeIn(maxHeight = 48.dp, maxWidth = 64.dp)
	)
}

@Composable
fun WeekViewSidebar(
	hourHeight: Dp,
	bottomPadding: Dp,
	modifier: Modifier = Modifier,
	hourList: List<WeekViewHour>,
	label: @Composable (hour: WeekViewHour) -> Unit = { WeekViewSidebarLabel(hour = it) },
) {
	// TODO: This implementation is prone to alignment issues due to rounding errors. Maybe use a Box with absolute padding instead (like the hour lines).
	Column(
		modifier = modifier
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

@Preview(showBackground = true)
@Composable
fun WeekViewSidebarPreview() {
	WeekViewSidebar(
		hourHeight = 72.dp,
		bottomPadding = 16.dp,
		hourList = (1..4).map {
			WeekViewHour(
				LocalTime(it + 8, 45),
				LocalTime(it + 9, 30),
				it.toString()
			)
		}
	)
}

fun DrawScope.WeekViewContentGrid(
	numDays: Int = 5,
	startTime: LocalTime,
	hourHeight: Dp,
	hourList: List<WeekViewHour>,
	dividerColor: Color,
	dividerWidth: Float,
) {
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

fun DrawScope.WeekViewBackground(
	numDays: Int = 5,
	startDate: LocalDate,
	startTime: LocalTime,
	hourHeight: Dp,
	pastBackgroundColor: Color,
	futureBackgroundColor: Color,
	currentTime: LocalDateTime = LocalDateTime.now(),
) {
	val dayWidth = size.width / numDays;

	repeat(numDays) {
		val fraction = Minutes
			.minutesBetween(
				startTime,
				currentTime.toLocalTime()
			).minutes / 60f * hourHeight.toPx()
			.coerceIn(0f, size.height) / size.height

		drawVerticalSplitRect(
			topColor = pastBackgroundColor,
			bottomColor = futureBackgroundColor,
			topLeft = Offset(it * dayWidth, 0f),
			size = Size(dayWidth, size.height),
			division = when {
				startDate.plusDays(it).isAfter(currentTime.toLocalDate()) -> 0f
				startDate.plusDays(it).isEqual(currentTime.toLocalDate()) -> fraction
				else -> 1f
			}
		)
	}
}

fun DrawScope.drawVerticalSplitRect(
	topColor: Color,
	bottomColor: Color,
	topLeft: Offset = Offset.Zero,
	size: Size,
	division: Float
) {
	when (division) {
		0f -> drawRect(bottomColor, topLeft, size)
		1f -> drawRect(topColor, topLeft, size)
		else -> {
			val topHeight = size.height * division
			drawRect(
				topColor,
				topLeft,
				size.copy(height = topHeight)
			)
			drawRect(
				bottomColor,
				topLeft.plus(Offset(0f, topHeight)),
				size.copy(height = size.height - topHeight)
			)
		}
	}
}

fun DrawScope.WeekViewIndicator(
	numDays: Int = 5,
	startDate: LocalDate,
	startTime: LocalTime,
	hourHeight: Dp,
	indicatorColor: Color,
	indicatorWidth: Float = 2.dp.toPx(),
	currentTime: LocalDateTime = LocalDateTime.now(),
) {
	val dayWidth = size.width / numDays;

	val yPos = Minutes
		.minutesBetween( // TODO: Can this be negative?
			startTime,
			currentTime.toLocalTime()
		).minutes / 60f * hourHeight.toPx()
	val startDayIndex = Days.daysBetween(startDate, currentTime.toLocalDate()).days

	if (startDayIndex in 0..numDays && yPos in 0f..size.height)
		drawLine(
			color = indicatorColor,
			start = Offset(startDayIndex * dayWidth, yPos),
			end = Offset((startDayIndex + 1) * dayWidth, yPos),
			strokeWidth = indicatorWidth
		)
}

@Composable
fun WeekViewContent(
	events: List<Event>,
	modifier: Modifier = Modifier,
	numDays: Int = 5,
	startDate: LocalDate,
	startTime: LocalTime,
	endTime: LocalTime,
	endTimeOffset: Float,
	hourHeight: Dp,
	hourList: List<WeekViewHour>,
	dividerColor: Color,
	indicatorColor: Color,
	pastBackgroundColor: Color,
	futureBackgroundColor: Color,
	dividerWidth: Float = Stroke.HairlineWidth,
	eventContent: @Composable (event: Event) -> Unit = { WeekViewEvent(event = it) }
) {
	// TODO: Find a way to arrange events before layout, but calculate minEventWidth to determine maxSimultaneous
	// TODO: Display indicator if there are more events than can be displayed
	//val minEventWidth = 24.dp
	val maxSimultaneous = 100//(dayWidth.toFloat() / minEventWidth.toPx()).toInt()
	arrangeEvents(events, maxSimultaneous);

	Layout(
		content = {
			events.sortedBy(Event::start).forEach { event ->
				Box(modifier = Modifier.eventData(event)) {
					eventContent(event)
				}
			}
		},
		modifier = modifier
			.drawWithContent {
				WeekViewBackground(
					numDays = numDays,
					startDate = startDate,
					startTime = startTime,
					hourHeight = hourHeight,
					pastBackgroundColor = pastBackgroundColor,
					futureBackgroundColor = futureBackgroundColor
				)

				WeekViewContentGrid(
					numDays = numDays,
					startTime = startTime,
					hourHeight = hourHeight,
					hourList = hourList,
					dividerColor = dividerColor,
					dividerWidth = dividerWidth
				)

				drawContent()

				WeekViewIndicator(
					numDays = numDays,
					startDate = startDate,
					startTime = startTime,
					hourHeight = hourHeight,
					indicatorColor = indicatorColor
				)
			}
	) { measureables, constraints ->
		val height = (Minutes.minutesBetween(startTime, endTime).minutes / 60f * hourHeight.toPx()
			+ endTimeOffset).roundToInt()
		val width = constraints.maxWidth
		val dayWidth = width / numDays;
		val placeablesWithEvents = measureables.map { measurable ->
			val event = measurable.parentData as Event
			val eventDurationMinutes = Minutes.minutesBetween(event.start, event.end).minutes
			val eventHeight = ((eventDurationMinutes / 60f) * hourHeight.toPx()).roundToInt()
			val placeable = measurable.measure(
				constraints.copy(
					minWidth = dayWidth / event.numSimultaneous,
					maxWidth = dayWidth / event.numSimultaneous,
					minHeight = eventHeight,
					maxHeight = eventHeight
				)
			)
			Pair(placeable, event)
		}

		layout(width, height) {
			placeablesWithEvents.forEach { (placeable, event) ->
				val eventOffsetMinutes =
					Minutes.minutesBetween(startTime, event.start.toLocalTime()).minutes
				val eventY = ((eventOffsetMinutes / 60f) * hourHeight.toPx()).roundToInt()
				val eventOffsetDays = Days.daysBetween(startDate, event.start.toLocalDate()).days
				val eventOffset = event.offsetSteps * (dayWidth / event.numSimultaneous)
				val eventX = eventOffsetDays * dayWidth + eventOffset
				placeable.place(eventX, eventY)
			}
		}
	}
}

/**
 * @param onItemClick Callback on event click. First value contains a list of all simultaneous events
 * (including the clicked one) and second value the index of the actually clicked event.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeekViewCompose(
	events: Map<LocalDate, List<Event>>,
	onPageChange: suspend (pageIndex: Int) -> Unit,
	onReload: suspend (pageIndex: Int) -> Unit,
	onItemClick: (Pair<List<PeriodData>, Int>) -> Unit,
	modifier: Modifier = Modifier,
	eventContent: @Composable (event: Event) -> Unit = {
		WeekViewEvent(event = it, onClick = {
			onItemClick(
				it.simultaneousEvents.mapNotNull { it.periodData }
					to it.simultaneousEvents.indexOf(it)
			)
		})
	},
	dayHeader: @Composable (day: LocalDate) -> Unit = { WeekViewHeaderDay(day = it) },
	startDate: LocalDate = LocalDate.now(),
	hourHeight: Dp = 72.dp,
	hourList: List<WeekViewHour> = emptyList(),
	colorScheme: WeekViewColorScheme = WeekViewColorScheme.default(),
	dividerWidth: Float = Stroke.HairlineWidth,
	startTime: LocalTime = hourList.firstOrNull()?.startTime ?: LocalTime.MIDNIGHT.plusHours(6),
	endTime: LocalTime = hourList.lastOrNull()?.endTime ?: LocalTime.MIDNIGHT.plusHours(18),
	endTimeOffset: Float = 0f,
) {
	val verticalScrollState = rememberScrollState()
	var sidebarWidth by remember { mutableIntStateOf(0) }
	var headerHeight by remember { mutableIntStateOf(0) }
	var contentHeight by remember { mutableIntStateOf(0) }

	val startPage = Int.MAX_VALUE / 2
	val pagerState = rememberPagerState(initialPage = startPage) { Int.MAX_VALUE }
	val numDays = 5

	val currentOnPageChange by rememberUpdatedState(onPageChange)

	var datePickerDialog by remember { mutableStateOf(false) }
	var jumpToDate by remember { mutableStateOf<LocalDate?>(null) }

	LaunchedEffect(events) {
		// The events object only changes when the user changes.
		// When loading new events, only the map value is updated.
		// This allows to initialize the first page when the user changes.
		currentOnPageChange(pagerState.currentPage - startPage)
	}

	LaunchedEffect(jumpToDate) {
		jumpToDate?.let {
			pagerState.scrollToPage(startPage + pageIndexForDate(it))
		}
	}

	Row(modifier = modifier) {
		Column {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.width(with(LocalDensity.current) { sidebarWidth.toDp() })
					.height(with(LocalDensity.current) { headerHeight.toDp() })
			) {
				IconButton(
					onClick = {
						datePickerDialog = true
					}
				) {
					Icon(
						imageVector = Icons.Outlined.DateRange,
						contentDescription = stringResource(R.string.all_open_datepicker)
					)
				}
			}

			WeekViewSidebar(
				hourHeight = hourHeight,
				bottomPadding = with(LocalDensity.current) { endTimeOffset.toDp() },
				hourList = hourList,
				modifier = Modifier
					.onGloballyPositioned { sidebarWidth = it.size.width }
					.verticalScroll(verticalScrollState)
					.animateContentSize()
			)

		}

		LaunchedEffect(pagerState) {
			snapshotFlow { pagerState.currentPage - startPage }.collect { page ->
				currentOnPageChange(page)
			}
		}

		HorizontalPager(
			state = pagerState,
			pageSpacing = with(LocalDensity.current) { dividerWidth.toDp() },
			flingBehavior = PagerDefaults.flingBehavior(
				state = pagerState,
				snapAnimationSpec = tween(
					easing = CubicBezierEasing(0.17f, 0.84f, 0.44f, 1f),
					durationMillis = 500
				)
			)
		) { index ->
			val pageOffset = index - startPage
			val visibleStartDate =
				startDate.withDayOfWeek(1).plusWeeks(pageOffset) // 1 = Monday, 7 = Sunday

			Column {
				WeekViewHeader(
					startDate = visibleStartDate,
					numDays = numDays,
					dayHeader = dayHeader,
					modifier = Modifier
						.onGloballyPositioned { headerHeight = it.size.height }
				)

				if (hourList.isNotEmpty()) {
					var isRefreshing by remember { mutableStateOf(false) }
					val scope = rememberCoroutineScope()

					SwipeRefresh(
						state = rememberSwipeRefreshState(isRefreshing),
						onRefresh = {
							isRefreshing = true
							scope.launch {
								onReload(pageOffset)
								isRefreshing = false
							}
						},
					) {
						WeekViewContent(
							events = events.getOrDefault(visibleStartDate, emptyList()),
							eventContent = eventContent,
							startDate = visibleStartDate,
							numDays = numDays,
							startTime = startTime,
							endTime = endTime,
							endTimeOffset = endTimeOffset,
							hourHeight = hourHeight,
							hourList = hourList,
							dividerWidth = dividerWidth,
							dividerColor = colorScheme.dividerColor,
							indicatorColor = colorScheme.indicatorColor,
							pastBackgroundColor = colorScheme.pastBackgroundColor,
							futureBackgroundColor = colorScheme.futureBackgroundColor,
							modifier = Modifier
								.weight(1f)
								.onGloballyPositioned { contentHeight = it.size.height }
								.verticalScroll(verticalScrollState)
						)
					}
				}
			}
		}
	}

	if (datePickerDialog)
		DatePickerDialog(
			initialSelection = jumpToDate ?: LocalDate.now(),
			onDismiss = { datePickerDialog = false }
		) {
			datePickerDialog = false
			jumpToDate = it
		}
}

data class WeekViewColorScheme(
	val dividerColor: Color,
	val pastBackgroundColor: Color,
	val futureBackgroundColor: Color,
	val indicatorColor: Color
) {
	companion object {
		@Composable
		fun default(): WeekViewColorScheme {
			return WeekViewColorScheme(
				dividerColor = MaterialTheme.colorScheme.outline,
				pastBackgroundColor = Color(0x40808080),
				futureBackgroundColor = Color.Transparent,
				indicatorColor = Color.White
			)
		}
	}
}

/*@Preview(showBackground = true)
@Composable
fun WeekViewPreview() {
	WeekViewCompose(
		preferences = rememberWeekViewPreferences(),
		loadItems = { _, _, _ -> }
	)
}*/

@Composable
fun WeekViewTest(
	bg: Color
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(bg)
	)
}

data class WeekViewHour(
	val startTime: LocalTime,
	val endTime: LocalTime,
	val label: String
)
