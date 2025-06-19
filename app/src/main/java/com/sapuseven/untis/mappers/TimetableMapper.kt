package com.sapuseven.untis.mappers

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.enumeration.PeriodState
import com.sapuseven.untis.api.model.untis.masterdata.timegrid.Day
import com.sapuseven.untis.api.model.untis.timetable.Period
import com.sapuseven.untis.data.repository.MasterDataRepository
import com.sapuseven.untis.data.repository.UserRepository
import com.sapuseven.untis.data.repository.UserSettingsRepository
import com.sapuseven.untis.data.repository.withDefault
import com.sapuseven.untis.helpers.BuildConfigFieldsProvider
import com.sapuseven.untis.models.PeriodItem
import com.sapuseven.untis.ui.weekview.Event
import com.sapuseven.untis.ui.weekview.EventColor
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.temporal.ChronoUnit

class TimetableMapper @AssistedInject constructor(
	private val userRepository: UserRepository,
	private val userSettingsRepository: UserSettingsRepository,
	private val masterDataRepository: MasterDataRepository,
	private val buildConfigFieldsProvider: BuildConfigFieldsProvider,
	@Assisted private val colorScheme: ColorScheme?,
) {
	@AssistedFactory
	interface Factory {
		fun create(colorScheme: ColorScheme? = null): TimetableMapper
	}

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
					contextType
				)
				.prepareEvents(
					timetableSubstitutionsIrregular,
					timetableBackgroundIrregular
				)
		}
	}

	suspend fun colorWeekViewTimetableEvents(
		events: List<Event<PeriodItem>>
	): List<Event<PeriodItem>> = waitForSettings().run {
		events.copyWithColor(
			EventColor.Custom(Color(backgroundRegular)).withDefault(hasBackgroundRegular(), EventColor.ThemePrimary),
			EventColor.Custom(Color(backgroundExam)).withDefault(hasBackgroundExam(), EventColor.ThemeError),
			EventColor.Custom(Color(backgroundCancelled)).withDefault(hasBackgroundCancelled(), EventColor.ThemeTertiary),
			EventColor.Custom(Color(backgroundIrregular)).withDefault(hasBackgroundIrregular(), EventColor.ThemeSecondary),
			schoolBackgroundList
		)
	}

	private suspend fun waitForSettings() = userSettingsRepository.getSettings().filterNotNull().first()

	private fun List<Period>.mapToEvents(
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
				colorScheme = if (buildConfigFieldsProvider.get().isDebug) EventColor.Debug else EventColor.ThemePrimary,
				start = period.startDateTime,
				end = period.endDateTime,
				data = periodItem
			)
		}
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

	private fun List<Event<PeriodItem>>.copyWithColor(
		regularColor: EventColor,
		examColor: EventColor,
		cancelledColor: EventColor,
		irregularColor: EventColor,
		useDefault: List<String>
	): List<Event<PeriodItem>> = map {
		it.copyWithColor(
			regularColor,
			examColor,
			cancelledColor,
			irregularColor,
			useDefault,
		)
	}

	private fun Event<PeriodItem>.copyWithColor(
		regularColor: EventColor,
		examColor: EventColor,
		cancelledColor: EventColor,
		irregularColor: EventColor,
		useDefault: List<String>
	): Event<PeriodItem> = data?.let {
		val subjectEntity =
			masterDataRepository.userData?.subjects?.find { it.id == data.subjects.firstOrNull()?.id }

		val defaultColor = EventColor.Custom(
			color = Color((subjectEntity?.backColor ?: data.originalPeriod.backColor).toColorInt()),
			textColor = Color((subjectEntity?.foreColor ?: data.originalPeriod.foreColor).toColorInt())
		)

		copy(
			colorScheme = when {
				data.isExam() -> if (useDefault.contains("exam")) defaultColor else examColor
				data.isCancelled() -> if (useDefault.contains("cancelled")) defaultColor else cancelledColor
				data.isIrregular() -> if (useDefault.contains("irregular")) defaultColor else irregularColor
				else -> if (useDefault.contains("regular")) defaultColor else regularColor
			}
		)
	} ?: this

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
