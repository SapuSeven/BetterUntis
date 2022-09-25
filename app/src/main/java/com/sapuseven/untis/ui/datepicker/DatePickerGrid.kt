package com.sapuseven.untis.ui.datepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.ui.common.conditional
import com.sapuseven.untis.ui.common.ifNotNull
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

@Composable
fun DatePickerGrid(
	date: LocalDate,
	selectedDay: LocalDate,
	onClick: (day: LocalDate) -> Unit
) {
	Column {
		Row(
			modifier = Modifier.padding(bottom = 8.dp)
		) {
			(1..WEEK_LENGTH).forEach {
				DatePickerGridItem(
					text = DayOfWeek.of(it)
						.getDisplayName(TextStyle.NARROW_STANDALONE, Locale.getDefault()),
					modifier = Modifier
						.weight(1f)
						.aspectRatio(1f),
					defaultTextColor = MaterialTheme.colorScheme.onSurface
				)
			}
		}

		val weekRange = date.dayOfMonth().minimumValue..date.dayOfMonth().maximumValue
		val emptySpacesBefore = date.dayOfMonth().withMinimumValue().dayOfWeek - 1
		val emptySpacesAfter = WEEK_LENGTH * MAX_WEEK_LINES - emptySpacesBefore - weekRange.count()

		LazyVerticalGrid(
			modifier = Modifier
				.fillMaxWidth(),
			columns = GridCells.Fixed(WEEK_LENGTH),
			userScrollEnabled = false,
			content = {
				items(emptySpacesBefore) {
					DatePickerGridItem(
						text = "",
						modifier = Modifier
							.fillMaxWidth()
							.aspectRatio(1f, true)
					)
				}

				items(weekRange.toList()) {
					val day = date.withDayOfMonth(it)

					DatePickerGridItem(
						text = day.toString(DateTimeFormat.forPattern("d")),
						modifier = Modifier
							.fillMaxWidth()
							.aspectRatio(1f, true),
						isToday = day == LocalDate(),
						isSelected = day == selectedDay
					) {
						onClick(day)
					}

					/*if (it > 0) {
						val day = getGeneratedDay(it, currentMonth, currentYear)
						val isCurrentDay = day == currentDay
						KalendarDay(
							kalendarDay = day.toKalendarDay(),
							modifier = Modifier,
							kalendarEvents = kalendarEvents,
							isCurrentDay = isCurrentDay,
							onCurrentDayClick = { kalendarDay, events ->
								selectedKalendarDate.value = kalendarDay.localDate
								onCurrentDayClick(kalendarDay, events)
							},
							selectedKalendarDay = selectedKalendarDate.value,
							kalendarDayColors = kalendarDayColors,
							dotColor = kalendarThemeColor.headerTextColor,
							dayBackgroundColor = kalendarThemeColor.dayBackgroundColor,
						)
					}*/
				}

				items(emptySpacesAfter) {
					DatePickerGridItem(
						text = "",
						modifier = Modifier
							.fillMaxWidth()
							.aspectRatio(1f, true)
					)
				}
			}
		)
	}
}

@Composable
fun DatePickerGridItem(
	text: String,
	modifier: Modifier = Modifier,
	isToday: Boolean = false,
	isSelected: Boolean = false,
	defaultTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
	onClick: (() -> Unit)? = null
) {
	Box(
		modifier = modifier
			.clip(CircleShape)
			.conditional(isSelected) {
				background(MaterialTheme.colorScheme.primaryContainer)
			}
			.conditional(!isSelected) {
				Modifier
					.conditional(isToday) {
						border(1.dp, MaterialTheme.colorScheme.secondary, CircleShape)
					}
					.ifNotNull(onClick) {
						clickable(onClick = it)
					}
			},
		contentAlignment = Alignment.Center
	) {
		Text(
			text = text,
			color = if (isSelected)
				MaterialTheme.colorScheme.onPrimaryContainer
			else if (isToday)
				MaterialTheme.colorScheme.secondary
			else
				defaultTextColor,
			style = MaterialTheme.typography.titleMedium
		)
	}
}

private const val WEEK_LENGTH = 7
private const val MAX_WEEK_LINES = 6
