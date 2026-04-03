package com.eazypaytech.securityframework.database.databaseClient

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.eazypaytech.securityframework.database.dao.IBatchDao
import com.eazypaytech.securityframework.database.dao.ITxnDao
import com.eazypaytech.securityframework.database.dao.IUserManagementDao
import com.eazypaytech.securityframework.database.entity.BatchEntity
import com.eazypaytech.securityframework.database.entity.TxnEntity
import com.eazypaytech.securityframework.database.entity.UserManagementEntity

@Database(entities = [BatchEntity::class,TxnEntity::class, UserManagementEntity::class], version = 2.toInt())
abstract class AppDatabaseClient : RoomDatabase() {
    abstract fun getBatchDao(): IBatchDao
    abstract fun getTxnDao(): ITxnDao
    abstract  fun getUserManagement():IUserManagementDao
}

val MIGRATION_1_2 = object : Migration(1.1.toInt(), 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE TxnTable ADD COLUMN Header1 TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE TxnTable ADD COLUMN Header2 TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE TxnTable ADD COLUMN Header3 TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE TxnTable ADD COLUMN Header4 TEXT DEFAULT ''")

        database.execSQL("ALTER TABLE TxnTable ADD COLUMN Footer1 TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE TxnTable ADD COLUMN Footer2 TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE TxnTable ADD COLUMN Footer3 TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE TxnTable ADD COLUMN Footer4 TEXT DEFAULT ''")
    }
}