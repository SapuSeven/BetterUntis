package com.sapuseven.untis.data.connectivity

import com.sapuseven.untis.helpers.SerializationUtils
import com.sapuseven.untis.models.untis.params.SchoolSearchParams
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

	@Test
	fun untisRequestData_serialization() {
		val data = UntisRequest.UntisRequestData()
		data.id = "data-id"
		data.jsonrpc = "data-jsonrpc"
		data.method = "data-method"
		data.params = listOf(SchoolSearchParams("school"))
		assertThat(SerializationUtils.getJSON().stringify(UntisRequest.UntisRequestData.serializer(), data),
				`is`("""{"id":"data-id","jsonrpc":"data-jsonrpc","method":"data-method","params":[{"search":"school","schoolid":0,"schoolname":""}]}"""))
	}
}