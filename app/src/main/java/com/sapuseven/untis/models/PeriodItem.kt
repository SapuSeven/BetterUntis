package com.sapuseven.untis.models

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StrikethroughSpan
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.enumeration.PeriodState
import com.sapuseven.untis.api.model.untis.timetable.Period
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.data.repository.ElementRepository
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class PeriodItem(
	@Transient private var elementRepository: ElementRepository? = null,
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
			elementRepository?.getShortName(it.id, type) ?: ELEMENT_NAME_UNKNOWN
		}

	fun getLong(type: ElementType, list: HashSet<PeriodElement> = getListFor(type)) =
		list.joinToString(ELEMENT_NAME_SEPARATOR) {
			elementRepository?.getLongName(it.id, type) ?: ELEMENT_NAME_UNKNOWN
		}

	fun getShortSpanned(
		type: ElementType,
		list: HashSet<PeriodElement> = getListFor(type),
		includeOrgIds: Boolean = true
	): SpannableString {
		val builder = SpannableStringBuilder()

		list.forEach {
			if (builder.isNotBlank())
				builder.append(ELEMENT_NAME_SEPARATOR)
			builder.append(
				elementRepository?.getShortName(it.id, type)
					?: ELEMENT_NAME_UNKNOWN
			)
			if (includeOrgIds && it.id != it.orgId && it.orgId != 0L) {
				builder.append(ELEMENT_NAME_SEPARATOR)
				builder.append(
					elementRepository?.getShortName(it.orgId, type)
						?: ELEMENT_NAME_UNKNOWN,
					StrikethroughSpan(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
				)
			}
		}

		return SpannableString.valueOf(builder)
	}

	fun isCancelled(): Boolean = originalPeriod.`is`.contains(PeriodState.CANCELLED)

	fun isIrregular(): Boolean = forceIrregular || originalPeriod.`is`.contains(PeriodState.IRREGULAR)

	fun isExam(): Boolean = originalPeriod.`is`.contains(PeriodState.EXAM)
}
