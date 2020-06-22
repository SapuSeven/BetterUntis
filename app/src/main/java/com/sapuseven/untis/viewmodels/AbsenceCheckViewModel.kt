package com.sapuseven.untis.viewmodels

import androidx.lifecycle.*
import com.github.kittinunf.fuel.core.FuelError
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisAuthentication
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.DateTimeUtils
import com.sapuseven.untis.helpers.SerializationUtils
import com.sapuseven.untis.models.UntisAbsence
import com.sapuseven.untis.models.untis.UntisTime
import com.sapuseven.untis.models.untis.params.AbsencesCheckedParams
import com.sapuseven.untis.models.untis.params.ImmediateAbsenceParams
import com.sapuseven.untis.models.untis.params.PeriodDataParams
import com.sapuseven.untis.models.untis.response.ImmediateAbsenceResponse
import com.sapuseven.untis.models.untis.response.PeriodDataResponse
import com.sapuseven.untis.models.untis.response.UntisStudent
import com.sapuseven.untis.models.untis.timetable.Period
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AbsenceCheckViewModel(private val user: UserDatabase.User, private val period: Period) : ViewModel() {
	private val absenceListLiveData: MutableLiveData<Map<UntisStudent, Absence>> = liveData {
		loadAbsenceList()?.let { emit(it) } // TODO: Show network error if null
	} as MutableLiveData<Map<UntisStudent, Absence>>

	private val query = UntisRequest.UntisRequestQuery(user).apply {
		//proxyHost = preferences.defaultPrefs.getString("preference_connectivity_proxy_host", null) // TODO: Implement
	}

	private suspend fun loadAbsenceList(): Map<UntisStudent, Absence>? {
		query.data.method = UntisApiConstants.METHOD_GET_PERIOD_DATA
		query.data.params = listOf(PeriodDataParams(
				listOf(period.id),
				UntisAuthentication.createAuthObject(user)
		))

		val result = UntisRequest().request(query)
		return result.fold({ data ->
			val untisResponse = SerializationUtils.getJSON().parse(PeriodDataResponse.serializer(), data)

			untisResponse.result?.let { periodData ->
				val absences = periodData.dataByTTId[period.id.toString()]?.absences
				// TODO: Check for absenceChecked
				periodData.referencedStudents.associateWith { student ->
					Absence(absences?.find { it.studentId == student.id })
				}
			}
		}, { null })
	}

	fun absenceList(): LiveData<Map<UntisStudent, Absence>> = absenceListLiveData

	fun createAbsence(student: UntisStudent) = viewModelScope.launch(Dispatchers.IO) {
		val originalValue = absenceListLiveData.value
		absenceListLiveData.postValue(absenceListLiveData.value?.mapValues { if (it.key == student) PendingAbsence() else it.value })

		query.data.method = UntisApiConstants.METHOD_CREATE_IMMEDIATE_ABSENCE
		query.data.params = listOf(ImmediateAbsenceParams(
				period.id,
				student.id,
				UntisTime(period.startDateTime.toLocalDateTime().toString(DateTimeUtils.tTimeNoSeconds())),
				UntisTime(period.endDateTime.toLocalDateTime().toString(DateTimeUtils.tTimeNoSeconds())),
				UntisAuthentication.createAuthObject(user)
		))

		UntisRequest().request(query).fold({ data ->
			val untisResponse = SerializationUtils.getJSON().parse(ImmediateAbsenceResponse.serializer(), data)

			untisResponse.result?.let { result ->
				absenceListLiveData.postValue(absenceListLiveData.value?.mapValues { if (it.key == student) Absence(result.absences[0]) else it.value })
			}
		}, {
			absenceListLiveData.postValue(originalValue)
			// TODO: Show network error
		})
	}

	fun submitAbsencesChecked(onSuccess: () -> Unit, onFailure: (FuelError) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
		query.data.method = UntisApiConstants.METHOD_SUBMIT_ABSENCES_CHECKED
		query.data.params = listOf(AbsencesCheckedParams(
				listOf(period.id),
				UntisAuthentication.createAuthObject(user)
		))

		UntisRequest().request(query).fold({
			onSuccess()
		}, {
			onFailure(it)
			// TODO: Show network error
		})
	}

	class Factory(private val user: UserDatabase.User?, private val period: Period?) : ViewModelProvider.Factory {
		override fun <T : ViewModel?> create(modelClass: Class<T>): T {
			return modelClass.getConstructor(UserDatabase.User::class.java, Period::class.java)
					.newInstance(user, period)
		}
	}

	open class Absence(
			val untisAbsence: UntisAbsence? = null
	)

	class PendingAbsence : Absence()
}
