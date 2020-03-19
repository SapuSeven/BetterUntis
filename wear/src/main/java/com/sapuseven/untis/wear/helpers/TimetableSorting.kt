package com.sapuseven.untis.wear.helpers

import com.sapuseven.untis.wear.data.TimeGridItem

object TimetableSorting {

    fun formatItems(items: List<TimeGridItem>): List<TimeGridItem> {
        return mergeLessons(quickSort(items))
    }

    private fun quickSort(items: List<TimeGridItem>): List<TimeGridItem>{
        if (items.count() < 2){
            return items
        }
        val pivot = items[items.count()/2].startDateTime.millis
        val equal = items.filter { it.startDateTime.millis == pivot }
        val less = items.filter { it.startDateTime.millis < pivot }
        val greater = items.filter { it.startDateTime.millis > pivot }
        return quickSort(less) + equal + quickSort(greater)
    }

    private fun mergeLessons(items: List<TimeGridItem>): List<TimeGridItem> {
        val mutableList = items.toMutableList()
        for (i in 0 until mutableList.size) {
            if (i >= mutableList.size) break

            val currentItem = mutableList[i]
            val nextItem = mutableList[i + 1]

            if (currentItem.endDateTime == nextItem.startDateTime) {
                currentItem.endDateTime = nextItem.endDateTime
                mutableList.removeAt(i + 1)
            }
        }
        return mutableList.toList()
    }
}