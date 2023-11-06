package com.sapuseven.untis.ui.functional

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

@Composable
fun insetsPaddingValues() = WindowInsets.navigationBars.add(WindowInsets.ime).asPaddingValues()

fun Modifier.bottomInsets() = this.then(Modifier.navigationBarsPadding().imePadding())

val WindowInsets.Companion.None: WindowInsets
	get() = WindowInsets(0, 0, 0, 0)
