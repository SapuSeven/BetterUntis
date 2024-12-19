package com.sapuseven.untis.api.serializer

import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class LocalTimeSerializerTest {
	@Test
	fun serialize() {
		Assert.assertEquals("\"T16:05\"", Json.encodeToString(LocalTimeSerializer, LocalTime.of(16, 5)))
	}

	@Test
	fun deserialize() {
		Assert.assertEquals(LocalTime.of(16, 5), Json.decodeFromString(LocalTimeSerializer, "\"T16:05\""))
	}
}
