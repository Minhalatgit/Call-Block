package com.call.blockcallnow.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_contacts")
data class BlockContact(

    @PrimaryKey(autoGenerate = true) val contactid: Int,
    @ColumnInfo(name = "name") var name: String?,
    @ColumnInfo(name = "number") var number: String?,
    @ColumnInfo(name = "block_number") var blockNumber: String?,
    @ColumnInfo(name = "block_status") var blockStatus: String,
    @ColumnInfo(name = "image") var uri: String?,
    @ColumnInfo(name = "last_call") var last_call: Int? ,
    @ColumnInfo(name = " times_called") var timesCalled: Int
)
