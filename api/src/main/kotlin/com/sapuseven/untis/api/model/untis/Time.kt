package com.sapuseven.untis.api.model.untis

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Serializable(Time.Companion::class)
class Time(
		val time: String
) {
	companion object : KSerializer<Time> {
		val TIME_FORMAT = DateTimeFormatter.ofPattern("'T'HH:mm", Locale.ENGLISH)
		override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("com.sapuseven.untis.api.model.untis.TimeSerializer", PrimitiveKind.STRING)

		override fun serialize(encoder: Encoder, value: Time) {
			encoder.encodeString(value.time)
		}

		override fun deserialize(decoder: Decoder): Time {
			return Time(decoder.decodeString())
		}

		fun fromLocalTime(localTime: LocalTime): Time {
			return Time(localTime.format(TIME_FORMAT))
		}
	}

	override fun toString(): String = time

	fun toLocalTime(): LocalTime = LocalTime.parse(time, TIME_FORMAT)
}
