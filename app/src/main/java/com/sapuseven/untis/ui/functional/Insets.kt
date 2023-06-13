package com.sapuseven.untis.ui.functional

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun insetsPaddingValues() = WindowInsets.navigationBars.add(WindowInsets.ime).asPaddingValues()

@Composable
fun Modifier.bottomInsets() = this.then(Modifier.navigationBarsPadding().imePadding())
