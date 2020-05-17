package com.sapuseven.untis.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface

class TimetableItemDetailsViewModel(val item: TimegridItem?, val timetableDatabaseInterface: TimetableDatabaseInterface?) : ViewModel() {
	class Factory(private val item: TimegridItem?, private val timetableDatabaseInterface: TimetableDatabaseInterface?) : ViewModelProvider.Factory {
		override fun <T : ViewModel?> create(modelClass: Class<T>): T {
			return modelClass.getConstructor(TimegridItem::class.java, TimetableDatabaseInterface::class.java)
					.newInstance(item, timetableDatabaseInterface)
		}
	}
}
