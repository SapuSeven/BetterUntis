package com.sapuseven.untis.api.serializer

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

internal typealias Date = @Serializable(LocalDateSerializer::class) LocalDate

internal typealias DateTime = @Serializable(LocalDateTimeSerializer::class) LocalDateTime

internal typealias Time = @Serializable(LocalTimeSerializer::class) LocalTime
