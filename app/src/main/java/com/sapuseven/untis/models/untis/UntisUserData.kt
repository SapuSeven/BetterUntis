package com.sapuseven.untis.models.untis

import com.sapuseven.untis.models.untis.response.UntisStudent
import kotlinx.serialization.Serializable

@Serializable
data class UntisUserData(
		val elemType: String?, // TODO: Enumerate all possible values
		val elemId: Int,
		val displayName: String,
		val schoolName: String,
		val departmentId: Int,
		val children: List<UntisStudent?>,
		val klassenIds: List<Int>,
		val rights: List<String>
)
