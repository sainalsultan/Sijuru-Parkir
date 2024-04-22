package com.sijuru.core.di.modules

import android.content.Context
import com.sijuru.core.utils.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ContextModule {

    @Singleton
    @Provides
    fun provideSessionManager(@ApplicationContext context: Context) : SessionManager {
        return SessionManager(context)
    }

}