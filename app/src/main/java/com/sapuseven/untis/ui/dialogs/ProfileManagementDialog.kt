package com.sapuseven.untis.ui.dialogs

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.sapuseven.untis.R
import com.sapuseven.untis.activities.BaseComposeActivity
import com.sapuseven.untis.activities.LoginActivity
import com.sapuseven.untis.activities.LoginDataInputActivity
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.helpers.config.deleteProfile
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.functional.insetsPaddingValues
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseComposeActivity.ProfileManagementDialog( // TODO: Remove inheritance of BaseComposeActivity
	onDismiss: () -> Unit
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	var dismissed by remember { mutableStateOf(false) }
	var profiles by remember { mutableStateOf(userDatabase.userDao().getAll()) }

	val loginDataInputLauncher =
		rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
			profiles = userDatabase.userDao().getAll()
		}

	var deleteDialog by rememberSaveable { mutableStateOf<User?>(null) }

	fun dismiss() {
		onDismiss()
		dismissed = true
	}

	BackHandler(
		enabled = !dismissed,
	) {
		dismiss()
	}

	AppScaffold(
		topBar = {
			CenterAlignedTopAppBar(
				title = { Text(stringResource(id = R.string.mainactivitydrawer_profile_edit)) },
				navigationIcon = {
					IconButton(onClick = {
						dismiss()
					}) {
						Icon(
							imageVector = Icons.Outlined.Close,
							contentDescription = stringResource(id = R.string.all_close)
						)
					}
				}
			)
		},
	) { innerPadding ->
		LazyColumn(
			modifier = Modifier
				.padding(innerPadding)
				.fillMaxSize(),
			contentPadding = insetsPaddingValues()
		) {
			item {
				ListItem(
					headlineContent = { Text(stringResource(R.string.mainactivitydrawer_profile_edit_hint)) },
					leadingContent = {
						Icon(
							imageVector = Icons.Outlined.Info,
							contentDescription = null
						)
					}
				)

				Divider(color = MaterialTheme.colorScheme.outline)
			}

			items(profiles) { profile ->
				ListItem(
					headlineContent = { Text(profile.getDisplayedName()) },
					supportingContent = { Text(profile.userData.schoolName) },
					leadingContent = {
						Icon(
							imageVector = Icons.Outlined.Person,
							contentDescription = null
						)
					},
					trailingContent = {
						IconButton(onClick = {
							deleteDialog = profile
						}) {
							Icon(
								imageVector = Icons.Outlined.Delete,
								contentDescription = stringResource(id = R.string.logindatainput_delete)
							)
						}
					},
					modifier = Modifier.clickable {
						loginDataInputLauncher.launch(
							Intent(
								context,
								LoginDataInputActivity::class.java
							).apply {
								putUserIdExtra(this, profile.id)
								putBackgroundColorExtra(this)
							})
					}
				)
			}

			item {
				ListItem(
					headlineContent = { Text(stringResource(id = R.string.mainactivitydrawer_profile_add)) },
					leadingContent = {
						Icon(
							imageVector = Icons.Outlined.Add,
							contentDescription = null
						)
					},
					modifier = Modifier.clickable {
						loginDataInputLauncher.launch(
							Intent(
								context,
								LoginActivity::class.java
							).apply {
								putBackgroundColorExtra(this)
								putExtra(LoginActivity.EXTRA_BOOLEAN_SHOW_BACK_BUTTON, true)
							}
						)
					}
				)
			}
		}

		deleteDialog?.let { user ->
			AlertDialog(
				onDismissRequest = {
					deleteDialog = null
				},
				title = {
					Text(stringResource(id = R.string.main_dialog_delete_profile_title))
				},
				text = {
					Text(
						stringResource(
							id = R.string.main_dialog_delete_profile_message,
							user.getDisplayedName(applicationContext),
							user.userData.schoolName
						)
					)
				},
				confirmButton = {
					TextButton(
						onClick = {
							scope.launch {
								userDatabase.userDao().delete(user)
								deleteProfile(user.id)
								profiles = userDatabase.userDao().getAll()
								if (profiles.isEmpty())
									recreate()
								deleteDialog = null
							}
						}) {
						Text(stringResource(id = R.string.all_delete))
					}
				},
				dismissButton = {
					TextButton(
						onClick = {
							deleteDialog = null
						}) {
						Text(stringResource(id = R.string.all_cancel))
					}
				}
			)
		}
	}
}
