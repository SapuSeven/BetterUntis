package com.sapuseven.compose.protostore.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree

@Composable
fun Modifier.disabled(disabled: Boolean = true): Modifier =
	if (disabled) this.alpha(0.38f) else this

@Composable
fun Modifier.conditional(
	condition: Boolean,
	modifier: @Composable Modifier.() -> Modifier
): Modifier =
	if (condition) then(modifier(Modifier)) else this

@Composable
fun <T> Modifier.ifNotNull(
	value: T?,
	modifier: @Composable Modifier.(value: T) -> Modifier
): Modifier =
	value?.let { then(modifier(Modifier, it)) } ?: this

// Inspired by https://bryanherbst.com/2021/04/13/compose-autofill/
@ExperimentalComposeUiApi
@Composable
fun Modifier.autofill(
	autofillTypes: List<AutofillType>,
	onFill: ((String) -> Unit),
): Modifier {
	val autofill = LocalAutofill.current
	val autofillNode = AutofillNode(onFill = onFill, autofillTypes = autofillTypes)
	LocalAutofillTree.current += autofillNode

	return this
		.onGloballyPositioned {
			autofillNode.boundingBox = it.boundsInWindow()
		}
		.onFocusChanged { focusState ->
			autofill?.run {
				if (focusState.isFocused) {
					requestAutofillForNode(autofillNode)
				} else {
					cancelAutofillForNode(autofillNode)
				}
			}
		}
}
