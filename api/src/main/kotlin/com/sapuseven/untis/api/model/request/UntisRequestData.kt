package com.sapuseven.untis.api.model.request

import com.sapuseven.untis.api.model.request.BaseParams
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class UntisRequestData(
	var id: String = "-1",
	@EncodeDefault(EncodeDefault.Mode.ALWAYS) var jsonrpc: String = "2.0",
	var method: String = "",
	var params: List<BaseParams> = emptyList()
)
