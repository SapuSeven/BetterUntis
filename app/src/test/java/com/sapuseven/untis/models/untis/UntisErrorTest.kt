package com.sapuseven.untis.models.untis

import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class UntisErrorTest {
	@Test
	fun untisError_serialization() {
		assertThat(getJSON().stringify(UntisError.serializer(), UntisError(
				code = 1000,
				message = "error message"
		)), `is`("""{"code":1000,"message":"error message","data":null}"""))

		assertThat(getJSON().stringify(UntisError.serializer(), UntisError(
				code = 1000,
				message = "error message",
				data = UntisErrorData(
						serverTime = 123456L
				)
		)), `is`("""{"code":1000,"message":"error message","data":{"serverTime":123456}}"""))
	}

	@Test
	fun untisError_deserialization() {
		val error1 = getJSON().parse(UntisError.serializer(), """{"code":1000,"message":"error message"}""")

		assertThat(error1.code, `is`(1000))
		assertThat(error1.message, `is`("error message"))
		assertThat(error1.data, nullValue())

		val error2 = getJSON().parse(UntisError.serializer(), """{"code":1000,"message":"error message","data":{"serverTime":123456}}""")

		assertThat(error2.code, `is`(1000))
		assertThat(error2.message, `is`("error message"))
		assertThat(error2.data!!.serverTime, `is`(123456L))
	}
}