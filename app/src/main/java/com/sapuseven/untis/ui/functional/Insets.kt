package com.sapuseven.untis.ui.functional

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun insetsPaddingValues() = WindowInsets.navigationBars.add(WindowInsets.ime).asPaddingValues()

fun Modifier.bottomInsets() = this.then(Modifier.navigationBarsPadding().imePadding())

val WindowInsets.Companion.None: WindowInsets
	get() = WindowInsets(0, 0, 0, 0)
