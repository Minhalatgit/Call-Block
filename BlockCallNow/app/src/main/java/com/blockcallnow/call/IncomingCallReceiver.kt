package com.blockcallnow.call

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.TELECOM_SERVICE
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.blockcallnow.app.BlockCallApplication
import com.blockcallnow.data.event.BaseNavEvent
import com.blockcallnow.data.model.PhoneNoDetailResponse
import com.blockcallnow.data.network.NetworkHelper
import com.blockcallnow.data.network.WebServices
import com.blockcallnow.data.preference.BlockCallsPref
import com.blockcallnow.data.preference.LoginPref
import com.blockcallnow.data.room.BlockContactDao
import com.blockcallnow.data.room.LogContact
import com.blockcallnow.util.LogUtil
import com.blockcallnow.util.Utils
import io.reactivex.rxjava3.disposables.CompositeDisposable
import retrofit2.Response

class IncomingCallReceiver : BroadcastReceiver() {

    val TAG: String = IncomingCallReceiver::class.java.simpleName

    private var alreadyOnCall = false
    lateinit var mContext: Context
    lateinit var contactDao: BlockContactDao

    public val mDisposable by lazy {
        CompositeDisposable()
    }

    override fun onReceive(context: Context, intent: Intent) {
        LogUtil.e(TAG, "onReceive Incoming")

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            return
//        }

        mContext = context
        val app = context.applicationContext as BlockCallApplication

        contactDao = app.db.contactDao()

        val user = LoginPref.getLoginObject(context)
        if (user == null) {
            LogUtil.e(TAG, "user is null")
            return
        }

        try {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            val blockNumber: String

            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING, ignoreCase = true)) {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                if (phoneNumber == null) {
                    return
                } else {
                    LogUtil.e(TAG, "Block num: ${Utils.getBlockNumber(context, phoneNumber)}")
                    blockNumber = Utils.getBlockNumber(context, phoneNumber)
                }

                if (contactDao.getBlockContactFromNumber(blockNumber) != null) {
                    rejectAndUpdate(phoneNumber, blockNumber, tm)
                } else {
                    LogUtil.e(TAG, "Number is not present in db")
                    if (BlockCallsPref.getBlockAllOption(context)) {
                        rejectCall(tm, phoneNumber)
                    } else if (phoneNumber.isNullOrEmpty() || phoneNumber == "-1" || phoneNumber == "-2") {
                        if (BlockCallsPref.getPvtNumOption(context)) {
                            rejectCall(tm, phoneNumber)
                        }
                    } else if (BlockCallsPref.getSpamOption(mContext)) {
                        checkForSpam(tm, phoneNumber, blockNumber)
                    } else {
                        if (BlockCallsPref.getUnknownNum(context)) {
                            if (!Utils.contactExists(context, phoneNumber)) {
                                rejectAndUpdate(phoneNumber, blockNumber, tm)
                            }
                        } else if (Utils.isInternationalNumber(mContext, phoneNumber)) {
                            if (BlockCallsPref.getInternationalOption(mContext)) {
                                rejectAndUpdate(phoneNumber, blockNumber, tm)
                            }
                        }
                    }
                }

            } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK, ignoreCase = true)) {
                alreadyOnCall = true
            } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE, ignoreCase = true)) {
                alreadyOnCall = false
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, e.message)
            e.printStackTrace()
        }
    }

    private fun rejectAndUpdate(phoneNumber: String, blockNumber: String, tm: TelephonyManager) {
        LogUtil.e(TAG, "rejectAndUpdate")

        val blockContact = contactDao.getBlockContactFromNumber(blockNumber)
        blockContact?.let {
            contactDao.updateBlockContact(System.currentTimeMillis(), blockContact.timesCalled + 1)
        }
        rejectCall(tm, phoneNumber)
    }

    private fun checkForSpam(tm: TelephonyManager, phoneNumber: String, blockNumber: String) {
        LogUtil.e(TAG, "checkForSpam")

        val api: WebServices = BlockCallApplication.getAppContext().api2
        api.getPhoneNoDetail(
            "https://dataapi.youmail.com/api/v2/phone/$phoneNumber",
            "173852c93f1a7335969fabf51794af843f59e2fa8d069d29036e6a5eaacab736717ed88e638947ce",
            "be88ca2b9dc6493286f44c30718a20c0"
        )
            .enqueue(object : retrofit2.Callback<PhoneNoDetailResponse> {
                override fun onFailure(
                    call: retrofit2.Call<PhoneNoDetailResponse>,
                    t: Throwable
                ) {
                    LogUtil.e(TAG, "onFailure $t")
                }

                override fun onResponse(
                    call: retrofit2.Call<PhoneNoDetailResponse>,
                    response: Response<PhoneNoDetailResponse>
                ) {
                    if (response.isSuccessful) {
                        if (response.body() != null && response.body()?.spamRisk != null && response.body()?.spamRisk?.level == 2) {
                            rejectCall(tm, phoneNumber)
                        } else {
                            LogUtil.e(TAG, "number not spam from api")
                        }
                    } else {
                        LogUtil.e(TAG, "response not success ")
                    }
                }
            })
        LogUtil.e(TAG, "exit checkForSpam")
    }

    @SuppressLint("MissingPermission")
    private fun rejectCall(tm: TelephonyManager, phoneNumber: String) {
        addToHistory(phoneNumber)

        // need to replace "to" number to phone number after purchasing twilio number
        Utils.callTwiloNumber(
            "+923312226066",
            "+12015033368",
            "http://demo.twilio.com/docs/voice.xml"
        )

        contactDao.insertLog(
            LogContact(
                id = 0,
                name = null,
                phoneNumber = phoneNumber,
                isCall = true
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val telManager = mContext.getSystemService(TELECOM_SERVICE) as TelecomManager
            try {
                if (telManager.endCall()) {
                    LogUtil.e(TAG, "Call rejected")
                } else {
                    LogUtil.e(TAG, "Call not rejected")
                }

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                LogUtil.e(TAG, "Couldn't end call with Tel Manager $e")
            }
        } else {
            try {
                val c = Class.forName(tm.javaClass.name)
                val m = c.getDeclaredMethod("getITelephony")
                m.isAccessible = true
                val telephonyService = m.invoke(tm)
                val telephonyServiceClass = Class.forName(telephonyService.javaClass.name)
                val endCallMethod = telephonyServiceClass.getDeclaredMethod("endCall")
                endCallMethod.invoke(telephonyService)
                LogUtil.e(TAG, "endCallMethod invoked")
            } catch (e: Exception) {
                e.printStackTrace()
                LogUtil.e(TAG, "exception while invoking $e")
            }
        }
    }

    private val historyEvent = MutableLiveData<BaseNavEvent<Nothing?>>()
    private fun addToHistory(phoneNumber: String?) {
        val api: WebServices = BlockCallApplication.getAppContext().api
        val token = "Bearer " + LoginPref.getApiToken(mContext)
        mDisposable.add(
            NetworkHelper.makeRequestInBackground(
                api.addToHistory(token, phoneNumber),
                historyEvent
            )
        )
    }
}