package com.sapuseven.untis.api.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

object DayOfWeekSerializer : KSerializer<DayOfWeek> {
	override val descriptor: SerialDescriptor =
		PrimitiveSerialDescriptor("java.time.DayOfWeek", PrimitiveKind.STRING)

	override fun serialize(encoder: Encoder, value: DayOfWeek) {
		encoder.encodeString(value.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).uppercase())
	}

	override fun deserialize(decoder: Decoder): DayOfWeek {
		val shortName = decoder.decodeString().uppercase()
		return DayOfWeek.entries.first { it.name.startsWith(shortName, true) }
	}
}
