package com.call.blockcallnow.app

import android.app.Application
import androidx.room.Room
import com.call.blockcallnow.data.network.WebFactory
import com.call.blockcallnow.data.room.AppDatabase
import com.google.common.annotations.VisibleForTesting

class BlockCallApplication : Application() {

    val api by lazy {
        WebFactory.getApiService()
    }
    val api2 by lazy {
        WebFactory.getApiService2()
    }
    val twilioApi by lazy {
        WebFactory.getTwilioService()
    }

    val db: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "callblocknow.db"
        ).allowMainThreadQueries().build()
    }

    override fun onCreate() {
        super.onCreate()
        ctx = this

    }

    companion object {
        private var sRunningTests = false

        @VisibleForTesting
        protected fun setTestsRunning() {
            sRunningTests = true
        }

        /**
         * @return true if we're running unit tests.
         */
        public fun isRunningTests(): Boolean {
            return sRunningTests
        }

        val TAG: String = BlockCallApplication::class.java.simpleName
        lateinit var ctx: BlockCallApplication
        fun getAppContext(): BlockCallApplication {
            return ctx
        }
    }
}