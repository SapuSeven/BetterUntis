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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sapuseven.untis.R
import com.sapuseven.untis.models.untis.UntisAttachment

@Composable
fun AttachmentsDialog(
	attachments: List<UntisAttachment>,
	onDismiss: () -> Unit
) {
	val uriHandler = LocalUriHandler.current

	Dialog(onDismissRequest = onDismiss) {
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
