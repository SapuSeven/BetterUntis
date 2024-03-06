package com.sapuseven.untis.api.model.request

import com.sapuseven.untis.api.model.untis.Auth
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement

@Serializable(with = BaseParamsSerializer::class)
open class BaseParams {
	var auth: Auth? = null;
}

object BaseParamsSerializer : JsonContentPolymorphicSerializer<BaseParams>(BaseParams::class) {
	override fun selectDeserializer(element: JsonElement) = throw Error("Not supported")
}
