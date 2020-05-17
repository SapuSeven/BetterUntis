package com.sapuseven.untis.models.untis

import com.sapuseven.untis.helpers.DateTimeUtils
import kotlinx.serialization.*
import org.joda.time.LocalDate
import org.joda.time.LocalTime

@Serializable
class UntisTime(
		val time: String
) {
	@Serializer(forClass = UntisTime::class)
	companion object : KSerializer<UntisTime> {
		override fun serialize(encoder: Encoder, obj: UntisTime) {
			encoder.encodeString(obj.time)
		}

		override fun deserialize(decoder: Decoder): UntisTime {
			return UntisTime(decoder.decodeString())
		}

		fun fromLocalDate(localDate: LocalDate): UntisTime {
			return UntisTime(localDate.toString(DateTimeUtils.tTimeNoSeconds()))
		}
	}

	override fun toString(): String {
		return time
	}

	fun toLocalTime(): LocalTime {
		return DateTimeUtils.tTimeNoSeconds().parseLocalTime(time)
	}
}
