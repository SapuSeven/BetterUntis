package com.sapuseven.untis.models.untis

import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class UntisAuthTest {
	@Test
	fun untisAuth_serialization() {
		assertThat(getJSON().encodeToString<UntisAuth>(UntisAuth(
				user = "user",
				otp = 123456L,
				clientTime = 123456L
		)), `is`("""{"user":"user","otp":123456,"clientTime":123456}"""))
	}

	@Test
	fun untisAuth_deserialization() {
		val auth = getJSON().decodeFromString<UntisAuth>("""{"user":"user","otp":123456,"clientTime":123456}""")

		assertThat(auth.user, `is`("user"))
		assertThat(auth.otp, `is`(123456L))
		assertThat(auth.clientTime, `is`(123456L))
	}
}
