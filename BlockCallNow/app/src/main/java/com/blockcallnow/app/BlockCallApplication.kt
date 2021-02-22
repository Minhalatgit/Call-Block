package com.blockcallnow.app

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.telephony.CarrierConfigManager
import android.util.Log
import androidx.appcompat.mms.CarrierConfigValuesLoader
import androidx.appcompat.mms.MmsManager
import androidx.room.Room
import com.android.messaging.Factory
import com.android.messaging.FactoryImpl.register
import com.android.messaging.receiver.SmsReceiver
import com.android.messaging.sms.ApnDatabase
import com.android.messaging.sms.BugleApnSettingsLoader
import com.android.messaging.sms.BugleUserAgentInfoLoader
import com.android.messaging.sms.MmsConfig
import com.android.messaging.ui.ConversationDrawables
import com.android.messaging.util.*
import com.blockcallnow.R
import com.blockcallnow.data.network.WebFactory
import com.blockcallnow.data.room.AppDatabase
import com.google.common.annotations.VisibleForTesting

class BlockCallApplication : Application(), Thread.UncaughtExceptionHandler {

    private var sSystemUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

    val api by lazy {
        WebFactory.getApiService()
    }
    val api2 by lazy {
        WebFactory.getApiService2()
    }

    val db: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "callblocknow.db"
        )
            .allowMainThreadQueries()
            .build()
    }

    override fun onCreate() {
        Trace.beginSection("app.onCreate")
        super.onCreate()
        ctx = this

        // Note onCreate is called in both test and real application environments
        if (!isRunningTests()) {
            // Only create the factory if not running tests
            register(applicationContext, this)
        } else {
            LogUtil.e(
                TAG,
                "BugleApplication.onCreate: FactoryImpl.register skipped for test run"
            )
        }

        sSystemUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
        Trace.endSection()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Update conversation drawables when changing writing systems
        // (Right-To-Left / Left-To-Right)
        ConversationDrawables.get().updateDrawables()
    }

    fun initializeSync(factory: Factory) {
        Trace.beginSection("app.initializeSync")
        val context = factory.applicationContext
        val bugleGservices = factory.bugleGservices
        val buglePrefs = factory.applicationPrefs
        val dataModel = factory.dataModel
        val carrierConfigValuesLoader: CarrierConfigValuesLoader = factory.carrierConfigValuesLoader
        maybeStartProfiling()
        updateAppConfig(context)

        // Initialize MMS lib
        initMmsLib(context, bugleGservices, carrierConfigValuesLoader)
        // Initialize APN database
        ApnDatabase.initializeAppContext(context)
        // Fixup messages in flight if we crashed and send any pending
        dataModel.onApplicationCreated()
        // Register carrier config change receiver
        if (OsUtil.isAtLeastM()) {
            registerCarrierConfigChangeReceiver(context)
        }
        Trace.endSection()
    }

    // Called from thread started in FactoryImpl.register() (i.e. not run in tests)
    fun initializeAsync(factory: Factory) {
        // Handle shared prefs upgrade & Load MMS Configuration
        Trace.beginSection("app.initializeAsync")
        maybeHandleSharedPrefsUpgrade(factory)
        MmsConfig.load()
        Trace.endSection()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (LogUtil.isLoggable(TAG, LogUtil.DEBUG)) {
            LogUtil.d(TAG, "BugleApplication.onLowMemory")
        }
        Factory.get().reclaimMemory()
    }

    override fun uncaughtException(thread: Thread, ex: Throwable?) {
        val background = mainLooper.thread !== thread
        if (background) {
            LogUtil.e(
                TAG,
                "Uncaught exception in background thread $thread", ex
            )
            val handler = Handler(mainLooper)
            handler.post { sSystemUncaughtExceptionHandler!!.uncaughtException(thread, ex) }
        } else {
            sSystemUncaughtExceptionHandler!!.uncaughtException(thread, ex)
        }
    }

    private fun maybeStartProfiling() {
        // App startup profiling support. To use it:
        //  adb shell setprop log.tag.BugleProfile DEBUG
        //  #   Start the app, wait for a 30s, download trace file:
        //  adb pull /data/data/com.android.messaging/cache/startup.trace /tmp
        //  # Open trace file (using adt/tools/traceview)
        if (Log.isLoggable(LogUtil.PROFILE_TAG, Log.DEBUG)) {
            // Start method tracing with a big enough buffer and let it run for 30s.
            // Note we use a logging tag as we don't want to wait for gservices to start up.
            val file = DebugUtils.getDebugFile("startup.trace", true)
            if (file != null) {
                Debug.startMethodTracing(file.absolutePath, 160 * 1024 * 1024)
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        Debug.stopMethodTracing()
                        // Allow world to see trace file
                        DebugUtils.ensureReadable(file)
                        LogUtil.d(
                            LogUtil.PROFILE_TAG,
                            "Tracing complete - "
                                    + file.absolutePath
                        )
                    }, 30000
                )
            }
        }
    }

    private fun maybeHandleSharedPrefsUpgrade(factory: Factory) {
        val existingVersion = factory.applicationPrefs.getInt(
            BuglePrefsKeys.SHARED_PREFERENCES_VERSION,
            BuglePrefsKeys.SHARED_PREFERENCES_VERSION_DEFAULT
        )
        val targetVersion = getString(R.string.pref_version).toInt()
        if (targetVersion > existingVersion) {
            LogUtil.i(
                LogUtil.BUGLE_TAG, "Upgrading shared prefs from " + existingVersion +
                        " to " + targetVersion
            )
            try {
                // Perform upgrade on application-wide prefs.
                factory.applicationPrefs.onUpgrade(existingVersion, targetVersion)
                // Perform upgrade on each subscription's prefs.
                PhoneUtils.forEachActiveSubscription { subId ->
                    factory.getSubscriptionPrefs(subId)
                        .onUpgrade(existingVersion, targetVersion)
                }
                factory.applicationPrefs.putInt(
                    BuglePrefsKeys.SHARED_PREFERENCES_VERSION,
                    targetVersion
                )
            } catch (ex: Exception) {
                // Upgrade failed. Don't crash the app because we can always fall back to the
                // default settings.
                LogUtil.e(LogUtil.BUGLE_TAG, "Failed to upgrade shared prefs", ex)
            }
        } else if (targetVersion < existingVersion) {
            // We don't care about downgrade since real user shouldn't encounter this, so log it
            // and ignore any prefs migration.
            LogUtil.e(
                LogUtil.BUGLE_TAG, "Shared prefs downgrade requested and ignored. " +
                        "oldVersion = " + existingVersion + ", newVersion = " + targetVersion
            )
        }
    }

    companion object {
        private fun registerCarrierConfigChangeReceiver(context: Context) {
            context.registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    LogUtil.i(TAG, "Carrier config changed. Reloading MMS config.")
                    MmsConfig.loadAsync()
                }
            }, IntentFilter(CarrierConfigManager.ACTION_CARRIER_CONFIG_CHANGED))
        }

        private fun initMmsLib(
            context: Context, bugleGservices: BugleGservices,
            carrierConfigValuesLoader: CarrierConfigValuesLoader
        ) {
            MmsManager.setApnSettingsLoader(BugleApnSettingsLoader(context))
            MmsManager.setCarrierConfigValuesLoader(carrierConfigValuesLoader)
            MmsManager.setUserAgentInfoLoader(BugleUserAgentInfoLoader(context))
            MmsManager.setUseWakeLock(true)
            // If Gservices is configured not to use mms api, force MmsManager to always use
            // legacy mms sending logic
            MmsManager.setForceLegacyMms(
                !bugleGservices.getBoolean(
                    BugleGservicesKeys.USE_MMS_API_IF_PRESENT,
                    BugleGservicesKeys.USE_MMS_API_IF_PRESENT_DEFAULT
                )
            )
            bugleGservices.registerForChanges {
                MmsManager.setForceLegacyMms(
                    !bugleGservices.getBoolean(
                        BugleGservicesKeys.USE_MMS_API_IF_PRESENT,
                        BugleGservicesKeys.USE_MMS_API_IF_PRESENT_DEFAULT
                    )
                )
            }
        }

        fun updateAppConfig(context: Context?) {
            // Make sure we set the correct state for the SMS/MMS receivers
            SmsReceiver.updateSmsReceiveHandler(context)
        }

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