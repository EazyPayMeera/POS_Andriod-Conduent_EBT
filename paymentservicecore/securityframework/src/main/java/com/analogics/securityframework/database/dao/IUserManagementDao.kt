package com.eazypaytech.securityframework.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.eazypaytech.securityframework.database.entity.UserManagementEntity

@Dao
interface IUserManagementDao {

    @Insert
    suspend fun insert(vararg userManagementEntity: UserManagementEntity)

    @Update
    suspend fun update(vararg userManagementEntity: UserManagementEntity)

    @Query("SELECT * FROM UserTable WHERE userId = :userId")
    suspend fun getUserDetails(userId: String): UserManagementEntity?

    @Query("SELECT * FROM UserTable")
    suspend fun getAllUserDetails(): List<UserManagementEntity>?

    @Query("SELECT COUNT(userId) FROM UserTable")
    suspend fun getUserCount(): Int

    @Query("SELECT COUNT(userId) FROM UserTable WHERE userType = 'ADMIN'")
    suspend fun getAdminCount(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM UserTable WHERE userId = :userId AND userType = 'ADMIN')")
    suspend fun isAdmin(userId: String): Boolean

    @Query("SELECT userId FROM UserTable")
    suspend fun getUserList(): List<String?>

    @Query("DELETE FROM UserTable WHERE userId = :userId")
    suspend fun deleteUser(userId: String)

    @Query("SELECT password FROM UserTable WHERE userId = :userId")
    suspend fun fetchPassword(userId: String): String?
}