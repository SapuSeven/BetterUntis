package com.sapuseven.untis.ui.dialogs

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sapuseven.untis.R
import com.sapuseven.untis.components.UserManager
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.functional.insetsPaddingValues
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileManagementDialog(
	userManager: UserManager,
	onEdit: (user: User?) -> Unit,
	onDismiss: () -> Unit
) {
	var dismissed by remember { mutableStateOf(false) }
	val users by userManager.allUsersState.collectAsStateWithLifecycle()
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

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

			items(users) { user ->
				ListItem(
					headlineContent = { Text(user.getDisplayedName()) },
					supportingContent = { Text(user.userData.schoolName) },
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
						onEdit(user)
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
						onEdit(null)
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
							scope.launch {
								userManager.deleteUser(user)
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
