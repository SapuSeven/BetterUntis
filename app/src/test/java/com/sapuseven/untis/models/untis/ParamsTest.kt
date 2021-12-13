package com.sapuseven.untis.models.untis

import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.models.untis.params.AppSharedSecretParams
import com.sapuseven.untis.models.untis.params.SchoolSearchParams
import com.sapuseven.untis.models.untis.params.UserDataParams
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class ParamsTest {
	@Test
	fun appSharedSecretParams_serialization() {
		assertThat(getJSON().encodeToString<AppSharedSecretParams>(AppSharedSecretParams(
				userName = "user",
				password = "pass"
		)), `is`("""{"userName":"user","password":"pass"}"""))
	}

	@Test
	fun schoolSearchParams_serialization() {
		assertThat(getJSON().encodeToString<SchoolSearchParams>(SchoolSearchParams(
				search = "school",
				schoolid = 123,
				schoolname = "name"
		)), `is`("""{"search":"school","schoolid":123,"schoolname":"name"}"""))

		assertThat(getJSON().encodeToString<SchoolSearchParams>(SchoolSearchParams(
				search = "school"
		)), `is`("""{"search":"school","schoolid":0,"schoolname":""}"""))
	}

	@Test
	fun userDataParams_serialization() {
		assertThat(getJSON().encodeToString<UserDataParams>(UserDataParams(
				auth = UntisAuth(
						user = "user",
						otp = 123456L,
						clientTime = 123456L
				)
		)), `is`("""{"auth":{"user":"user","otp":123456,"clientTime":123456}}"""))
	}

	@Test
	fun appSharedSecretParams_deserialization() {
		val appSharedSecretParams = getJSON().decodeFromString<AppSharedSecretParams>("""{"userName":"user","password":"pass"}""")
		assertThat(appSharedSecretParams.userName, `is`("user"))
		assertThat(appSharedSecretParams.password, `is`("pass"))
	}

	@Test
	fun schoolSearchParams_deserialization() {
		val schoolSearchParams = getJSON().decodeFromString<SchoolSearchParams>("""{"search":"school","schoolid":123}""")
		assertThat(schoolSearchParams.search, `is`("school"))
		assertThat(schoolSearchParams.schoolid, `is`(123))
	}

	@Test
	fun userDataParams_deserialization() {
		val userDataParams = getJSON().decodeFromString<UserDataParams>("""{"auth":{"user":"user","otp":123456,"clientTime":123456}}""")
		assertThat(userDataParams.auth.user, `is`("user"))
		assertThat(userDataParams.auth.otp, `is`(123456L))
		assertThat(userDataParams.auth.clientTime, `is`(123456L))
	}
}
