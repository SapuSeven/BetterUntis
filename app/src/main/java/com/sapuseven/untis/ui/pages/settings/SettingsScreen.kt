package com.sapuseven.untis.ui.pages.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.sapuseven.untis.R
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.functional.bottomInsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
	navController: NavHostController,
	title: String?,
	colorScheme: ColorScheme = MaterialTheme.colorScheme,
	viewModel: SettingsScreenViewModel = hiltViewModel<SettingsScreenViewModel, SettingsScreenViewModel.Factory>(
		creationCallback = { factory -> factory.create(colorScheme) }
	),
	content: @Composable (SettingsScreenViewModel) -> Unit
) {
	AppScaffold(
		topBar = {
			CenterAlignedTopAppBar(
				title = {
					Text(
						title
							?: stringResource(id = R.string.activity_title_settings)
					)
				},
				navigationIcon = {
					IconButton(onClick = { navController.navigateUp() }) {
						Icon(
							imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
							contentDescription = stringResource(id = R.string.all_back)
						)
					}
				}
			)
		}
	) { innerPadding ->
		Column(
			modifier = Modifier
				.verticalScroll(rememberScrollState())
                .padding(innerPadding)
				.bottomInsets()
                .fillMaxSize()
		) {
			content(viewModel)
		}
	}
}
