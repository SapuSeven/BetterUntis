package com.sapuseven.untis.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase

@Composable
fun ProfileSelectorAction(
	users: List<UserDatabase.User>,
	currentSelectionId: Long, // TODO: Better to use a UserDatabase.User reference?
	showProfileActions: Boolean = false,
	hideIfSingleProfile: Boolean = false,
	onSelectionChange: (UserDatabase.User) -> Unit,
	onActionEdit: () -> Unit = {}
) {
	var expanded by remember { mutableStateOf(false) }

	if (!showProfileActions && hideIfSingleProfile && users.size <= 1) return

	IconButton(onClick = { expanded = true }) {
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
internal fun DropdownMenuDivider() {
	Divider(modifier = Modifier.padding(vertical = 8.dp))
}
