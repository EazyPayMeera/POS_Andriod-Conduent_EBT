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

/**
 * Hilt module responsible for providing Room database and DAO dependencies.
 *
 * Scope:
 * - Singleton database instance across the application
 * - Centralized access for DAOs (Batch, Transaction, User)
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {


    /**
     * Provides singleton Room database instance.
     *
     * Configuration:
     * - Database name from constants
     * - Migration support enabled
     * - ⚠ fallbackToDestructiveMigration enabled (DANGEROUS in production)
     */
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

    /**
     * Provides Batch DAO for batch lifecycle operations.
     */
    @Provides
    fun provideBatchDao(database: AppDatabaseClient): IBatchDao {
        return database.getBatchDao()
    }

    /**
     * Provides Transaction DAO for transaction processing.
     */
    @Provides
    fun provideTxnDao(database: AppDatabaseClient): ITxnDao {
        return database.getTxnDao()
    }

    /**
     * Provides User Management DAO for clerk/admin operations.
     */
    @Provides
    fun provideUserManagement(database: AppDatabaseClient): IUserManagementDao {
        return database.getUserManagement()
    }
}
