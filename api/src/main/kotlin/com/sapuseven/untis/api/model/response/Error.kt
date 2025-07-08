package com.sapuseven.untis.api.model.response

import com.sapuseven.untis.api.serializer.UntisErrorCodeSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Error(
	val code: UntisErrorCode,
	val message: String? = null,
	val data: UntisErrorData? = null
)

@Serializable
data class UntisErrorData(
	val exceptionTypeName: String? = null,
	val message: String? = null,
	val serverTime: Long? = null
)

@Serializable(with = UntisErrorCodeSerializer::class)
enum class UntisErrorCode(val code: Int) {
	UNKNOWN(0),
	TOO_MANY_SCHOOL_SEARCH_RESULTS(-6003),
	METHOD_NOT_FOUND(-32601),
	NO_SUCCESS(9000),
	INVALID_SCHOOL(-8500),
	NO_SPECIFIED_USER(-8502),
	INVALID_PASSWORD(-8504),
	NO_PERIOD(-8508),
	NO_RIGHT(-8509),
	LOCKED_ACCESS(-8511),
	ABSENCE_INVALID_START_TIME(-8514),
	ABSENCE_INVALID_END_TIME(-8515),
	REQUIRE2_FACTOR_AUTHENTICATION_TOKEN(-8519),
	REQUIRED_AUTHENTICATION(-8520),
	AUTHENTICATION_ERROR(-8521),
	NO_PUBLIC_ACCESS(-8523),
	INVALID_CLIENT_TIME(-8524),
	INVALID_USER_STATUS(-8525),
	INVALID_USER_ROLE(-8526),
	INVALID_TIME_TABLE_TYPE(-7001),
	INVALID_ELEMENT_ID(-7002),
	INVALID_PERSON_TYPE(-7003),
	INVALID_DATE(-7004),
	REQUEST_PASSWORD_RESET_INVALID_CREDENTIALS(-7510),
	REQUEST_PASSWORD_RESET_NO_RESET_ALLOWED(-7511),
	REQUEST_PASSWORD_RESET_EMAIL_COULD_NOT_BE_SEND(-7512),
	ACCESS_DENIED(-42000),
	ACCESS_DENIED_CUSTOM(-42001),
	ACCESS_DENIED_APP(-42002),
	ACCESS_DENIED_SERVER_MAINTENANCE(-42003),
	ROLL_BACK_TO_LEGACY_API(-42100),
	API_KEY_CHANGED_AND_WORKING_OFFLINE(-50001),
	NO_RESULT(10000),
	UNSPECIFIED_ERROR(-8998);

	companion object {
		fun fromCode(code: Int) = entries.find { it.code == code } ?: UNKNOWN
	}
}
