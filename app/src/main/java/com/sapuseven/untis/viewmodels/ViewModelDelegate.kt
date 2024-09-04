package com.sapuseven.untis.viewmodels

import com.sapuseven.untis.ui.navigation.AppRoutes
import kotlinx.coroutines.CoroutineScope

abstract class ViewModelDelegate {
	lateinit var delegateScope: CoroutineScope

	fun init(delegateScope: CoroutineScope) {
		this.delegateScope = delegateScope
		init()
	}

	open fun init() {};
}

interface ViewModelDelegateFactory<out D : ViewModelDelegate> {
	fun create(delegateScope: CoroutineScope): D
}
