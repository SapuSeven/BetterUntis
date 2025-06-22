package com.sapuseven.untis.ui.weekview

import android.text.format.DateFormat
import android.util.Log
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import com.sapuseven.untis.R
import com.sapuseven.untis.services.WeekLogicService
import com.sapuseven.untis.ui.common.conditional
import com.sapuseven.untis.ui.common.ifNotNull
import com.sapuseven.untis.ui.dialogs.DatePickerDialog
import com.sapuseven.untis.ui.functional.useDebounce
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.roundToInt

private class EventDataModifier(
	val event: Event<*>,
) : ParentDataModifier {
	override fun Density.modifyParentData(parentData: Any?) = event
}

private fun Modifier.eventData(event: Event<*>) = this.then(EventDataModifier(event))

private val dayNameFormat = DateTimeFormatter.ofPattern("EEE")
private val dayDateFormat = DateTimeFormatter.ofPattern("d. MMM")
private val timeFormat12h = DateTimeFormatter.ofPattern("h:mm a")
private val timeFormat24h = DateTimeFormatter.ofPattern("H:mm")

@Composable
fun WeekViewHeaderDay(
	day: LocalDate,
	modifier: Modifier = Modifier,
	isToday: Boolean = false,
	onClick: ((day: LocalDate) -> Unit)? = null
) {
	Column(
		modifier = Modifier
			.padding(2.dp)
			.clip(RoundedCornerShape(4.dp))
			.ifNotNull(onClick) {
				clickable { it(day) }
			}
			.padding(2.dp)
	) {
		Text(
			text = dayNameFormat.format(day),
			textAlign = TextAlign.Center,
			style = MaterialTheme.typography.titleLarge,
			maxLines = 1,
			color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
			modifier = modifier
				.fillMaxWidth()
		)
		Text(
			text = dayDateFormat.format(day),
			textAlign = TextAlign.Center,
			style = MaterialTheme.typography.titleSmall,
			maxLines = 1,
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
	currentDate: LocalDate,
	numDays: Int,
	modifier: Modifier = Modifier,
	dayHeader: @Composable (day: LocalDate) -> Unit = { WeekViewHeaderDay(day = it, isToday = currentDate == it) },
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
	) {
		repeat(numDays) { i ->
			Box(modifier = Modifier.weight(1f)) {
				dayHeader(startDate.plusDays(i.toLong()))
			}
		}
	}
}

@Preview(showBackground = true)
@Composable
fun WeekViewHeaderPreview() {
	WeekViewHeader(
		startDate = LocalDate.now(),
		currentDate = LocalDate.now().plusDays(1),
		numDays = 5,
		modifier = Modifier.sizeIn(maxWidth = 360.dp)
	)
}

@Composable
fun WeekViewSidebarLabel(
	hour: WeekViewHour,
	modifier: Modifier = Modifier,
) {
	val eventStyle = LocalWeekViewEventStyle.current
	val timeFormat = if (DateFormat.is24HourFormat(LocalContext.current)) timeFormat24h else timeFormat12h

	Box(
		modifier = modifier
			.padding(horizontal = 4.dp)
			.fillMaxSize()
	) {
		Text(
			text = timeFormat.format(hour.startTime),
			style = eventStyle.lessonInfoStyle,
			maxLines = 1,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier
				.align(Alignment.TopStart)
				.padding(end = 4.dp)
		)
		Text(
			text = hour.label,
			style = eventStyle.lessonNameStyle,
			maxLines = 1,
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier
				.align(Alignment.Center)
		)
		Text(
			text = timeFormat.format(hour.endTime),
			style = eventStyle.lessonInfoStyle,
			maxLines = 1,
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
	WeekViewStyle {
		WeekViewSidebarLabel(
			hour = WeekViewHour(
				LocalTime.of(9, 45),
				LocalTime.of(10, 30),
				"1"
			),
			Modifier.sizeIn(maxHeight = 64.dp, maxWidth = 72.dp)
		)
	}
}

@Preview(showBackground = true)
@Composable
fun CompactSidebarLabelPreview() {
	WeekViewStyle {
		WeekViewSidebarLabel(
			hour = WeekViewHour(
				LocalTime.of(9, 45),
				LocalTime.of(10, 30),
				"1"
			),
			Modifier.sizeIn(maxHeight = 48.dp, maxWidth = 68.dp)
		)
	}
}

@Composable
fun WeekViewSidebar(
	startTime: LocalTime,
	endTime: LocalTime,
	hourHeight: Dp,
	modifier: Modifier = Modifier,
	hourList: List<WeekViewHour>,
	label: @Composable (hour: WeekViewHour) -> Unit = { WeekViewSidebarLabel(hour = it) },
) {
	Box(
		modifier = modifier
			.height(
				hourHeight * (Duration
					.between(startTime, endTime)
					.toMinutes() / 60f)
			)
			.width(IntrinsicSize.Max)
	) {
		hourList.forEach { hour ->
			val topOffset = hourHeight *
				(Duration.between(startTime, hour.startTime).toMinutes() / 60f)

			val height = hourHeight *
				(Duration.between(hour.startTime, hour.endTime).toMinutes() / 60f)

			Box(
				modifier = Modifier
					.offset(y = topOffset)
					.height(height)
					.fillMaxWidth()
			) {
				label(hour)
			}
		}
	}
}

@Preview(showBackground = true)
@Composable
fun WeekViewSidebarPreview() {
	WeekViewStyle {
		WeekViewSidebar(
			startTime = LocalTime.of(9, 30),
			endTime = LocalTime.of(13, 45),
			hourHeight = 72.dp,
			hourList = (1..4).map {
				WeekViewHour(
					LocalTime.of(it + 8, 45),
					LocalTime.of(it + 9, 30),
					it.toString()
				)
			}
		)
	}
}

fun DrawScope.weekViewContentGrid(
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
			ChronoUnit.MINUTES.between(startTime, it) / 60f * hourHeight.toPx()

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
		start = Offset(size.width + dividerWidth + 1, 0f),
		end = Offset(size.width + dividerWidth + 1, size.height),
		strokeWidth = dividerWidth
	)
}

fun DrawScope.weekViewBackground(
	numDays: Int = 5,
	startDate: LocalDate,
	startTime: LocalTime,
	hourHeight: Dp,
	pastBackgroundColor: Color,
	futureBackgroundColor: Color,
	currentTime: LocalDateTime = LocalDateTime.now(),
) {
	val dayWidth = size.width / numDays

	repeat(numDays) {
		val fraction = ChronoUnit.MINUTES.between(
			startTime,
			currentTime.toLocalTime()
		) / 60f * hourHeight.toPx()
			.coerceIn(0f, size.height) / size.height

		drawVerticalSplitRect(
			topColor = pastBackgroundColor,
			bottomColor = futureBackgroundColor,
			topLeft = Offset(it * dayWidth, 0f),
			size = Size(dayWidth, size.height),
			division = when {
				startDate.plusDays(it.toLong()).isAfter(currentTime.toLocalDate()) -> 0f
				startDate.plusDays(it.toLong()).isEqual(currentTime.toLocalDate()) -> fraction
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

fun DrawScope.weekViewIndicator(
	numDays: Int = 5,
	startDate: LocalDate,
	startTime: LocalTime,
	hourHeight: Dp,
	indicatorColor: Color,
	indicatorWidth: Float = 2.dp.toPx(),
	currentTime: LocalDateTime = LocalDateTime.now(),
) {
	val dayWidth = size.width / numDays

	val yPos = ChronoUnit.MINUTES.between( // TODO: Can this be negative?
		startTime,
		currentTime.toLocalTime()
	) / 60f * hourHeight.toPx()
	val startDayIndex = ChronoUnit.DAYS.between(startDate, currentTime.toLocalDate())

	if (startDayIndex in 0..<numDays && yPos in 0f..size.height)
		drawLine(
			color = indicatorColor,
			start = Offset(startDayIndex * dayWidth, yPos),
			end = Offset((startDayIndex + 1) * dayWidth, yPos),
			strokeWidth = indicatorWidth
		)
}

suspend fun PointerInputScope.detectZoomGesture(
	onGesture: (centroid: Offset, zoom: Float) -> Unit
) {
	awaitEachGesture {
		var zoom = 1f
		var pastTouchSlop = false
		val touchSlop = viewConfiguration.touchSlop

		awaitFirstDown(requireUnconsumed = false)
		do {
			val event = awaitPointerEvent()
			val canceled = event.changes.fastAny { it.isConsumed }
			if (!canceled) {
				val zoomChange = event.calculateZoom()

				if (!pastTouchSlop) {
					zoom *= zoomChange

					val centroidSize = event.calculateCentroidSize(useCurrent = false)
					val zoomMotion = abs(1 - zoom) * centroidSize

					if (zoomMotion > touchSlop) {
						pastTouchSlop = true
					}
				}

				if (pastTouchSlop) {
					val centroid = event.calculateCentroid(useCurrent = false)
					if (zoomChange != 1f) {
						onGesture(centroid, zoomChange)
					}
					event.changes.fastForEach {
						if (it.positionChanged()) {
							it.consume()
						}
					}
				}
			}
		} while (!canceled && event.changes.fastAny { it.pressed })
	}
}

@Composable
fun <T> WeekViewContent(
	events: List<Event<T>>,
	modifier: Modifier = Modifier,
	numDays: Int = 5,
	startDate: LocalDate,
	startTime: LocalTime,
	endTime: LocalTime,
	endTimeOffset: Dp,
	hourHeight: Dp,
	hourList: List<WeekViewHour>,
	indicatorColor: Color,
	pastBackgroundColor: Color,
	futureBackgroundColor: Color,
	dividerColor: Color = MaterialTheme.colorScheme.outline,
	dividerWidth: Float = Stroke.HairlineWidth,
	currentTime: LocalDateTime = LocalDateTime.now(),
	eventContent: @Composable (event: Event<T>) -> Unit = { WeekViewEvent(event = it) }
) {
	// OPTIMIZE: Find a way to arrange events before layout, but calculate minEventWidth to determine maxSimultaneous
	// TODO: Display indicator if there are more events than can be displayed
	//val minEventWidth = 24.dp
	val maxSimultaneous = 100//(dayWidth.toFloat() / minEventWidth.toPx()).toInt()

	arrangeEvents(events, maxSimultaneous)

	Layout(
		content = {
			events.sortedBy(Event<*>::start).forEach { event ->
				Box(modifier = Modifier.eventData(event)) {
					eventContent(event)
				}
			}
		},
		modifier = modifier
			.drawWithContent {
				weekViewBackground(
					numDays = numDays,
					startDate = startDate,
					startTime = startTime,
					hourHeight = hourHeight,
					pastBackgroundColor = pastBackgroundColor,
					futureBackgroundColor = futureBackgroundColor,
					currentTime = currentTime
				)

				weekViewContentGrid(
					numDays = numDays,
					startTime = startTime,
					hourHeight = hourHeight,
					hourList = hourList,
					dividerColor = dividerColor,
					dividerWidth = dividerWidth
				)

				drawContent()

				weekViewIndicator(
					numDays = numDays,
					startDate = startDate,
					startTime = startTime,
					hourHeight = hourHeight,
					indicatorColor = indicatorColor,
					currentTime = currentTime
				)
			}
	) { measureables, constraints ->
		val height = constraints.minHeight.coerceAtLeast(
			(ChronoUnit.MINUTES.between(
				startTime,
				endTime
			) / 60f * hourHeight.toPx() + endTimeOffset.toPx()).roundToInt()
		)
		val width = constraints.maxWidth + dividerWidth.toInt()
		val dayWidth = width.toFloat() / numDays
		val placeablesWithEvents = measureables.map { measurable ->
			val event = measurable.parentData as Event<*>
			val eventDurationMinutes = ChronoUnit.MINUTES.between(event.start, event.end)
			val eventHeight = ((eventDurationMinutes / 60f) * hourHeight.toPx()).roundToInt()
			val placeable = measurable.measure(
				constraints.copy(
					minWidth = (dayWidth / event.numSimultaneous).toInt(),
					maxWidth = (dayWidth / event.numSimultaneous).toInt(),
					minHeight = eventHeight,
					maxHeight = eventHeight
				)
			)
			Pair(placeable, event)
		}

		layout(width, height) {
			placeablesWithEvents.forEach { (placeable, event) ->
				val eventOffsetMinutes = ChronoUnit.MINUTES.between(startTime, event.start.toLocalTime())
				val eventY = ((eventOffsetMinutes / 60f) * hourHeight.toPx()).roundToInt()
				val eventOffsetDays = ChronoUnit.DAYS.between(startDate, event.start.toLocalDate())
				val eventOffset = event.offsetSteps * (dayWidth / event.numSimultaneous)
				val eventX = eventOffsetDays * dayWidth + eventOffset
				placeable.place(eventX.toInt(), eventY)
			}
		}
	}
}

/**
 * @param onItemClick Callback on event click. First value contains a list of all simultaneous events
 * (including the clicked one) and second value the index of the actually clicked event.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> WeekViewCompose(
	events: Map<LocalDate, List<Event<T>>>,
	holidays: List<Holiday>,
	loading: Boolean? = false,
	weekLogicService: WeekLogicService,
	onPageChange: suspend (pageIndex: Int) -> Unit,
	onReload: suspend (pageIndex: Int) -> Unit,
	onItemClick: (Pair<List<Event<T>>, Int>) -> Unit,
	modifier: Modifier = Modifier,
	onZoom: suspend (zoomLevel: Float) -> Unit = {},
	currentTime: LocalDateTime = LocalDateTime.now(),
	eventContent: @Composable (event: Event<T>) -> Unit = { event ->
		WeekViewEvent(
			event = event,
			currentTime = currentTime,
			onClick = {
				onItemClick(
					event.simultaneousEvents.toList() to event.simultaneousEvents.indexOf(event)
				)
			})
	},
	enableZoomGesture: Boolean = true,
	initialScale: Float = 1f,
	hourHeight: Dp = 72.dp,
	hourList: List<WeekViewHour> = emptyList(),
	colorScheme: WeekViewColorScheme = WeekViewColorScheme.default(),
	dividerWidth: Float = Stroke.HairlineWidth,
	startTime: LocalTime = hourList.firstOrNull()?.startTime ?: LocalTime.MIDNIGHT.plusHours(6),
	endTime: LocalTime = hourList.lastOrNull()?.endTime ?: LocalTime.MIDNIGHT.plusHours(18),
	endTimeOffset: Dp = 0.dp,
	overlayContent: @Composable ((startPadding: Dp) -> Unit)? = null
) {
	val scope = rememberCoroutineScope()
	val verticalScrollState = rememberScrollState()
	var sidebarWidth by rememberSaveable { mutableIntStateOf(0) }
	var headerHeight by remember { mutableIntStateOf(0) }
	var contentHeight by remember { mutableIntStateOf(0) }

	var scale by remember { mutableFloatStateOf(initialScale) }

	val startPage = Int.MAX_VALUE / 2
	val pagerState = rememberPagerState(initialPage = startPage) { Int.MAX_VALUE }
	val numDays = 5

	val currentOnPageChange by rememberUpdatedState(onPageChange)

	var datePickerDialog by remember { mutableStateOf(false) }
	var jumpToDate by remember { mutableStateOf<LocalDate?>(null) }

	LaunchedEffect(jumpToDate) {
		jumpToDate?.let {
			pagerState.scrollToPage((startPage + pageIndexForDate(it)).toInt())
		}
	}

	scale.useDebounce {
		scope.launch { onZoom(it) }
	}

	val earliestEventTime by remember(events) {
		derivedStateOf {
			// TODO: Not efficient for large amounts of events
			events.values.flatten().minByOrNull { it.start.toLocalTime() }?.start?.toLocalTime()
		}
	}

	val latestEventTime by remember(events) {
		derivedStateOf {
			// TODO: Not efficient for large amounts of events
			events.values.flatten().maxByOrNull { it.end.toLocalTime() }?.end?.toLocalTime()
		}
	}

	val startTimeOffsetMinutes by animateIntAsState(
		Duration.between(earliestEventTime ?: startTime, startTime).toMinutes().coerceAtLeast(0).toInt(),
		label = "startTimeOffsetMinutes"
	)

	val endTimeOffsetMinutes by animateIntAsState(
		Duration.between(endTime, latestEventTime ?: endTime).toMinutes().coerceAtLeast(0).toInt(),
		label = "endTimeOffsetMinutes"
	)

	val startTimeWithOffset = remember(startTime, startTimeOffsetMinutes) {
		startTime.minusMinutes(startTimeOffsetMinutes.toLong())
	}

	val endTimeWithOffset = remember(endTime, endTimeOffsetMinutes) {
		endTime.plusMinutes(endTimeOffsetMinutes.toLong())
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
				startTime = startTimeWithOffset,
				endTime = endTimeWithOffset,
				hourHeight = hourHeight * scale,
				hourList = hourList,
				modifier = Modifier
					.onGloballyPositioned {
						Log.d("WeekViewCompose", "Sidebar width: ${it.size.width}")
						if (it.size.width > 0)
							sidebarWidth = it.size.width
					}
					.verticalScroll(verticalScrollState)
					.padding(bottom = endTimeOffset)
			)
		}

		LaunchedEffect(pagerState) {
			snapshotFlow { pagerState.currentPage - startPage }.collect { page ->
				currentOnPageChange(page)
			}
		}

		HorizontalPager(state = pagerState) { index ->
			val pageOffset = index - startPage
			val visibleStartDate =
				weekLogicService.currentWeekStartDate().plusWeeks(pageOffset.toLong()) // 1 = Monday, 7 = Sunday

			Column {
				WeekViewHeader(
					startDate = visibleStartDate,
					currentDate = currentTime.toLocalDate(),
					numDays = numDays,
					modifier = Modifier
						.onGloballyPositioned { headerHeight = it.size.height }
				)

				if (hourList.isNotEmpty()) {
					var isRefreshing by remember { mutableStateOf(false) }
					val pullRefreshState = rememberWeekViewPullToRefreshState()
					val onRefresh: () -> Unit = {
						isRefreshing = true
						scope.launch {
							onReload(pageOffset)
							pullRefreshState.snapTo(0f)
							isRefreshing = false
						}
					}

					val holidayEvents = remember {
						holidays.filter { holiday ->
							visibleStartDate.isBefore(holiday.end) && visibleStartDate.plusDays(numDays.toLong())
								.isAfter(holiday.start)
						}.map {
							// TODO Handle multi-day holidays
							Event<T>(
								title = it.title,
								colorScheme = it.colorScheme,
								start = it.start.atStartOfDay(),
								end = it.end.atTime(LocalTime.MAX),
							)
						}
					}

					Column(
						modifier = Modifier
							.pullToRefresh(
								state = pullRefreshState,
								enabled = verticalScrollState.value == 0, // Prevent refreshing when flinging to top
								isRefreshing = isRefreshing,
								onRefresh = onRefresh
							)
					) {
						WeekViewPullRefreshIndicator(
							refreshing = loading ?: isRefreshing,
							state = pullRefreshState,
							modifier = Modifier
								.fillMaxWidth()
						)

						WeekViewContent(
							// Potential improvement: Map the event list by individual days to reduce the number of events passed to be rendered
							events = events.getOrDefault(visibleStartDate, emptyList()) + holidayEvents,
							eventContent = eventContent,
							currentTime = currentTime,
							startDate = visibleStartDate,
							numDays = numDays,
							startTime = startTimeWithOffset,
							endTime = endTimeWithOffset,
							endTimeOffset = endTimeOffset,
							hourHeight = hourHeight * scale,
							hourList = hourList,
							dividerWidth = dividerWidth,
							indicatorColor = colorScheme.indicatorColor,
							pastBackgroundColor = colorScheme.pastBackgroundColor,
							futureBackgroundColor = colorScheme.futureBackgroundColor,
							modifier = Modifier
								.fillMaxHeight()
								.onGloballyPositioned { contentHeight = it.size.height }
								.conditional(enableZoomGesture) {
									pointerInput(Unit) {
										detectZoomGesture { centroid, zoom ->
											val oldScale = scale
											// Constrain min/max zoom
											scale = (scale * zoom).coerceIn(0.75f..2f)

											// Don't move scroll position if no effective zoom occurred
											val actualZoom = scale / oldScale
											val scrollY = verticalScrollState.value * actualZoom

											// Center zooming around gesture origin
											val scrollOffset = (zoom - 1) * (scrollY - centroid.y)

											scope.launch {
												verticalScrollState.scrollTo(scrollY.roundToInt() - scrollOffset.roundToInt())
											}
										}
									}
								}
								.verticalScroll(verticalScrollState)
						)
					}
				}
			}
		}
	}

	with(LocalDensity.current) {
		overlayContent?.invoke(sidebarWidth.toDp())
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
	val pastBackgroundColor: Color,
	val futureBackgroundColor: Color,
	val indicatorColor: Color
) {
	companion object {
		//@Composable
		//fun default(): WeekViewColorScheme = default(MaterialTheme.colorScheme)
		fun default(): WeekViewColorScheme {
			return WeekViewColorScheme(
				pastBackgroundColor = Color(0x40808080),
				futureBackgroundColor = Color.Transparent,
				indicatorColor = Color.White
			)
		}

		fun default(colorScheme: ColorScheme): WeekViewColorScheme {
			return WeekViewColorScheme(
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

data class WeekViewHour(
	val startTime: LocalTime,
	val endTime: LocalTime,
	val label: String
)
