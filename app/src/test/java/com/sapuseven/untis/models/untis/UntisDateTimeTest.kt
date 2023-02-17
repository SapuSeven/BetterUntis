package com.sapuseven.untis.models.untis

import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.joda.time.LocalDateTime
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UntisDateTimeTest {
	@Test
	fun untisDateTime_serialization() {
		Assertions.assertEquals(
			"\"2023-12-30T13:48Z\"",
			getJSON().encodeToString(UntisDateTime("2023-12-30T13:48Z"))
		)
	}

	@Test
	fun untisDateTime_deserialization() {
		val deserialized = getJSON().decodeFromString(UntisDateTime .serializer(), "\"2023-12-30T13:48Z\"")

		Assertions.assertEquals(
			"2023-12-30T13:48Z",
			deserialized.dateTime
		)
	}

	@Test
	fun untisDateTime_localDateTimeConstructor() {
		Assertions.assertEquals(
			"2023-12-30T13:48Z",
			UntisDateTime(LocalDateTime(2023, 12, 30, 13, 48)).dateTime
		)
	}

	@Test
	fun untisDateTime_toLocalDateTime() {
		Assertions.assertEquals(
			LocalDateTime(2023, 12, 30, 13, 48),
			UntisDateTime("2023-12-30T13:48Z").toLocalDateTime()
		)
	}
}
