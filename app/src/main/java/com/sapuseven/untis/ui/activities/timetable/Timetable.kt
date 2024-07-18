package com.sapuseven.untis.ui.activities.timetable

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Timetable(
	viewModel: TimetableViewModel = hiltViewModel()
) {
	Text(text = "${viewModel.userId}")
}
