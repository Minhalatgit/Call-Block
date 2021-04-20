package com.call.blockcallnow.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BlockContact::class, LogContact::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): BlockContactDao
}
