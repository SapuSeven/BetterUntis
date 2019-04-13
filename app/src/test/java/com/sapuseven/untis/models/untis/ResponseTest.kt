package com.sapuseven.untis.models.untis

import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.models.UntisSchoolInfo
import com.sapuseven.untis.models.untis.response.*
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class ResponseTest {
	@Test
	fun baseResponse_serialization() {
		assertThat(getJSON().stringify(BaseResponse.serializer(), BaseResponse()), `is`("""{"id":null,"error":null,"jsonrpc":null}"""))
	}

	@Test
	fun appSharedSecretResponse_serialization() {
		assertThat(getJSON().stringify(AppSharedSecretResponse.serializer(), AppSharedSecretResponse()), `is`("""{"id":null,"error":null,"jsonrpc":null,"result":""}"""))

		assertThat(getJSON().stringify(AppSharedSecretResponse.serializer(), AppSharedSecretResponse(
				result = "key"
		)), `is`("""{"id":null,"error":null,"jsonrpc":null,"result":"key"}"""))
	}

	@Test
	fun schoolSearchResponse_serialization() {
		assertThat(getJSON().stringify(SchoolSearchResponse.serializer(), SchoolSearchResponse()), `is`("""{"id":null,"error":null,"jsonrpc":null,"result":null}"""))

		assertThat(getJSON().stringify(SchoolSearchResponse.serializer(), SchoolSearchResponse(
				result = SchoolSearchResult(
						size = 2,
						schools = listOf(UntisSchoolInfo(
								server = "server 1",
								useMobileServiceUrlAndroid = true,
								useMobileServiceUrlIos = false,
								address = "123",
								displayName = "school display name 1",
								loginName = "LOGIN_NAME",
								schoolId = 123,
								serverUrl = "http://",
								mobileServiceUrl = "http://"
						), UntisSchoolInfo(
								server = "server 2",
								useMobileServiceUrlAndroid = true,
								useMobileServiceUrlIos = false,
								address = "123",
								displayName = "school display name 2",
								loginName = "LOGIN_NAME",
								schoolId = 123,
								serverUrl = "http://",
								mobileServiceUrl = "http://"
						))
				)
		)), `is`("""{"id":null,"error":null,"jsonrpc":null,"result":{"size":2,"schools":[{"server":"server 1","useMobileServiceUrlAndroid":true,"useMobileServiceUrlIos":false,"address":"123","displayName":"school display name 1","loginName":"LOGIN_NAME","schoolId":123,"serverUrl":"http://","mobileServiceUrl":"http://"},{"server":"server 2","useMobileServiceUrlAndroid":true,"useMobileServiceUrlIos":false,"address":"123","displayName":"school display name 2","loginName":"LOGIN_NAME","schoolId":123,"serverUrl":"http://","mobileServiceUrl":"http://"}]}}"""))
	}

	@Test
	fun userDataResponse_serialization() {
		assertThat(getJSON().stringify(UserDataResponse.serializer(), UserDataResponse()), `is`("""{"id":null,"error":null,"jsonrpc":null,"result":null}"""))

		/*assertThat(getJSON().stringify(UserDataResponse(
				result = UntisUserData(
						masterData = MasterData(),
						userData = UserData(),
						settings = Settings()
				)
		)), `is`("""{"id":null,"error":null,"jsonrpc":null,"result":{"masterData":{"timeStamp":0},"userData":{"elemType":null,"elemId":0,"displayName":"","schoolName":"","departmentId":0},"settings":{"showAbsenceReason":true,"showAbsenceText":true,"absenceCheckRequired":false,"defaultAbsenceReasonId":0,"defaultLatenessReasonId":0,"defaultAbsenceEndTime":null,"customAbsenceEndTime":null}}}"""))*/
	}

	@Test
	fun appSharedSecretResponse_deserialization() {
		val appSharedSecretResponse1 = getJSON().parse(AppSharedSecretResponse.serializer(), """{"id":"id","error":{"code":1000,"message":"error message"},"jsonrpc":"2.0"}""")
		assertThat(appSharedSecretResponse1.id, `is`("id"))
		assertThat(appSharedSecretResponse1.jsonrpc, `is`("2.0"))
		assertThat(appSharedSecretResponse1.result, `is`(""))
		assertThat(appSharedSecretResponse1.error!!.code, `is`(1000))
		assertThat(appSharedSecretResponse1.error!!.message, `is`("error message"))
		assertThat(appSharedSecretResponse1.error!!.data, nullValue())

		val appSharedSecretResponse2 = getJSON().parse(AppSharedSecretResponse.serializer(), """{"id":"id","jsonrpc":"2.0","result":"key"}""")
		assertThat(appSharedSecretResponse2.id, `is`("id"))
		assertThat(appSharedSecretResponse2.jsonrpc, `is`("2.0"))
		assertThat(appSharedSecretResponse2.result, `is`("key"))
		assertThat(appSharedSecretResponse2.error, nullValue())
	}

	@Test
	fun schoolSearchResponse_deserialization() {
		val schoolSearchResponse1 = getJSON().parse(SchoolSearchResponse.serializer(), """{"id":"id","error":{"code":1000,"message":"error message"},"jsonrpc":"2.0"}""")
		assertThat(schoolSearchResponse1.id, `is`("id"))
		assertThat(schoolSearchResponse1.jsonrpc, `is`("2.0"))
		assertThat(schoolSearchResponse1.result, nullValue())
		assertThat(schoolSearchResponse1.error!!.code, `is`(1000))
		assertThat(schoolSearchResponse1.error!!.message, `is`("error message"))
		assertThat(schoolSearchResponse1.error!!.data, nullValue())

		val schoolSearchResponse2 = getJSON().parse(SchoolSearchResponse.serializer(), """{"id":"id","jsonrpc":"2.0","result":{"size":2,"schools":[{"server":"server 1","useMobileServiceUrlAndroid":true,"useMobileServiceUrlIos":false,"address":"123","displayName":"school display name 1","loginName":"LOGIN_NAME","schoolId":123,"serverUrl":"http://","mobileServiceUrl":"http://"},{"server":"server 2","useMobileServiceUrlAndroid":true,"useMobileServiceUrlIos":false,"address":"123","displayName":"school display name 2","loginName":"LOGIN_NAME","schoolId":123,"serverUrl":"http://","mobileServiceUrl":"http://"}]}}""")
		assertThat(schoolSearchResponse2.id, `is`("id"))
		assertThat(schoolSearchResponse2.jsonrpc, `is`("2.0"))
		assertThat(schoolSearchResponse2.result!!.size, `is`(2))
		assertThat(schoolSearchResponse2.result!!.schools[0], `is`(UntisSchoolInfo(
				server = "server 1",
				useMobileServiceUrlAndroid = true,
				useMobileServiceUrlIos = false,
				address = "123",
				displayName = "school display name 1",
				loginName = "LOGIN_NAME",
				schoolId = 123,
				serverUrl = "http://",
				mobileServiceUrl = "http://"
		)))
		assertThat(schoolSearchResponse2.result!!.schools[1], `is`(UntisSchoolInfo(
				server = "server 2",
				useMobileServiceUrlAndroid = true,
				useMobileServiceUrlIos = false,
				address = "123",
				displayName = "school display name 2",
				loginName = "LOGIN_NAME",
				schoolId = 123,
				serverUrl = "http://",
				mobileServiceUrl = "http://"
		)))
		assertThat(schoolSearchResponse2.error, nullValue())
	}

	@Test
	fun userDataResponse_deserialization() {
		val userDataResponse1 = getJSON().parse(UserDataResponse.serializer(), """{"id":"id","error":{"code":1000,"message":"error message"},"jsonrpc":"2.0"}""")
		assertThat(userDataResponse1.id, `is`("id"))
		assertThat(userDataResponse1.jsonrpc, `is`("2.0"))
		assertThat(userDataResponse1.result, nullValue())
		assertThat(userDataResponse1.error, notNullValue())
		assertThat(userDataResponse1.error!!.code, `is`(1000))
		assertThat(userDataResponse1.error!!.message, `is`("error message"))
		assertThat(userDataResponse1.error!!.data, nullValue())

		val userDataResponse2 = getJSON().parse(UserDataResponse.serializer(), """{"id":"id","jsonrpc":"2.0","result":{"masterData":{"timeStamp":0},"userData":{"elemType":null,"elemId":0,"displayName":"","schoolName":"","departmentId":0},"settings":{"showAbsenceReason":true,"showAbsenceText":true,"absenceCheckRequired":false,"defaultAbsenceReasonId":0,"defaultLatenessReasonId":0,"defaultAbsenceEndTime":null,"customAbsenceEndTime":null}}}""")
		assertThat(userDataResponse2.id, `is`("id"))
		assertThat(userDataResponse2.jsonrpc, `is`("2.0"))
		assertThat(userDataResponse2.result, notNullValue())
		assertThat(userDataResponse2.error, nullValue())
	}
}