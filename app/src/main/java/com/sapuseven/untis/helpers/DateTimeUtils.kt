package com.sapuseven.untis.helpers

import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormatterBuilder
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.format.ISODateTimeFormat.date
import java.util.*

object DateTimeUtils {
	fun shortDisplayableTime(): DateTimeFormatter {
		return Constants.sdt
	}

	fun tTimeNoSeconds(): DateTimeFormatter {
		return Constants.ttxx
	}

	fun isoDateTimeNoSeconds(): DateTimeFormatter {
		return Constants.idtxx
	}

	object Constants {
		internal val sdt = DateTimeFormatterBuilder()
				.appendHourOfDay(1)
				.appendLiteral(':')
				.appendMinuteOfHour(2)
				.toFormatter()
				.withOffsetParsed()

		internal val ttxx = DateTimeFormatterBuilder()
				.appendLiteral('T')
				.append(ISODateTimeFormat.hourMinute())
				.toFormatter()
				.withOffsetParsed()

		internal val idtxx = DateTimeFormatterBuilder()
				.append(date())
				.appendLiteral('T')
				.appendHourOfDay(2)
				.appendLiteral(':')
				.appendMinuteOfHour(2)
				.appendTimeZoneOffset("Z", true, 2, 4)
				.toFormatter()
				.withOffsetParsed()
	}

	fun today(): Calendar {
		val today = Calendar.getInstance()
		today.set(Calendar.HOUR_OF_DAY, 0)
		today.set(Calendar.MINUTE, 0)
		today.set(Calendar.SECOND, 0)
		today.set(Calendar.MILLISECOND, 0)
		return today
	}
}
