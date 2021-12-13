package com.sapuseven.untis.models.untis

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat

// TODO: Change all occurrences of startDate or endDate in string format to this type
@Serializable
class UntisDate(
		val date: String
) {
	@Serializer(forClass = UntisDate::class)
	companion object : KSerializer<UntisDate> {
		override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UntisDate", PrimitiveKind.STRING)

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

	fun toLocalDate(): LocalDate {
		return ISODateTimeFormat.date().parseLocalDate(date)
	}
}
