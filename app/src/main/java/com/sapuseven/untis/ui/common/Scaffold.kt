package com.sapuseven.untis.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.sapuseven.untis.ui.functional.None

// Wrapper for Material 3 Scaffold to remove contentWindowInsets by default
@ExperimentalMaterial3Api
@Composable
fun AppScaffold(
	modifier: Modifier = Modifier,
	topBar: @Composable () -> Unit = {},
	bottomBar: @Composable () -> Unit = {},
	snackbarHost: @Composable () -> Unit = {},
	floatingActionButton: @Composable () -> Unit = {},
	floatingActionButtonPosition: FabPosition = FabPosition.End,
	containerColor: Color = MaterialTheme.colorScheme.background,
	contentColor: Color = contentColorFor(containerColor),
	contentWindowInsets: WindowInsets = WindowInsets.None,
	content: @Composable (PaddingValues) -> Unit
) = Scaffold(
	modifier = modifier,
	topBar = topBar,
	bottomBar = bottomBar,
	snackbarHost = snackbarHost,
	floatingActionButton = floatingActionButton,
	floatingActionButtonPosition = floatingActionButtonPosition,
	containerColor = containerColor,
	contentColor = contentColor,
	contentWindowInsets = contentWindowInsets,
	content = content
)
