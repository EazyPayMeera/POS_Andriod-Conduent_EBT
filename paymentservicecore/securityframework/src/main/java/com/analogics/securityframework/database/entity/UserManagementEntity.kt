package com.analogics.securityframework.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "UserTable")
data class UserManagementEntity(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,

    /* User Info */
    @ColumnInfo(name = "userId") var userId: String? = null,
    @ColumnInfo(name = "userType") var userType: String? = null,
    @ColumnInfo(name = "password") var password: String? = null,
    ) : Serializable