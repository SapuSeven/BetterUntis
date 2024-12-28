package com.sapuseven.untis.api.serializer

import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class DayOfWeekSerializerTest {
	@Test
	fun serialize() {
		Assert.assertEquals("\"MON\"", Json.encodeToString(DayOfWeekSerializer, DayOfWeek.MONDAY))
		Assert.assertEquals("\"TUE\"", Json.encodeToString(DayOfWeekSerializer, DayOfWeek.TUESDAY))
		Assert.assertEquals("\"WED\"", Json.encodeToString(DayOfWeekSerializer, DayOfWeek.WEDNESDAY))
		Assert.assertEquals("\"THU\"", Json.encodeToString(DayOfWeekSerializer, DayOfWeek.THURSDAY))
		Assert.assertEquals("\"FRI\"", Json.encodeToString(DayOfWeekSerializer, DayOfWeek.FRIDAY))
		Assert.assertEquals("\"SAT\"", Json.encodeToString(DayOfWeekSerializer, DayOfWeek.SATURDAY))
		Assert.assertEquals("\"SUN\"", Json.encodeToString(DayOfWeekSerializer, DayOfWeek.SUNDAY))
	}

	@Test
	fun deserialize() {
		Assert.assertEquals(DayOfWeek.MONDAY, Json.decodeFromString(DayOfWeekSerializer, "\"MON\""))
		Assert.assertEquals(DayOfWeek.TUESDAY, Json.decodeFromString(DayOfWeekSerializer, "\"TUE\""))
		Assert.assertEquals(DayOfWeek.WEDNESDAY, Json.decodeFromString(DayOfWeekSerializer, "\"WED\""))
		Assert.assertEquals(DayOfWeek.THURSDAY, Json.decodeFromString(DayOfWeekSerializer, "\"THU\""))
		Assert.assertEquals(DayOfWeek.FRIDAY, Json.decodeFromString(DayOfWeekSerializer, "\"FRI\""))
		Assert.assertEquals(DayOfWeek.SATURDAY, Json.decodeFromString(DayOfWeekSerializer, "\"SAT\""))
		Assert.assertEquals(DayOfWeek.SUNDAY, Json.decodeFromString(DayOfWeekSerializer, "\"SUN\""))
	}

	/**
	 * Test deserialization with old format (v4.x)
	 */
	@Test
	fun deserializeCompat() {
		Assert.assertEquals(DayOfWeek.MONDAY, Json.decodeFromString(DayOfWeekSerializer, "\"Monday\""))
		Assert.assertEquals(DayOfWeek.TUESDAY, Json.decodeFromString(DayOfWeekSerializer, "\"Tuesday\""))
		Assert.assertEquals(DayOfWeek.WEDNESDAY, Json.decodeFromString(DayOfWeekSerializer, "\"Wednesday\""))
		Assert.assertEquals(DayOfWeek.THURSDAY, Json.decodeFromString(DayOfWeekSerializer, "\"Thursday\""))
		Assert.assertEquals(DayOfWeek.FRIDAY, Json.decodeFromString(DayOfWeekSerializer, "\"Friday\""))
		Assert.assertEquals(DayOfWeek.SATURDAY, Json.decodeFromString(DayOfWeekSerializer, "\"Saturday\""))
		Assert.assertEquals(DayOfWeek.SUNDAY, Json.decodeFromString(DayOfWeekSerializer, "\"Sunday\""))
	}
}
