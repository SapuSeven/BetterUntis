package com.sapuseven.untis.ui.pages.settings.fragments

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.sapuseven.untis.R
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.functional.insetsPaddingValues

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsCategoryAboutLibraries(navController: NavController) {
	AppScaffold(
		topBar = {
			CenterAlignedTopAppBar(
				title = {
					Text(stringResource(id = R.string.preference_info_libraries))
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
		LibrariesContainer(
			Modifier
				.padding(innerPadding)
				.fillMaxSize(),
			contentPadding = insetsPaddingValues(),
		)
	}
}
