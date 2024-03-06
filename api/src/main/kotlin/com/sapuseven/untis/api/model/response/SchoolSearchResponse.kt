package com.sapuseven.untis.api.model.response

import com.sapuseven.untis.api.model.untis.SchoolInfo
import kotlinx.serialization.Serializable

@Serializable
data class SchoolSearchResponse(
		val result: SchoolSearchResult? = null
) : BaseResponse()

@Serializable
data class SchoolSearchResult(
		val size: Int,
		val schools: List<SchoolInfo>
)
