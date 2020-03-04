package com.sapuseven.untis.models

import com.sapuseven.untis.helpers.ErrorLogger
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.json.JsonInput

@Serializable
class UnknownObject(val jsonString: String?) {
	@Serializer(forClass = UnknownObject::class)
	companion object : KSerializer<UnknownObject> {
		override val descriptor: SerialDescriptor = StringDescriptor.withName("UnknownObject")

		override fun serialize(encoder: Encoder, obj: UnknownObject) {}

		override fun deserialize(decoder: Decoder): UnknownObject {
			return UnknownObject((decoder as? JsonInput)?.decodeJson()?.toString())
		}

		fun validate(fields: Map<String, UnknownObject?>) {
			fields.forEach {
				it.value?.let { value ->
					if (value.jsonString?.isNotBlank() == true && value.jsonString.toIntOrNull() != 0) {
						ErrorLogger.instance?.log("Unknown JSON object \"${it.key}\" encountered, value: ${value.jsonString}")
					}
				}
			}
		}
	}
}
