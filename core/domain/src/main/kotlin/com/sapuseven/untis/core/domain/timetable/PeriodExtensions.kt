package com.sapuseven.untis.core.domain.timetable

import com.sapuseven.untis.core.model.timetable.Element
import com.sapuseven.untis.core.model.timetable.ElementType
import com.sapuseven.untis.core.model.timetable.Period
import com.sapuseven.untis.core.model.timetable.PeriodState

const val ELEMENT_NAME_SEPARATOR = ", "

fun List<Element>.toShortString(): String =
	joinToString(ELEMENT_NAME_SEPARATOR) { it.shortName }

fun List<Element>.toLongString(): String =
	joinToString(ELEMENT_NAME_SEPARATOR) { it.shortName }

val Period.classes get() = elements.filter { it.type == ElementType.CLASS }
val Period.teachers get() = elements.filter { it.type == ElementType.TEACHER }
val Period.subjects get() = elements.filter { it.type == ElementType.SUBJECT }
val Period.rooms get() = elements.filter { it.type == ElementType.ROOM }

fun Period.isCancelled(): Boolean = states.contains(PeriodState.CANCELLED)

fun Period.isIrregular(): Boolean = forceIrregular || states.contains(PeriodState.IRREGULAR)

fun Period.isExam(): Boolean = states.contains(PeriodState.EXAM)

/**
 * Checks if this period equals another period, ignoring the start and end time, as well as the id.
 *
 * This can be used to check if two periods represent the same lesson, even if they are at different times.
 *
 * @param other The other period to compare with.
 * @return `true` if the periods are equal ignoring time and id, `false` otherwise.
 */
fun Period.equalsIgnoreTime(other: Period): Boolean {
	if (this === other) return true

	if (lessonId != other.lessonId) return false
	if (onlinePeriod != other.onlinePeriod) return false
	if (foreColor != other.foreColor) return false
	if (backColor != other.backColor) return false
	if (innerForeColor != other.innerForeColor) return false
	if (innerBackColor != other.innerBackColor) return false
	if (infoTexts != other.infoTexts) return false
	if (elements != other.elements) return false
	if (rights != other.rights) return false
	if (states != other.states) return false
	if (attachments != other.attachments) return false
	if (homeworks != other.homeworks) return false
	if (exam != other.exam) return false
	if (onlinePeriodLink != other.onlinePeriodLink) return false

	return true
}

operator fun Period.plus(other: Period) = copy(elements = this.elements + other.elements,)

/**
 * Groups all lessons by start time and merges them into a single PeriodItem for each start time.
 * After this operation, every time period only has a single lesson containing all subjects, teachers, rooms and classes.
 */
fun List<Period>.merged(): List<Period> = groupBy { it.startDateTime }
	.map { (_, items) -> items.reduce { acc, item -> acc + item } }

/**
 * Creates a copy of a zipped list with the very last element duplicated into a new Pair whose second element is null.
 * If the list is empty or the last element doesn't have a second value, it is returned as is.
 */
fun <E> List<Pair<E, E?>>.withLast(): List<Pair<E, E?>> =
	if (this.isEmpty() || this.last().second == null) this
	else this.toMutableList().apply { add(Pair(this.last().second!!, null)) }.toList()
