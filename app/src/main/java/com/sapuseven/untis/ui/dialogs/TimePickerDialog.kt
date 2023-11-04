/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.sapuseven.untis.ui.dialogs

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sapuseven.untis.R
import org.joda.time.LocalTime
import java.text.DateFormat


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
	context: Context? = null,
	initialSelection: LocalTime = LocalTime.now(),
	onDismiss: () -> Unit,
	onTimeSelected: (LocalTime) -> Unit,
) {
	Dialog(onDismissRequest = onDismiss) {
		val timePickerState = rememberTimePickerState(
			initialHour = initialSelection.hourOfDay,
			initialMinute = initialSelection.minuteOfHour,
			is24Hour = context?.let { android.text.format.DateFormat.is24HourFormat(context) } ?: true
		)

		Surface(
			shape = AlertDialogDefaults.shape,
			color = AlertDialogDefaults.containerColor
		) {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Text(
					text = stringResource(R.string.all_timepicker_select).uppercase(),
					style = MaterialTheme.typography.labelSmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 24.dp, vertical = 16.dp)
						.padding(bottom = 8.dp)
				)

				TimePicker(
					state = timePickerState
				)

				Row(
					horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 24.dp, vertical = 16.dp)
				) {
					TextButton(
						onClick = onDismiss
					) {
						Text(text = stringResource(id = R.string.all_cancel))
					}

					TextButton(
						onClick = { onTimeSelected(LocalTime(timePickerState.hour, timePickerState.minute)) }
					) {
						Text(text = stringResource(id = R.string.all_ok))
					}
				}
			}
		}
	}
}



