package com.sapuseven.untis.ui.pages.login.datainput

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.sapuseven.untis.R
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.common.LabeledCheckbox
import com.sapuseven.untis.ui.common.LabeledSwitch
import com.sapuseven.untis.ui.common.MessageBubble
import com.sapuseven.untis.ui.common.SmallCircularProgressIndicator
import com.sapuseven.untis.ui.common.ifNotNull
import com.sapuseven.untis.ui.functional.bottomInsets
import com.sapuseven.untis.ui.pages.login.schoolsearch.SchoolSearch
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun LoginDataInput(
	viewModel: LoginDataInputViewModel = hiltViewModel()
) {
	viewModel.setCodeScanLauncher(rememberLauncherForActivityResult(ScanContract()) { viewModel.codeScanResultHandler(it.contents) })

	if (viewModel.showProfileUpdate)
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
		}
	else
		AppScaffold(
			floatingActionButtonPosition = FabPosition.End,
			floatingActionButton = {
				if (!viewModel.searchMode) {
					ExtendedFloatingActionButton(
						modifier = Modifier.bottomInsets(),
						containerColor = MaterialTheme.colorScheme.primary,
						icon = {
							if (viewModel.loading)
								SmallCircularProgressIndicator(color = LocalContentColor.current)
							else
								Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null)
						},
						text = { Text(stringResource(id = R.string.logindatainput_login)) },
						onClick = { viewModel.onLoginClick() }
					)
				}
			},
			topBar = {
				CenterAlignedTopAppBar(
					title = {
						Text(
							if (viewModel.isExistingUser)
								stringResource(id = R.string.logindatainput_title_edit)
							else
								stringResource(id = R.string.logindatainput_title_add)
						)
					},
					actions = {
						IconButton(onClick = { viewModel.onCodeScanClick() }) {
							Icon(
								painter = painterResource(id = R.drawable.login_scan_code),
								contentDescription = stringResource(id = R.string.login_scan_code)
							)
						}
					},
					navigationIcon = {
						IconButton(onClick = {
							viewModel.goBack()
						}) {
							Icon(
								imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
								contentDescription = stringResource(id = R.string.all_back)
							)
						}
					}
				)
			}
		) { innerPadding ->
			val focusRequester = remember { FocusRequester() }
			val focusManager = LocalFocusManager.current

			if (viewModel.searchMode) {
				Column(
					modifier = Modifier
						.padding(innerPadding)
						.fillMaxSize()
						.bottomInsets()
				) {
					SchoolSearch(
						modifier = Modifier
							.fillMaxWidth()
							.weight(1f),
						searchText = viewModel.loginData.schoolName.value ?: "",
						onSchoolSelected = { viewModel.selectSchool(it) }
					)

					InputField(
						state = viewModel.loginData.schoolName,
						label = { Text(stringResource(id = R.string.logindatainput_school)) },
						enabled = !viewModel.loading && !viewModel.schoolIdLocked,
						valid = !viewModel.validate || viewModel.schoolNameValid.value,
						errorText = stringResource(id = R.string.logindatainput_error_field_empty),
						trailingIcon = {
							IconButton(
								enabled = !viewModel.loading && !viewModel.schoolIdLocked,
								onClick = { viewModel.searchMode = true }
							) {
								Icon(
									imageVector = Icons.Outlined.Search,
									contentDescription = stringResource(R.string.login_search_by_school_name_or_address)
								)
							}
						},
						focusManager = focusManager,
						modifier = Modifier
							.focusRequester(focusRequester)
							.padding(bottom = 8.dp)
					)

					LaunchedEffect(Unit) {
						focusRequester.requestFocus()
					}
				}
			} else {
				Column(
					modifier = Modifier
						.padding(innerPadding)
						.fillMaxSize()
						.bottomInsets()
						.verticalScroll(rememberScrollState())
						.padding(bottom = 88.dp) // Space for FAB
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
						messageText = viewModel.errorText,
						messageTextRaw = viewModel.errorTextRaw
					)

					InputField(
						state = viewModel.loginData.profileName,
						label = { Text(stringResource(id = R.string.logindatainput_profilename)) },
						enabled = !viewModel.loading,
						focusManager = focusManager
					)
					InputField(
						state = viewModel.loginData.schoolName,
						label = { Text(stringResource(id = R.string.logindatainput_school)) },
						enabled = !viewModel.loading && !viewModel.schoolIdLocked,
						valid = !viewModel.validate || viewModel.schoolNameValid.value,
						errorText = stringResource(id = R.string.logindatainput_error_field_empty),
						trailingIcon = {
							IconButton(
								enabled = !viewModel.loading && !viewModel.schoolIdLocked,
								onClick = { viewModel.searchMode = true }
							) {
								Icon(
									imageVector = Icons.Outlined.Search,
									contentDescription = stringResource(R.string.login_search_by_school_name_or_address)
								)
							}
						},
						focusManager = focusManager
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
								valid = !viewModel.validate || viewModel.usernameValid.value,
								errorText = stringResource(id = R.string.logindatainput_error_field_empty),
								contentType = ContentType.Username,
								focusManager = focusManager
							)
							InputField(
								state = viewModel.loginData.password,
								type = KeyboardType.Password,
								label = {
									Text(
										if (viewModel.useStoredPassword)
											stringResource(id = R.string.logindatainput_key_saved)
										else
											stringResource(id = R.string.logindatainput_key)
									)
								},
								enabled = !viewModel.loading,
								contentType = ContentType.Password,
								focusManager = focusManager
							)
							AnimatedVisibility(visible = viewModel.showSecondFactorInput) {
								val focusRequester = remember { FocusRequester() }

								InputField(
									state = viewModel.loginData.secondFactor,
									type = KeyboardType.NumberPassword,
									label = {
										Text(
											stringResource(id = R.string.logindatainput_2fa)
										)
									},
									enabled = !viewModel.loading,
									focusManager = focusManager,
									modifier = Modifier
										.focusRequester(focusRequester)
								)

								LaunchedEffect(Unit) {
									focusRequester.requestFocus()
								}
							}
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
							// Proxy is not supported currently
							/*InputField(
								state = viewModel.loginData.proxyUrl,
								type = KeyboardType.Uri,
								label = { Text(stringResource(id = R.string.logindatainput_proxy_host)) },
								enabled = !viewModel.loading,
								valid = !viewModel.validate || viewModel.proxyUrlValid.value,
								errorText = stringResource(id = R.string.logindatainput_error_invalid_url)
							)*/
							InputField(
								state = viewModel.loginData.apiUrl,
								type = KeyboardType.Uri,
								label = { Text(stringResource(id = R.string.logindatainput_api_url)) },
								enabled = !viewModel.loading,
								valid = !viewModel.validate || viewModel.apiUrlValid.value,
								errorText = stringResource(id = R.string.logindatainput_error_invalid_url),
								focusManager = focusManager
							)
						}
					}

					if (viewModel.showQrCodeErrorDialog) {
						AlertDialog(
							onDismissRequest = {
								viewModel.onQrCodeErrorDialogDismiss()
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
										viewModel.onQrCodeErrorDialogDismiss()
									}) {
									Text(stringResource(id = R.string.all_ok))
								}
							}
						)
					}
				}
			}
		}
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
	contentType: ContentType? = null,
	trailingIcon: @Composable (() -> Unit)? = null,
	focusManager: FocusManager? = null,
	modifier: Modifier = Modifier
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
			keyboardOptions = KeyboardOptions(
				keyboardType = type,
				imeAction = if (focusManager == null) ImeAction.Unspecified else ImeAction.Next
			),
			keyboardActions = KeyboardActions(
				onNext = {
					focusManager?.moveFocus(FocusDirection.Next)
				}
			),
			visualTransformation = if (type == KeyboardType.Password) PasswordVisualTransformation() else VisualTransformation.None,
			label = label,
			enabled = enabled,
			isError = !valid,
			trailingIcon = trailingIcon,
			modifier = modifier
				.fillMaxWidth()
				.onFocusChanged {
					if (it.isFocused) {
						coroutineScope.launch {
							bringIntoViewRequester.bringIntoView()
						}
					}
				}
				.ifNotNull(contentType) {
					semantics { this.contentType = it }
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
