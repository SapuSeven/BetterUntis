import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.components.ElementPicker
import com.sapuseven.untis.data.timetable.PeriodData.Companion.ELEMENT_NAME_UNKNOWN

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

	val shortName = when (element.type) {
		ElementType.CLASS -> allClassesState.value.get(element.id)?.name
		ElementType.TEACHER -> allTeachersState.value.get(element.id)?.name
		ElementType.SUBJECT -> allSubjectsState.value.get(element.id)?.name
		ElementType.ROOM -> allRoomsState.value.get(element.id)?.name
		else -> null
	} ?: ELEMENT_NAME_UNKNOWN

	val longName = when (element.type) {
		ElementType.CLASS -> allClassesState.value.get(element.id)?.longName
		ElementType.TEACHER -> allTeachersState.value.get(element.id)?.run { "$firstName $lastName" }
		ElementType.SUBJECT -> allSubjectsState.value.get(element.id)?.longName
		ElementType.ROOM -> allRoomsState.value.get(element.id)?.longName
		else -> null
	} ?: ELEMENT_NAME_UNKNOWN

	val isAllowed = when (element.type) {
		ElementType.CLASS -> allClassesState.value.get(element.id)?.displayable
		ElementType.TEACHER -> allTeachersState.value.get(element.id)?.displayAllowed
		ElementType.SUBJECT -> allSubjectsState.value.get(element.id)?.displayAllowed
		ElementType.ROOM -> allRoomsState.value.get(element.id)?.displayAllowed
		else -> null
	} ?: false

	content(shortName, longName, isAllowed)
}
