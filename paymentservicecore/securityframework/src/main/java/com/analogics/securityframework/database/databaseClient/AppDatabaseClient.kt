package com.eazypaytech.securityframework.database.databaseClient

import androidx.room.Database
import androidx.room.RoomDatabase
import com.eazypaytech.securityframework.database.dao.IBatchDao
import com.eazypaytech.securityframework.database.dao.ITxnDao
import com.eazypaytech.securityframework.database.dao.IUserManagementDao
import com.eazypaytech.securityframework.database.entity.BatchEntity
import com.eazypaytech.securityframework.database.entity.TxnEntity
import com.eazypaytech.securityframework.database.entity.UserManagementEntity


//@Database(entities = [TxnDtlsEntity::class], version = 1)
//abstract class AppDatabaseClient : RoomDatabase() {
//    abstract fun getTxnDao(): ITxnDao?
//
//    companion object {
//        @Volatile
//        private var INSTANCE: AppDatabaseClient? = null
//
//        fun getDatabase(context: Context): AppDatabaseClient? {
//            if (INSTANCE == null) {
//                synchronized(AppDatabaseClient::class.java) {
//                    if (INSTANCE == null) {
//                        INSTANCE = Room.databaseBuilder(
//                            context.applicationContext,
//                            AppDatabaseClient::class.java, DBConstant.TPAYDB
//                        )
//                            .build()
//                    }
//                }
//            }
//            return INSTANCE
//        }
//    }
//}

@Database(entities = [BatchEntity::class,TxnEntity::class, UserManagementEntity::class], version = 1.1.toInt())
abstract class AppDatabaseClient : RoomDatabase() {
    abstract fun getBatchDao(): IBatchDao
    abstract fun getTxnDao(): ITxnDao
    abstract  fun getUserManagement():IUserManagementDao
}