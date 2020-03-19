package com.sapuseven.untis.wear.helpers

import com.sapuseven.untis.wear.data.TimeGridItem

object TimetableSorting {
    fun formatItems(items: List<TimeGridItem>): List<TimeGridItem> {
        return quickSort(items)
    }

    private fun quickSort(items:List<TimeGridItem>):List<TimeGridItem>{
        if (items.count() < 2){
            return items
        }
        val pivot = items[items.count()/2].startDateTime.millis
        val equal = items.filter { it.startDateTime.millis == pivot }
        val less = items.filter { it.startDateTime.millis < pivot }
        val greater = items.filter { it.startDateTime.millis > pivot }
        return quickSort(less) + equal + quickSort(greater)
    }
}