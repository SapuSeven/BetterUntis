package com.sapuseven.untis.viewmodels

import androidx.lifecycle.*
import com.github.kittinunf.fuel.core.FuelError
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisAuthentication
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.helpers.DateTimeUtils
import com.sapuseven.untis.helpers.SerializationUtils
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.UntisAbsence
import com.sapuseven.untis.models.untis.UntisTime
import com.sapuseven.untis.models.untis.params.*
import com.sapuseven.untis.models.untis.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString

class PeriodDataViewModel(
		private val user: UserDatabase.User,
		val item: TimegridItem,
		val timetableDatabaseInterface: TimetableDatabaseInterface?
) : ViewModel() {
	private val period = item.periodData.element

	private val apiLiveData: MutableLiveData<PeriodDataResult> = liveData {
		loadPeriodData()?.let { emit(it) } // TODO: Show network error if null
	} as MutableLiveData<PeriodDataResult>

	private val absenceLiveData = MediatorLiveData<Map<UntisStudent, Absence>>()

	private val periodLiveData = MediatorLiveData<UntisPeriodData>()

	private val query = UntisRequest.UntisRequestQuery(user).apply {
		//proxyHost = preferences.defaultPrefs.getString("preference_connectivity_proxy_host", null) // TODO: Implement
	}

	init {
		absenceLiveData.addSource(apiLiveData) { result ->
			result?.let { absenceLiveData.value = absencesFromPeriodData(result.dataByTTId[period.id.toString()], result.referencedStudents) }
		}
		periodLiveData.addSource(apiLiveData) { result ->
			result?.let { periodLiveData.value = result.dataByTTId[period.id.toString()] }
		}
	}

	private suspend fun loadPeriodData(): PeriodDataResult? {
		query.data.method = UntisApiConstants.METHOD_GET_PERIOD_DATA
		query.data.params = listOf(PeriodDataParams(
				listOf(period.id),
				UntisAuthentication.createAuthObject(user)
		))

		val result = UntisRequest().request(query)
		return result.fold({ data ->
			val untisResponse = SerializationUtils.getJSON().decodeFromString<PeriodDataResponse>(data)

			untisResponse.result
		}, { null })
	}

	fun absenceData(): LiveData<Map<UntisStudent, Absence>> = absenceLiveData

	fun periodData(): LiveData<UntisPeriodData> = periodLiveData

	private fun absencesFromPeriodData(periodData: UntisPeriodData?, periodStudents: List<UntisStudent>): Map<UntisStudent, Absence> {
		return periodStudents.associateWith { student ->
			Absence(periodData?.absences?.find { it.studentId == student.id })
		}
	}

	fun createAbsence(student: UntisStudent) = viewModelScope.launch(Dispatchers.IO) {
		val originalValue = absenceLiveData.value
		absenceLiveData.postValue(absenceLiveData.value?.mapValues { if (it.key == student) PendingAbsence() else it.value })

		query.data.method = UntisApiConstants.METHOD_CREATE_IMMEDIATE_ABSENCE
		query.data.params = listOf(CreateImmediateAbsenceParams(
				period.id,
				student.id,
				UntisTime(period.startDateTime.toLocalDateTime().toString(DateTimeUtils.tTimeNoSeconds())),
				UntisTime(period.endDateTime.toLocalDateTime().toString(DateTimeUtils.tTimeNoSeconds())),
				UntisAuthentication.createAuthObject(user)
		))

		UntisRequest().request(query).fold({ data ->
			val untisResponse = SerializationUtils.getJSON().decodeFromString<CreateImmediateAbsenceResponse>(data)

			untisResponse.result?.let { result ->
				absenceLiveData.postValue(absenceLiveData.value?.mapValues { if (it.key == student) Absence(result.absences[0]) else it.value })
			}
		}, {
			absenceLiveData.postValue(originalValue)
			// TODO: Show network error
		})
	}

	fun deleteAbsence(student: UntisStudent, absence: UntisAbsence) = viewModelScope.launch(Dispatchers.IO) {
		val originalValue = absenceLiveData.value
		absenceLiveData.postValue(absenceLiveData.value?.mapValues { if (it.key == student) PendingAbsence() else it.value })

		query.data.method = UntisApiConstants.METHOD_DELETE_ABSENCE
		query.data.params = listOf(DeleteAbsenceParams(
				absence.id,
				UntisAuthentication.createAuthObject(user)
		))

		UntisRequest().request(query).fold({ data ->
			val untisResponse = SerializationUtils.getJSON().decodeFromString<DeleteAbsenceResponse>(data)

			untisResponse.result?.let { result ->
				if (result.success)
					absenceLiveData.postValue(absenceLiveData.value?.mapValues { if (it.key == student) Absence() else it.value })
				else
					absenceLiveData.postValue(originalValue) // TODO: show error
			}
		}, {
			absenceLiveData.postValue(originalValue)
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
			// TODO: Handle Untis API errors (check if success==true)
			periodLiveData.postValue(periodLiveData.value?.copy(absenceChecked = true))
			onSuccess()
		}, {
			onFailure(it)
			// TODO: Show network error
		})
	}

	fun submitLessonTopic(lessonTopic: String, onSuccess: () -> Unit = {}, onFailure: (FuelError) -> Unit = {}) = viewModelScope.launch(Dispatchers.IO) {
		query.data.method = UntisApiConstants.METHOD_SUBMIT_LESSON_TOPIC
		query.data.params = listOf(SubmitLessonTopicParams(
				lessonTopic,
				period.id,
				UntisAuthentication.createAuthObject(user)
		))

		UntisRequest().request(query).fold({
			// TODO: Handle Untis API errors (check if success==true)
			periodLiveData.postValue(periodLiveData.value?.let {
				it.copy(topic = it.topic?.copy(text = lessonTopic))
			})
			onSuccess()
		}, {
			onFailure(it)
			// TODO: Show network error
		})
	}

	class Factory(private val user: UserDatabase.User?, private val item: TimegridItem?, val timetableDatabaseInterface: TimetableDatabaseInterface?) : ViewModelProvider.Factory {
		override fun <T : ViewModel?> create(modelClass: Class<T>): T {
			return modelClass.getConstructor(UserDatabase.User::class.java, TimegridItem::class.java, TimetableDatabaseInterface::class.java)
					.newInstance(user, item, timetableDatabaseInterface)
		}
	}

	open class Absence(
			val untisAbsence: UntisAbsence? = null
	)

	class PendingAbsence : Absence()
}
