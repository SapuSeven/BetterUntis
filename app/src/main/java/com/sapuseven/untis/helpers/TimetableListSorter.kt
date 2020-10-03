package com.sapuseven.untis.helpers

// This code will later be used for the backend once the project is split into modules

/*object TimetableListSorter {
	fun formatItems(items: List<TimegridItem>): List<TimegridItem> {
		return mergeLessons(quickSort(items))
	}

	private fun quickSort(items: List<TimegridItem>): List<TimegridItem> {
		if (items.count() < 2) {
			return items
		}
		val pivot = items[items.count() / 2].startDateTime.millis
		val equal = items.filter { it.startDateTime.millis == pivot }
		val less = items.filter { it.startDateTime.millis < pivot }
		val greater = items.filter { it.startDateTime.millis > pivot }
		return quickSort(less) + equal + quickSort(greater)
	}

	private fun mergeLessons(items: List<TimegridItem>): List<TimegridItem> {
		val mutableList = items.toMutableList()
		for (i in 0 until mutableList.size) {
			if (i + 1 >= mutableList.size) break
			val currentItem = mutableList[i]
			var nextItem: TimegridItem
			for (j in i + 1 until mutableList.size) {
				nextItem = mutableList[j]
				if (
						currentItem.periodData.getShortTitle() == nextItem.periodData.getShortTitle()
						&& currentItem.endDateTime == nextItem.startDateTime
						&& currentItem.periodData.isCancelled() == nextItem.periodData.isCancelled()
				) {
					currentItem.endDateTime = nextItem.endDateTime
					mutableList.removeAt(j)
					break
				}
			}
		}
		return mutableList.toList()
	}
}*/
