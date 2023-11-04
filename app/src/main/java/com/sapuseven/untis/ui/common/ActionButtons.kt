package com.sapuseven.untis.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.entities.User

@Composable
fun ProfileSelectorAction(
	users: List<User>,
	currentSelectionId: Long, // TODO: Better to use a UserDatabase.User reference?
	showProfileActions: Boolean = false,
	hideIfSingleProfile: Boolean = false,
	onSelectionChange: (User) -> Unit,
	onActionEdit: () -> Unit = {}
) {
	var expanded by remember { mutableStateOf(false) }

	if (!showProfileActions && hideIfSingleProfile && users.size <= 1) return

	IconButton(onClick = { expanded = true }, modifier = Modifier.testTag("action_profiles")) {
		Icon(
			imageVector = Icons.Outlined.AccountCircle,
			contentDescription = stringResource(id = R.string.mainactivitydrawer_dropdown_show)
		)
	}

	DropdownMenu(
		expanded = expanded,
		onDismissRequest = { expanded = false }
	) {
		users.forEach {
			DropdownMenuItem(
				text = { Text(it.getDisplayedName()) },
				leadingIcon = {
					if (currentSelectionId == it.id) {
						Icon(
							Icons.Outlined.Check,
							contentDescription = null
						)
					}
				},
				onClick = {
					expanded = false
					onSelectionChange(it)
				}
			)
		}

		if (showProfileActions) {
			DropdownMenuDivider()
			DropdownMenuItem(
				text = { Text(stringResource(id = R.string.mainactivitydrawer_profile_edit)) },
				leadingIcon = {
					Icon(
						Icons.Outlined.Edit,
						contentDescription = null
					)
				},
				onClick = {
					expanded = false
					onActionEdit()
				}
			)
		}
	}
}

@Composable
fun DebugInfoAction() {
	var showInfoDialog by remember { mutableStateOf(false) }

	IconButton(onClick = { showInfoDialog = true }) {
		Icon(
			painterResource(R.drawable.all_debug),
			contentDescription = "Debug info"
		)
	}

	if (showInfoDialog) {
		AlertDialog(
			onDismissRequest = { showInfoDialog = false },
			title = { Text("Debug information") },
			text = {
				Text(
					"You are running a debug build of the app.\n\n" +
							"This means that the app is not optimized and you will see some additional settings and functions.\n" +
							"It is only recommended to use this variant when developing or gathering information about specific issues.\n" +
							"For normal daily use, you should switch to a stable release build of the app.\n\n" +
							"Please remember that diagnostic data may include personal details, " +
							"so it is your responsibility to check and obfuscate any gathered data before uploading."
				)
			},
			confirmButton = {
				TextButton(
					onClick = { showInfoDialog = false }) {
					Text(stringResource(R.string.all_ok))
				}
			}
		)
	}
}

@Composable
internal fun DropdownMenuDivider() {
	Divider(modifier = Modifier.padding(vertical = 8.dp))
}
