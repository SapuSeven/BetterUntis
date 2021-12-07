package com.sapuseven.untis.models.untis.params

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement

@Serializable(with = BaseParamsSerializer::class)
open class BaseParams

object BaseParamsSerializer : JsonContentPolymorphicSerializer<BaseParams>(BaseParams::class) {
	override fun selectDeserializer(element: JsonElement) = throw Error("Not supported")
}
