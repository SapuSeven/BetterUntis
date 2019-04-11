package com.sapuseven.untis.models.untis

import kotlinx.serialization.*
import org.joda.time.LocalDateTime

@Serializable
class UntisDate(
		val date: String
) {
	@Serializer(forClass = UntisDate::class)
	companion object : KSerializer<UntisDate> {
		override fun serialize(output: Encoder, obj: UntisDate) {
			output.encodeString(obj.date)
		}

		override fun deserialize(input: Decoder): UntisDate {
			return UntisDate(input.decodeString())
		}
	}

	override fun toString(): String {
		return date
	}

	fun toLocalDateTime(): LocalDateTime {
		return LocalDateTime(date)
	}
}