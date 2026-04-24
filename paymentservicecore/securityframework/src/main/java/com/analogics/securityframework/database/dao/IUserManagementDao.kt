package com.analogics.securityframework.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.analogics.securityframework.database.entity.UserManagementEntity

/**
 * DAO responsible for user management operations.
 *
 * Handles:
 * - User CRUD operations
 * - Role-based checks (ADMIN)
 * - Authentication-related queries (⚠ sensitive)
 */
@Dao
interface IUserManagementDao {

    /**
     * Inserts a new user into database.
     */
    @Insert
    suspend fun insert(vararg userManagementEntity: UserManagementEntity)

    /**
     * Updates existing user details.
     */
    @Update
    suspend fun update(vararg userManagementEntity: UserManagementEntity)

    /**
     * Fetch user details by userId.
     */
    @Query("SELECT * FROM UserTable WHERE userId = :userId")
    suspend fun getUserDetails(userId: String): UserManagementEntity?

    /**
     * Returns all users in system.
     */
    @Query("SELECT * FROM UserTable")
    suspend fun getAllUserDetails(): List<UserManagementEntity>?

    /**
     * Returns total number of users.
     */
    @Query("SELECT COUNT(userId) FROM UserTable")
    suspend fun getUserCount(): Int

    /**
     * Returns number of admin users in system.
     */
    @Query("SELECT COUNT(userId) FROM UserTable WHERE userType = 'ADMIN'")
    suspend fun getAdminCount(): Int

    /**
     * Checks whether a user is ADMIN.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM UserTable WHERE userId = :userId AND userType = 'ADMIN')")
    suspend fun isAdmin(userId: String): Boolean

    /**
     * Returns all user IDs.
     */
    @Query("SELECT userId FROM UserTable")
    suspend fun getUserList(): List<String?>

    /**
     * Deletes user by ID.
     */
    @Query("DELETE FROM UserTable WHERE userId = :userId")
    suspend fun deleteUser(userId: String)

    /**
     * Fetches stored password for user.
     *
     * ⚠ SECURITY RISK:
     * Storing plaintext passwords in DB is NOT secure.
     */
    @Query("SELECT password FROM UserTable WHERE userId = :userId")
    suspend fun fetchPassword(userId: String): String?
}