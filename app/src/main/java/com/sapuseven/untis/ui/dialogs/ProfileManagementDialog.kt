package com.sapuseven.untis.ui.dialogs

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.sapuseven.untis.activities.NewMainAppState
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.functional.insetsPaddingValues

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileManagementDialog(
	state: NewMainAppState,
	onDismiss: () -> Unit
) {
	var dismissed by remember { mutableStateOf(false) }
	var users = state.listUsers()
	val context = LocalContext.current

	val loginDataInputLauncher =
		rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
			users = state.listUsers()
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
					headlineText = { Text(stringResource(R.string.mainactivitydrawer_profile_edit_hint)) },
					leadingContent = {
						Icon(
							imageVector = Icons.Outlined.Info,
							contentDescription = null
						)
					}
				)

				Divider(color = MaterialTheme.colorScheme.outline)
			}

			items(users) { user ->
				ListItem(
					headlineText = { Text(user.getDisplayedName()) },
					supportingText = { Text(user.userData.schoolName) },
					leadingContent = {
						Icon(
							imageVector = Icons.Outlined.Person,
							contentDescription = null
						)
					},
					trailingContent = {
						IconButton(onClick = {
							deleteDialog = user
						}) {
							Icon(
								imageVector = Icons.Outlined.Delete,
								contentDescription = stringResource(id = R.string.logindatainput_delete)
							)
						}
					},
					modifier = Modifier.clickable {
						state.editUser(user, loginDataInputLauncher)
					}
				)
			}

			item {
				ListItem(
					headlineText = { Text(stringResource(id = R.string.mainactivitydrawer_profile_add)) },
					leadingContent = {
						Icon(
							imageVector = Icons.Outlined.Add,
							contentDescription = null
						)
					},
					modifier = Modifier.clickable {
						state.editUser(null, loginDataInputLauncher)
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
							user.getDisplayedName(context),
							user.userData.schoolName
						)
					)
				},
				confirmButton = {
					TextButton(
						onClick = {
							state.deleteUser(user)
							deleteDialog = null
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
