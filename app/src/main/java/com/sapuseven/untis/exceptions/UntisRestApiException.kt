package com.sapuseven.untis.exceptions

import com.sapuseven.untis.model.rest.ErrorResponse

class UntisRestApiException(
	val errorCode: String? = null,
	val requestId: String? = null,
	val traceId: String? = null,
	val validationErrors: List<Pair<String, String>>? = null,
	override val message: String? = null
) : Throwable(message) {
	constructor(errorResponse: ErrorResponse) : this(
		errorCode = errorResponse.errorCode?.value,
		requestId = errorResponse.requestId,
		traceId = errorResponse.traceId,
		validationErrors = errorResponse.validationErrors?.map { it.path to it.errorMessage },
		message = getBestErrorMessage(errorResponse)
	)

	companion object {
		private fun getBestErrorMessage(response: ErrorResponse): String? =
			response.errorMessage?.takeIf(String::isNotBlank)
				?: response.validationErrors?.takeIf(List<*>::isNotEmpty)?.joinToString("\n") { err ->
					val prefix = err.path.takeIf(String::isNotBlank)?.let { "$it: " } ?: ""
					prefix + err.errorMessage
				}
	}

	override fun toString(): String {
		return "UntisRestApiException(errorCode=$errorCode, requestId=$requestId, traceId=$traceId, validationErrors=$validationErrors, message=$message)"
	}
}
