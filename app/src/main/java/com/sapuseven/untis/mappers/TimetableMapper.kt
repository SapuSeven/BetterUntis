package com.sapuseven.untis.mappers

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.enumeration.PeriodState
import com.sapuseven.untis.api.model.untis.masterdata.timegrid.Day
import com.sapuseven.untis.api.model.untis.timetable.Period
import com.sapuseven.untis.data.repository.MasterDataRepository
import com.sapuseven.untis.models.PeriodItem
import com.sapuseven.untis.scope.UserScopeManager
import com.sapuseven.untis.ui.pages.settings.UserSettingsRepository
import com.sapuseven.untis.ui.weekview.Event
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.temporal.ChronoUnit

class TimetableMapper @AssistedInject constructor(
	settingsRepositoryFactory: UserSettingsRepository.Factory,
	private val masterDataRepository: MasterDataRepository,
	private val userScopeManager: UserScopeManager,
	@Assisted private val colorScheme: ColorScheme?,
) {
	private val settings = settingsRepositoryFactory.create(colorScheme ?: lightColorScheme()).getSettings()

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
		.mergePeriods(userScopeManager.user.timeGrid.days)

	suspend fun mapTimetablePeriodsToWeekViewEvents(
		items: List<Period>,
		contextType: ElementType
	): List<Event<PeriodItem>> {
		assertColorScheme()
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
	): List<Event<PeriodItem>> {
		assertColorScheme()
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

	private fun assertColorScheme() = assert(colorScheme != null) { "A colorScheme needs to be provided to the factory in order to use this function" }

	private suspend fun waitForSettings() = settings.filterNotNull().first()

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
				color = Color.Transparent,
				pastColor = Color.Transparent,
				textColor = Color.Transparent,
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
		regularColor: Color,
		regularPastColor: Color,
		examColor: Color,
		examPastColor: Color,
		cancelledColor: Color,
		cancelledPastColor: Color,
		irregularColor: Color,
		irregularPastColor: Color,
		useDefault: List<String>
	): List<Event<PeriodItem>> = map {
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

	private fun Event<PeriodItem>.copyWithColor(
		regularColor: Color,
		regularPastColor: Color,
		examColor: Color,
		examPastColor: Color,
		cancelledColor: Color,
		cancelledPastColor: Color,
		irregularColor: Color,
		irregularPastColor: Color,
		useDefault: List<String>
	): Event<PeriodItem> = data?.let {
		val subjectEntity =
			masterDataRepository.currentUserData?.subjects?.find { it.id == data.subjects.firstOrNull()?.id }

		val defaultColor = Color(
			android.graphics.Color.parseColor(
				subjectEntity?.backColor ?: data.originalPeriod.backColor
			)
		)
		val defaultTextColor = Color(
			android.graphics.Color.parseColor(
				subjectEntity?.foreColor ?: data.originalPeriod.foreColor
			)
		)

		copy(
			color = when {
				data.isExam() -> if (useDefault.contains("exam")) defaultColor else examColor
				data.isCancelled() -> if (useDefault.contains("cancelled")) defaultColor else cancelledColor
				data.isIrregular() -> if (useDefault.contains("irregular")) defaultColor else irregularColor
				else -> if (useDefault.contains("regular")) defaultColor else regularColor
			},
			pastColor = when {
				data.isExam() -> if (useDefault.contains("exam")) defaultColor.copy(alpha = .7f) else examPastColor
				data.isCancelled() -> if (useDefault.contains("cancelled")) defaultColor.copy(alpha = .7f) else cancelledPastColor
				data.isIrregular() -> if (useDefault.contains("irregular")) defaultColor.copy(alpha = .7f) else irregularPastColor
				else -> if (useDefault.contains("regular")) defaultColor.copy(alpha = .7f) else regularPastColor
			},
			textColor = when {
				data.isExam() -> if (useDefault.contains("exam")) defaultTextColor else colorOn(examColor)
				data.isCancelled() -> if (useDefault.contains("cancelled")) defaultTextColor else colorOn(cancelledColor)
				data.isIrregular() -> if (useDefault.contains("irregular")) defaultTextColor else colorOn(irregularColor)
				else -> if (useDefault.contains("regular")) defaultTextColor else colorOn(regularColor)
			}
		)
	} ?: this

	private fun colorOn(color: Color): Color {
		return when (color.copy(alpha = 1f)) {
			colorScheme?.primary -> colorScheme.onPrimary
			colorScheme?.secondary -> colorScheme.onSecondary
			colorScheme?.tertiary -> colorScheme.onTertiary
			else -> if (ColorUtils.calculateLuminance(color.toArgb()) < 0.5) Color.White else Color.Black
		}.copy(alpha = color.alpha)
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
