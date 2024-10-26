package com.analogics.securityframework.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.analogics.securityframework.database.entity.UserManagementEntity

@Dao
interface IUserManagementDao {

    @Insert
    suspend fun insert(vararg userManagementEntity: UserManagementEntity)

    @Update
    suspend fun update(vararg userManagementEntity: UserManagementEntity)

    @Query("SELECT * FROM UserTable WHERE userId = :userId")
    suspend fun getUserDetails(userId: String): UserManagementEntity?

    @Query("SELECT COUNT(userId) FROM UserTable")
    suspend fun getUserCount(): Int
}