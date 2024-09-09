package com.sapuseven.compose.protostore.ui.preferences

import androidx.annotation.IntRange
import androidx.compose.foundation.layout.Row
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
import com.google.protobuf.MessageLite
import com.sapuseven.compose.protostore.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun <Model : MessageLite, ModelBuilder : MessageLite.Builder> SliderPreference(
	title: (@Composable () -> Unit),
	summary: (@Composable () -> Unit)? = null,
	//supportingContent: @Composable ((value: Float, enabled: Boolean) -> Unit)? = null,
	leadingContent: (@Composable () -> Unit)? = null,
	trailingContent: @Composable ((value: Float, enabled: Boolean) -> Unit)? = null,
	settingsRepository: SettingsRepository<Model, ModelBuilder>,
	value: (Model) -> Float,
	scope: CoroutineScope = rememberCoroutineScope(),
	enabledCondition: (Model) -> Boolean = { true },
	highlight: Boolean = false,
	valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
	@IntRange(from = 0) steps: Int = 0,
	showSeekBarValue: Boolean = false,
	onValueChange: (ModelBuilder.(value: Float) -> Unit)? = null,
) {
	Preference(
		title = title,
		summary = summary,
		supportingContent = { currentValue, enabled ->
			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				Slider(
					modifier = Modifier.weight(1f),
					valueRange = valueRange,
					steps = steps,
					value = currentValue,
					onValueChange = {
						scope.launch {
							settingsRepository.updateSettings {
								onValueChange?.invoke(this, it)
							}
						}
					},
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
							format(currentValue.toDouble())
						}
					)
			}
		},
		leadingContent = leadingContent,
		trailingContent = trailingContent,
		settingsRepository = settingsRepository,
		value = value,
		scope = scope,
		enabledCondition = enabledCondition,
		highlight = highlight
	)
}
