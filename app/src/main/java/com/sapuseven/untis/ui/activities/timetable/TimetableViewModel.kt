package com.sapuseven.untis.ui.activities.timetable

import androidx.lifecycle.SavedStateHandle
import com.sapuseven.untis.ui.activities.ActivityViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
	savedStateHandle: SavedStateHandle
) : ActivityViewModel() {
	var userId = savedStateHandle.get<Long>("userId")
}
