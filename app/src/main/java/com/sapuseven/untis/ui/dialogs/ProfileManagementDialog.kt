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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.sapuseven.untis.R
import com.sapuseven.untis.activities.BaseComposeActivity
import com.sapuseven.untis.activities.BaseComposeActivity.Companion.EXTRA_LONG_PROFILE_ID
import com.sapuseven.untis.activities.LoginActivity
import com.sapuseven.untis.activities.LoginDataInputActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseComposeActivity.ProfileManagementDialog(
	onDismiss: () -> Unit
) {
	var dismissed by remember { mutableStateOf(false) }
	var profiles by remember { mutableStateOf(this.userDatabase.getAllUsers()) }
	val context = LocalContext.current

	val loginDataInputLauncher =
		rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
			profiles = this.userDatabase.getAllUsers()
		}

	fun dismiss() {
		onDismiss()
		dismissed = true
	}

	BackHandler(
		enabled = !dismissed,
	) {
		dismiss()
	}

	Scaffold(
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
				.fillMaxSize()
		) {
			item {
				ListItem(
					headlineText = { Text("Tap on a profile to edit") },
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
					headlineText = { Text(profile.getDisplayedName()) },
					supportingText = { Text(profile.userData.schoolName) },
					leadingContent = {
						Icon(
							imageVector = Icons.Outlined.Person,
							contentDescription = null
						)
					},
					trailingContent = {
						IconButton(onClick = {
							//dismiss()
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
								putExtra(EXTRA_LONG_PROFILE_ID, profile.id)
							})
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
						loginDataInputLauncher.launch(
							Intent(
								context,
								LoginActivity::class.java
							)
						)
					}
				)
			}
		}
	}
}
