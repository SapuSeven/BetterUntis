package com.sapuseven.untis.models.untis

import com.sapuseven.untis.helpers.DateTimeUtils
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime

@Serializable
class UntisDateTime(
		val dateTime: String
) {
	@OptIn(ExperimentalSerializationApi::class)
	@Serializer(forClass = UntisDateTime::class)
	companion object : KSerializer<UntisDateTime> {
		override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UntisDateTime", PrimitiveKind.STRING)

		override fun serialize(encoder: Encoder, value: UntisDateTime) {
			encoder.encodeString(value.dateTime)
		}

		override fun deserialize(decoder: Decoder): UntisDateTime {
			return UntisDateTime(decoder.decodeString())
		}
	}

	constructor(localDateTime: LocalDateTime) : this(localDateTime.toString(DateTimeUtils.isoDateTimeNoSeconds()) + "Z") // Not sure why 'Z' isn't added by the format

	override fun toString(): String {
		return dateTime
	}

	fun toLocalDateTime(): LocalDateTime {
		return DateTimeUtils.isoDateTimeNoSeconds().withZone(DateTimeZone.getDefault()).parseLocalDateTime(dateTime)
	}

	fun toDateTime(): DateTime {
		return toLocalDateTime().toDateTime()
	}
}
