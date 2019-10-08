package com.sapuseven.untis.models.untis

import com.sapuseven.untis.models.UnknownObject
import kotlinx.serialization.Serializable

@Serializable
data class UserData (
		val elemType: String?, // TODO: Enumerate all possible values
		val elemId: Int,
		val displayName: String,
		val schoolName: String,
		val departmentId: Int, // TODO: Revert back to Int after debugging
		val children: List<UnknownObject>, // TODO: This value type is unknown
		val klassenIds: List<Int>,
		val rights: List<String>
) {
	init {
		children.forEach {
			UnknownObject.validate(mapOf("children" to it))
		}
	}
}
