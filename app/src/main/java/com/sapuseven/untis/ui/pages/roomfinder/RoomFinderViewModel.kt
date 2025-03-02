package com.sapuseven.untis.ui.pages.roomfinder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.components.ElementPicker
import com.sapuseven.untis.data.database.entities.RoomFinderDao
import com.sapuseven.untis.data.database.entities.RoomFinderEntity
import com.sapuseven.untis.data.database.entities.UserDao
import com.sapuseven.untis.domain.GetRoomFinderItemsUseCase
import com.sapuseven.untis.models.RoomFinderHour
import com.sapuseven.untis.models.RoomFinderItem
import com.sapuseven.untis.scope.UserScopeManager
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.navigation.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class RoomFinderViewModel @Inject constructor(
	private val roomFinderDao: RoomFinderDao,
	private val navigator: AppNavigator,
	clock: Clock,
	getRoomFinderItems: GetRoomFinderItemsUseCase,
	userScopeManager: UserScopeManager,
	userDao: UserDao
) : ViewModel() {
	val user = userScopeManager.user
	val elementPicker = ElementPicker(user, userDao)

	private val currentDateTime = LocalDateTime.now(clock)

	private val _hourList =
		userScopeManager.user.timeGrid.days.flatMap { day -> day.units.map { unit -> RoomFinderHour(day, unit) } }
	val hourList: StateFlow<List<RoomFinderHour>> = flowOf(_hourList)
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = emptyList()
		)

	private val defaultHourIndex = _hourList.indexOfFirst {
		it.timeGridDay.day == currentDateTime.dayOfWeek
			&& currentDateTime.toLocalTime().isBefore(it.timeGridUnit.endTime)
	}.coerceAtLeast(0)

	private val _selectedHourIndex = MutableStateFlow(defaultHourIndex)
	val selectedHourIndex: StateFlow<Int> = _selectedHourIndex

	val roomList: StateFlow<List<RoomFinderItem>> = combine(
		getRoomFinderItems(),
		selectedHourIndex,
		::sortRoomList
	)
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = emptyList()
		)

	private fun sortRoomList(roomList: List<RoomFinderItem>, selectedIndex: Int) = roomList.sortedWith(
		compareByDescending<RoomFinderItem> { it.freeHoursAt(selectedIndex) }
			.thenBy { it.entity.longName }
	)

	fun goBack() {
		navigator.popBackStack()
	}

	fun addRooms(rooms: List<PeriodElement>) = viewModelScope.launch {
		roomFinderDao.insertAll(*rooms.map { RoomFinderEntity(it.id, user.id) }.toTypedArray())
	}

	fun deleteRoom(room: RoomFinderItem) = viewModelScope.launch {
		roomFinderDao.delete(RoomFinderEntity(room.entity.id, user.id))
	}

	fun onRoomClick(room: RoomFinderItem) {
		navigator.navigate(AppRoutes.Timetable(ElementType.ROOM, room.entity.id)) {
			popUpTo(AppRoutes.RoomFinder) { inclusive = true }
		}
	}

	fun selectHour(hourIndex: Int?) = viewModelScope.launch {
		_selectedHourIndex.emit(hourIndex ?: defaultHourIndex)
	}
}
