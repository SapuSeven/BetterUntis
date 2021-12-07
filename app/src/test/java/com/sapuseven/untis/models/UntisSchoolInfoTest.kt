package com.sapuseven.untis.models

import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class UntisSchoolInfoTest {
	@Test
	fun untisSchoolInfo_serialization() {
		assertThat(getJSON().encodeToString<UntisSchoolInfo>(UntisSchoolInfo(
				server = "server",
				useMobileServiceUrlAndroid = true,
				useMobileServiceUrlIos = false,
				address = "123",
				displayName = "school display name",
				loginName = "LOGIN_NAME",
				schoolId = 123,
				serverUrl = "http://",
				mobileServiceUrl = "http://"
		)), `is`("""{"server":"server","useMobileServiceUrlAndroid":true,"useMobileServiceUrlIos":false,"address":"123","displayName":"school display name","loginName":"LOGIN_NAME","schoolId":123,"serverUrl":"http://","mobileServiceUrl":"http://"}"""))
	}

	@Test
	fun untisSchoolInfo_deserialization() {
		val schoolInfo = getJSON().decodeFromString<UntisSchoolInfo>("""{"server":"server","useMobileServiceUrlAndroid":true,"useMobileServiceUrlIos":false,"address":"123","displayName":"school display name","loginName":"LOGIN_NAME","schoolId":123,"serverUrl":"http://","mobileServiceUrl":"http://"}""")

		assertThat(schoolInfo.server , `is`("server"))
		assertThat(schoolInfo.server, `is`("server"))
		assertThat(schoolInfo.useMobileServiceUrlAndroid, `is`(true))
		assertThat(schoolInfo.useMobileServiceUrlIos, `is`(false))
		assertThat(schoolInfo.address, `is`("123"))
		assertThat(schoolInfo.displayName, `is`("school display name"))
		assertThat(schoolInfo.loginName, `is`("LOGIN_NAME"))
		assertThat(schoolInfo.schoolId, `is`(123))
		assertThat(schoolInfo.serverUrl, `is`("http://"))
		assertThat(schoolInfo.mobileServiceUrl, `is`("http://"))
	}
}
