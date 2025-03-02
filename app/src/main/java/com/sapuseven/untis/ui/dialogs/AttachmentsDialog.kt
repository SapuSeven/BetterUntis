package com.sapuseven.untis.ui.dialogs


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.R
import com.sapuseven.untis.api.model.untis.Attachment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentsDialog(
	attachments: List<Attachment>,
	onDismiss: () -> Unit
) {
	val uriHandler = LocalUriHandler.current

	BasicAlertDialog(onDismissRequest = onDismiss) {
		Surface(
			modifier = Modifier.padding(vertical = 24.dp),
			shape = AlertDialogDefaults.shape,
			color = AlertDialogDefaults.containerColor,
			tonalElevation = AlertDialogDefaults.TonalElevation
		) {
			Column {
				ProvideTextStyle(value = MaterialTheme.typography.headlineSmall) {
					Box(
						Modifier
							.padding(top = 24.dp, bottom = 16.dp)
							.padding(horizontal = 24.dp)
					) {
						Text(text = stringResource(id = R.string.infocenter_messages_attachments))
					}
				}

				LazyColumn(
					modifier = Modifier.fillMaxWidth()
				) {
					items(attachments) {
						ListItem(
							colors = ListItemDefaults.colors(containerColor = Color.Transparent), // No idea why, but ListItem has the wrong background color otherwise
							headlineContent = { Text(it.name) },
							leadingContent = {
								Icon(
									painter = painterResource(id = R.drawable.infocenter_attachment),
									contentDescription = null
								)
							},
							modifier = Modifier
								.clickable {
									uriHandler.openUri(it.url)
								}
								.padding(horizontal = 8.dp)
						)
					}
				}

				TextButton(
					modifier = Modifier
						.align(Alignment.End)
						.padding(top = 16.dp, bottom = 24.dp)
						.padding(horizontal = 24.dp),
					onClick = onDismiss
				) {
					Text(text = stringResource(id = R.string.all_close))
				}
			}
		}
	}
}
