package com.sapuseven.untis.wear.data

import android.util.Log
import com.sapuseven.untis.data.timetable.PeriodData
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import org.joda.time.DateTime

class TimeGridItem(
        id: Long,
        val startDateTime: DateTime,
        val endDateTime: DateTime,
        contextType: String,
        val periodData: PeriodData
) {

    init {
        periodData.setup()

        val title = periodData.getShortTitle()
        val top = if (contextType == TimetableDatabaseInterface.Type.TEACHER.name) periodData.getShortClasses() else periodData.getShortTeachers()
        val bottom = if (contextType == TimetableDatabaseInterface.Type.ROOM.name) periodData.getShortClasses() else periodData.getShortRooms()

        Log.w("Untis", title + ", " + top+ ", " + bottom)
    }

    /*fun mergeWith(items: MutableList<TimegridItem>): Boolean {
        items.toList().forEachIndexed { i, _ ->
            if (i >= items.size) return@forEachIndexed // Needed because the number of elements can change

            val candidate = items[i]

            if (candidate.startTime.dayOfYear != startTime.dayOfYear) return@forEachIndexed

            if (this.equalsIgnoreTime(candidate)) {
                endTime = candidate.endTime
                periodData.element.endDateTime = candidate.periodData.element.endDateTime
                items.removeAt(i)
                return true
            }
        }
        return false
    }

    fun mergeValuesWith(item: TimegridItem) {
        periodData.apply {
            classes.addAll(item.periodData.classes)
            teachers.addAll(item.periodData.teachers)
            subjects.addAll(item.periodData.subjects)
            rooms.addAll(item.periodData.rooms)
        }
    }

    fun equalsIgnoreTime(secondItem: TimegridItem) = periodData.element.equalsIgnoreTime(secondItem.periodData.element)*/
}
