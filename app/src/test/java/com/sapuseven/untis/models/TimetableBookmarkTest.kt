package com.sapuseven.untis.models

import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class TimetableBookmarkTest {
	@Test
	fun timetableBookmark_serialize() {
		val bookmark = TimetableBookmark(
			123,
			"CLASS",
			"Test Class"
		)

		assertThat(
			getJSON().encodeToString<TimetableBookmark>(bookmark),
			`is`("""{"elementId":123,"elementType":"CLASS","displayName":"Test Class"}""")
		)
	}

	@Test
	fun timetableBookmark_deserialize() {
		val bookmark = TimetableBookmark(
			123,
			"CLASS",
			"Test Class"
		)

		assertThat(
			getJSON().decodeFromString<TimetableBookmark>("""{"elementId":123,"elementType":"CLASS","displayName":"Test Class"}"""),
			`is`(bookmark)
		)
	}

	@Test
	fun timetableBookmark_deserializeLegacy() {
		val bookmark = TimetableBookmark(
			123,
			"CLASS",
			"Test Class"
		)

		assertThat(
			getJSON().decodeFromString<TimetableBookmark>("""{"classId":123,"type":"CLASS","displayName":"Test Class","drawableId":123456789}"""),
			`is`(bookmark)
		)
	}
}
