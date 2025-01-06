package com.sapuseven.untis.ui.functional

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable

/**
 * This class allows you to define a string resource with optional format args in a non-composable context.
 * It can then be converted to an actual string inside a composable function using the `stringResource()` method.
 */
class StringResourceDescriptor(
	@StringRes val resourceId: Int,
	vararg val args: Any
) {
	@Composable
	fun stringResource() = androidx.compose.ui.res.stringResource(resourceId, args)
}
