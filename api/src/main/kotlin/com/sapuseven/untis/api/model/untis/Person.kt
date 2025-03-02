package com.sapuseven.untis.api.model.untis

import kotlinx.serialization.Serializable

@Serializable
data class Person(
	val id: Long,
	val firstName: String,
	val lastName: String
) {
	fun fullName(): String = "$firstName $lastName"
}
