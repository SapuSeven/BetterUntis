package com.sapuseven.untis.ui.pages.timetable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.activities.main.NavItemNavigation
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.data.repository.MasterDataRepository
import com.sapuseven.untis.data.settings.model.TimetableElement
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.navigation.AppRoutes
import com.sapuseven.untis.ui.pages.settings.UserSettingsRepository
import com.sapuseven.untis.ui.preferences.toTimetableElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimetableDrawerViewModel @Inject constructor(
	userSettingsRepositoryFactory: UserSettingsRepository.Factory,
	private val navigator: AppNavigator,
	private val masterDataRepository: MasterDataRepository,
) : ViewModel() {
	var enableDrawerGestures: Boolean = true
		private set

	var bookmarkDeleteDialog by mutableStateOf<PeriodElement?>(null)
		private set

	private val userSettingsRepository = userSettingsRepositoryFactory.create()

	private val _bookmarks = MutableStateFlow<List<TimetableElement>>(emptyList())
	val bookmarks: StateFlow<List<TimetableElement>> = _bookmarks

	init {
		viewModelScope.launch {
			userSettingsRepository.getSettings().collect {
				_bookmarks.value = it.bookmarksList
			}
		}
	}

	fun onNavigationItemClick(item: NavItemNavigation) {
		navigator.navigate(item.route)
	}

	fun onBookmarkClick(bookmark: PeriodElement) {
		navigator.navigate(AppRoutes.Timetable(bookmark.type, bookmark.id))
	}

	fun addBookmark(item: PeriodElement) = viewModelScope.launch {
		userSettingsRepository.updateSettings {
			if (!bookmarksList.any { it.matches(item) }) {
				addBookmarks(item.toTimetableElement())
			}
		}
	}

	fun removeBookmark(bookmark: PeriodElement) = viewModelScope.launch {
		userSettingsRepository.updateSettings {
			removeBookmarks(bookmarksList.indexOfFirst { it.matches(bookmark) })
		}
		dismissBookmarkDeleteDialog()
	}

	fun showBookmarkDeleteDialog(bookmark: PeriodElement) {
		bookmarkDeleteDialog = bookmark
	}

	fun dismissBookmarkDeleteDialog() {
		bookmarkDeleteDialog = null
	}

	fun getBookmarkDisplayName(bookmark: PeriodElement): String {
		return masterDataRepository.getLongName(bookmark)
	}
}

private fun TimetableElement.matches(bookmark: PeriodElement): Boolean {
	return elementType == bookmark.type.id && elementId == bookmark.id
}
