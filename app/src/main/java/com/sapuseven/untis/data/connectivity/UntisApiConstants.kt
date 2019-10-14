package com.sapuseven.untis.data.connectivity

object UntisApiConstants {
	const val DEFAULT_WEBUNTIS_PROTOCOL = "https://"
	const val DEFAULT_WEBUNTIS_HOST = "mobile.webuntis.com"
	const val DEFAULT_WEBUNTIS_PATH = "/ms/app/"

	const val METHOD_GET_USER_DATA = "getUserData2017"
	const val METHOD_GET_TIMETABLE = "getTimetable2017"
	const val METHOD_GET_ABSENCES = "getStudentAbsences2017"
	const val METHOD_SEARCH_SCHOOLS = "searchSchool"
	const val METHOD_GET_APP_SHARED_SECRET = "getAppSharedSecret"

	const val SCHOOL_SEARCH_URL = "$DEFAULT_WEBUNTIS_PROTOCOL$DEFAULT_WEBUNTIS_HOST/ms/schoolquery2/"
}
