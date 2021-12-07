package com.sapuseven.untis.models

import com.sapuseven.untis.helpers.ErrorLogger
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder

@Serializable
class UnknownObject(val jsonString: String?) {
	@Serializer(forClass = UnknownObject::class)
	companion object : KSerializer<UnknownObject> {
		override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UnknownObject", PrimitiveKind.STRING)

		override fun serialize(encoder: Encoder, obj: UnknownObject) {}

		override fun deserialize(decoder: Decoder): UnknownObject {
			return UnknownObject((decoder as? JsonDecoder)?.decodeJsonElement().toString())
		}

		fun validate(fields: Map<String, UnknownObject?>) {
			fields.forEach {
				it.value?.let { value ->
					if (value.jsonString?.isNotBlank() == true
							&& value.jsonString.toIntOrNull() != 0
							&& value.jsonString != "\"\""
							&& value.jsonString != "[]"
							&& value.jsonString != "{}")
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
