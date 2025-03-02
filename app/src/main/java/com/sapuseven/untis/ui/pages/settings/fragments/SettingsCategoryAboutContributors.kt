package com.sapuseven.untis.ui.pages.settings.fragments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sapuseven.untis.R
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.common.MessageBubble
import com.sapuseven.untis.ui.functional.insetsPaddingValues
import com.sapuseven.untis.ui.pages.settings.Contributor
import com.sapuseven.untis.ui.pages.settings.SettingsScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsCategoryAboutContributors(navController: NavController) {
	val uriHandler = LocalUriHandler.current
	val colorScheme = MaterialTheme.colorScheme
	val viewModel =
		hiltViewModel<SettingsScreenViewModel, SettingsScreenViewModel.Factory>(creationCallback = { factory ->
			factory.create(colorScheme)
		})

	val contributors by viewModel.contributors.collectAsStateWithLifecycle()
	val contributorsError by viewModel.contributorsError.collectAsStateWithLifecycle()

	AppScaffold(
		topBar = {
			CenterAlignedTopAppBar(
				title = {
					Text(stringResource(id = R.string.preference_info_contributors))
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
		Box(
			modifier = Modifier
				.padding(innerPadding)
		) {
			AnimatedVisibility(
				contributorsError == null,
				enter = fadeIn(),
				exit = fadeOut()
			) {
				LazyColumn(
					modifier = Modifier.fillMaxSize(),
					contentPadding = insetsPaddingValues()
				) {
					items(if (contributors.isEmpty()) 20 else contributors.size) {
						val user = contributors.getOrNull(it)
						Contributor(
							githubUser = user,
							onClick = user?.htmlUrl?.let { { uriHandler.openUri(it) } }
						)
					}
				}
			}

			AnimatedVisibility(
				contributorsError != null,
				enter = fadeIn() + expandVertically(),
				exit = shrinkVertically() + fadeOut()
			) {
				MessageBubble(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 16.dp, vertical = 8.dp),
					icon = {
						Icon(
							painter = painterResource(id = R.drawable.all_error),
							contentDescription = stringResource(id = R.string.all_error)
						)
					},
					messageText = R.string.preference_info_contributors_error,
					messageTextRaw = contributorsError?.localizedMessage
				)
			}
		}
	}

	LaunchedEffect(Unit) {
		viewModel.loadContributors()
	}
}
