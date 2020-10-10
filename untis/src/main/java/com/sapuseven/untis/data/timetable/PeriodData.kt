package com.sapuseven.untis.data.timetable

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StrikethroughSpan
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.Period
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import java.io.Serializable

class PeriodData(
		private var timetableDatabaseInterface: TimetableDatabaseInterface? = null,
		var element: Period
) : Serializable {
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

	private fun parseElements() {
		element.elements.forEach { element ->
			when (element.type) {
				TimetableDatabaseInterface.Type.CLASS.name -> addClass(element)
				TimetableDatabaseInterface.Type.TEACHER.name -> addTeacher(element)
				TimetableDatabaseInterface.Type.SUBJECT.name -> addSubject(element)
				TimetableDatabaseInterface.Type.ROOM.name -> addRoom(element)
			}
		}
	}

	private fun addClass(element: PeriodElement) = classes.add(element)

	private fun addTeacher(element: PeriodElement) = teachers.add(element)

	private fun addSubject(element: PeriodElement) = subjects.add(element)

	private fun addRoom(element: PeriodElement) = rooms.add(element)

	fun setup() = parseElements()

	fun getShort(list: HashSet<PeriodElement>, type: TimetableDatabaseInterface.Type) =
			list.joinToString(ELEMENT_NAME_SEPARATOR) {
				timetableDatabaseInterface?.getShortName(it.id, type) ?: ELEMENT_NAME_UNKNOWN
			}

	fun getLong(list: HashSet<PeriodElement>, type: TimetableDatabaseInterface.Type) =
			list.joinToString(ELEMENT_NAME_SEPARATOR) {
				timetableDatabaseInterface?.getLongName(it.id, type) ?: ELEMENT_NAME_UNKNOWN
			}

	fun getShortSpanned(list: HashSet<PeriodElement>, type: TimetableDatabaseInterface.Type, includeOrgIds: Boolean = true): SpannableString {
		val builder = SpannableStringBuilder()

		list.forEach {
			if (builder.isNotBlank())
				builder.append(ELEMENT_NAME_SEPARATOR)
			builder.append(timetableDatabaseInterface?.getShortName(it.id, type)
					?: ELEMENT_NAME_UNKNOWN)
			if (includeOrgIds && it.id != it.orgId && it.orgId != 0) {
				builder.append(ELEMENT_NAME_SEPARATOR)
				builder.append(timetableDatabaseInterface?.getShortName(it.orgId, type)
						?: ELEMENT_NAME_UNKNOWN,
						StrikethroughSpan(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
			}
		}

		return SpannableString.valueOf(builder)
	}

	@Deprecated("Use getShort instead.")
	fun getShortTitle() = subjects.joinToString(ELEMENT_NAME_SEPARATOR) {
		timetableDatabaseInterface?.getShortName(it.id, TimetableDatabaseInterface.Type.SUBJECT)
				?: ELEMENT_NAME_UNKNOWN
	}

	@Deprecated("Use getLong instead.")
	fun getLongTitle() = subjects.joinToString(ELEMENT_NAME_SEPARATOR) {
		timetableDatabaseInterface?.getLongName(it.id, TimetableDatabaseInterface.Type.SUBJECT)
				?: ELEMENT_NAME_UNKNOWN
	}

	@Deprecated("Use getShort instead.")
	fun getShortTeachers() = teachers.joinToString(ELEMENT_NAME_SEPARATOR) {
		timetableDatabaseInterface?.getShortName(it.id, TimetableDatabaseInterface.Type.TEACHER)
				?: ELEMENT_NAME_UNKNOWN
	}

	@Deprecated("Use getLong instead.")
	fun getLongTeachers() = teachers.joinToString(ELEMENT_NAME_SEPARATOR) {
		timetableDatabaseInterface?.getLongName(it.id, TimetableDatabaseInterface.Type.TEACHER)
				?: ELEMENT_NAME_UNKNOWN
	}

	@Deprecated("Use getShort instead.")
	fun getShortRooms() = rooms.joinToString(ELEMENT_NAME_SEPARATOR) {
		timetableDatabaseInterface?.getShortName(it.id, TimetableDatabaseInterface.Type.ROOM)
				?: ELEMENT_NAME_UNKNOWN
	}

	@Deprecated("Use getLong instead.")
	fun getLongRooms() = rooms.joinToString(ELEMENT_NAME_SEPARATOR) {
		timetableDatabaseInterface?.getLongName(it.id, TimetableDatabaseInterface.Type.ROOM)
				?: ELEMENT_NAME_UNKNOWN
	}

	@Deprecated("Use getShort instead.")
	fun getShortClasses() = classes.joinToString(ELEMENT_NAME_SEPARATOR) {
		timetableDatabaseInterface?.getShortName(it.id, TimetableDatabaseInterface.Type.CLASS)
				?: ELEMENT_NAME_UNKNOWN
	}

	@Deprecated("Use getLong instead.")
	fun getLongClasses() = classes.joinToString(ELEMENT_NAME_SEPARATOR) {
		timetableDatabaseInterface?.getLongName(it.id, TimetableDatabaseInterface.Type.CLASS)
				?: ELEMENT_NAME_UNKNOWN
	}

	fun isCancelled(): Boolean = element.`is`.contains(Period.CODE_CANCELLED)

	fun isIrregular(): Boolean = forceIrregular || element.`is`.contains(Period.CODE_IRREGULAR)

	fun isExam(): Boolean = element.`is`.contains(Period.CODE_EXAM)
}