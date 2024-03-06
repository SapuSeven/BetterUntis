package com.sapuseven.untis.api.model.untis

import com.sapuseven.untis.api.model.untis.masterdata.Student
import kotlinx.serialization.Serializable

@Serializable
data class UserData(
	val elemType: String?, // TODO: Enumerate all possible values
	val elemId: Int,
	val displayName: String,
	val schoolName: String,
	val departmentId: Int,
	val children: List<Student?>,
	val klassenIds: List<Int>,
	val rights: List<String>
)
