package com.sapuseven.untis.data.connectivity

import com.sapuseven.untis.helpers.SerializationUtils
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.jupiter.api.Disabled

class UntisRequestTest {
	@OptIn(ExperimentalSerializationApi::class)
	@Test
	@Disabled
	fun untisRequestData_serialization() {
		val data = UntisRequest.UntisRequestData()
		data.id = "data-id"
		data.jsonrpc = "data-jsonrpc"
		data.method = "data-method"
		//data.params = listOf(SchoolSearchParams("school")) Not compatible with new API
		assertThat(SerializationUtils.getJSON().encodeToString<UntisRequest.UntisRequestData>(data),
				`is`("""{"id":"data-id","jsonrpc":"data-jsonrpc","method":"data-method","params":[{"search":"school"}]}""")) //,"schoolid":0,"schoolname":"" are not encoded as `@EncodeDefault(EncodeDefault.Mode.NEVER)` is set
	}
}
