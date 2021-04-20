package com.call.blockcallnow.call

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.TELECOM_SERVICE
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.call.blockcallnow.app.BlockCallApplication
import com.call.blockcallnow.data.event.BaseNavEvent
import com.call.blockcallnow.data.model.BaseResponse
import com.call.blockcallnow.data.model.BlockNoDetail
import com.call.blockcallnow.data.model.PhoneNoDetailResponse
import com.call.blockcallnow.data.network.ApiConstant
import com.call.blockcallnow.data.network.ApiConstant.Companion.TWILIO_NUMBER
import com.call.blockcallnow.data.network.NetworkHelper
import com.call.blockcallnow.data.network.WebServices
import com.call.blockcallnow.data.preference.BlockCallsPref
import com.call.blockcallnow.data.preference.LoginPref
import com.call.blockcallnow.data.room.BlockContactDao
import com.call.blockcallnow.data.room.LogContact
import com.call.blockcallnow.util.LogUtil
import com.call.blockcallnow.util.Utils
import io.reactivex.rxjava3.disposables.CompositeDisposable
import retrofit2.Response
import java.net.URLEncoder

class IncomingCallReceiver : BroadcastReceiver() {

    val TAG: String = IncomingCallReceiver::class.java.simpleName

    private var alreadyOnCall = false
    lateinit var mContext: Context
    lateinit var contactDao: BlockContactDao
    var isBlockContact: Boolean = false

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
        if (user == null || user.is_expired) {
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
                    LogUtil.e(
                        TAG,
                        "Phone number: $phoneNumber and  Block num: ${Utils.getBlockNumber(
                            context,
                            phoneNumber
                        )}"
                    )
                    blockNumber = Utils.getBlockNumber(context, phoneNumber)
                }

                val blockContact = contactDao.getBlockContactFromNumber(blockNumber)
                Log.e(TAG, "onReceive: ${blockContact?.number}")

                if (blockContact != null) {
                    isBlockContact = true
                    rejectAndUpdate(phoneNumber, blockNumber, tm)
                } else {
                    LogUtil.e(TAG, "Number is not present in db")
                    if (BlockCallsPref.getBlockAllOption(context)) {
                        isBlockContact = false
                        rejectCall(tm, phoneNumber)
                    } else if (phoneNumber.isNullOrEmpty() || phoneNumber == "-1" || phoneNumber == "-2") {
                        if (BlockCallsPref.getPvtNumOption(context)) {
                            isBlockContact = false
                            rejectCall(tm, phoneNumber)
                        }
                    } else if (BlockCallsPref.getSpamOption(mContext)) {
                        isBlockContact = false
                        checkForSpam(tm, phoneNumber, blockNumber)
                    } else {
                        if (BlockCallsPref.getUnknownNum(context)) {
                            if (!Utils.contactExists(context, phoneNumber)) {
                                isBlockContact = false
                                rejectAndUpdate(phoneNumber, blockNumber, tm)
                            }
                        } else if (Utils.isInternationalNumber(mContext, phoneNumber)) {
                            if (BlockCallsPref.getInternationalOption(mContext)) {
                                isBlockContact = false
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
                        LogUtil.e(TAG, "response not success")
                    }
                }
            })
        LogUtil.e(TAG, "exit checkForSpam")
    }

    @SuppressLint("MissingPermission")
    private fun rejectCall(tm: TelephonyManager, phoneNumber: String) {
        addToHistory(phoneNumber)

        Log.e(TAG, "rejectCall: ${Utils.getBlockNumber(mContext, phoneNumber)}")

        if (isBlockContact) {
            BlockCallApplication.getAppContext().api2.getBlockNoDetailForAudio(
                "Bearer " + LoginPref.getApiToken(mContext),
                Utils.getBlockNumber(mContext, phoneNumber).replace("[\\s\\-]".toRegex(), "")
            ).enqueue(object : retrofit2.Callback<BaseResponse<BlockNoDetail>> {
                override fun onFailure(
                    call: retrofit2.Call<BaseResponse<BlockNoDetail>>,
                    t: Throwable
                ) {
                    Log.e(TAG, "onFailure: ${t.message} ")
                }

                override fun onResponse(
                    call: retrofit2.Call<BaseResponse<BlockNoDetail>>,
                    response: Response<BaseResponse<BlockNoDetail>>
                ) {
                    Log.e(TAG, "onResponse: ${response.body()}")

                    val blockDetail = response.body()?.data?.blockNoDetails

                    var contactName = blockDetail?.name
                    val contactPhone = blockDetail?.phoneNo
                    if (contactName.equals("Unknown", true)) {
                        contactName = contactPhone
                    }

                    contactDao.insertLog(
                        LogContact(
                            id = 0,
                            name = contactName,
                            phoneNumber = contactPhone!!,
                            isCall = true
                        )
                    )

                    var messageEnc = URLEncoder.encode(
                        "The person you’ve called has blocked you. If you feel as though you’ve reached\n" +
                                "this message in error, leave a message and you may or may not receive a call\n" +
                                "back. Good Bye!", "utf-8"
                    )

                    if (blockDetail?.status == Utils.FULL_BLOCK) {
                        Log.e(TAG, "onResponse: Full block")

                        val genderEnc = URLEncoder.encode("man", "utf-8")
                        val languageEnc = URLEncoder.encode("en", "utf-8")

                        if (blockDetail.is_generic_text == 0) {
                            messageEnc = URLEncoder.encode(blockDetail.message, "utf-8")
                            Utils.callTwiloNumber(
                                blockDetail.phoneNo!!,
                                TWILIO_NUMBER,
                                "http://blockcallsnow.com/response.php?message=$messageEnc&gender=$genderEnc&language=$languageEnc"
                            )
                        } else {
                            Utils.callTwiloNumber(
                                blockDetail.phoneNo!!,
                                TWILIO_NUMBER,
                                response.body()?.data?.audio?.fileUrl
                                    ?: "http://blockcallsnow.com/response.php?message=$messageEnc&gender=$genderEnc&language=$languageEnc"
                            )
                        }

                    } else {
                        // Partial block
                        val user = LoginPref.getLoginObject(mContext)

                        if (blockDetail?.message != null) {
                            messageEnc = URLEncoder.encode(blockDetail.message, "utf-8")
                        }
                        var genderEnc: String
                        val language: String

                        if (user?.paywhirl_plan_id == Utils.PLAN_PRO) {
                            // Pro plan
                            genderEnc = if (blockDetail?.set_voice_gender == "F") {
                                URLEncoder.encode("woman", "utf-8")
                            } else {
                                URLEncoder.encode("man", "utf-8")
                            }
                            language = Utils.getLanguage(blockDetail?.set_voice_lang!!)
                        } else {
                            // Standard
                            genderEnc = URLEncoder.encode("man", "utf-8")
                            language = "en"
                        }

                        if (language == "ru-RU" || language == "zh-CN") {
                            genderEnc = URLEncoder.encode("alice", "utf-8")
                        }

                        val languageEnc = URLEncoder.encode(language, "utf-8")

                        Utils.callTwiloNumber(
                            blockDetail?.phoneNo!!,
                            TWILIO_NUMBER,
                            "http://blockcallsnow.com/response.php?message=$messageEnc&gender=$genderEnc&language=$languageEnc"
                        )
                    }
                }
            })
        } else {
            val messageEnc = URLEncoder.encode(
                "The person you’ve called has blocked you. If you feel as though you’ve reached\n" +
                        "this message in error, leave a message and you may or may not receive a call\n" +
                        "back. Good Bye!", "utf-8"
            )
            val genderEnc = URLEncoder.encode("man", "utf-8")
            val languageEnc = URLEncoder.encode("en", "utf-8")

            contactDao.insertLog(
                LogContact(
                    id = 0,
                    name = phoneNumber,
                    phoneNumber = phoneNumber,
                    isCall = true
                )
            )

            Utils.callTwiloNumber(
                phoneNumber,
                ApiConstant.TWILIO_NUMBER,
                "http://blockcallsnow.com/response.php?message=$messageEnc&gender=$genderEnc&language=$languageEnc"
            )
        }

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