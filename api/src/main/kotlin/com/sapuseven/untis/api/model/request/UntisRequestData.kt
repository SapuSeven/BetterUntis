package com.sapuseven.untis.api.model.request

import com.sapuseven.untis.api.model.request.BaseParams
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class UntisRequestData(
	@EncodeDefault(EncodeDefault.Mode.ALWAYS) var id: String = "untis-mobile-android",
	@EncodeDefault(EncodeDefault.Mode.ALWAYS) var jsonrpc: String = "2.0",
	@EncodeDefault(EncodeDefault.Mode.ALWAYS) var method: String = "",
	@EncodeDefault(EncodeDefault.Mode.ALWAYS) var params: List<BaseParams> = emptyList()
)
