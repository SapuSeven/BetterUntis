package com.sapuseven.untis.models.untis

import com.sapuseven.untis.models.untis.params.AppSharedSecretParams
import com.sapuseven.untis.models.untis.params.PlainObjectSerializer
import com.sapuseven.untis.models.untis.params.SchoolSearchParams
import com.sapuseven.untis.models.untis.params.UserDataParams
import kotlinx.serialization.json.JSON
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class ParamsTest {
	@Test
	fun appSharedSecretParams_serialization() {
		assertThat(getJSON().stringify(PlainObjectSerializer, AppSharedSecretParams(
				userName = "user",
				password = "pass"
		)), `is`("""{"userName":"user","password":"pass"}"""))

		assertThat(getJSON().stringify(AppSharedSecretParams.serializer(), AppSharedSecretParams(
				userName = "user",
				password = "pass"
		)), `is`("""{"userName":"user","password":"pass"}"""))
	}

	@Test
	fun schoolSearchParams_serialization() {
		assertThat(getJSON().stringify(SchoolSearchParams.serializer(), SchoolSearchParams(
				search = "school",
				schoolid = 123
		)), `is`("""{"search":"school","schoolid":123}"""))

		assertThat(getJSON().stringify(SchoolSearchParams.serializer(), SchoolSearchParams(
				search = "school"
		)), `is`("""{"search":"school","schoolid":0}"""))
	}

	@Test
	fun userDataParams_serialization() {
		assertThat(getJSON().stringify(UserDataParams.serializer(), UserDataParams(
				auth = UntisAuth(
						user = "user",
						otp = 123456L,
						clientTime = 123456L
				)
		)), `is`("""{"auth":{"user":"user","otp":123456,"clientTime":123456}}"""))
	}

	@Test
	fun appSharedSecretParams_deserialization() {
		val appSharedSecretParams = getJSON().parse(AppSharedSecretParams.serializer(), """{"userName":"user","password":"pass"}""")
		assertThat(appSharedSecretParams.userName, `is`("user"))
		assertThat(appSharedSecretParams.password, `is`("pass"))
	}

	@Test
	fun schoolSearchParams_deserialization() {
		val schoolSearchParams = getJSON().parse(SchoolSearchParams.serializer(), """{"search":"school","schoolid":123}""")
		assertThat(schoolSearchParams.search, `is`("school"))
		assertThat(schoolSearchParams.schoolid, `is`(123))
	}

	@Test
	fun userDataParams_deserialization() {
		val userDataParams = getJSON().parse(UserDataParams.serializer(), """{"auth":{"user":"user","otp":123456,"clientTime":123456}}""")
		assertThat(userDataParams.auth.user, `is`("user"))
		assertThat(userDataParams.auth.otp, `is`(123456L))
		assertThat(userDataParams.auth.clientTime, `is`(123456L))
	}
}