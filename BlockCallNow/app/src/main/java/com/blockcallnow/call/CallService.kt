package com.blockcallnow.call

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
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

@RequiresApi(Build.VERSION_CODES.Q)
class CallService : CallScreeningService() {

    val TAG: String = CallService::class.java.simpleName

    lateinit var dao: BlockContactDao

    public val mDisposable by lazy {
        CompositeDisposable()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onScreenCall(callDetails: Call.Details) {
        LogUtil.e(TAG, "onScreenCall")

        val user = LoginPref.getLoginObject(this)
        if (user == null) {
            return
        }

        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING)
            return

        val app = application as BlockCallApplication

        dao = app.db.contactDao()
        val phoneNumber = callDetails.handle?.schemeSpecificPart
        val blockNumber = Utils.getBlockNumber(this, phoneNumber!!)

        LogUtil.e(TAG, "phoneNumber $phoneNumber block number $blockNumber")

        val response = CallResponse.Builder()

        if (!phoneNumber.isNullOrEmpty() && dao.getBlockContactFromNumber(blockNumber) != null) {
            LogUtil.e(TAG, "Number is present in db")
            rejectAndUpdate(phoneNumber, response, callDetails, dao)
        } else if (BlockCallsPref.getBlockAllOption(this)) {
            LogUtil.e(TAG, "block all")
            rejectCall(response, callDetails, phoneNumber, dao)
        } else if (phoneNumber.isNullOrEmpty() || phoneNumber == "-1" || phoneNumber == "-2") {
            if (BlockCallsPref.getPvtNumOption(this)) {
                rejectCall(response, callDetails, phoneNumber, dao)
            }
        } else if (BlockCallsPref.getSpamOption(this)) {
            checkForSpam(response, callDetails, phoneNumber, app)
        } else {
            if (BlockCallsPref.getUnknownNum(this)) {
                if (!Utils.contactExists(this, phoneNumber)) {
                    rejectAndUpdate(phoneNumber, response, callDetails, dao)
                }
            } else if (Utils.isInternationalNumber(this, phoneNumber)) {
                if (BlockCallsPref.getInternationalOption(this)) {
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
//                        LogUtil.e(TAG, "response " + Gson().toJson(response))
                if (response.isSuccessful) {
                    if (response.body() != null && response.body()?.spamRisk != null && response.body()?.spamRisk?.level == 2) {
//                                rejectAndUpdate(phoneNumber!!, builder, callDetails, app)
                        rejectCall(builder, callDetails, phoneNumber, dao)
                    } else {
                        LogUtil.e(TAG, "number not spam form api")
                    }
                } else {
                    LogUtil.e(TAG, "response not success ")
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
        val blockNumber = Utils.getBlockNumber(this, phoneNumber)
        val blockContact = dao.getBlockContactFromNumber(blockNumber)
        blockContact?.let {
            dao.updateBlockContact(System.currentTimeMillis(), blockContact.timesCalled + 1)
        }
        response.setDisallowCall(true)
        response.setRejectCall(true)

        respondToCall(callDetails!!, response.build())

        // need to replace "to" number to phone number after purchasing twilio number
        Utils.callTwiloNumber(
            "+923312226066",
            "+12015033368",
            "http://demo.twilio.com/docs/voice.xml"
        )

        Utils.smsTwiloNumber("+923312226066", "+12015033368", "You are blocked by $phoneNumber")

        dao.insertLog(
            LogContact(
                id = 0,
                name = null,
                phoneNumber = phoneNumber,
                isCall = true
            )
        )

        addToHistory(phoneNumber)
    }

    fun rejectCall(
        response: CallResponse.Builder,
        callDetails: Call.Details?,
        phoneNumber: String?,
        dao: BlockContactDao
    ) {
        // need to replace "to" number to phone number after purchasing twilio number
        Utils.callTwiloNumber(
            "+923312226066",
            "+12015033368",
            "http://demo.twilio.com/docs/voice.xml"
        )

        Utils.smsTwiloNumber("+923312226066", "+12015033368", "You are blocked by $phoneNumber")

        dao.insertLog(
            LogContact(
                id = 0,
                name = null,
                phoneNumber = phoneNumber!!,
                isCall = true
            )
        )
        response.setDisallowCall(true)
        response.setRejectCall(true)
        respondToCall(callDetails!!, response.build())
        addToHistory(phoneNumber)
    }

    private val historyEvent = MutableLiveData<BaseNavEvent<Nothing?>>()

    private fun addToHistory(phoneNumber: String?) {
        val api: WebServices = BlockCallApplication.getAppContext().api
        val token = "Bearer " + LoginPref.getApiToken(this)
        mDisposable.add(
            NetworkHelper.makeRequestInBackground(
                api.addToHistory(token, phoneNumber),
                historyEvent
            )
        )
    }
}