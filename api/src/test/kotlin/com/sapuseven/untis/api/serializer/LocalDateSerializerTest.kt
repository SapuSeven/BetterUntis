package com.sapuseven.untis.api.serializer

import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class LocalDateSerializerTest {
	@Test
	fun serialize() {
		Assert.assertEquals("\"2024-05-25\"", Json.encodeToString(LocalDateSerializer, LocalDate.of(2024, 5, 25)))
	}

	@Test
	fun deserialize() {
		Assert.assertEquals(LocalDate.of(2024, 5, 25), Json.decodeFromString(LocalDateSerializer, "\"2024-05-25\""))
	}

	@Test
	fun deserializeEmpty_returnsDefaultValue() {
		Assert.assertEquals(LocalDate.of(1970, 1, 1), Json.decodeFromString(LocalDateSerializer, "\"\""))
	}
}
