package com.capystick.app.update

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object InAppUpdateModule {
    @Provides
    @ActivityScoped
    fun provideInAppUpdateCoordinator(client: InAppUpdateClient): InAppUpdateCoordinator = DefaultInAppUpdateCoordinator(client)

    @Provides
    @ActivityScoped
    fun provideInAppUpdateClient(
        @ActivityContext context: Context,
    ): InAppUpdateClient = PlayInAppUpdateClient(context)
}
