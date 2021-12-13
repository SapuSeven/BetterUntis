package com.sapuseven.untis.models.untis.params

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
@ExperimentalSerializationApi
data class SchoolSearchParams(
		@EncodeDefault(EncodeDefault.Mode.NEVER) val search: String? = null,
		@EncodeDefault(EncodeDefault.Mode.NEVER) val schoolid: Int = 0,
		@EncodeDefault(EncodeDefault.Mode.NEVER) val schoolname: String = ""
) : BaseParams()
