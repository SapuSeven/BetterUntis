package com.sapuseven.untis.models.untis

import com.sapuseven.untis.helpers.DateTimeUtils
import kotlinx.serialization.*
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

@Serializable
class UntisDateTime(
		val dateTime: String
) {
	@Serializer(forClass = UntisDateTime::class)
	companion object : KSerializer<UntisDateTime> {
		override fun serialize(encoder: Encoder, obj: UntisDateTime) {
			encoder.encodeString(obj.dateTime)
		}

		override fun deserialize(decoder: Decoder): UntisDateTime {
			return UntisDateTime(decoder.decodeString())
		}

		fun fromLocalDate(localDate: LocalDate): UntisDateTime {
			return UntisDateTime(localDate.toString(DateTimeUtils.isoDateTimeNoSeconds()))
		}
	}

	override fun toString(): String {
		return dateTime
	}

	fun toLocalDateTime(): LocalDateTime {
		return DateTimeUtils.isoDateTimeNoSeconds().parseLocalDateTime(dateTime)
	}

	fun toDateTime(): DateTime {
		return toLocalDateTime().toDateTime()
	}
}
