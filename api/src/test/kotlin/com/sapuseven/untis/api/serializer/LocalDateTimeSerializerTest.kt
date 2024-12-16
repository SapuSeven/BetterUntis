package com.sapuseven.untis.api.serializer

import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class LocalDateTimeSerializerTest {
	@Test
	fun serialize() {
		Assert.assertEquals("\"2024-05-25T16:45:03Z\"", Json.encodeToString(LocalDateTimeSerializer, LocalDateTime.of(2024, 5, 25, 16, 45, 3)))
	}

	@Test
	fun deserialize() {
		Assert.assertEquals(LocalDateTime.of(2024, 5, 25, 16, 45, 3), Json.decodeFromString(LocalDateTimeSerializer, "\"2024-05-25T16:45:03Z\""))
	}
}
