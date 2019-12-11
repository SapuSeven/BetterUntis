package com.sapuseven.untis.data.connectivity

import com.sapuseven.untis.models.untis.UntisAuth
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTimeUtils
import org.junit.After
import org.junit.Before
import org.junit.Test

private const val TIME_MILLIS = 1537524711916L

class UntisAuthenticationTest {
	@Before
	@Throws(Exception::class)
	fun before() {
		DateTimeUtils.setCurrentMillisFixed(TIME_MILLIS)
	}

	@After
	@Throws(Exception::class)
	fun after() {
		DateTimeUtils.setCurrentMillisSystem()
	}

	@Test
	fun getAuthObject_returnsCorrect() {
		assertThat(UntisAuthentication.createAuthObject("user", "ABCDEFGHIJKLMNOP"), `is`(UntisAuth(
				user = "user",
				otp = 439266,
				clientTime = TIME_MILLIS
		)))
	}

	@Test
	fun getAuthObject_nullValueForKey() {
		assertThat(UntisAuthentication.createAuthObject("user", null), `is`(UntisAuth(
				user = "user",
				otp = 0,
				clientTime = TIME_MILLIS
		)))
	}

	@Test
	fun getAnonymousAuthObject_returnsCorrect() {
		assertThat(UntisAuthentication.createAuthObject(), `is`(UntisAuth(
				user = "#anonymous#",
				otp = 0,
				clientTime = TIME_MILLIS
		)))
	}
}