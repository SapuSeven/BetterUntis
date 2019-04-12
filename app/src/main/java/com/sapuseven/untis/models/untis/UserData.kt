package com.sapuseven.untis.models.untis

import kotlinx.serialization.Serializable

@Serializable
data class UserData (
		val elemType: String, // TODO: Enumerate all possible values
		val elemId: Int,
		val displayName: String,
		val schoolName: String,
		val departmentId: Int,
		//val children: List<Any>, // TODO: This value type is unknown
		val klassenIds: List<Int>,
		val rights: List<String>
)