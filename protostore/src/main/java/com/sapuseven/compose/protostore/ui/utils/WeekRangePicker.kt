package com.sapuseven.compose.protostore.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeekRangePicker(
	value: SelectionState,
	onValueChange: (SelectionState) -> Unit
) {
	Row {
		val locale = remember { Locale.getDefault() }

		val days = Weekday.getOrderedDaysOfWeek(locale)

		val selection = remember { RangeSelectionMode(locale) }

		days.forEachIndexed { index, day ->
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.weight(1f)
					.aspectRatio(1f)
			) {
				val isSelected = value.selectedDays.contains(day)
				val isPrevSelected = value.selectedDays.contains(days.getOrNull(index - 1))
				val isNextSelected = value.selectedDays.contains(days.getOrNull(index + 1))
				val isBounds = isSelected && !(isPrevSelected && isNextSelected)

				Row(modifier = Modifier.fillMaxSize()) {
					Box(
						modifier = Modifier
							.fillMaxHeight()
							.weight(1f)
							.conditional(isSelected && isPrevSelected) {
								background(MaterialTheme.colorScheme.primaryContainer)
							}
					)
					Box(
						modifier = Modifier
							.fillMaxHeight()
							.weight(1f)
							.conditional(isSelected && isNextSelected) {
								background(MaterialTheme.colorScheme.primaryContainer)
							}
					)
				}
				Box(
					contentAlignment = Alignment.Center,
					modifier = Modifier
						.fillMaxSize()
						.clip(RoundedCornerShape(50))
						.conditional(isBounds) {
							background(MaterialTheme.colorScheme.primary)
						}
						.clickable {
							onValueChange(
								if (isSelected)
									selection.getSelectionStateAfterDeselecting(value, day)
								else
									selection.getSelectionStateAfterSelecting(value, day)
							)
						}
				) {
					Text(
						text = day.getAbbreviationFor(locale),
						color = if (isBounds)
							MaterialTheme.colorScheme.onPrimary
						else if (isSelected)
							MaterialTheme.colorScheme.onPrimaryContainer
						else
							MaterialTheme.colorScheme.onSurface
					)
				}
			}
		}
	}
}

private class RangeSelectionMode(private val locale: Locale) : SelectionMode {
	override fun getSelectionStateAfterSelecting(
		lastSelectionState: SelectionState,
		dayToSelect: Weekday
	): SelectionState {
		return createRangedSelectionState(
			lastSelectionState = lastSelectionState,
			dayPressed = dayToSelect
		)
	}

	override fun getSelectionStateAfterDeselecting(
		lastSelectionState: SelectionState,
		dayToDeselect: Weekday
	): SelectionState {
		return createRangedSelectionState(
			lastSelectionState = lastSelectionState,
			dayPressed = dayToDeselect
		)
	}

	private fun createRangedSelectionState(
		lastSelectionState: SelectionState,
		dayPressed: Weekday
	): SelectionState {
		val previouslySelectedDays = lastSelectionState.selectedDays
		val orderedWeekdays =
			Weekday.getOrderedDaysOfWeek(locale)
		val ordinalsOfPreviouslySelectedDays =
			previouslySelectedDays.map { orderedWeekdays.indexOf(it) }

		val ordinalOfFirstDayInPreviousRange = ordinalsOfPreviouslySelectedDays.minOrNull()
		val ordinalOfLastDayInPreviousRange = ordinalsOfPreviouslySelectedDays.maxOrNull()
		val ordinalOfSelectedDay = orderedWeekdays.indexOf(dayPressed)

		return when {
			ordinalOfFirstDayInPreviousRange == null || ordinalOfLastDayInPreviousRange == null -> {
				// We had no previous selection so just return the day pressed as the selection.
				SelectionState.withSingleDay(dayPressed)
			}
			ordinalOfFirstDayInPreviousRange == ordinalOfLastDayInPreviousRange && ordinalOfFirstDayInPreviousRange == ordinalOfSelectedDay -> {
				// User pressed the only day in the range selection. Return an empty selection.
				SelectionState()
			}
			ordinalOfSelectedDay == ordinalOfFirstDayInPreviousRange || ordinalOfSelectedDay == ordinalOfLastDayInPreviousRange -> {
				// User pressed the first or last item in range. Just deselect that item.
				lastSelectionState.withDayDeselected(dayPressed)
			}
			ordinalOfSelectedDay < ordinalOfFirstDayInPreviousRange -> {
				// User pressed a day on the left of the previous date range. Grow the starting point of the range to that.
				SelectionState(
					selectedDays = orderedWeekdays.subList(
						ordinalOfSelectedDay,
						ordinalOfLastDayInPreviousRange + 1
					)
				)
			}
			else -> {
				// User pressed a day on the right of the start of the date range. Update the ending point to that position.
				SelectionState(
					selectedDays = orderedWeekdays.subList(
						ordinalOfFirstDayInPreviousRange,
						ordinalOfSelectedDay + 1
					)
				)
			}
		}
	}
}

// Based on https://github.com/gantonious/MaterialDayPicker
enum class Weekday {
	SUNDAY,
	MONDAY,
	TUESDAY,
	WEDNESDAY,
	THURSDAY,
	FRIDAY,
	SATURDAY;

	/**
	 * Gets a localized abbreviation of this [Weekday].
	 *
	 * i.e. In an english based locale:
	 *
	 * ```kotlin
	 *     Weekday.MONDAY.abbreviation == "M"
	 *     Weekday.THURSDAY.abbreviation == "T"
	 * ```
	 *
	 * @param locale the locale which the abbreviation should be translated for
	 * @return The abbreviation as a string
	 */
	fun getAbbreviationFor(locale: Locale): String {
		val dayOfWeek = when (this) {
			SUNDAY -> Calendar.SUNDAY
			MONDAY -> Calendar.MONDAY
			TUESDAY -> Calendar.TUESDAY
			WEDNESDAY -> Calendar.WEDNESDAY
			THURSDAY -> Calendar.THURSDAY
			FRIDAY -> Calendar.FRIDAY
			SATURDAY -> Calendar.SATURDAY
		}

		val calendar = Calendar.getInstance().apply {
			set(Calendar.DAY_OF_WEEK, dayOfWeek)
		}

		return SimpleDateFormat("EEEEE", locale).format(calendar.time)
	}

	companion object {
		operator fun get(index: Int): Weekday {
			return entries[index]
		}

		private val allDays: List<Weekday>
			get() = entries

		/**
		 * Gets a list of [Weekday]s starting with the first day of the week
		 * for a given [locale]
		 *
		 * @param locale the locale to evaluate the first day for
		 * @return A list of [Weekday]s starting on the first day of
		 * the week for the given locale
		 */
		fun getOrderedDaysOfWeek(locale: Locale): List<Weekday> {
			return getOrderedDaysOfWeek(getFirstDayOfWeekFor(locale))
		}

		/**
		 * Gets a list of [Weekday]s starting with the provided [firstDayOfWeek]
		 *
		 * @param firstDayOfWeek the first week day to use
		 * @return A list of [Weekday]s starting on the [firstDayOfWeek]
		 */
		private fun getOrderedDaysOfWeek(firstDayOfWeek: Weekday): List<Weekday> {
			val daysOfTheWeekStartingOnSunday = allDays
			val indexOfFirstDay = daysOfTheWeekStartingOnSunday.indexOf(firstDayOfWeek)
			val daysToMoveToEndOfWeek = daysOfTheWeekStartingOnSunday.take(indexOfFirstDay)
			return daysOfTheWeekStartingOnSunday.drop(indexOfFirstDay) + daysToMoveToEndOfWeek
		}

		/**
		 * Gets the first day of the calendar week for a given [locale]
		 *
		 * @param locale the locale to evaluate the first day for
		 * @return The [Weekday] this week starts on for the given [locale]
		 */
		private fun getFirstDayOfWeekFor(locale: Locale = Locale.getDefault()): Weekday {
			return when (val firstDayOfWeek = Calendar.getInstance(locale).firstDayOfWeek) {
				Calendar.SUNDAY -> SUNDAY
				Calendar.MONDAY -> MONDAY
				Calendar.TUESDAY -> TUESDAY
				Calendar.WEDNESDAY -> WEDNESDAY
				Calendar.THURSDAY -> THURSDAY
				Calendar.FRIDAY -> FRIDAY
				Calendar.SATURDAY -> SATURDAY
				else -> throw IllegalStateException("Failed to resolve first day of week matching $firstDayOfWeek")
			}
		}
	}
}

// Based on https://github.com/gantonious/MaterialDayPicker
data class SelectionState @JvmOverloads constructor(
	val selectedDays: List<Weekday> = emptyList()
) {
	/**
	 * Creates a new [SelectionState] with [dayToSelect] selected.
	 *
	 * @param dayToSelect the day to select
	 * @return a new instance of a [SelectionState] with [dayToSelect] selected
	 */
	fun withDaySelected(dayToSelect: Weekday): SelectionState {
		return SelectionState(selectedDays + dayToSelect)
	}

	/**
	 * Creates a new [SelectionState] with [dayToDeselect] deselected.
	 *
	 * @param dayToDeselect the day to deselect
	 * @return a new instance of a [SelectionState] with [dayToDeselect] deselected
	 */
	fun withDayDeselected(dayToDeselect: Weekday): SelectionState {
		return SelectionState(selectedDays - dayToDeselect)
	}

	companion object {
		/**
		 * Creates a [SelectionState] with only [day] selected
		 *
		 * @param day the day to select
		 * @return a new instance of a [SelectionState] with only [day] selected
		 */
		fun withSingleDay(day: Weekday): SelectionState {
			return SelectionState().withDaySelected(day)
		}
	}
}

// Based on https://github.com/gantonious/MaterialDayPicker
interface SelectionMode {
	/**
	 * Takes the last [SelectionState] and transforms it to the next
	 * [SelectionState] based on the day the user just selected
	 *
	 * @param lastSelectionState the last [SelectionState]
	 * @param dayToSelect the [Weekday] that was just selected
	 * @return An updated [SelectionState] taking into account the day selected
	 */
	fun getSelectionStateAfterSelecting(
		lastSelectionState: SelectionState,
		dayToSelect: Weekday
	): SelectionState

	/**
	 * Takes the last [SelectionState] and transforms it to the next
	 * [SelectionState] based on the day the user just deselected
	 *
	 * @param lastSelectionState the last [SelectionState]
	 * @param dayToDeselect the [Weekday] that was just deselected
	 * @return An updated [SelectionState] taking into account the day deselected
	 */
	fun getSelectionStateAfterDeselecting(
		lastSelectionState: SelectionState,
		dayToDeselect: Weekday
	): SelectionState
}

fun <E> List<E>.bounds(): Pair<E, E?>? = if (size >= 1)
	first() to if (size >= 2) last() else null
else null

fun Weekday.toLocalizedString(): String =
	SimpleDateFormat("EEEE", Locale.getDefault()).format(Calendar.getInstance().apply {
		set(Calendar.DAY_OF_WEEK, toCalendar())
	}.time)

fun Weekday.toCalendar(): Int = when (this) {
	Weekday.SUNDAY -> Calendar.SUNDAY
	Weekday.MONDAY -> Calendar.MONDAY
	Weekday.TUESDAY -> Calendar.TUESDAY
	Weekday.WEDNESDAY -> Calendar.WEDNESDAY
	Weekday.THURSDAY -> Calendar.THURSDAY
	Weekday.FRIDAY -> Calendar.FRIDAY
	Weekday.SATURDAY -> Calendar.SATURDAY
}
