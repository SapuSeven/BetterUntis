package com.sapuseven.untis.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisAuthentication
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.SerializationUtils
import com.sapuseven.untis.models.UntisAbsence
import com.sapuseven.untis.models.untis.params.PeriodDataParams
import com.sapuseven.untis.models.untis.response.PeriodDataResponse
import com.sapuseven.untis.models.untis.response.UntisStudent

class AbsenceCheckViewModel(private val user: UserDatabase.User, private val lessonId: Int) : ViewModel() {
	private val absenceListLiveData: LiveData<Map<UntisStudent, UntisAbsence?>> = liveData {
		loadAbsenceList()?.let { emit(it) }
	}

	private suspend fun loadAbsenceList(): Map<UntisStudent, UntisAbsence?>? {
		val query = UntisRequest.UntisRequestQuery(user)

		query.data.method = UntisApiConstants.METHOD_GET_PERIOD_DATA
		//query.proxyHost = preferences.defaultPrefs.getString("preference_connectivity_proxy_host", null) // TODO: Implement
		query.data.params = listOf(PeriodDataParams(
				listOf(lessonId),
				UntisAuthentication.createAuthObject(user)
		))

		val result = UntisRequest().request(query)
		return result.fold({ data ->
			val untisResponse = SerializationUtils.getJSON().parse(PeriodDataResponse.serializer(), data)

			untisResponse.result?.let { periodData ->
				val absences = periodData.dataByTTId[lessonId.toString()]?.absences
				periodData.referencedStudents.associateWith { student ->
					absences?.find { it.studentId == student.id }
				}
			}
		}, { null })
	}

	fun absenceList(): LiveData<Map<UntisStudent, UntisAbsence?>> = absenceListLiveData

	class Factory(private val user: UserDatabase.User?, private val lessonId: Int?) : ViewModelProvider.Factory {
		override fun <T : ViewModel?> create(modelClass: Class<T>): T {
			return modelClass.getConstructor(UserDatabase.User::class.java, Int::class.java)
					.newInstance(user, lessonId)
		}
	}
}
