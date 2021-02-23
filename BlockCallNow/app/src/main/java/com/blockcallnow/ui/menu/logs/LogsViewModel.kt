package com.blockcallnow.ui.menu.logs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blockcallnow.data.room.AppDatabase
import com.blockcallnow.data.room.LogContact

class LogsViewModel : ViewModel() {

    private val _logList = MutableLiveData<List<LogContact>>()
    val logList: LiveData<List<LogContact>>
        get() = _logList


    fun getCallLogs(db: AppDatabase): LiveData<List<LogContact>> {
        return db.contactDao().getLogs(true)
    }

    fun getMessageLogs(db: AppDatabase): LiveData<List<LogContact>> {
        return db.contactDao().getLogs(false)
    }
}