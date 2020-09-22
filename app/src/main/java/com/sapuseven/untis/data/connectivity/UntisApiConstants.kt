package com.sapuseven.untis.data.connectivity

object UntisApiConstants {
	const val DEFAULT_WEBUNTIS_PROTOCOL = "https://"
	const val DEFAULT_WEBUNTIS_HOST = "mobile.webuntis.com"
	const val DEFAULT_WEBUNTIS_PATH = "/ms/app/"

	const val METHOD_CREATE_IMMEDIATE_ABSENCE = "createImmediateAbsence2017"
	const val METHOD_DELETE_ABSENCE = "deleteAbsence2017"
	const val METHOD_GET_ABSENCES = "getStudentAbsences2017"
	const val METHOD_GET_APP_SHARED_SECRET = "getAppSharedSecret"
	const val METHOD_GET_EXAMS = "getExams2017"
	const val METHOD_GET_HOMEWORKS = "getHomeWork2017"
	const val METHOD_GET_MESSAGES = "getMessagesOfDay2017"
	const val METHOD_GET_OFFICEHOURS = "getOfficeHours2017"
	const val METHOD_GET_PERIOD_DATA = "getPeriodData2017"
	const val METHOD_GET_TIMETABLE = "getTimetable2017"
	const val METHOD_GET_USER_DATA = "getUserData2017"
	const val METHOD_SEARCH_SCHOOLS = "searchSchool"
	const val METHOD_SUBMIT_ABSENCES_CHECKED = "submitAbsencesChecked2017"
	const val METHOD_GET_LESSON_TOPIC = "getLessonTopic2017"
	const val METHOD_SUBMIT_LESSON_TOPIC = "submitLessonTopic"

	const val CAN_READ_STUDENT_ABSENCE = "READ_STUD_ABSENCE"
	const val CAN_WRITE_STUDENT_ABSENCE = "WRITE_STUD_ABSENCE"
	const val CAN_READ_LESSON_TOPIC = "READ_LESSONTOPIC"
	const val CAN_WRITE_LESSON_TOPIC = "WRITE_LESSONTOPIC"
	const val CAN_READ_HOMEWORK = "READ_HOMEWORK"
	const val CAN_WRITE_HOMEWORK = "WRITE_HOMEWORK"
	const val CAN_READ_CLASSREG_EVENT = "READ_CLASSREGEVENT"
	const val CAN_WRITE_CLASSREG_EVENT = "WRITE_CLASSREGEVENT"
	const val CAN_DELETE_CLASSREG_EVENT = "DELETE_CLASSREGEVENT"
	const val CAN_READ_CLASS_ROLE = "READ_CLASSROLE"
	const val CAN_READ_PERIOD_INFO = "READ_PERIODINFO"
	const val CAN_WRITE_PERIOD_INFO = "WRITE_PERIODINFO"
	const val CAN_ACTION_CHANGE_ROOM = "ACTION_CHANGE_ROOM"

	const val RIGHT_OFFICEHOURS = "R_OFFICEHOURS"
	const val RIGHT_ABSENCES = "R_MY_ABSENCES"
	const val RIGHT_CLASSREGISTER = "CLASSREGISTER"

	const val SCHOOL_SEARCH_URL = "$DEFAULT_WEBUNTIS_PROTOCOL$DEFAULT_WEBUNTIS_HOST/ms/schoolquery2/"
}
