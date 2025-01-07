package com.sapuseven.untis.api.model.request

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
@ExperimentalSerializationApi
data class SchoolSearchParams(
	@EncodeDefault(EncodeDefault.Mode.NEVER) val search: String? = null,
	@EncodeDefault(EncodeDefault.Mode.NEVER) val schoolid: Long = 0,
	@EncodeDefault(EncodeDefault.Mode.NEVER) val schoolname: String = ""
) : BaseParams()
