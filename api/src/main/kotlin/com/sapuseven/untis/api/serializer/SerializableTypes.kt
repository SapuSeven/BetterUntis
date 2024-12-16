package com.sapuseven.untis.api.serializer

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

typealias Date = @Serializable(LocalDateSerializer::class) LocalDate

typealias DateTime = @Serializable(LocalDateTimeSerializer::class) LocalDateTime
