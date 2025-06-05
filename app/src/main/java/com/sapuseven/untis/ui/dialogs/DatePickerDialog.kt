package com.sapuseven.untis.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.sapuseven.untis.R
import com.sapuseven.untis.ui.datepicker.DatePicker
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DatePickerDialog(
	initialSelection: LocalDate = LocalDate.now(),
	onDismiss: () -> Unit,
	onDateSelected: (date: LocalDate) -> Unit
) {
	Dialog(onDismissRequest = onDismiss) {
		val selectedDay = remember { mutableStateOf(initialSelection) }

		Surface(
			shape = AlertDialogDefaults.shape,
			color = AlertDialogDefaults.containerColor
		) {
			Column {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.background(
							MaterialTheme.colorScheme.surfaceColorAtElevation(
								AlertDialogDefaults.TonalElevation
							)
						)
						.padding(horizontal = 24.dp, vertical = 16.dp)
				) {
					Text(
						text = stringResource(R.string.all_datepicker_select).uppercase(),
						style = MaterialTheme.typography.labelSmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)

					Text(
						text = selectedDay.value.format(DateTimeFormatter.ofPattern("EE, MMM d")),
						style = MaterialTheme.typography.headlineMedium,
						color = MaterialTheme.colorScheme.onSurface,
						modifier = Modifier.padding(top = 16.dp),
						maxLines = 1
					)
				}

				DatePicker(selectedDayState = selectedDay)

				FlowRow(
					mainAxisAlignment = MainAxisAlignment.End,
					mainAxisSpacing = 8.dp,
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 24.dp, vertical = 16.dp)
				) {
					TextButton(
						onClick = { onDateSelected(LocalDate.now()) }
					) {
						Text(text = stringResource(id = R.string.all_dialog_datepicker_button_today))
					}

					Row(
						horizontalArrangement = Arrangement.spacedBy(8.dp)
					) {
						TextButton(
							modifier = Modifier,
							onClick = onDismiss
						) {
							Text(text = stringResource(id = R.string.all_cancel))
						}

						TextButton(
							modifier = Modifier,
							onClick = { onDateSelected(selectedDay.value) }
						) {
							Text(text = stringResource(id = R.string.all_ok))
						}
					}
				}
			}
		}
	}
}
