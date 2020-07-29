package com.sapuseven.untis.models

import com.sapuseven.untis.helpers.ErrorLogger
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.json.JsonInput

@Serializable
class UnknownObject(val jsonString: String?) {
	@Serializer(forClass = UnknownObject::class)
	companion object : KSerializer<UnknownObject> {
		override val descriptor: SerialDescriptor = PrimitiveDescriptor("UnknownObject", PrimitiveKind.STRING)

		override fun serialize(encoder: Encoder, obj: UnknownObject) {}

		override fun deserialize(decoder: Decoder): UnknownObject {
			return UnknownObject((decoder as? JsonInput)?.decodeJson()?.toString())
		}

		fun validate(fields: Map<String, UnknownObject?>) {
			fields.forEach {
				it.value?.let { value ->
					if (value.jsonString?.isNotBlank() == true && value.jsonString.toIntOrNull() != 0 && value.jsonString != "[]")
						ErrorLogger.instance?.log("Unknown JSON object \"${it.key}\" encountered, value: ${value.jsonString}")
				}
			}
		}
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as UnknownObject

		if (jsonString != other.jsonString) return false

		return true
	}

	override fun hashCode(): Int {
		return jsonString?.hashCode() ?: 0
	}
}
