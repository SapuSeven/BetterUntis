package com.sapuseven.untis.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData

class AbsenceCheckViewModel(val test: String) : ViewModel() {
	private val absenceListLiveData: LiveData<List<String>> = liveData {
		val data = loadAbsenceList()
		emit(data)
	}

	private suspend fun loadAbsenceList(): List<String> {
		return listOf("A", "B", "C", test)
	}

	fun absenceList(): LiveData<List<String>> = absenceListLiveData

	class Factory(val test: String) : ViewModelProvider.Factory {
		override fun <T : ViewModel?> create(modelClass: Class<T>): T {
			return modelClass.getConstructor(String::class.java).newInstance(test)
		}
	}
}
