package com.sapuseven.untis.data.connectivity

import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class UntisApiConstantsTest {
	@Test
	fun accessTest() {
		assertThat(UntisApiConstants, notNullValue())
		assertThat(UntisApiConstants.DEFAULT_WEBUNTIS_PROTOCOL, notNullValue())
		assertThat(UntisApiConstants.DEFAULT_WEBUNTIS_PATH, notNullValue())
		assertThat(UntisApiConstants.METHOD_GET_USER_DATA, notNullValue())
		assertThat(UntisApiConstants.METHOD_GET_TIMETABLE, notNullValue())
		assertThat(UntisApiConstants.METHOD_SEARCH_SCHOOLS, notNullValue())
		assertThat(UntisApiConstants.METHOD_GET_APP_SHARED_SECRET, notNullValue())
		assertThat(UntisApiConstants.SCHOOL_SEARCH_URL, notNullValue())
	}
}
