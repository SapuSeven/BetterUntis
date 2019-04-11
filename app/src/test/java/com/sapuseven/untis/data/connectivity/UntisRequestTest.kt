package com.sapuseven.untis.data.connectivity

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.net.URI

class UntisRequestTest {
	@Test
	fun untisRequestQuery_getUri() {
		val query = UntisRequest.UntisRequestQuery()
		query.url = "https://www.example.com"
		assertThat(query.getURI("UTF-8"), `is`(URI("https://www.example.com")))

		query.school = "school"
		assertThat(query.getURI("UTF-8"), `is`(URI("https://www.example.com?school=school")))

		query.data = UntisRequest.UntisRequestData()
		query.data.method = "testMethod"
		assertThat(query.getURI("UTF-8"), `is`(URI("https://www.example.com?school=school&m=testMethod")))

		query.school = ""
		assertThat(query.getURI("UTF-8"), `is`(URI("https://www.example.com?m=testMethod")))
	}
}