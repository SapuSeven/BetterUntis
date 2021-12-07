package com.sapuseven.untis.data.connectivity

import com.sapuseven.untis.helpers.SerializationUtils
import com.sapuseven.untis.models.untis.params.SchoolSearchParams
import kotlinx.serialization.encodeToString
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class UntisRequestTest {
	@Test
	fun untisRequestData_serialization() {
		val data = UntisRequest.UntisRequestData()
		data.id = "data-id"
		data.jsonrpc = "data-jsonrpc"
		data.method = "data-method"
		data.params = listOf(SchoolSearchParams("school"))
		assertThat(SerializationUtils.getJSON().encodeToString<UntisRequest.UntisRequestData>(data),
				`is`("""{"id":"data-id","jsonrpc":"data-jsonrpc","method":"data-method","params":[{"search":"school","schoolid":0,"schoolname":""}]}"""))
	}
}
