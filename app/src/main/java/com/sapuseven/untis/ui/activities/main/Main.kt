package com.sapuseven.untis.ui.activities.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sapuseven.untis.modules.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(
	viewModel: MainViewModel = viewModel(),
	themeViewModel: ThemeViewModel = viewModel()
) {
	val themeState by themeViewModel.themeState.collectAsState()

	Column(
		modifier = Modifier.padding(top = 48.dp)
	) {
		Text(text = "Dark mode: ${themeState.isDarkMode}")
		Button(onClick = { themeViewModel.toggleTheme() }) {
			Text(text = "Toggle")
		}
	}
}
