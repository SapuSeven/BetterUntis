package com.sapuseven.untis.models.untis.params

import kotlinx.serialization.*

@Serializable(with = PlainObjectSerializer::class)
open class BaseParams

@Serializer(forClass = BaseParams::class)
object PlainObjectSerializer : KSerializer<Any> {
	@ImplicitReflectionSerializer
	override fun serialize(output: Encoder, obj: Any) {
		val saver = serializerByValue(obj, output.context) // TODO: remove reflection if possible
		output.encodeSerializableValue(saver, obj)
	}
}