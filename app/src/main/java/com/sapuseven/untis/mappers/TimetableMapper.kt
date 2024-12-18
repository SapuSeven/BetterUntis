package com.sapuseven.untis.mappers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.sapuseven.untis.api.model.untis.masterdata.timegrid.Day
import com.sapuseven.untis.api.model.untis.timetable.Period
import com.sapuseven.untis.data.timetable.PeriodData
import com.sapuseven.untis.helpers.DateTimeUtils
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.scope.UserScopeManager
import com.sapuseven.untis.ui.activities.settings.SettingsRepository
import com.sapuseven.untis.ui.weekview.Event
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.joda.time.DateTimeConstants
import org.joda.time.LocalDateTime

class TimetableMapper @AssistedInject constructor(
	private val repository: SettingsRepository,
	private val userScopeManager: UserScopeManager,
) {
	private val settings = repository.getSettings()

	@AssistedFactory
	interface Factory {
		fun create(): TimetableMapper
	}

	private data class Preferences(
		var hideCancelled: Boolean,
		var substitutionsIrregular: Boolean,
	)

	init {
	}

	public suspend fun mapTimetablePeriodsToWeekViewEvents(
		items: List<Period>,
		contextType: TimetableDatabaseInterface.Type
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
				.color(
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

	private suspend fun waitForSettings() = settings.filterNotNull().first()

	private fun List<Period>.map(
		contextType: TimetableDatabaseInterface.Type,
		includeOrgIds: Boolean = true,
	): List<Event> {
		return map { period ->
			// TODO rethink this part
			val periodData = PeriodData(
				element = period
			)
			periodData.setup()

			Event(
				title = periodData.getShort(TimetableDatabaseInterface.Type.SUBJECT),
				top = (
					if (contextType == TimetableDatabaseInterface.Type.TEACHER)
						periodData.getShortSpanned(
							TimetableDatabaseInterface.Type.CLASS,
							includeOrgIds = includeOrgIds
						)
					else
						periodData.getShortSpanned(
							TimetableDatabaseInterface.Type.TEACHER,
							includeOrgIds = includeOrgIds
						)
					).toString(),
				bottom = (
					if (contextType == TimetableDatabaseInterface.Type.ROOM)
						periodData.getShortSpanned(
							TimetableDatabaseInterface.Type.CLASS,
							includeOrgIds = includeOrgIds
						)
					else
						periodData.getShortSpanned(
							TimetableDatabaseInterface.Type.ROOM,
							includeOrgIds = includeOrgIds
						)
					).toString(),
				color = Color.Transparent,
				pastColor = Color.Transparent,
				textColor = Color.Transparent,
				start = LocalDateTime(period.startDateTime),
				end = LocalDateTime(period.endDateTime),
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
			DateTimeConstants.MONDAY //DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH).parseDateTime(days.first().day).dayOfWeek

		// Put all items into a two dimensional array depending on day and hour
		forEach { item ->
			if (item.periodData == null) return@forEach // cannot merge items without period data

			val startDateTime = LocalDateTime(item.periodData.element.startDateTime)
			val endDateTime = LocalDateTime(item.periodData.element.endDateTime)

			val day = endDateTime.dayOfWeek - firstDayOfWeek

			if (day < 0 || day >= days.size) return@forEach

			val thisUnitStartIndex = days[day].units.indexOfFirst {
				it.startTime.time == startDateTime.toString(DateTimeUtils.tTimeNoSeconds())
			}

			val thisUnitEndIndex = days[day].units.indexOfFirst {
				it.endTime.time == endDateTime.toString(DateTimeUtils.tTimeNoSeconds())
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

	private suspend fun List<Event>.color(
		regularColor: Color,
		regularPastColor: Color,
		examColor: Color,
		examPastColor: Color,
		cancelledColor: Color,
		cancelledPastColor: Color,
		irregularColor: Color,
		irregularPastColor: Color,
		useDefault: List<String>
	): List<Event> {
		forEach { item ->
			item.periodData?.let {
				val defaultColor =
					Color(android.graphics.Color.parseColor(item.periodData.element.backColor))
				val defaultTextColor =
					Color(android.graphics.Color.parseColor(item.periodData.element.foreColor))

				item.color = when {
					item.periodData.isExam() -> if (useDefault.contains("exam")) defaultColor else examColor
					item.periodData.isCancelled() -> if (useDefault.contains("cancelled")) defaultColor else cancelledColor
					item.periodData.isIrregular() -> if (useDefault.contains("irregular")) defaultColor else irregularColor
					else -> if (useDefault.contains("regular")) defaultColor else regularColor
				}

				item.pastColor = when {
					item.periodData.isExam() -> if (useDefault.contains("exam")) defaultColor.darken(
						0.25f
					) else examPastColor

					item.periodData.isCancelled() -> if (useDefault.contains("cancelled")) defaultColor.darken(
						0.25f
					) else cancelledPastColor

					item.periodData.isIrregular() -> if (useDefault.contains("irregular")) defaultColor.darken(
						0.25f
					) else irregularPastColor

					else -> if (useDefault.contains("regular")) defaultColor.darken(0.25f) else regularPastColor
				}

				item.textColor = when {
					item.periodData.isExam() -> if (useDefault.contains("exam")) defaultTextColor else colorOn(
						examColor
					)

					item.periodData.isCancelled() -> if (useDefault.contains("cancelled")) defaultTextColor else colorOn(
						cancelledColor
					)

					item.periodData.isIrregular() -> if (useDefault.contains("irregular")) defaultTextColor else colorOn(
						irregularColor
					)

					else -> if (useDefault.contains("regular")) defaultTextColor else colorOn(
						regularColor
					)
				}
			}
		}
		return this
	}

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
