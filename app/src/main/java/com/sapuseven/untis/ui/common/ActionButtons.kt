package com.sapuseven.untis.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.R
import com.sapuseven.untis.persistence.entity.User

@Composable
fun ProfileSelectorAction(
	users: List<User>,
	currentSelection: User? = null, // TODO: remove default and remove id below once not used anymore
	currentSelectionId: Long? = null,
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
					if (currentSelection?.id == it.id) {
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
