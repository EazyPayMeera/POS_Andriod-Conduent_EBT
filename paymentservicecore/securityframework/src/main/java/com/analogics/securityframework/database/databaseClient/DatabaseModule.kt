package com.eazypaytech.securityframework.database.databaseClient

import android.content.Context
import androidx.room.Room
import com.eazypaytech.securityframework.database.dao.IBatchDao
import com.eazypaytech.securityframework.database.dao.ITxnDao
import com.eazypaytech.securityframework.database.dao.IUserManagementDao
import com.eazypaytech.securityframework.database.dbConstant.DBConstant
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabaseClient {
        return Room.databaseBuilder(
            appContext,
            AppDatabaseClient::class.java,
            DBConstant.TXN_DB_NAME
        ).build()
    }

    @Provides
    fun provideBatchDao(database: AppDatabaseClient): IBatchDao {
        return database.getBatchDao()
    }

    @Provides
    fun provideTxnDao(database: AppDatabaseClient): ITxnDao {
        return database.getTxnDao()
    }
    @Provides
    fun provideUserManagement(database: AppDatabaseClient): IUserManagementDao {
        return database.getUserManagement()
    }
}
