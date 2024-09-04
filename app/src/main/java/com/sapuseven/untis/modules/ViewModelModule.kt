package com.sapuseven.untis.modules

import com.sapuseven.untis.viewmodels.CloseableCoroutineScope
import com.sapuseven.untis.viewmodels.ElementPickerDelegate
import com.sapuseven.untis.viewmodels.ElementPickerDelegateImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

@Module
@InstallIn(ViewModelComponent::class)
interface ViewModelDelegateModule {
	@Binds
	fun bindElementPickerDelegate(
		impl: ElementPickerDelegateImpl
	): ElementPickerDelegate

	companion object {
		@ViewModelScoped
		@Provides
		fun provideDelegateScope(): CoroutineScope {
			return CloseableCoroutineScope()
		}
	}
}
