package com.sapuseven.untis.models.untis

import kotlinx.serialization.*
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat

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

		fun fromLocalDate(localDate: LocalDate): UntisDate {
			return UntisDate(localDate.toString(ISODateTimeFormat.date()))
		}
	}

	override fun toString(): String {
		return date
	}

	fun toDateTime(): DateTime {
		return DateTime(date)
	}
}
