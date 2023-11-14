package com.sapuseven.untis.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import com.sapuseven.untis.R
import com.sapuseven.untis.activities.LoginDataInputActivity.Companion.EXTRA_BOOLEAN_DEMO_LOGIN
import com.sapuseven.untis.activities.ScanCodeActivity.Companion.EXTRA_STRING_SCAN_RESULT
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisApiConstants.SCHOOL_SEARCH_URL
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.helpers.ErrorMessageDictionary
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.models.UntisSchoolInfo
import com.sapuseven.untis.models.untis.params.SchoolSearchParams
import com.sapuseven.untis.models.untis.response.SchoolSearchResponse
import com.sapuseven.untis.ui.common.AppScaffold
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString

class LoginActivity : BaseComposeActivity() {
	private val requestPermissionLauncher =
		registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
			if (isGranted)
				scanCodeLauncher.launch(Intent(this, ScanCodeActivity::class.java))
		}

	private val scanCodeLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
			if (it.resultCode == Activity.RESULT_OK) {
				loginLauncher.launch(Intent(this, LoginDataInputActivity::class.java).apply {
					it.data?.let { scanResult ->
						data = Uri.parse(scanResult.getStringExtra(EXTRA_STRING_SCAN_RESULT))
					}
				})
			}
		}

	private val loginLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
			if (it.resultCode == Activity.RESULT_OK) {
				setResult(Activity.RESULT_OK, it.data)
				finish()
			}
		}

	@OptIn(ExperimentalMaterial3Api::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			AppTheme {
				var searchText by rememberSaveable { mutableStateOf("") }
				var searchMode by rememberSaveable { mutableStateOf(false) }

				val focusManager = LocalFocusManager.current

				BackHandler(
					enabled = searchMode
				) {
					focusManager.clearFocus()
					searchText = ""
					searchMode = false
				}

				AppScaffold(
					topBar = {
						CenterAlignedTopAppBar(
							title = { Text(stringResource(id = R.string.app_name)) },
							actions = {
								IconButton(onClick = { scanCode() }) {
									Icon(
										painter = painterResource(id = R.drawable.login_scan_code),
										contentDescription = stringResource(id = R.string.login_scan_code)
									)
								}
							},
							navigationIcon = {
								if (searchMode)
									IconButton(onClick = {
										focusManager.clearFocus()
										searchText = ""
										searchMode = false
									}) {
										Icon(
											imageVector = Icons.Outlined.ArrowBack,
											contentDescription = stringResource(id = R.string.all_back)
										)
									}
							}
						)
					}
				) { innerPadding ->
					Column(
						modifier = Modifier
							.padding(innerPadding)
							.fillMaxSize()
					) {
						if (!searchMode)
							Column(
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
						else
							SchoolSearch(
								modifier = Modifier
									.fillMaxWidth()
									.weight(1f),
								searchText = searchText
							)
						Column(
							modifier = Modifier
								.fillMaxWidth()
						) {
							OutlinedTextField(
								value = searchText,
								onValueChange = { searchText = it },
								singleLine = true,
								modifier = Modifier
									.fillMaxWidth()
									.padding(horizontal = dimensionResource(id = R.dimen.margin_login_input_horizontal))
									.onFocusChanged { if (it.isFocused) searchMode = true }
									.then(
										if (searchMode)
											Modifier.padding(bottom = dimensionResource(id = R.dimen.margin_login_input_horizontal))
										else
											Modifier
									),
								label = {
									Text(stringResource(id = R.string.login_search_by_school_name_or_address))
								}
							)
							if (!searchMode)
								Row(
									modifier = Modifier
										.fillMaxWidth()
										.padding(
											horizontal = dimensionResource(id = R.dimen.margin_login_input_horizontal),
											vertical = dimensionResource(id = R.dimen.margin_login_input_vertical)
										),
									horizontalArrangement = Arrangement.SpaceBetween
								) {
									TextButton(
										onClick = {
											loginLauncher.launch(
												Intent(
													this@LoginActivity,
													LoginDataInputActivity::class.java
												).apply {
													putBackgroundColorExtra(this)
													putExtra(EXTRA_BOOLEAN_DEMO_LOGIN, true)
												}
											)
										}
									) {
										Text(text = stringResource(id = R.string.login_demo))
									}

									TextButton(
										onClick = {
											loginLauncher.launch(
												Intent(
													this@LoginActivity,
													LoginDataInputActivity::class.java
												).apply {
													putBackgroundColorExtra(this)
												}
											)
										}
									) {
										Text(text = stringResource(id = R.string.login_manual_data_input))
									}
								}
						}
					}
				}
			}
		}
	}

	private fun scanCode() {
		if (ContextCompat.checkSelfPermission(
				this,
				Manifest.permission.CAMERA
			) != PackageManager.PERMISSION_GRANTED
		)
			requestPermissionLauncher.launch(Manifest.permission.CAMERA)
		else
			scanCodeLauncher.launch(Intent(this, ScanCodeActivity::class.java))
	}

	@OptIn(ExperimentalSerializationApi::class, ExperimentalMaterial3Api::class)
	@Composable
	fun SchoolSearch(
		modifier: Modifier,
		searchText: String
	) {
		var items by remember { mutableStateOf(emptyList<UntisSchoolInfo>()) }
		var loading by remember { mutableStateOf(false) }
		var error by remember { mutableStateOf<String?>(null) }
		val api: UntisRequest = remember { UntisRequest() }
		val query: UntisRequest.UntisRequestQuery = remember { UntisRequest.UntisRequestQuery() }
		val composableScope = rememberCoroutineScope()

		LaunchedEffect(searchText) {
			error = null
			items = emptyList()

			if (searchText.isEmpty())
				return@LaunchedEffect

			loading = true

			composableScope.launch {
				var untisResponse = SchoolSearchResponse()

				query.data.method = UntisApiConstants.METHOD_SEARCH_SCHOOLS
				query.url = SCHOOL_SEARCH_URL
				query.data.params = listOf(SchoolSearchParams(searchText))

				api.request<SchoolSearchResponse>(query).fold({ data ->
					untisResponse = data
				}, { error ->
					println("An error of type ${error.exception} happened: ${error.message}") // TODO: Implement proper error handling
				})

				loading = false

				if (untisResponse.result != null) {
					untisResponse.result?.let { items = it.schools }
				} else {
					error = ErrorMessageDictionary.getErrorMessage(
						resources,
						untisResponse.error?.code,
						untisResponse.error?.message.orEmpty()
					)
				}
			}
		}

		if (items.isNotEmpty())
			LazyColumn(modifier) {
				items(items) {
					ListItem(
						headlineText = { Text(it.displayName) },
						supportingText = { Text(it.address) },
						modifier = Modifier.clickable {
							val builder = Uri.Builder()
								.appendQueryParameter("schoolInfo", getJSON().encodeToString(it))

							loginLauncher.launch(
								Intent(
									this@LoginActivity,
									LoginDataInputActivity::class.java
								).apply {
									data = builder.build()
								})
						}
					)
				}
			}
		else
			Column(
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = modifier
			) {
				if (loading)
					CircularProgressIndicator()
				else if (!error.isNullOrEmpty())
					Text(text = error!!)
				else if (items.isEmpty())
					Text(text = stringResource(id = R.string.login_no_results))
			}
	}
}
