package com.sapuseven.untis.models

import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.models.untis.response.UserDataResult
import kotlinx.serialization.decodeFromString
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class UntisUserDataResultTest {
	@Test
	fun untisUserData_serialization() {
		/*assertThat(getJSON().encodeToString<UntisUserData(
				masterData = MasterData(10L),
				userData = UserData(
						elemType = "STUDENT",
						elemId = 10,
						displayName = "display name",
						schoolName = "school-name",
						departmentId = 10
				),
				settings = Settings(
						showAbsenceReason = true,
						showAbsenceText = false,
						absenceCheckRequired = true,
						defaultAbsenceReasonId = 10,
						defaultLatenessReasonId = 20,
						defaultAbsenceEndTime = "startTime",
						customAbsenceEndTime = "endTime"
				)
		)), `is`("""{"masterData":{"timeStamp":10},"userData":{"elemType":"STUDENT","elemId":10,"displayName":"display name","schoolName":"school-name","departmentId":10},"settings":{"showAbsenceReason":true,"showAbsenceText":false,"absenceCheckRequired":true,"defaultAbsenceReasonId":10,"defaultLatenessReasonId":20,"defaultAbsenceEndTime":"startTime","customAbsenceEndTime":"endTime"}}"""))

		assertThat(getJSON().encodeToString<UntisUserData(
				masterData = MasterData(),
				userData = UserData(),
				settings = Settings()
		)), `is`("""{"masterData":{"timeStamp":0},"userData":{"elemType":null,"elemId":0,"displayName":"","schoolName":"","departmentId":0},"settings":{"showAbsenceReason":true,"showAbsenceText":true,"absenceCheckRequired":false,"defaultAbsenceReasonId":0,"defaultLatenessReasonId":0,"defaultAbsenceEndTime":null,"customAbsenceEndTime":null}}"""))*/
	}

	@Test
	fun untisUserData_deserialization() {
		val userData = getJSON().decodeFromString<UserDataResult>("""{"masterData":{"timeStamp":10,"absenceReasons":[],"departments":[],"duties":[],"eventReasons":[],"eventReasonGroups":[],"excuseStatuses":[],"holidays":[],"klassen":[],"rooms":[],"subjects":[],"teachers":[],"teachingMethods":[],"schoolyears":[],"timeGrid":{"days":[]}},"userData":{"elemType":"STUDENT","elemId":10,"displayName":"display name","schoolName":"school-name","departmentId":10,"children":[],"klassenIds":[],"rights":[]},"settings":{"showAbsenceReason":true,"showAbsenceText":false,"absenceCheckRequired":true,"defaultAbsenceReasonId":10,"defaultLatenessReasonId":20,"defaultAbsenceEndTime":"startTime","customAbsenceEndTime":"endTime"}}""")

		assertThat(userData.masterData.timeStamp, `is`(10L))

		assertThat(userData.userData.elemType, `is`("STUDENT"))
		assertThat(userData.userData.elemId, `is`(10))
		assertThat(userData.userData.displayName, `is`("display name"))
		assertThat(userData.userData.schoolName, `is`("school-name"))
		assertThat(userData.userData.departmentId, `is`(10))

		assertThat(userData.settings?.showAbsenceReason, `is`(true))
		assertThat(userData.settings?.showAbsenceText, `is`(false))
		assertThat(userData.settings?.absenceCheckRequired, `is`(true))
		assertThat(userData.settings?.defaultAbsenceReasonId, `is`(10))
		assertThat(userData.settings?.defaultLatenessReasonId, `is`(20))
		assertThat(userData.settings?.defaultAbsenceEndTime, `is`("startTime"))
		assertThat(userData.settings?.customAbsenceEndTime, `is`("endTime"))
	}
}
