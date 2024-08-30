package com.analogics.securityframework.database.databaseClient

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.analogics.securityframework.database.dao.ITxnDao
import com.analogics.securityframework.database.dbConstant.DBConstant
import com.analogics.securityframework.database.entity.TxnDtlsEntity
import kotlin.concurrent.Volatile




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

@Database(entities = [TxnDtlsEntity::class], version = 1.toInt())
abstract class AppDatabaseClient : RoomDatabase() {
    abstract fun getTxnDao(): ITxnDao
}