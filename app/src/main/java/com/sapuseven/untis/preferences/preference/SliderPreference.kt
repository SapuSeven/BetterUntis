package com.sapuseven.untis.preferences.preference

import androidx.annotation.IntRange
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.preferences.UntisPreferenceDataStore
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

@Composable
fun SliderPreference(
	valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
	@IntRange(from = 0) steps: Int = 0,
	title: (@Composable () -> Unit),
	summary: (@Composable () -> Unit)? = null,
	icon: (@Composable () -> Unit)? = null,
	showSeekBarValue: Boolean = false,
	dependency: UntisPreferenceDataStore<*>? = null,
	dataStore: UntisPreferenceDataStore<Float>
) {
	val scope = rememberCoroutineScope()

	Preference(
		title = title,
		summary = summary,
		icon = icon,
		dependency = dependency,
		dataStore = dataStore,
		supportingContent = { value, enabled ->
			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				Slider(
					modifier = Modifier.weight(1f),
					valueRange = valueRange,
					steps = steps,
					value = value,
					onValueChange = { scope.launch { dataStore.saveValue(it) } },
					enabled = enabled
				)

				if (showSeekBarValue)
					Text(
						modifier = Modifier
							.padding(start = 8.dp)
							.defaultMinSize(minWidth = 24.dp),
						style = MaterialTheme.typography.labelLarge,
						textAlign = TextAlign.End,
						text = DecimalFormat(
							"0",
							DecimalFormatSymbols.getInstance(Locale.ENGLISH)
						).run {
							maximumFractionDigits = 2
							isGroupingUsed = false
							format(value)
						}
					)
			}
		}
	)
}
