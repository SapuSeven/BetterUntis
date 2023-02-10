package com.sapuseven.untis.models

import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.Test

class UnknownObjectTest {
	@Test
	fun unknownObject_serializeList_producesJsonNullList() {
		val testList = Array(3) { UnknownObject(null) }.toList()

		assertThat(
			getJSON().encodeToString<List<UnknownObject>>(testList),
			`is`("""[null,null,null]""")
		)
	}
	@Test
	fun unknownObject_deserializeListOfNullValues_producesUnknownObjectList() {
		assertThat(
			getJSON().decodeFromString<List<UnknownObject>>("""[null,null,null]"""),
			allOf(
				hasSize(3),
				everyItem(any(UnknownObject::class.java))
			)
		)
	}
}
