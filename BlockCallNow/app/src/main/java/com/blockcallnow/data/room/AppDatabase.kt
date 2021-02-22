package com.blockcallnow.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BlockContact::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): BlockContactDao
}
