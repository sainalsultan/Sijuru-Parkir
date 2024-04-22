package com.sijuru.core.di.modules

import android.content.Context
import androidx.fragment.app.Fragment
import com.sijuru.core.utils.FragmentHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(FragmentComponent::class)
object AppModule {

    @Provides
    fun provideFragmentHelper(fragment: Fragment) : FragmentHelper {
        return FragmentHelper(fragment)
    }

}