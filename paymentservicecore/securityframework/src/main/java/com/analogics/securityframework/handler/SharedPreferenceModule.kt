package com.eazypaytech.securityframework.handler

import android.content.Context
import com.eazypaytech.securityframework.preferences.SecuredSharedPrefManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object SharedPreferenceModule {
    @Provides
    @Singleton
    fun provideSecuredSharedPrefManager(@ApplicationContext context: Context): SecuredSharedPrefManager {
        return SecuredSharedPrefManager(context, "AppPrefs")
    }
}