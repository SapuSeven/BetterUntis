package com.sapuseven.untis.models

import android.util.Log
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.json.JsonInput

@Serializable
class UnknownObject(val jsonString: String?) {
	@Serializer(forClass = UnknownObject::class)
	companion object : KSerializer<UnknownObject> {
		override val descriptor: SerialDescriptor = StringDescriptor.withName("UnknownObject")

		override fun serialize(encoder: Encoder, obj: UnknownObject) {
			encoder.encodeString("")
		}

		@ImplicitReflectionSerializer
		override fun deserialize(decoder: Decoder): UnknownObject {
			return UnknownObject((decoder as? JsonInput)?.decodeJson()?.toString())
		}

		fun validate(fields: Map<String, UnknownObject?>) {
			fields.forEach {
				it.value?.let { value ->
					if (value.jsonString?.isNotBlank() == true && value.jsonString.toIntOrNull() != 0) {
						Log.w(descriptor.name, "Unknown JSON object ${it.key} encountered: ${value.jsonString}")
					}
				}
			}
		}
	}
}
