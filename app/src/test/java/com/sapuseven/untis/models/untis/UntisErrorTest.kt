package com.sapuseven.untis.models.untis

import kotlinx.serialization.json.JSON
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test

class UntisErrorTest {
	@Test
	fun untisError_serialization() {
		MatcherAssert.assertThat(getJSON().stringify(UntisError.serializer(), UntisError(
				code = 1000,
				message = "error message"
		)), CoreMatchers.`is`("""{"code":1000,"message":"error message","data":null}"""))

		MatcherAssert.assertThat(getJSON().stringify(UntisError.serializer(), UntisError(
				code = 1000,
				message = "error message",
				data = UntisErrorData(
						serverTime = 123456L
				)
		)), CoreMatchers.`is`("""{"code":1000,"message":"error message","data":{"serverTime":123456}}"""))
	}

	@Test
	fun untisError_deserialization() {
		val error1 = getJSON().parse(UntisError.serializer(), """{"code":1000,"message":"error message"}""")

		MatcherAssert.assertThat(error1.code, CoreMatchers.`is`(1000))
		MatcherAssert.assertThat(error1.message, CoreMatchers.`is`("error message"))
		MatcherAssert.assertThat(error1.data, CoreMatchers.nullValue())

		val error2 = getJSON().parse(UntisError.serializer(), """{"code":1000,"message":"error message","data":{"serverTime":123456}}""")

		MatcherAssert.assertThat(error2.code, CoreMatchers.`is`(1000))
		MatcherAssert.assertThat(error2.message, CoreMatchers.`is`("error message"))
		MatcherAssert.assertThat(error2.data!!.serverTime, CoreMatchers.`is`(123456L))
	}
}