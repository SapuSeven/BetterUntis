package com.sapuseven.untis.ui.activities.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.sapuseven.untis.R
import com.sapuseven.untis.ui.activities.login.schoolsearch.SchoolSearch
import com.sapuseven.untis.ui.activities.login.schoolsearch.SchoolSearchViewModel
import com.sapuseven.untis.ui.common.AppScaffold
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(
	viewModel: LoginViewModel = hiltViewModel()
) {
	val focusManager = LocalFocusManager.current

	LaunchedEffect(Unit) {
		viewModel.events.collectLatest { event ->
			when (event) {
				LoginEvents.ClearFocus -> {
					focusManager.clearFocus()
				}
			}
		}
	}

	BackHandler(
		enabled = viewModel.searchMode
	) {
		viewModel.goBack()
	}

	AppScaffold(
		topBar = {
			CenterAlignedTopAppBar(
				title = { Text(stringResource(id = R.string.app_name)) },
				actions = {
					IconButton(onClick = { viewModel.onCodeScanClick() }) {
						Icon(
							painter = painterResource(id = R.drawable.login_scan_code),
							contentDescription = stringResource(id = R.string.login_scan_code)
						)
					}
				},
				navigationIcon = {
					if (viewModel.shouldShowBackButton.value) IconButton(onClick = {
						viewModel.goBack()
					}) {
						Icon(
							imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
							contentDescription = stringResource(id = R.string.all_back)
						)
					}
				},
				// TODO: Maybe use this in preferences where the theme can be changed
				colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
					containerColor = Color.Transparent,
					scrolledContainerColor = Color.Transparent
				)
			)
		},
		modifier = Modifier
			.safeDrawingPadding()
	) { innerPadding ->
		val schoolSearchText = viewModel.schoolSearchText.collectAsState("")

		Column(
			modifier = Modifier
				.padding(innerPadding)
				.fillMaxSize()
		) {
			if (!viewModel.searchMode) Column(
				verticalArrangement = Arrangement.Center,
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f)
			) {
				Icon(
					painter = painterResource(id = R.drawable.settings_about_app_icon),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.primary,
					modifier = Modifier
						.width(dimensionResource(id = R.dimen.size_login_icon))
						.height(dimensionResource(id = R.dimen.size_login_icon))
						.align(Alignment.CenterHorizontally)
						.padding(bottom = dimensionResource(id = R.dimen.margin_login_pleaselogin_top))
				)
				Text(
					text = stringResource(id = R.string.login_welcome),
					style = MaterialTheme.typography.headlineLarge,
					textAlign = TextAlign.Center,
					modifier = Modifier.fillMaxWidth()
				)
			} else {
				SchoolSearch(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f),
					searchText = schoolSearchText.value,
					onSchoolSelected = { viewModel.onSchoolSelected(it) }
				)
			}

			Column(
				modifier = Modifier.fillMaxWidth()
			) {
				OutlinedTextField(
					value = schoolSearchText.value,
					onValueChange = { viewModel.updateSchoolSearchText(it) },
					singleLine = true,
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = dimensionResource(id = R.dimen.margin_login_input_horizontal))
						.onFocusChanged { viewModel.onSchoolSearchFocusChanged(it.isFocused) }
						.then(
							if (viewModel.searchMode) Modifier.padding(
								bottom = dimensionResource(
									id = R.dimen.margin_login_input_horizontal
								)
							)
							else Modifier
						),
					label = {
						Text(stringResource(id = R.string.login_search_by_school_name_or_address))
					}
				)
				if (!viewModel.searchMode) Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(
							horizontal = dimensionResource(id = R.dimen.margin_login_input_horizontal),
							vertical = dimensionResource(id = R.dimen.margin_login_input_vertical)
						), horizontalArrangement = Arrangement.SpaceBetween
				) {
					TextButton(onClick = { viewModel.onDemoClick() }) {
						Text(text = stringResource(id = R.string.login_demo))
					}

					TextButton(onClick = { viewModel.onManualDataInputClick() }) {
						Text(text = stringResource(id = R.string.login_manual_data_input))
					}
				}
			}
		}
	}
}
