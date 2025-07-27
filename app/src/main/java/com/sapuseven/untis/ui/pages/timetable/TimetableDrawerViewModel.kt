package com.sapuseven.untis.ui.pages.timetable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.activities.main.NavItemNavigation
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.data.repository.MasterDataRepository
import com.sapuseven.untis.data.repository.UserSettingsRepository
import com.sapuseven.untis.data.settings.model.TimetableElement
import com.sapuseven.untis.persistence.entity.ElementEntity
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.navigation.AppRoutes
import com.sapuseven.untis.ui.preferences.toTimetableElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimetableDrawerViewModel @Inject constructor(
	private val userSettingsRepository: UserSettingsRepository,
	private val navigator: AppNavigator,
	private val masterDataRepository: MasterDataRepository,
) : ViewModel() {
	var enableDrawerGestures: Boolean = true
		private set

	var bookmarkDeleteDialog by mutableStateOf<ElementEntity?>(null)
		private set

	private val _elements = combine(
		masterDataRepository.classes,
		masterDataRepository.teachers,
		masterDataRepository.subjects,
		masterDataRepository.rooms
	) { classes, teachers, subjects, rooms ->
		mapOf(
			ElementType.CLASS to classes,
			ElementType.TEACHER to teachers,
			ElementType.SUBJECT to subjects,
			ElementType.ROOM to rooms
		)
	}
	val elements: StateFlow<Map<ElementType, List<ElementEntity>>> = _elements.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = emptyMap()
	)

	private val _bookmarks = MutableStateFlow<List<TimetableElement>>(emptyList())
	val bookmarks: StateFlow<List<TimetableElement>> = _bookmarks

	init {
		viewModelScope.launch {
			userSettingsRepository.getSettings().collect {
				it.themeColor
				_bookmarks.value = it.bookmarksList
			}
		}
	}

	fun onNavigationItemClick(item: NavItemNavigation) {
		navigator.navigate(item.route)
	}

	fun onBookmarkClick(bookmark: ElementEntity) {
		navigator.navigate(AppRoutes.Timetable(bookmark.getType(), bookmark.id))
	}

	fun addBookmark(item: ElementEntity) = viewModelScope.launch {
		userSettingsRepository.updateSettings {
			if (!bookmarksList.any { it.matches(item) }) {
				addBookmarks(item.toTimetableElement())
			}
		}
	}

	fun removeBookmark(bookmark: ElementEntity) = viewModelScope.launch {
		userSettingsRepository.updateSettings {
			removeBookmarks(bookmarksList.indexOfFirst { it.matches(bookmark) })
		}
		dismissBookmarkDeleteDialog()
	}

	fun showBookmarkDeleteDialog(bookmark: ElementEntity) {
		bookmarkDeleteDialog = bookmark
	}

	fun dismissBookmarkDeleteDialog() {
		bookmarkDeleteDialog = null
	}

	fun getBookmarkDisplayName(bookmark: ElementEntity): String {
		return bookmark.getLongName()
	}
}

private fun TimetableElement.matches(bookmark: ElementEntity): Boolean {
	return elementType == bookmark.getType().id && elementId == bookmark.id
}
