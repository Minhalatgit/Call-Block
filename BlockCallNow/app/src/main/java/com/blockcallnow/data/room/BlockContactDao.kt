package com.blockcallnow.data.room

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

    @Delete
    fun deleteContact(contact: BlockContact)

    @Query("DELETE FROM block_contacts")
    fun clearTable()

//    @Insert
//    fun insertLog()
//
//    @Delete
//    fun deleteLog()
//
//    @Query("")
//    fun deleteAllLogs()

}