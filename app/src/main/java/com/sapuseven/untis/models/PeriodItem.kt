package com.sapuseven.untis.models

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.enumeration.PeriodState
import com.sapuseven.untis.api.model.untis.timetable.Period
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.data.repository.MasterDataRepository
import com.sapuseven.untis.ui.weekview.Event
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class PeriodItem(
	@Transient private var masterDataRepository: MasterDataRepository? = null,
	var originalPeriod: Period
) {
	val classes = HashSet<PeriodElement>()
	val teachers = HashSet<PeriodElement>()
	val subjects = HashSet<PeriodElement>()
	val rooms = HashSet<PeriodElement>()

	var forceIrregular = false

	companion object {
		// TODO: Convert to string resources
		const val ELEMENT_NAME_SEPARATOR = ", "
		const val ELEMENT_NAME_UNKNOWN = "?"
	}

	init {
		parseElements()
	}

	private fun parseElements() {
		originalPeriod.elements.forEach { originalPeriod ->
			when (originalPeriod.type) {
				ElementType.CLASS -> classes.add(originalPeriod)
				ElementType.TEACHER -> teachers.add(originalPeriod)
				ElementType.SUBJECT -> subjects.add(originalPeriod)
				ElementType.ROOM -> rooms.add(originalPeriod)
				else -> {}
			}
		}
	}

	private fun getListFor(type: ElementType): java.util.HashSet<PeriodElement> =
		when (type) {
			ElementType.CLASS -> classes
			ElementType.TEACHER -> teachers
			ElementType.SUBJECT -> subjects
			ElementType.ROOM -> rooms
			else -> hashSetOf()
		}

	fun getShort(type: ElementType, list: HashSet<PeriodElement> = getListFor(type)) =
		list.joinToString(ELEMENT_NAME_SEPARATOR) {
			masterDataRepository?.getShortName(it.id, type) ?: ELEMENT_NAME_UNKNOWN
		}

	fun getLong(type: ElementType, list: HashSet<PeriodElement> = getListFor(type)) =
		list.joinToString(ELEMENT_NAME_SEPARATOR) {
			masterDataRepository?.getLongName(it.id, type) ?: ELEMENT_NAME_UNKNOWN
		}

	fun getShortAnnotated(
		type: ElementType,
		list: HashSet<PeriodElement> = getListFor(type),
		includeOrgIds: Boolean = true
	): AnnotatedString {
		return buildAnnotatedString {
			list.forEach {
				if (length > 0) append(ELEMENT_NAME_SEPARATOR)
				append(masterDataRepository?.getShortName(it.id, type) ?: ELEMENT_NAME_UNKNOWN)

				if (includeOrgIds && it.id != it.orgId && it.orgId != 0L) {
					append(ELEMENT_NAME_SEPARATOR)
					withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
						append(masterDataRepository?.getShortName(it.orgId, type) ?: ELEMENT_NAME_UNKNOWN)
					}
				}
			}
		}
	}

	fun isCancelled(): Boolean = originalPeriod.`is`.contains(PeriodState.CANCELLED)

	fun isIrregular(): Boolean = forceIrregular || originalPeriod.`is`.contains(PeriodState.IRREGULAR)

	fun isExam(): Boolean = originalPeriod.`is`.contains(PeriodState.EXAM)
}

fun PeriodItem.mergeValuesWith(item: PeriodItem) {
	apply {
		classes.addAll(item.classes)
		teachers.addAll(item.teachers)
		subjects.addAll(item.subjects)
		rooms.addAll(item.rooms)
	}
}

fun PeriodItem.equalsIgnoreTime(secondItem: PeriodItem) =
	originalPeriod.equalsIgnoreTime(secondItem.originalPeriod)
