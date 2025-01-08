package com.sapuseven.untis.models

import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder

@Serializable(UnknownObject.Companion::class)
class UnknownObject(val jsonString: String?) {
	@OptIn(ExperimentalSerializationApi::class)
	companion object : KSerializer<UnknownObject> {
		override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UnknownObject", PrimitiveKind.STRING)

		@OptIn(ExperimentalSerializationApi::class)
		override fun serialize(encoder: Encoder, value: UnknownObject) {
			encoder.encodeNull()
		}

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
						&& value.jsonString != "{}"
						&& value.jsonString != "null"
					)
						Sentry.captureMessage(
							"Unknown JSON object \"${it.key}\" encountered, value: ${value.jsonString}",
							SentryLevel.DEBUG
						)
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
