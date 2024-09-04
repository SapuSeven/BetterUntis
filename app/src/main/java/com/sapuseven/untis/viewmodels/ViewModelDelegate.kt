package com.sapuseven.untis.viewmodels

import kotlinx.coroutines.CoroutineScope

interface ViewModelDelegate {
	val delegateScope: CoroutineScope
}
