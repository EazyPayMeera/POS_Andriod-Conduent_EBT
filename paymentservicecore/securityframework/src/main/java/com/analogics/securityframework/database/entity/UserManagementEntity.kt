package com.analogics.securityframework.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "UserTable")
data class UserManagementEntity(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,

    /* Batch Info */
    @ColumnInfo(name = "userId") var userId: String? = "10000",
    @ColumnInfo(name = "userRole") var userRole: String? = "12222",
    @ColumnInfo(name = "password") var password: String? = "11111",
    //pos config
    ) : Serializable