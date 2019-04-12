package com.sapuseven.untis.models.untis

import kotlinx.serialization.*
import org.joda.time.LocalDateTime

@Serializable
class UntisDate(
		val date: String
) {
	@Serializer(forClass = UntisDate::class)
	companion object : KSerializer<UntisDate> {
		override fun serialize(encoder: Encoder, obj: UntisDate) {
			encoder.encodeString(obj.date)
		}

		override fun deserialize(decoder: Decoder): UntisDate {
			return UntisDate(decoder.decodeString())
		}
	}

	override fun toString(): String {
		return date
	}

	fun toLocalDateTime(): LocalDateTime {
		return LocalDateTime(date)
	}
}