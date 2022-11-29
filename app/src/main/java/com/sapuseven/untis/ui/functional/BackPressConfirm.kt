package com.sapuseven.untis.ui.functional

import androidx.activity.compose.BackHandler
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.sapuseven.untis.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BackPressConfirm(
	snackbarHostState: SnackbarHostState,
	doublePressDelay: Long = 2000
) {
	var nextBackPressIsDouble by remember { mutableStateOf(false) }
	val errorText = stringResource(id = R.string.main_press_back_double)

	val scope = rememberCoroutineScope()

	BackHandler(
		enabled = !nextBackPressIsDouble,
	) {
		nextBackPressIsDouble = true

		scope.launch {
			val job = scope.launch {
				snackbarHostState.showSnackbar(
					message = errorText,
					duration = SnackbarDuration.Indefinite
				)
			}
			delay(doublePressDelay)
			job.cancel()
			nextBackPressIsDouble = false
		}
	}
}
