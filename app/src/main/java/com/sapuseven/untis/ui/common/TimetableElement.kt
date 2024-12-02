import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import com.sapuseven.untis.components.ElementPicker
import com.sapuseven.untis.data.timetable.PeriodData.Companion.ELEMENT_NAME_UNKNOWN
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface.Type
import com.sapuseven.untis.models.untis.timetable.PeriodElement

@Composable
fun ElementItem(
	element: PeriodElement,
	elementPicker: ElementPicker,
	content: @Composable (shortName: String, longName: String, isAllowed: Boolean) -> Unit
) {
	val allClassesState = elementPicker.allClasses.observeAsState(emptyMap())
	val allTeachersState = elementPicker.allTeachers.observeAsState(emptyMap())
	val allSubjectsState = elementPicker.allSubjects.observeAsState(emptyMap())
	val allRoomsState = elementPicker.allRooms.observeAsState(emptyMap())

	val shortName = when (Type.valueOf(element.type)) {
		Type.CLASS -> allClassesState.value.get(element.id)?.name
		Type.TEACHER -> allTeachersState.value.get(element.id)?.name
		Type.SUBJECT -> allSubjectsState.value.get(element.id)?.name
		Type.ROOM -> allRoomsState.value.get(element.id)?.name
		else -> null
	} ?: ELEMENT_NAME_UNKNOWN

	val longName = when (Type.valueOf(element.type)) {
		Type.CLASS -> allClassesState.value.get(element.id)?.longName
		Type.TEACHER -> allTeachersState.value.get(element.id)?.run { "$firstName $lastName" }
		Type.SUBJECT -> allSubjectsState.value.get(element.id)?.longName
		Type.ROOM -> allRoomsState.value.get(element.id)?.longName
		else -> null
	} ?: ELEMENT_NAME_UNKNOWN

	val isAllowed = when (Type.valueOf(element.type)) {
		Type.CLASS -> allClassesState.value.get(element.id)?.displayable
		Type.TEACHER -> allTeachersState.value.get(element.id)?.displayAllowed
		Type.SUBJECT -> allSubjectsState.value.get(element.id)?.displayAllowed
		Type.ROOM -> allRoomsState.value.get(element.id)?.displayAllowed
		else -> null
	} ?: false

	content(shortName, longName, isAllowed)
}
