package com.sapuseven.untis.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData

class AbsenceCheckViewModel : ViewModel() {
	val absenceList: LiveData<List<String>> = liveData {
		val data = listOf("A", "B", "C") // This would be a suspend fun to fetch the list
		emit(data)
	}
}
