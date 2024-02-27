package com.sapuseven.untis.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.sapuseven.untis.R
import com.sapuseven.untis.activities.LoginDataInputActivity.Companion.EXTRA_BOOLEAN_DEMO_LOGIN
import com.sapuseven.untis.api.client.SchoolSearchApi
import com.sapuseven.untis.api.model.SchoolInfo
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.helpers.ErrorMessageDictionary
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.api.model.request.SchoolSearchParams
import com.sapuseven.untis.api.model.response.SchoolSearchResponse
import com.sapuseven.untis.services.CodeScanService
import com.sapuseven.untis.services.CodeScanServiceImpl
import com.sapuseven.untis.ui.common.AppScaffold
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString


class LoginActivity : BaseComposeActivity() {
	companion object {
		const val EXTRA_BOOLEAN_SHOW_BACK_BUTTON =
			"com.sapuseven.untis.activities.login.showBackButton"
	}

	private val loginLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
			if (it.resultCode == Activity.RESULT_OK) {
				setResult(Activity.RESULT_OK, it.data)
				finish()
			}
		}

	private lateinit var codeScanService: CodeScanService

	@OptIn(ExperimentalMaterial3Api::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		codeScanService = CodeScanServiceImpl(this, activityResultRegistry)
		lifecycle.addObserver(codeScanService as CodeScanServiceImpl)

		setContent {
			AppTheme {
				var searchText by rememberSaveable { mutableStateOf("") }
				var searchMode by rememberSaveable { mutableStateOf(false) }

				val focusManager = LocalFocusManager.current

				val showBackButton =
					searchMode || intent.getBooleanExtra(EXTRA_BOOLEAN_SHOW_BACK_BUTTON, false)

				BackHandler(
					enabled = searchMode
				) {
					focusManager.clearFocus()
					searchText = ""
					searchMode = false
				}

				AppScaffold(topBar = {
					CenterAlignedTopAppBar(title = { Text(stringResource(id = R.string.app_name)) },
						actions = {
							IconButton(onClick = {
								codeScanService.scanCode {
									loginLauncher.launch(
										Intent(
											this@LoginActivity,
											LoginDataInputActivity::class.java
										).apply {
											data = it
										})
								}
							}) {
								Icon(
									painter = painterResource(id = R.drawable.login_scan_code),
									contentDescription = stringResource(id = R.string.login_scan_code)
								)
							}
						},
						navigationIcon = {
							if (showBackButton) IconButton(onClick = {
								if (searchMode) {
									focusManager.clearFocus()
									searchText = ""
									searchMode = false
								} else {
									onBackPressedDispatcher.onBackPressed()
								}
							}) {
								Icon(
									imageVector = Icons.Outlined.ArrowBack,
									contentDescription = stringResource(id = R.string.all_back)
								)
							}
						})
				}) { innerPadding ->
					Column(
						modifier = Modifier
							.padding(innerPadding)
							.fillMaxSize()
					) {
						if (!searchMode) Column(
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
						}
						else SchoolSearch(
							modifier = Modifier
								.fillMaxWidth()
								.weight(1f), searchText = searchText
						)
						Column(
							modifier = Modifier.fillMaxWidth()
						) {
							OutlinedTextField(value = searchText,
								onValueChange = { searchText = it },
								singleLine = true,
								modifier = Modifier
									.fillMaxWidth()
									.padding(horizontal = dimensionResource(id = R.dimen.margin_login_input_horizontal))
									.onFocusChanged { if (it.isFocused) searchMode = true }
									.then(
										if (searchMode) Modifier.padding(
											bottom = dimensionResource(
												id = R.dimen.margin_login_input_horizontal
											)
										)
										else Modifier
									),
								label = {
									Text(stringResource(id = R.string.login_search_by_school_name_or_address))
								})
							if (!searchMode) Row(
								modifier = Modifier
									.fillMaxWidth()
									.padding(
										horizontal = dimensionResource(id = R.dimen.margin_login_input_horizontal),
										vertical = dimensionResource(id = R.dimen.margin_login_input_vertical)
									), horizontalArrangement = Arrangement.SpaceBetween
							) {
								TextButton(onClick = {
									loginLauncher.launch(Intent(
										this@LoginActivity,
										LoginDataInputActivity::class.java
									).apply {
										putBackgroundColorExtra(this)
										putExtra(EXTRA_BOOLEAN_DEMO_LOGIN, true)
									})
								}) {
									Text(text = stringResource(id = R.string.login_demo))
								}

								TextButton(onClick = {
									loginLauncher.launch(Intent(
										this@LoginActivity,
										LoginDataInputActivity::class.java
									).apply {
										putBackgroundColorExtra(this)
									})
								}) {
									Text(text = stringResource(id = R.string.login_manual_data_input))
								}
							}
						}
					}
				}
			}
		}
	}

	@OptIn(ExperimentalSerializationApi::class, ExperimentalMaterial3Api::class)
	@Composable
	fun SchoolSearch(
		modifier: Modifier, searchText: String, debounceMillis: Long = 500
	) {

		var items by remember { mutableStateOf(emptyList<SchoolInfo>()) }
		var loading by remember { mutableStateOf(false) }
		var error by remember { mutableStateOf<String?>(null) }
		val composableScope = rememberCoroutineScope()
		var searchJob by remember { mutableStateOf<Job?>(null) }
		val schoolSearchApi = remember { SchoolSearchApi(CIO) }

		DisposableEffect(searchText) {
			error = null
			items = emptyList()

			searchJob?.cancel()
			searchJob = composableScope.launch {
				if (searchText.isEmpty()) return@launch

				loading = true
				delay(debounceMillis)
				val schoolSearchResult = schoolSearchApi.searchSchools(searchText)
				loading = false

				schoolSearchResult.fold({
					items = it.schools
				}, {
					error = ErrorMessageDictionary.getErrorMessage(resources, it.code, it.message.orEmpty())
				})
			}

			onDispose {
				searchJob?.cancel()
			}
		}

		if (items.isNotEmpty()) LazyColumn(modifier) {
			items(items) {
				ListItem(
					headlineContent = { Text(it.displayName) },
					supportingContent = { Text(it.address) },
					modifier = Modifier.clickable {
						val builder = Uri.Builder()
							.appendQueryParameter("schoolInfo", getJSON().encodeToString(it))

						loginLauncher.launch(Intent(
							this@LoginActivity, LoginDataInputActivity::class.java
						).apply {
							data = builder.build()
						})
					})
			}
		}
		else Column(
			verticalArrangement = Arrangement.Center,
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = modifier
		) {
			if (loading) CircularProgressIndicator()
			else if (!error.isNullOrEmpty()) Text(text = error!!)
			else if (items.isEmpty()) Text(text = stringResource(id = R.string.login_no_results))
		}
	}
}
