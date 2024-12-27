package com.sapuseven.untis.models.untis

import com.sapuseven.untis.api.serializer.LocalDateSerializer
import com.sapuseven.untis.api.serializer.LocalDateTimeSerializer
import com.sapuseven.untis.api.serializer.LocalTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Deprecated("Use LocalDate instead", ReplaceWith("LocalDate", "java.time.LocalDate"))
typealias UntisDate = @Serializable(LocalDateSerializer::class) LocalDate

@Deprecated("Use LocalDateTime instead" )
typealias UntisDateTime = @Serializable(LocalDateTimeSerializer::class) LocalDateTime
