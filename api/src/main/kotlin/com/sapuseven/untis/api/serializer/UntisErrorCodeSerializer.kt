package com.sapuseven.untis.api.serializer

import com.sapuseven.untis.api.model.response.UntisErrorCode
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object UntisErrorCodeSerializer : KSerializer<UntisErrorCode> {
	override val descriptor: SerialDescriptor =
		PrimitiveSerialDescriptor("UntisErrorCode", PrimitiveKind.INT)

	override fun serialize(encoder: Encoder, value: UntisErrorCode) {
		encoder.encodeInt(value.code)
	}

	override fun deserialize(decoder: Decoder): UntisErrorCode {
		return UntisErrorCode.fromCode(decoder.decodeInt())
	}
}
