package com.call.blockcallnow.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_log_table")
data class LogContact @JvmOverloads constructor(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @ColumnInfo(name = "time_stamp")
    var currentTime: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "name")
    var name: String?,
    @ColumnInfo(name = "phone_number")
    var phoneNumber: String,
    @ColumnInfo(name = "is_call")
    var isCall: Boolean,
)