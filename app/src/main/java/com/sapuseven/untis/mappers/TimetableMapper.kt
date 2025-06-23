package com.sapuseven.untis.mappers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.graphics.toColorInt
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.enumeration.PeriodState
import com.sapuseven.untis.api.model.untis.masterdata.timegrid.Day
import com.sapuseven.untis.api.model.untis.timetable.Period
import com.sapuseven.untis.data.repository.MasterDataRepository
import com.sapuseven.untis.data.repository.UserRepository
import com.sapuseven.untis.data.repository.UserSettingsRepository
import com.sapuseven.untis.data.repository.withDefault
import com.sapuseven.untis.data.settings.model.UserSettings
import com.sapuseven.untis.helpers.BuildConfigFieldsProvider
import com.sapuseven.untis.models.PeriodItem
import com.sapuseven.untis.ui.weekview.Event
import com.sapuseven.untis.ui.weekview.EventStyle
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class TimetableMapper @Inject constructor(
	private val userRepository: UserRepository,
	private val userSettingsRepository: UserSettingsRepository,
	private val masterDataRepository: MasterDataRepository,
	private val buildConfigFieldsProvider: BuildConfigFieldsProvider,
) {
	/**
	 * Prepares periods for further processing or displaying.
	 *
	 * This function filters out items that should be hidden and merges multi-hour periods.
	 *
	 * @param items List of items that should be prepared
	 * @param hideCancelled Whether cancelled items should be removed
	 * @return A list of prepared items
	 */
	fun preparePeriods(
		items: List<Period>,
		hideCancelled: Boolean
	): List<Period> = items
		.filterPeriods(hideCancelled)
		.mergePeriods(userRepository.currentUser!!.timeGrid.days)

	suspend fun mapTimetablePeriodsToWeekViewEvents(
		items: List<Period>,
		contextType: ElementType
	): List<Event<PeriodItem>> {
		waitForSettings().apply {
			return preparePeriods(items, timetableHideCancelled)
				.mapToEvents(
					this,
					contextType
				)
				.prepareEvents(
					timetableSubstitutionsIrregular,
					timetableBackgroundIrregular
				)
		}
	}

	private suspend fun waitForSettings() = userSettingsRepository.getSettings().filterNotNull().first()

	private fun List<Period>.mapToEvents(
		userSettings: UserSettings,
		contextType: ElementType,
		includeOrgIds: Boolean = true,
	): List<Event<PeriodItem>> {
		return map { period ->
			val periodItem = PeriodItem(
				masterDataRepository = masterDataRepository,
				originalPeriod = period
			)

			Event(
				title = periodItem.getShort(ElementType.SUBJECT),
				top = (
					if (contextType == ElementType.TEACHER)
						periodItem.getShortAnnotated(
							ElementType.CLASS,
							includeOrgIds = includeOrgIds
						)
					else
						periodItem.getShortAnnotated(
							ElementType.TEACHER,
							includeOrgIds = includeOrgIds
						)
					),
				bottom = (
					if (contextType == ElementType.ROOM)
						periodItem.getShortAnnotated(
							ElementType.CLASS,
							includeOrgIds = includeOrgIds
						)
					else
						periodItem.getShortAnnotated(
							ElementType.ROOM,
							includeOrgIds = includeOrgIds
						)
					),
				eventStyle = getColorScheme(userSettings, periodItem),
				start = period.startDateTime,
				end = period.endDateTime,
				data = periodItem
			)
		}
	}

	private fun getColorScheme(
		userSettings: UserSettings,
		periodItem: PeriodItem
	): EventStyle = with(userSettings) {
		val subjectEntity = masterDataRepository.userData?.subjects?.find { it.id == periodItem.subjects.firstOrNull()?.id }
		val textDecoration = if (periodItem.isCancelled()) TextDecoration.LineThrough else TextDecoration.None

		val defaultColor = EventStyle.Custom(
			color = Color((subjectEntity?.backColor ?: periodItem.originalPeriod.backColor).toColorInt()),
			textStyle = TextStyle(color = Color((subjectEntity?.foreColor ?: periodItem.originalPeriod.foreColor).toColorInt()))
		)

		val regularColor = EventStyle.Custom(Color(backgroundRegular)).withDefault(hasBackgroundRegular(), EventStyle.ThemePrimary)
		val examColor = EventStyle.Custom(Color(backgroundExam)).withDefault(hasBackgroundExam(), EventStyle.ThemeError)
		val cancelledColor = EventStyle.Custom(Color(backgroundCancelled)).withDefault(hasBackgroundCancelled(), EventStyle.ThemeTertiary)
		val irregularColor = EventStyle.Custom(Color(backgroundIrregular)).withDefault(hasBackgroundIrregular(), EventStyle.ThemeSecondary)

		return when {
			periodItem.isExam() -> if (schoolBackgroundList.contains("exam")) defaultColor else examColor
			periodItem.isCancelled() -> (if (schoolBackgroundList.contains("cancelled")) defaultColor else cancelledColor)
			periodItem.isIrregular() -> if (schoolBackgroundList.contains("irregular")) defaultColor else irregularColor
			else -> if (schoolBackgroundList.contains("regular")) defaultColor else regularColor
		}.withTextStyle(TextStyle(textDecoration = textDecoration))
	}

	/**
	 * Prepares the items for the timetable.
	 *
	 * This function filters out items that should be hidden
	 *
	 * @param hideCancelled Whether cancelled items should be removed
	 * @return A list of prepared items
	 */
	private fun List<Period>.filterPeriods(
		hideCancelled: Boolean,
	): List<Period> = mapNotNull { item ->
		if (hideCancelled && item.`is`(PeriodState.CANCELLED)) return@mapNotNull null
		item
	}

	/**
	 * Prepares the items for the timetable.
	 *
	 * This function marks items as irregular when they match certain rules
	 *
	 * @param substitutionsIrregular Whether items with substitutions should be marked as irregular
	 * @param backgroundIrregular Whether irregular items should have a different background color
	 * @return A list of prepared items
	 */
	private fun List<Event<PeriodItem>>.prepareEvents(
		substitutionsIrregular: Boolean,
		backgroundIrregular: Boolean
	): List<Event<PeriodItem>> = mapNotNull { item ->
		if (substitutionsIrregular) {
			item.data?.apply {
				forceIrregular =
					classes.find { it.id != it.orgId } != null
						|| teachers.find { it.id != it.orgId } != null
						|| subjects.find { it.id != it.orgId } != null
						|| rooms.find { it.id != it.orgId } != null
				//TODO|| backgroundIrregular.getValue() && item.data.element.backColor != UNTIS_DEFAULT_COLOR
			}
		}
		item
	}

	private fun List<Period>.mergePeriods(days: List<Day>): List<Period> {
		val itemGrid: Array<Array<MutableList<Period>>> =
			Array(days.size) { Array(days.maxByOrNull { it.units.size }!!.units.size) { mutableListOf() } }
		val leftover: MutableList<Period> = mutableListOf()

		// TODO: Check if the day from the Untis API is always an english string
		val firstDayOfWeek =
			DayOfWeek.MONDAY //DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH).parseDateTime(days.first().day).dayOfWeek

		// Put all items into a two dimensional array depending on day and hour
		forEach { item ->
			val startDateTime = item.startDateTime
			val endDateTime = item.endDateTime

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

		val newItems = itemGrid.flatMap { unitsOfDay ->
			unitsOfDay.flatMapIndexed { unitIndex, items ->
				items.onEach {
					var i = 1
					while (unitIndex + i < unitsOfDay.size && it.mergeWith(unitsOfDay[unitIndex + i])) i++
				}
			}
		}.toMutableList()

		newItems.addAll(leftover) // Add items that didn't fit inside the timegrid. These will always be single lessons.

		return newItems
	}

	private fun Period.mergeWith(items: MutableList<Period>): Boolean {
		items.toList().forEachIndexed { i, _ ->
			if (i >= items.size) return@forEachIndexed // Needed because the number of elements can change

			val candidate = items[i]

			if (candidate.startDateTime.dayOfYear != startDateTime.dayOfYear) return@forEachIndexed

			if (this.equalsIgnoreTime(candidate)) {
				endDateTime = candidate.endDateTime
				items.removeAt(i)
				return true
			}
		}
		return false
	}
}
