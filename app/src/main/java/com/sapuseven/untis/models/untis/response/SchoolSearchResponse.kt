package com.sapuseven.untis.models.untis.response

import com.sapuseven.untis.models.UntisSchoolInfo
import kotlinx.serialization.Serializable

@Serializable
data class SchoolSearchResponse(
		val result: SchoolSearchResult? = null
) : BaseResponse()

@Serializable
data class SchoolSearchResult(
		val size: Int,
		val schools: List<UntisSchoolInfo>
)