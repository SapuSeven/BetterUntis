package com.sapuseven.untis.api.serializer

import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test
import java.time.LocalDateTime

class LocalDateTimeSerializerTest {
	@Test
	fun serialize() {
		Assert.assertEquals("\"2024-05-25T16:05:00Z\"", Json.encodeToString(LocalDateTimeSerializer, LocalDateTime.of(2024, 5, 25, 16, 5)))
	}

	@Test
	fun deserialize() {
		Assert.assertEquals(LocalDateTime.of(2024, 5, 25, 16, 5), Json.decodeFromString(LocalDateTimeSerializer, "\"2024-05-25T16:05Z\""))
	}

	@Test
	fun deserializeEmpty_returnsDefaultValue() {
		Assert.assertEquals(LocalDateTime.of(1970, 1, 1, 0, 0), Json.decodeFromString(LocalDateTimeSerializer, "\"\""))
	}
}
