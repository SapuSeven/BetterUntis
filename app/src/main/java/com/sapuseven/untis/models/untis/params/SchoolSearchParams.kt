package com.sapuseven.untis.models.untis.params

import kotlinx.serialization.Serializable

@Serializable
data class SchoolSearchParams(
		val search: String,
		val schoolid: Int = 0
) : BaseParams()