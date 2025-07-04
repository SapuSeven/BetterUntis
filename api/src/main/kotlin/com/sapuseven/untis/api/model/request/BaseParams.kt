package com.sapuseven.untis.api.model.request

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement

@Serializable(with = BaseParamsSerializer::class)
abstract class BaseParams

object BaseParamsSerializer : JsonContentPolymorphicSerializer<BaseParams>(BaseParams::class) {
	override fun selectDeserializer(element: JsonElement) = throw Error("Not supported")
}
