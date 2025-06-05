package com.sapuseven.untis.modules

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
	// Nothing extra needed here; Hilt will see @Subcomponent(UserComponent) automatically.
	// If you had any top-level, app-wide @Provides, put them here.
}
