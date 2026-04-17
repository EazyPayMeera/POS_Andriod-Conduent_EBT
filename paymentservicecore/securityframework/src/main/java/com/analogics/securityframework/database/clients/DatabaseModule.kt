package com.analogics.securityframework.database.clients

import android.content.Context
import androidx.room.Room
import com.analogics.securityframework.database.dao.IBatchDao
import com.analogics.securityframework.database.dao.ITxnDao
import com.analogics.securityframework.database.dao.IUserManagementDao
import com.analogics.securityframework.database.constants.DBConstant
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
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
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
