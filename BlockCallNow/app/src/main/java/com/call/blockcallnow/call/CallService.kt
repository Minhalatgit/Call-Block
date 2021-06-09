package com.call.blockcallnow.call

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.call.blockcallnow.app.BlockCallApplication
import com.call.blockcallnow.data.event.BaseNavEvent
import com.call.blockcallnow.data.model.BaseResponse
import com.call.blockcallnow.data.model.BlockNoDetail
import com.call.blockcallnow.data.model.PhoneNoDetailResponse
import com.call.blockcallnow.data.network.ApiConstant
import com.call.blockcallnow.data.network.NetworkHelper
import com.call.blockcallnow.data.network.WebServices
import com.call.blockcallnow.data.preference.BlockCallsPref
import com.call.blockcallnow.data.preference.LoginPref
import com.call.blockcallnow.data.room.BlockContactDao
import com.call.blockcallnow.data.room.LogContact
import com.call.blockcallnow.util.LogUtil
import com.call.blockcallnow.util.Utils
import com.call.blockcallnow.util.Utils.Companion.PLAN_PRO
import com.call.blockcallnow.util.Utils.Companion.twilioResponseUrl
import io.reactivex.rxjava3.disposables.CompositeDisposable
import retrofit2.Response
import java.net.URLEncoder

@RequiresApi(Build.VERSION_CODES.Q)
class CallService : CallScreeningService() {

    val TAG: String = CallService::class.java.simpleName

    lateinit var dao: BlockContactDao
    lateinit var user: String
    var isBlockContact: Boolean = false
    var token: String? = ""

    public val mDisposable by lazy {
        CompositeDisposable()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onScreenCall(callDetails: Call.Details) {
        LogUtil.e(TAG, "onScreenCall")

        val user = LoginPref.getLoginObject(this)
        if (user == null || user.is_expired) {
            return
        }

        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING)
            return

        val app = application as BlockCallApplication

        token = LoginPref.getApiToken(app)

        dao = app.db.contactDao()
        val phoneNumber = callDetails.handle?.schemeSpecificPart
        val blockNumber = Utils.getBlockNumber(this, phoneNumber!!)

        LogUtil.e(TAG, "phoneNumber $phoneNumber block number $blockNumber")

        val response = CallResponse.Builder()

        if (!phoneNumber.isNullOrEmpty() && dao.getBlockContactFromNumber(blockNumber) != null) {
            isBlockContact = true
            LogUtil.e(TAG, "Number is present in db")
            rejectAndUpdate(phoneNumber, response, callDetails, dao)
        } else if (BlockCallsPref.getBlockAllOption(this)) {
            isBlockContact = false
            LogUtil.e(TAG, "block all")
            rejectCall(response, callDetails, phoneNumber, dao)
        } else if (phoneNumber.isNullOrEmpty() || phoneNumber == "-1" || phoneNumber == "-2") {
            if (BlockCallsPref.getPvtNumOption(this)) {
                isBlockContact = false
                rejectCall(response, callDetails, phoneNumber, dao)
            }
        } else if (BlockCallsPref.getSpamOption(this)) {
            isBlockContact = false
            checkForSpam(response, callDetails, phoneNumber, app)
        } else {
            if (BlockCallsPref.getUnknownNum(this)) {
                if (!Utils.contactExists(this, phoneNumber)) {
                    isBlockContact = false
                    rejectAndUpdate(phoneNumber, response, callDetails, dao)
                }
            } else if (Utils.isInternationalNumber(this, phoneNumber)) {
                if (BlockCallsPref.getInternationalOption(this)) {
                    isBlockContact = false
                    rejectAndUpdate(phoneNumber, response, callDetails, dao)
                }
            }
        }
    }

    private fun checkForSpam(
        builder: CallResponse.Builder,
        callDetails: Call.Details?,
        phoneNumber: String?,
        app: BlockCallApplication
    ) {
        LogUtil.e(TAG, "checkForSpam")

        val api: WebServices = BlockCallApplication.getAppContext().api2
        val response = api.getPhoneNoDetail(
            "https://dataapi.youmail.com/api/v2/phone/$phoneNumber",
            "173852c93f1a7335969fabf51794af843f59e2fa8d069d29036e6a5eaacab736717ed88e638947ce",
            "be88ca2b9dc6493286f44c30718a20c0"
        ).enqueue(object : retrofit2.Callback<PhoneNoDetailResponse> {
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
//                                rejectAndUpdate(phoneNumber!!, builder, callDetails, app)
                        LogUtil.e(TAG, "number is spam from ,rejecting")
                        rejectCall(builder, callDetails, phoneNumber, dao)
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

    private fun rejectAndUpdate(
        phoneNumber: String,
        response: CallResponse.Builder,
        callDetails: Call.Details?,
        dao: BlockContactDao
    ) {
        LogUtil.e(TAG, "Call rejected")
        val blockNumber = Utils.getBlockNumber(this, phoneNumber)
        val blockContact = dao.getBlockContactFromNumber(blockNumber)
        blockContact?.let {
            dao.updateBlockContact(System.currentTimeMillis(), blockContact.timesCalled + 1)
        }
        response.setDisallowCall(true)
        response.setRejectCall(true)

        respondToCall(callDetails!!, response.build())

        getDetailsAndCall(phoneNumber)

        addToHistory(phoneNumber)
    }

    fun rejectCall(
        response: CallResponse.Builder,
        callDetails: Call.Details?,
        phoneNumber: String?,
        dao: BlockContactDao
    ) {
        LogUtil.e(TAG, "Call rejected")

        getDetailsAndCall(phoneNumber!!)

        response.setDisallowCall(true)
        response.setRejectCall(true)
        respondToCall(callDetails!!, response.build())
        addToHistory(phoneNumber)
    }

    private val historyEvent = MutableLiveData<BaseNavEvent<Nothing?>>()
    private fun addToHistory(phoneNumber: String?) {
        val blockNumber = Utils.getBlockNumber(this, phoneNumber!!)
        val api: WebServices = BlockCallApplication.getAppContext().api
        val token = "Bearer " + LoginPref.getApiToken(this)
        mDisposable.add(
            NetworkHelper.makeRequestInBackground(
                api.addToHistory(token, blockNumber),
                historyEvent
            )
        )
    }

    private fun getDetailsAndCall(phoneNumber: String) {
        LogUtil.e(TAG, "Phone number $phoneNumber")

        if (isBlockContact) {
            // number is from block contact list
            val blockNumber = Utils.getBlockNumber(this, phoneNumber)
            BlockCallApplication.getAppContext().api2.getBlockNoDetailForAudio(
                "Bearer " + LoginPref.getApiToken(this),
                blockNumber.replace("[\\s\\-]".toRegex(), "")
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

                    var genderEnc = if (blockDetail?.set_voice_gender == "F") {
                        URLEncoder.encode("woman", "utf-8")
                    } else {
                        URLEncoder.encode("man", "utf-8")
                    }

                    val language = Utils.getLanguage(blockDetail?.set_voice_lang!!)
                    if (language == "ru-RU" || language == "zh-CN") {
                        genderEnc = URLEncoder.encode("alice", "utf-8")
                    }
                    val languageEnc = URLEncoder.encode(language, "utf-8")

                    dao.insertLog(
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

                        if (blockDetail.is_generic_text == 0) {
                            messageEnc = URLEncoder.encode(blockDetail.message, "utf-8")
                            Utils.callTwilioNumber(
                                contactPhone,
                                ApiConstant.TWILIO_NUMBER,
                                twilioResponseUrl(messageEnc, genderEnc, languageEnc)
                            )
                        } else {
                            Utils.callTwilioNumber(
                                contactPhone,
                                ApiConstant.TWILIO_NUMBER,
                                response.body()?.data?.audio?.fileUrl
                                    ?: twilioResponseUrl(messageEnc, genderEnc, languageEnc)
                            )
                        }

                    } else {
                        // Partial block
                        Log.d(TAG, "Block detail: $blockDetail")
                        val user = LoginPref.getLoginObject(this@CallService)

                        if (blockDetail?.message != null) {
                            messageEnc = URLEncoder.encode(blockDetail.message, "utf-8")
                        }

                        Utils.callTwilioNumber(
                            contactPhone,
                            ApiConstant.TWILIO_NUMBER,
                            twilioResponseUrl(messageEnc, genderEnc, languageEnc)
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

            dao.insertLog(
                LogContact(
                    id = 0,
                    name = phoneNumber,
                    phoneNumber = phoneNumber,
                    isCall = true
                )
            )

            Utils.callTwilioNumber(
                phoneNumber,
                ApiConstant.TWILIO_NUMBER,
                twilioResponseUrl(messageEnc, genderEnc, languageEnc)
            )
        }


    }
}