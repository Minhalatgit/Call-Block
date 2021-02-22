package com.blockcallnow.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_log_table")
data class LogContact(
    @PrimaryKey(autoGenerate = true)
    var id: Int = -1,
    var name: String,
    @ColumnInfo(name = "phone_number")
    var phoneNumber: String,

    )