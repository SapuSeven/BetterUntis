package com.sapuseven.untis.ui.activities

import android.app.Activity
import android.content.res.Configuration
import android.util.Log
import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sapuseven.untis.R
import com.sapuseven.untis.activities.LoginDataInputActivity
import com.sapuseven.untis.activities.LoginDataInputActivity.Companion.EXTRA_BOOLEAN_PROFILE_UPDATE
import com.sapuseven.untis.api.model.untis.SchoolInfo
import com.sapuseven.untis.helpers.ErrorMessageDictionary
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.helpers.api.LoginDataInfo
import com.sapuseven.untis.helpers.api.LoginHelper
import com.sapuseven.untis.api.model.untis.masterdata.TimeGrid
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.common.LabeledCheckbox
import com.sapuseven.untis.ui.common.LabeledSwitch
import com.sapuseven.untis.ui.common.SmallCircularProgressIndicator
import com.sapuseven.untis.ui.common.autofill
import com.sapuseven.untis.ui.common.ifNotNull
import com.sapuseven.untis.ui.functional.bottomInsets
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun LoginDataInput(
	viewModel: LoginDataInputViewModel = viewModel()
) {
	val snackbarHostState = remember { SnackbarHostState() }

	var qrCodeErrorDialog by rememberSaveable { mutableStateOf(false) }

	var schoolIdLocked by rememberSaveable { mutableStateOf(false) }

	/*val proxyHostPref = dataStorePreferences.proxyHost
	LaunchedEffect(Unit) {
		existingUser?.let {
			proxyUrl.value = proxyHostPref.getValue()
		}

		val appLinkData = intent.data

		if (appLinkData?.isHierarchical == true) {
			if (appLinkData.scheme == "untis") {
				if (appLinkData.host == "setschool") {
					// Untis-supported values
					schoolId.value = appLinkData.getQueryParameter("school")
					username.value = appLinkData.getQueryParameter("user")
					password.value = appLinkData.getQueryParameter("key")

					// Custom values
					anonymous.value =
						appLinkData.getBooleanQueryParameter("anonymous", false)
					proxyUrl.value = appLinkData.getQueryParameter("proxyUrl") ?: ""
					apiUrl.value = appLinkData.getQueryParameter("apiUrl")
					skipAppSecret.value =
						appLinkData.getBooleanQueryParameter("skipAppSecret", false)

					advanced =
						proxyUrl.value?.isNotEmpty() == true || apiUrl.value?.isNotEmpty() == true
				} else {
					qrCodeErrorDialog = true
				}
			} else {
				appLinkData.getQueryParameter("schoolInfo")?.let {
					schoolInfoFromSearch =
						getJSON().decodeFromString<SchoolInfo>(it)
					schoolId.value = schoolInfoFromSearch?.schoolId.toString()
					schoolIdLocked = true
				}
			}
		}
	}

	fun loadData() {
		loading = true
		coroutineScope.launch {
			LoginHelper(
				loginData = LoginDataInfo(
					username.value ?: "",
					password.value ?: existingUser?.key ?: "",
					anonymous.value ?: false
				),
				proxyHost = proxyUrl.value,
				onStatusUpdate = { status ->
					Log.d(
						LoginDataInputActivity::class.java.simpleName,
						getString(status)
					)
				},
				onError = { error ->
					val errorMessage = when {
						error.errorCode != null -> ErrorMessageDictionary.getErrorMessage(
							resources,
							error.errorCode,
							error.errorMessage
						)

						error.errorMessageStringRes != null -> getString(
							error.errorMessageStringRes,
							error.errorMessage
						)

						else -> error.errorMessage
							?: getString(R.string.all_error)
					}

					loading = false
					coroutineScope.launch {
						snackbarHostState.showSnackbar(
							errorMessage,
							duration = SnackbarDuration.Long
						)
					}
				}).run {
				val schoolInfo = (
					when {
						schoolInfoFromSearch != null -> schoolInfoFromSearch
						advanced && !apiUrl.value.isNullOrBlank() -> SchoolInfo(
							server = "",
							useMobileServiceUrlAndroid = true,
							useMobileServiceUrlIos = true,
							address = "",
							displayName = schoolId.value ?: "",
							loginName = schoolId.value ?: "",
							schoolId = schoolId.value?.toIntOrNull()
								?: 0,
							serverUrl = apiUrl.value ?: "",
							mobileServiceUrl = apiUrl.value
						)

						else -> loadSchoolInfo(
							schoolId.value ?: ""
						)
					}) ?: return@run
				val untisApiUrl =
					if (advanced && !apiUrl.value.isNullOrBlank())
						apiUrl.value ?: ""
					else if (schoolInfo.useMobileServiceUrlAndroid && !schoolInfo.mobileServiceUrl.isNullOrBlank()) schoolInfo.mobileServiceUrl!!
					else Uri.parse(schoolInfo.serverUrl).buildUpon()
						.appendEncodedPath("jsonrpc_intern.do")
						.build().toString()
				val appSharedSecret =
					when {
						loginData.anonymous -> ""
						skipAppSecret.value == true -> loginData.password
						else -> loadAppSharedSecret(untisApiUrl)
							?: return@run
					}
				val userDataResponse =
					loadUserData(untisApiUrl, appSharedSecret)
						?: return@run
				val bookmarks =
					existingUserId?.let { user ->
						userDatabase.userDao().getById(user)?.bookmarks?.toSet()
					}
						?: emptySet()
				var userId = existingUserId ?: 0
				val user = User(
					userId,
					profileName.value ?: "",
					untisApiUrl,
					schoolInfo.schoolId.toString(),
					if (anonymous.value != true) loginData.user else null,
					if (anonymous.value != true) appSharedSecret else null,
					anonymous.value == true,
					userDataResponse.masterData.timeGrid
						?: TimeGrid.generateDefault(),
					userDataResponse.masterData.timeStamp,
					userDataResponse.userData,
					userDataResponse.settings,
					bookmarks = bookmarks
				)

				userDatabase.userDao().let { dao ->
					if (existingUserId == null)
						userId = dao.insert(user)
					else
						dao.update(user)

					dao.deleteUserData(userId)
					dao.insertUserData(
						userId,
						userDataResponse.masterData
					)
				}

				if (advanced && !proxyUrl.value.isNullOrEmpty())
					proxyHostPref.saveValue(proxyUrl.value)

				setResult(Activity.RESULT_OK)
				finish()
			}
		}
	}

	if (intent.getBooleanExtra(EXTRA_BOOLEAN_PROFILE_UPDATE, false))
		Surface {
			Column(
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier.fillMaxSize()
			) {
				Text(
					text = "A new school year has begun.",
					style = MaterialTheme.typography.titleLarge,
					textAlign = TextAlign.Center,
					modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
				)
				Text(
					text = "Please wait, BetterUntis is loading the new timetable data.\nThis may take a moment.",
					style = MaterialTheme.typography.bodyMedium,
					textAlign = TextAlign.Center,
					modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
				)
				CircularProgressIndicator(
					modifier = Modifier.padding(top = 16.dp)
				)
			}

			loadData()
		}
	else*/
		AppScaffold(
			snackbarHost = { SnackbarHost(snackbarHostState) },
			floatingActionButtonPosition = FabPosition.End,
			floatingActionButton = {
				ExtendedFloatingActionButton(
					modifier = Modifier.bottomInsets(),
					icon = {
						if (viewModel.loading)
							SmallCircularProgressIndicator()
						else
							Icon(Icons.Outlined.ArrowForward, contentDescription = null)
					},
					text = { Text(stringResource(id = R.string.logindatainput_login)) },
					onClick = { viewModel.onLoginClick() }
				)
			},
			topBar = {
				CenterAlignedTopAppBar(
					title = {
						Text(
							//if (existingUserId == null)
								stringResource(id = R.string.logindatainput_title_add)
							//else
								//stringResource(id = R.string.logindatainput_title_edit)
						)
					},
					navigationIcon = {
						IconButton(onClick = {
							//finish()
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
					.verticalScroll(rememberScrollState())
			) {
				if (LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE)
					Icon(
						painter = painterResource(id = R.drawable.login_profile),
						contentDescription = null,
						tint = MaterialTheme.colorScheme.primary,
						modifier = Modifier
                            .width(dimensionResource(id = R.dimen.size_login_icon))
                            .height(dimensionResource(id = R.dimen.size_login_icon))
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = dimensionResource(id = R.dimen.margin_login_pleaselogin_top))
					)
				InputField(
					state = viewModel.loginData.profileName,
					label = { Text(stringResource(id = R.string.logindatainput_profilename)) },
					enabled = !viewModel.loading
				)
				InputField(
					state = viewModel.loginData.schoolId,
					label = { Text(stringResource(id = R.string.logindatainput_school)) },
					enabled = !viewModel.loading && !schoolIdLocked,
					valid = viewModel.schoolIdValid.value,
					//validator = !it.isNullOrEmpty()
					errorText = stringResource(id = R.string.logindatainput_error_field_empty)
				)
				Spacer(
					modifier = Modifier.height(32.dp)
				)
				InputSwitch(
					state = viewModel.loginData.anonymous,
					label = { Text(stringResource(id = R.string.logindatainput_anonymous_login)) },
					enabled = !viewModel.loading
				)
				AnimatedVisibility(visible = viewModel.loginData.anonymous.value != true) {
					Column {
						InputField(
							state = viewModel.loginData.username,
							label = { Text(stringResource(id = R.string.logindatainput_username)) },
							enabled = !viewModel.loading,
							valid = viewModel.usernameValid.value,
							//validator = viewModel.loginData.anonymous.value == true || !it.isNullOrEmpty()
							errorText = stringResource(id = R.string.logindatainput_error_field_empty),
							autofillType = AutofillType.Username
						)
						InputField(
							state = viewModel.loginData.password,
							type = KeyboardType.Password,
							label = {
								Text(
									//if (existingUser?.key == null || password.value != null)
										stringResource(id = R.string.logindatainput_key)
									//else
										//stringResource(id = R.string.logindatainput_key_saved)
								)
							},
							enabled = !viewModel.loading,
							autofillType = AutofillType.Password
						)
						Spacer(
							modifier = Modifier.height(32.dp)
						)
					}
				}
				LabeledSwitch(
					label = { Text(stringResource(id = R.string.logindatainput_show_advanced)) },
					modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
					checked = viewModel.advanced,
					onCheckedChange = { viewModel.advanced = it },
					enabled = !viewModel.loading
				)
				AnimatedVisibility(visible = viewModel.advanced) {
					Column {
						InputField(
							state = viewModel.loginData.proxyUrl,
							type = KeyboardType.Uri,
							label = { Text(stringResource(id = R.string.logindatainput_proxy_host)) },
							enabled = !viewModel.loading,
							valid = viewModel.proxyUrlValid.value,
							// validator = it.isNullOrEmpty() || Patterns.WEB_URL.matcher(it).matches()
							errorText = stringResource(id = R.string.logindatainput_error_invalid_url)
						)
						InputField(
							state = viewModel.loginData.apiUrl,
							type = KeyboardType.Uri,
							label = { Text(stringResource(id = R.string.logindatainput_api_url)) },
							enabled = !viewModel.loading,
							valid = viewModel.apiUrlValid.value,
							//validator = it.isNullOrEmpty() || Patterns.WEB_URL.matcher(it).matches()
							errorText = stringResource(id = R.string.logindatainput_error_invalid_url)
						)
						InputCheckbox(
							state = viewModel.loginData.skipAppSecret,
							label = { Text(stringResource(id = R.string.logindatainput_skip_app_secret)) },
							enabled = !viewModel.loading
						)
					}
				}
				Spacer(
					modifier = Modifier
						.bottomInsets()
						.height(80.dp)
				)

				if (qrCodeErrorDialog) {
					AlertDialog(
						onDismissRequest = {
							qrCodeErrorDialog = false
						},
						title = {
							Text(stringResource(id = R.string.logindatainput_dialog_qrcodeinvalid_title))
						},
						text = {
							Text(stringResource(id = R.string.logindatainput_dialog_qrcodeinvalid_text))
						},
						confirmButton = {
							TextButton(
								onClick = {
									qrCodeErrorDialog = false
								}) {
								Text(stringResource(id = R.string.all_ok))
							}
						}
					)
				}
			}
		}

	/*if (intent.getBooleanExtra(EXTRA_BOOLEAN_DEMO_LOGIN, false)) {
		anonymous.value = true
		schoolId.value = "demo"
		advanced = true
		apiUrl.value = DEMO_API_URL

		loadData()
	}*/
}

@OptIn(
	ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
	ExperimentalFoundationApi::class
)
@Composable
private fun InputField(
	state: MutableState<String?>,
	type: KeyboardType = KeyboardType.Text,
	label: @Composable (() -> Unit)? = null,
	enabled: Boolean = true,
	valid: Boolean = true,
	errorText: String = "",
	autofillType: AutofillType? = null
) {
	val bringIntoViewRequester = remember { BringIntoViewRequester() }
	val coroutineScope = rememberCoroutineScope()

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 8.dp)
			.bringIntoViewRequester(bringIntoViewRequester)
	) {
		OutlinedTextField(
			value = state.value ?: "",
			onValueChange = { state.value = it },
			singleLine = true,
			keyboardOptions = KeyboardOptions(keyboardType = type),
			visualTransformation = if (type == KeyboardType.Password) PasswordVisualTransformation() else VisualTransformation.None,
			label = label,
			enabled = enabled,
			isError = !valid,
			modifier = Modifier
				.fillMaxWidth()
				.onFocusEvent { focusState ->
					Log.d("LoginDataInput", "onFocus event")
					if (focusState.isFocused) {
						Log.d("LoginDataInput", "onFocus isFocused")
						coroutineScope.launch {
							bringIntoViewRequester.bringIntoView()
						}
					}
				}
				.ifNotNull(autofillType) {
					autofill(listOf(it)) {
						state.value = it
					}
				}
		)

		AnimatedVisibility(visible = !valid) {
			Text(
				modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
				color = MaterialTheme.colorScheme.error,
				style = MaterialTheme.typography.bodyMedium,
				text = errorText
			)
		}
	}
}

@Composable
private fun InputSwitch(
	state: MutableState<Boolean?>,
	label: @Composable () -> Unit,
	enabled: Boolean = true
) {
	LabeledSwitch(
		label = label,
		modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
		checked = state.value ?: false,
		onCheckedChange = { state.value = it },
		enabled = enabled
	)
}

@Composable
private fun InputCheckbox(
	state: MutableState<Boolean?>,
	label: @Composable () -> Unit,
	enabled: Boolean = true
) {
	LabeledCheckbox(
		label = label,
		modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
		checked = state.value ?: false,
		onCheckedChange = { state.value = it },
		enabled = enabled
	)
}
