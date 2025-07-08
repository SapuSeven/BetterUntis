package com.sapuseven.untis.exceptions

import com.sapuseven.untis.model.rest.ErrorResponse

class UntisRestApiException(
	val errorCode: String? = null,
	val requestId: String? = null,
	val traceId: String? = null,
	override val message: String? = null
) : Throwable(message) {
	constructor(errorResponse: ErrorResponse) : this(
		errorCode = errorResponse.errorCode?.value,
		requestId = errorResponse.requestId?.toString(),
		traceId = errorResponse.traceId,
		message = errorResponse.errorMessage
	)

	override fun toString(): String {
		return "UntisRestApiException(errorCode=$errorCode, requestId=$requestId, traceId=$traceId, message=$message)"
	}
}
