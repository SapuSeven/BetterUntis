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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

//TODO: Extract string ressources

@Composable
fun TimePickerDialog(
	title: String = "Select Time",
	onCancel: () -> Unit,
	onConfirm: () -> Unit,
	toggle: @Composable RowScope.() -> Unit = {},
	content: @Composable () -> Unit,
) {
	Dialog(
		onDismissRequest = onCancel,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		Surface(
			shape = MaterialTheme.shapes.extraLarge,
			tonalElevation = 6.dp,
			modifier = Modifier
				.width(IntrinsicSize.Min)
				.background(
					shape = MaterialTheme.shapes.extraLarge,
					color = MaterialTheme.colorScheme.surface
				),
		) {
			Column(
				modifier = Modifier.padding(24.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Text(
					modifier = Modifier
						.fillMaxWidth()
						.padding(bottom = 20.dp),
					text = title,
					style = MaterialTheme.typography.labelMedium
				)
				content()
				Row(
					modifier = Modifier
						.height(40.dp)
						.fillMaxWidth()
				) {
					toggle()
					Spacer(modifier = Modifier.weight(1f))
					TextButton(
						onClick = onCancel
					) { Text("Cancel") }
					TextButton(
						onClick = onConfirm
					) { Text("OK") }
				}
			}
		}
	}
}



