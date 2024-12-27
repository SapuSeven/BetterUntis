package com.sapuseven.untis.mappers

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.masterdata.timegrid.Day
import com.sapuseven.untis.api.model.untis.timetable.Period
import com.sapuseven.untis.data.timetable.PeriodData
import com.sapuseven.untis.scope.UserScopeManager
import com.sapuseven.untis.ui.activities.settings.UserSettingsRepository
import com.sapuseven.untis.ui.weekview.Event
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.temporal.ChronoUnit

class TimetableMapper @AssistedInject constructor(
	private val repositoryFactory: UserSettingsRepository.Factory,
	private val userScopeManager: UserScopeManager,
	@Assisted private val colorScheme: ColorScheme,
) {
	private val settings = repositoryFactory.create(colorScheme).getSettings()

	@AssistedFactory
	interface Factory {
		fun create(colorScheme: ColorScheme): TimetableMapper
	}

	private data class Preferences(
		var hideCancelled: Boolean,
		var substitutionsIrregular: Boolean,
	)

	public suspend fun mapTimetablePeriodsToWeekViewEvents(
		items: List<Period>,
		contextType: ElementType
	): List<Event> {
		waitForSettings().apply {
			return items
				.map(
					contextType
				)
				.filter(
					timetableHideCancelled,
					timetableSubstitutionsIrregular,
					timetableBackgroundIrregular
				)
				.merge(
					userScopeManager.user.timeGrid.days
				)
		}
	}

	public suspend fun colorWeekViewTimetableEvents(
		events: List<Event>
	): List<Event> {
		waitForSettings().apply {
			return events.copyWithColor(
				Color(backgroundRegular),
				Color(backgroundRegularPast),
				Color(backgroundExam),
				Color(backgroundExamPast),
				Color(backgroundCancelled),
				Color(backgroundCancelledPast),
				Color(backgroundIrregular),
				Color(backgroundIrregularPast),
				schoolBackgroundList
			)
		}
	}

	public suspend fun color(event: Event) {
		waitForSettings().apply {
			return event.color(
				Color(backgroundRegular),
				Color(backgroundRegularPast),
				Color(backgroundExam),
				Color(backgroundExamPast),
				Color(backgroundCancelled),
				Color(backgroundCancelledPast),
				Color(backgroundIrregular),
				Color(backgroundIrregularPast),
				schoolBackgroundList
			)
		}
	}

	private suspend fun Event.color(
		regularColor: Color,
		regularPastColor: Color,
		examColor: Color,
		examPastColor: Color,
		cancelledColor: Color,
		cancelledPastColor: Color,
		irregularColor: Color,
		irregularPastColor: Color,
		useDefault: List<String>
	) {
		periodData?.let {
			val defaultColor =
				Color(android.graphics.Color.parseColor(periodData.element.backColor))
			val defaultTextColor =
				Color(android.graphics.Color.parseColor(periodData.element.foreColor))

			color = when {
				periodData.isExam() -> if (useDefault.contains("exam")) defaultColor else examColor
				periodData.isCancelled() -> if (useDefault.contains("cancelled")) defaultColor else cancelledColor
				periodData.isIrregular() -> if (useDefault.contains("irregular")) defaultColor else irregularColor
				else -> if (useDefault.contains("regular")) defaultColor else regularColor
			}

			pastColor = when {
				periodData.isExam() -> if (useDefault.contains("exam")) defaultColor.darken(
					0.25f
				) else examPastColor

				periodData.isCancelled() -> if (useDefault.contains("cancelled")) defaultColor.darken(
					0.25f
				) else cancelledPastColor

				periodData.isIrregular() -> if (useDefault.contains("irregular")) defaultColor.darken(
					0.25f
				) else irregularPastColor

				else -> if (useDefault.contains("regular")) defaultColor.darken(0.25f) else regularPastColor
			}

			textColor = when {
				periodData.isExam() -> if (useDefault.contains("exam")) defaultTextColor else colorOn(
					examColor
				)

				periodData.isCancelled() -> if (useDefault.contains("cancelled")) defaultTextColor else colorOn(
					cancelledColor
				)

				periodData.isIrregular() -> if (useDefault.contains("irregular")) defaultTextColor else colorOn(
					irregularColor
				)

				else -> if (useDefault.contains("regular")) defaultTextColor else colorOn(
					regularColor
				)
			}
		}
	}

	private suspend fun waitForSettings() = settings.filterNotNull().first()

	private fun List<Period>.map(
		contextType: ElementType,
		includeOrgIds: Boolean = true,
	): List<Event> {
		return map { period ->
			// TODO rethink this part
			val periodData = PeriodData(
				element = period
			)
			periodData.setup()

			Event(
				title = periodData.getShort(ElementType.SUBJECT),
				top = (
					if (contextType == ElementType.TEACHER)
						periodData.getShortSpanned(
							ElementType.CLASS,
							includeOrgIds = includeOrgIds
						)
					else
						periodData.getShortSpanned(
							ElementType.TEACHER,
							includeOrgIds = includeOrgIds
						)
					).toString(),
				bottom = (
					if (contextType == ElementType.ROOM)
						periodData.getShortSpanned(
							ElementType.CLASS,
							includeOrgIds = includeOrgIds
						)
					else
						periodData.getShortSpanned(
							ElementType.ROOM,
							includeOrgIds = includeOrgIds
						)
					).toString(),
				color = Color.Transparent,
				pastColor = Color.Transparent,
				textColor = Color.Transparent,
				start = period.startDateTime,
				end = period.endDateTime,
				periodData = periodData
			)
		}
	}

	/**
	 * Prepares the items for the timetable.
	 *
	 * This function filters out items that should be hidden, marks items with substitutions as irregular
	 *
	 * @param items List of items that should be prepared
	 * @param hideCancelled Whether cancelled items should be removed
	 * @param substitutionsIrregular Whether items with substitutions should be marked as irregular
	 * @param backgroundIrregular Whether irregular items should have a different background color
	 * @return A list of prepared items
	 */
	private suspend fun List<Event>.filter(
		hideCancelled: Boolean,
		substitutionsIrregular: Boolean,
		backgroundIrregular: Boolean
	): List<Event> = mapNotNull { item ->
		if (hideCancelled && item.periodData?.isCancelled() == true) return@mapNotNull null

		if (substitutionsIrregular) {
			item.periodData?.apply {
				forceIrregular =
					classes.find { it.id != it.orgId } != null
						|| teachers.find { it.id != it.orgId } != null
						|| subjects.find { it.id != it.orgId } != null
						|| rooms.find { it.id != it.orgId } != null
				//TODO|| backgroundIrregular.getValue() && item.periodData.element.backColor != UNTIS_DEFAULT_COLOR
			}
		}
		item
	}

	private fun List<Event>.merge(days: List<Day>): List<Event> {
		val itemGrid: Array<Array<MutableList<Event>>> =
			Array(days.size) { Array(days.maxByOrNull { it.units.size }!!.units.size) { mutableListOf() } }
		val leftover: MutableList<Event> = mutableListOf()

		// TODO: Check if the day from the Untis API is always an english string
		val firstDayOfWeek =
			DayOfWeek.MONDAY //DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH).parseDateTime(days.first().day).dayOfWeek

		// Put all items into a two dimensional array depending on day and hour
		forEach { item ->
			if (item.periodData == null) return@forEach // cannot merge items without period data

			val startDateTime = item.periodData.element.startDateTime
			val endDateTime = item.periodData.element.endDateTime

			val day = endDateTime.dayOfWeek.value - firstDayOfWeek.value

			if (day < 0 || day >= days.size) return@forEach

			val thisUnitStartIndex = days[day].units.indexOfFirst {
				it.startTime.truncatedTo(ChronoUnit.MINUTES)
					.equals(startDateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES))
			}

			val thisUnitEndIndex = days[day].units.indexOfFirst {
				it.endTime.truncatedTo(ChronoUnit.MINUTES)
					.equals(endDateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES))
			}

			if (thisUnitStartIndex != -1 && thisUnitEndIndex != -1) itemGrid[day][thisUnitStartIndex].add(
				item
			)
			else leftover.add(item)
		}

		val newItems = mutableListOf<Event>()
		newItems.addAll(leftover) // Add items that didn't fit inside the timegrid. These will always be single lessons.
		itemGrid.forEach { unitsOfDay ->
			unitsOfDay.forEachIndexed { unitIndex, items ->
				items.forEach {
					var i = 1
					while (unitIndex + i < unitsOfDay.size && it.mergeWith(unitsOfDay[unitIndex + i])) i++
				}

				newItems.addAll(items)
			}
		}
		return newItems
	}

	private suspend fun List<Event>.copyWithColor(
		regularColor: Color,
		regularPastColor: Color,
		examColor: Color,
		examPastColor: Color,
		cancelledColor: Color,
		cancelledPastColor: Color,
		irregularColor: Color,
		irregularPastColor: Color,
		useDefault: List<String>
	): List<Event> = map {
		it.copyWithColor(
			regularColor,
			regularPastColor,
			examColor,
			examPastColor,
			cancelledColor,
			cancelledPastColor,
			irregularColor,
			irregularPastColor,
			useDefault,
		)
	}

	private suspend fun Event.copyWithColor(
		regularColor: Color,
		regularPastColor: Color,
		examColor: Color,
		examPastColor: Color,
		cancelledColor: Color,
		cancelledPastColor: Color,
		irregularColor: Color,
		irregularPastColor: Color,
		useDefault: List<String>
	): Event = periodData?.let {
		val defaultColor = Color(android.graphics.Color.parseColor(periodData.element.backColor))
		val defaultTextColor =
			Color(android.graphics.Color.parseColor(periodData.element.foreColor))

		copy(
			color = when {
				periodData.isExam() -> if (useDefault.contains("exam")) defaultColor else examColor
				periodData.isCancelled() -> if (useDefault.contains("cancelled")) defaultColor else cancelledColor
				periodData.isIrregular() -> if (useDefault.contains("irregular")) defaultColor else irregularColor
				else -> if (useDefault.contains("regular")) defaultColor else regularColor
			},
			pastColor = when {
				periodData.isExam() -> if (useDefault.contains("exam")) defaultColor.darken(0.25f) else examPastColor
				periodData.isCancelled() -> if (useDefault.contains("cancelled")) defaultColor.darken(0.25f) else cancelledPastColor
				periodData.isIrregular() -> if (useDefault.contains("irregular")) defaultColor.darken(0.25f) else irregularPastColor
				else -> if (useDefault.contains("regular")) defaultColor.darken(0.25f) else regularPastColor
			},
			textColor = when {
				periodData.isExam() -> if (useDefault.contains("exam")) defaultTextColor else colorOn(examColor)
				periodData.isCancelled() -> if (useDefault.contains("cancelled")) defaultTextColor else colorOn(
					cancelledColor
				)

				periodData.isIrregular() -> if (useDefault.contains("irregular")) defaultTextColor else colorOn(
					irregularColor
				)

				else -> if (useDefault.contains("regular")) defaultTextColor else colorOn(regularColor)
			}
		)
	} ?: this

	private fun Color.darken(ratio: Float) = lerp(this, Color.Black, ratio)

	private fun colorOn(color: Color): Color {
		return /*when (color.copy(alpha = 1f)) {
		colorScheme.primary -> colorScheme.onPrimary
		colorScheme.secondary -> colorScheme.onSecondary
		colorScheme.tertiary -> colorScheme.onTertiary
		else ->*/ if (ColorUtils.calculateLuminance(color.toArgb()) < 0.5) Color.White else Color.Black
		//}.copy(alpha = color.alpha)
	}


	private fun Event.mergeWith(items: MutableList<Event>): Boolean {
		if (periodData == null) return false // cannot merge items without period data

		items.toList().forEachIndexed { i, _ ->
			if (i >= items.size) return@forEachIndexed // Needed because the number of elements can change

			val candidate = items[i]

			if (candidate.periodData == null) return@forEachIndexed // cannot merge items without period data

			if (candidate.start.dayOfYear != start.dayOfYear) return@forEachIndexed

			if (this.equalsIgnoreTime(candidate)) {
				end = candidate.end
				periodData.element.endDateTime = candidate.periodData.element.endDateTime
				items.removeAt(i)
				return true
			}
		}
		return false
	}

	private fun Event.mergeValuesWith(item: Event) {
		item.periodData?.let { periodData ->
			this.periodData?.apply {
				classes.addAll(periodData.classes)
				teachers.addAll(periodData.teachers)
				subjects.addAll(periodData.subjects)
				rooms.addAll(periodData.rooms)
			}
		}
	}

	private fun Event.equalsIgnoreTime(secondItem: Event) =
		secondItem.periodData?.element?.let { periodData?.element?.equalsIgnoreTime(it) } == true
}
