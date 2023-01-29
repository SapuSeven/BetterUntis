package com.sapuseven.untis.activities

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.ErrorMessageDictionary
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.helpers.api.LoginDataInfo
import com.sapuseven.untis.helpers.api.LoginErrorInfo
import com.sapuseven.untis.helpers.api.LoginHelper
import com.sapuseven.untis.models.UntisSchoolInfo
import com.sapuseven.untis.models.untis.masterdata.TimeGrid
import com.sapuseven.untis.preferences.dataStorePreferences
import com.sapuseven.untis.ui.common.LabeledCheckbox
import com.sapuseven.untis.ui.common.LabeledSwitch
import com.sapuseven.untis.ui.common.SmallCircularProgressIndicator
import com.sapuseven.untis.ui.functional.bottomInsets
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString


val Context.backupDataStore: DataStore<Preferences> by preferencesDataStore(name = LoginDataInputActivity.BACKUP_PREF_NAME)

class LoginDataInputActivity : BaseComposeActivity() {
	companion object {
		const val BACKUP_PREF_NAME = "loginDataInputBackup"

		//private const val FRAGMENT_TAG_PROFILE_UPDATE = "profileUpdate"

		const val EXTRA_BOOLEAN_PROFILE_UPDATE = "com.sapuseven.untis.activities.profileupdate"
		const val EXTRA_BOOLEAN_DEMO_LOGIN = "com.sapuseven.untis.activities.demoLogin"

		const val DEMO_API_URL = "https://api.sapuseven.com/untis/testing"

		val PREFS_BACKUP_SCHOOLID = stringPreferencesKey("logindatainput_backup_schoolid")
		val PREFS_BACKUP_ANONYMOUS = booleanPreferencesKey("logindatainput_backup_anonymous")
		val PREFS_BACKUP_USERNAME = stringPreferencesKey("logindatainput_backup_username")
		val PREFS_BACKUP_PASSWORD = stringPreferencesKey("logindatainput_backup_password")
		val PREFS_BACKUP_PROXYURL = stringPreferencesKey("logindatainput_backup_proxyurl")
		val PREFS_BACKUP_APIURL = stringPreferencesKey("logindatainput_backup_apiurl")
		val PREFS_BACKUP_SKIPAPPSECRET =
			booleanPreferencesKey("logindatainput_backup_skipappsecret")
	}

	private var existingUserId: Long? = null

	private var schoolInfoFromSearch: UntisSchoolInfo? = null
	private var existingUser: UserDatabase.User? = null

	@OptIn(ExperimentalMaterial3Api::class, ExperimentalSerializationApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		intent.getUserIdExtra()?.let { userId ->
			existingUserId = userId

			existingUserId?.let { id ->
				existingUser = userDatabase.getUser(id)?.also { user ->
					setUser(user)
				}
			}
		}

		userDatabase = UserDatabase.createInstance(this)

		setContent {
			AppTheme(navBarInset = false) {
				val coroutineScope = rememberCoroutineScope()
				val snackbarHostState = remember { SnackbarHostState() }

				var loading by rememberSaveable { mutableStateOf(false) }
				var validate by rememberSaveable { mutableStateOf(false) }

				var qrCodeErrorDialog by rememberSaveable { mutableStateOf(false) }

				val profileName = rememberSaveable { mutableStateOf(existingUser?.profileName) }
				val schoolId = rememberSaveable { mutableStateOf(existingUser?.schoolId) }
				val anonymous = rememberSaveable { mutableStateOf(existingUser?.anonymous) }
				val username = rememberSaveable { mutableStateOf(existingUser?.user) }
				val password = rememberSaveable { mutableStateOf<String?>(null) }
				val proxyUrl = rememberSaveable { mutableStateOf<String?>(null) }
				val apiUrl = rememberSaveable { mutableStateOf(existingUser?.apiUrl) }
				val skipAppSecret = rememberSaveable { mutableStateOf<Boolean?>(null) }

				var schoolIdLocked by rememberSaveable { mutableStateOf(false) }

				var advanced by rememberSaveable {
					mutableStateOf(
						proxyUrl.value?.isNotEmpty() == true ||
								apiUrl.value?.isNotEmpty() == true
					)
				}

				val proxyHostPref = dataStorePreferences.proxyHost
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
									getJSON().decodeFromString<UntisSchoolInfo>(it)
								schoolId.value = schoolInfoFromSearch?.schoolId.toString()
								schoolIdLocked = true
							}
						}
					}
				}

				val schoolIdError = schoolId.value.isNullOrEmpty()
				val usernameError = anonymous.value != true && username.value.isNullOrEmpty()
				val passwordError = anonymous.value != true && existingUser?.key == null && password.value.isNullOrEmpty()
				val proxyUrlError =
					!proxyUrl.value.isNullOrEmpty() && !Patterns.WEB_URL.matcher(proxyUrl.value!!)
						.matches()
				val apiUrlError =
					!apiUrl.value.isNullOrEmpty() && !Patterns.WEB_URL.matcher(apiUrl.value!!)
						.matches()

				val anyError =
					schoolIdError || usernameError || passwordError || proxyUrlError || apiUrlError

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
									snackbarHostState.showSnackbar(errorMessage, duration = SnackbarDuration.Long)
								}
							}).run {
							val schoolInfo = (
									when {
										schoolInfoFromSearch != null -> schoolInfoFromSearch
										advanced && !apiUrl.value.isNullOrBlank() -> UntisSchoolInfo(
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
									userDatabase.getUser(
										user
									)?.bookmarks?.toSet()
								}
									?: emptySet()
							val user = UserDatabase.User(
								existingUserId ?: -1,
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

							val userId =
								if (existingUserId == null) userDatabase.addUser(
									user
								) else userDatabase.editUser(
									user
								)

							userId?.let {
								userDatabase.setAdditionalUserData(
									userId,
									userDataResponse.masterData
								)

								if (advanced && !proxyUrl.value.isNullOrEmpty())
									proxyHostPref.saveValue(proxyUrl.value)

								setResult(Activity.RESULT_OK)
								finish()
							} ?: run {
								onError(LoginErrorInfo(errorMessageStringRes = R.string.logindatainput_adding_user_unknown_error))
							}
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
				else
					Scaffold(
						snackbarHost = { SnackbarHost(snackbarHostState) },
						floatingActionButtonPosition = FabPosition.End,
						floatingActionButton = {
							ExtendedFloatingActionButton(
								modifier = Modifier.bottomInsets(),
								icon = {
									if (loading)
										SmallCircularProgressIndicator()
									else
										Icon(Icons.Outlined.Login, contentDescription = null)
								},
								text = { Text(stringResource(id = R.string.logindatainput_login)) },
								onClick = {
									validate = true
									if (!anyError) {
										snackbarHostState.currentSnackbarData?.dismiss()
										loadData()
									}
								}
							)
						},
						topBar = {
							CenterAlignedTopAppBar(
								title = {
									Text(
										if (existingUserId == null)
											stringResource(id = R.string.logindatainput_title_add)
										else
											stringResource(id = R.string.logindatainput_title_edit)
									)
								},
								navigationIcon = {
									IconButton(onClick = {
										finish()
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
								state = profileName,
								label = { Text(stringResource(id = R.string.logindatainput_profilename)) },
								enabled = !loading
							)
							InputField(
								state = schoolId,
								label = { Text(stringResource(id = R.string.logindatainput_school)) },
								prefKey = PREFS_BACKUP_SCHOOLID,
								enabled = !loading && !schoolIdLocked,
								error = validate && schoolIdError,
								errorText = stringResource(id = R.string.logindatainput_error_field_empty)
							)
							Spacer(
								modifier = Modifier.height(32.dp)
							)
							InputSwitch(
								state = anonymous,
								label = { Text(stringResource(id = R.string.logindatainput_anonymous_login)) },
								prefKey = PREFS_BACKUP_ANONYMOUS,
								enabled = !loading
							)
							AnimatedVisibility(visible = anonymous.value != true) {
								Column {
									InputField(
										state = username,
										label = { Text(stringResource(id = R.string.logindatainput_username)) },
										prefKey = PREFS_BACKUP_USERNAME,
										enabled = !loading,
										error = validate && usernameError,
										errorText = stringResource(id = R.string.logindatainput_error_field_empty)
									)
									InputField(
										state = password,
										type = KeyboardType.Password,
										label = { Text(
											if (existingUser?.key == null || password.value != null)
												stringResource(id = R.string.logindatainput_key)
											else
												stringResource(id = R.string.logindatainput_key_saved)
										) },
										prefKey = PREFS_BACKUP_PASSWORD,
										enabled = !loading,
										error = validate && passwordError,
										errorText = stringResource(id = R.string.logindatainput_error_field_empty)
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
								checked = advanced,
								onCheckedChange = { advanced = it },
								enabled = !loading
							)
							AnimatedVisibility(visible = advanced) {
								Column {
									InputField(
										state = proxyUrl,
										type = KeyboardType.Uri,
										label = { Text(stringResource(id = R.string.logindatainput_proxy_host)) },
										prefKey = PREFS_BACKUP_PROXYURL,
										enabled = !loading,
										error = validate && proxyUrlError,
										errorText = stringResource(id = R.string.logindatainput_error_invalid_url)
									)
									InputField(
										state = apiUrl,
										type = KeyboardType.Uri,
										label = { Text(stringResource(id = R.string.logindatainput_api_url)) },
										prefKey = PREFS_BACKUP_APIURL,
										enabled = !loading,
										error = validate && apiUrlError,
										errorText = stringResource(id = R.string.logindatainput_error_invalid_url)
									)
									InputCheckbox(
										state = skipAppSecret,
										label = { Text(stringResource(id = R.string.logindatainput_skip_app_secret)) },
										prefKey = PREFS_BACKUP_SKIPAPPSECRET,
										enabled = !loading
									)
								}
							}
							Spacer(modifier = Modifier
								.bottomInsets()
								.height(80.dp)
							)

							if (qrCodeErrorDialog) {
								AlertDialog(
									onDismissRequest = {
										qrCodeErrorDialog = false
									},
									title = {
										Text(getString(R.string.logindatainput_dialog_qrcodeinvalid_title))
									},
									text = {
										Text(getString(R.string.logindatainput_dialog_qrcodeinvalid_text))
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

				if (intent.getBooleanExtra(EXTRA_BOOLEAN_DEMO_LOGIN, false)) {
					anonymous.value = true
					schoolId.value = "demo"
					advanced = true
					apiUrl.value = DEMO_API_URL

					loadData()
				}
			}
		}
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Composable
	private fun InputField(
		state: MutableState<String?>,
		type: KeyboardType = KeyboardType.Text,
		label: @Composable (() -> Unit)? = null,
		prefKey: Preferences.Key<String>? = null,
		enabled: Boolean = true,
		error: Boolean = false,
		errorText: String = ""
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 8.dp)
		) {
			OutlinedTextField(
				value = state.value ?: "",
				onValueChange = { state.value = it },
				singleLine = true,
				keyboardOptions = KeyboardOptions(keyboardType = type),
				visualTransformation = if (type == KeyboardType.Password) PasswordVisualTransformation() else VisualTransformation.None,
				label = label,
				modifier = Modifier.fillMaxWidth(),
				enabled = enabled,
				isError = error
			)

			AnimatedVisibility(visible = error) {
				Text(
					modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
					color = MaterialTheme.colorScheme.error,
					style = MaterialTheme.typography.bodyMedium,
					text = errorText
				)
			}
		}

		PersistentState(state, prefKey)
	}

	@Composable
	private fun InputSwitch(
		state: MutableState<Boolean?>,
		label: @Composable () -> Unit,
		prefKey: Preferences.Key<Boolean>? = null,
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

		PersistentState(state, prefKey)
	}

	@Composable
	private fun InputCheckbox(
		state: MutableState<Boolean?>,
		label: @Composable () -> Unit,
		prefKey: Preferences.Key<Boolean>? = null,
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

		PersistentState(state, prefKey)
	}

	@OptIn(DelicateCoroutinesApi::class)
	@Composable
	fun <T> PersistentState(
		state: MutableState<T?>,
		prefKey: Preferences.Key<T>? = null
	) {
		/*prefKey?.let {
			val coroutineScope = rememberCoroutineScope()

			DisposableEffect(Unit) {
				if (state.value == null)
					coroutineScope.launch {
						backupDataStore.data
							.map { prefs -> prefs[prefKey] }
							.first()
							?.let {
								state.value = it
							}
					}

				onDispose {
					state.value?.let {
						// Not the cleanest, but this needs to run after the local coroutineScope has been cancelled
						GlobalScope.launch(Dispatchers.IO) {
							backupDataStore.edit { prefs ->
								prefs[prefKey] = it
							}
						}
					}
				}
			}
		}*/
	}
}
