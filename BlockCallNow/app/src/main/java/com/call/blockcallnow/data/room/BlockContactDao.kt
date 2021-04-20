package com.call.blockcallnow.data.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BlockContactDao {
    @Query("SELECT * FROM block_contacts")
    fun getAllBlockedContacts(): List<BlockContact>

    @Query("SELECT * FROM block_contacts where block_number=:number")
    fun getBlockContactFromNumber(number: String?): BlockContact?

    @Update
    fun updateBlockContact(vararg users: BlockContact)

    @Insert
    fun insertAll(contact: List<BlockContact>)

    @Query("UPDATE block_contacts set  ` times_called` = :timesCalled , last_call = :lastCall")
    fun updateBlockContact(lastCall: Long, timesCalled: Int)

    @Query("SELECT name FROM block_contacts where block_number=:number")
    fun getNameFromNumber(number: String): String?

    @Delete
    fun deleteContact(contact: BlockContact)

    @Query("DELETE FROM block_contacts")
    fun clearTable()

    @Insert
    fun insertLog(logContact: LogContact)

    @Query("DELETE FROM call_log_table WHERE is_call = 1")
    fun deleteCallLogs()

    @Query("DELETE FROM call_log_table WHERE is_call = 0")
    fun deleteMessageLogs()

    @Query("DELETE FROM call_log_table")
    fun deleteAllLogs()

    @Query("SELECT * FROM call_log_table WHERE is_call=:isCall ORDER BY time_stamp DESC")
    fun getLogs(isCall: Boolean): LiveData<List<LogContact>>
}