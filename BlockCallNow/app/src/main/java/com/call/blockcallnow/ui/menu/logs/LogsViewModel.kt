package com.call.blockcallnow.ui.menu.logs

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.call.blockcallnow.data.room.AppDatabase
import com.call.blockcallnow.data.room.LogContact

class LogsViewModel : ViewModel() {

    fun getCallLogs(db: AppDatabase): LiveData<List<LogContact>> {
        return db.contactDao().getLogs(true)
    }

    fun getMessageLogs(db: AppDatabase): LiveData<List<LogContact>> {
        return db.contactDao().getLogs(false)
    }
}