package com.analogics.securityframework.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import com.analogics.securityframework.database.entity.BatchEntity

@Dao
interface IBatchDao {

    @Insert
    suspend fun insert(vararg batchEntity: BatchEntity)

    @Update
    suspend fun update(vararg batchEntity: BatchEntity)
}