package com.blockcallnow.util

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.AudioFormat
import android.media.MediaRecorder
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.provider.ContactsContract.PhoneLookup
import android.util.Log
import com.blockcallnow.app.BlockCallApplication
import com.blockcallnow.data.model.CallResponse
import com.blockcallnow.data.model.SmsResponse
import com.blockcallnow.data.room.BlockContact
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import omrecorder.AudioRecordConfig
import omrecorder.PullableSource
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class Utils {
    companion object {

        const val PARTIAL_BLOCK: String = "partial"
        const val FULL_BLOCK: String = "full"
        const val PLAN_TRIAL = 1
        const val PLAN_STAND = 134321
        const val PLAN_PRO = 119832

        fun isInternationalNumber(context: Context, phoneNumber: String): Boolean {
            val phoneNumberUtil = PhoneNumberUtil.createInstance(context)
            try {
                val formattedNUmber = phoneNumberUtil.parseAndKeepRawInput(phoneNumber, "en")
                LogUtil.e(
                    "isInternationalNumber countryCode ",
                    formattedNUmber.countryCode.toString()
                )
                LogUtil.e(
                    "isInternationalNumber nationalNumber ",
                    formattedNUmber.nationalNumber.toString()
                )
                val region = phoneNumberUtil.getRegionCodeForNumber(formattedNUmber)
                LogUtil.e("isInternationalNumber region ", region)
                return region != "US"
            } catch (e: NumberParseException) {
                LogUtil.e("Utils", "parseAndKeepRawInput $e")

            }
            return false
        }

        fun getLanguage(language: String): String {
            return when (language) {
                "ru" -> "ru-RU" //alice
                "es" -> "es"
                "fr" -> "fr"
                "chi" -> "zh-CN" //alice
                else -> "en"
            }
        }

        fun contactExists(context: Context, number: String?): Boolean {
            /// number is the phone number
            val lookupUri = Uri.withAppendedPath(
                PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number)
            )
            val mPhoneNumberProjection =
                arrayOf(PhoneLookup._ID, PhoneLookup.NUMBER, PhoneLookup.DISPLAY_NAME)
            val cur =
                context.contentResolver.query(lookupUri, mPhoneNumberProjection, null, null, null)
            try {
                if (cur!!.moveToFirst()) {
                    LogUtil.e(TAG, "contact exists")
                    return true
                }
            } finally {
                cur?.close()
            }
            return false
        }

        fun getMic(): PullableSource.Default {
            return PullableSource.Default(
                AudioRecordConfig.Default(
                    MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                    AudioFormat.CHANNEL_IN_MONO, 44100
                )
            )
        }

        fun getCursorFromUri(context: Context, uri: Uri): Cursor? {
            return context.contentResolver?.query(uri, null, null, null, null)
        }

        val TAG = Utils::class.simpleName
        fun cursorToBlockContact(context: Context, cursor: Cursor, status: String): BlockContact {

            val phoneIndex: Int =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val contactIdIdx: Int =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
            val nameIdx: Int =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val namePrimaryIdx: Int =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY)
            val photoUriIdx: Int =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            val number: String = cursor.getString(phoneIndex)
            val id: String = cursor.getString(contactIdIdx)
            val name: String = cursor.getString(nameIdx)
            val namePrimary: String = cursor.getString(namePrimaryIdx)
            var photoUri: String? = null
            if (cursor.getType(photoUriIdx) == Cursor.FIELD_TYPE_STRING) {
                photoUri = cursor.getString(photoUriIdx)
                LogUtil.e(TAG, "photoUri $photoUri")
            }

            LogUtil.e(TAG, "num $number")
            LogUtil.e(TAG, "getBlockNumber  ${getBlockNumber(context, number)}")
            LogUtil.e(TAG, "id $id")
            LogUtil.e(TAG, "name $name")
            LogUtil.e(TAG, "namePrimary $namePrimary")
            return BlockContact(
                0,
                name,
                number,
                getBlockNumber(context, number),
                status,
                photoUri,
                0,
                0
            )
        }

        /*
        * pass phone number and it returns number without country code
        * 0,+92,+1 or 1
        *
        * */
        fun getBlockNumber(context: Context, phoneNo: String): String {
            var phoneNumber = phoneNo
            val phoneNumberUtil = PhoneNumberUtil.createInstance(context)
            try {
                val formattedNumber = phoneNumberUtil.parseAndKeepRawInput(phoneNumber, "en")
                LogUtil.e("countryCode ", formattedNumber.countryCode.toString())
                LogUtil.e("nationalNumber ", formattedNumber.nationalNumber.toString())
                return formattedNumber.nationalNumber.toString()
            } catch (e: NumberParseException) {
                LogUtil.e("Utils", "parseAndKeepRawInput $e")
            }
            phoneNumber = phoneNumber.replace("\\D+".toRegex(), "")
            LogUtil.e(TAG, "phoneNumber is not with country code $phoneNumber")
            if (phoneNumber.startsWith("0") || phoneNumber.startsWith("1")) {
                LogUtil.e(TAG, "number starts with 0 or 1 ")
                val number = phoneNumber.substring(1, phoneNumber.length)
                LogUtil.e(TAG, "return number  $number")

                return number
            } else if (phoneNumber.startsWith("92")) {
                LogUtil.e(TAG, "number starts with 92")
                val number = phoneNumber.substring(2, phoneNumber.length)
                LogUtil.e(TAG, "return number $number")
                return number
            }
            return phoneNumber
        }

        fun openLink(context: Context, url: String) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        fun getDateTime(timeInMillis: Long): String? {
            val formatter = SimpleDateFormat("d MMM, hh:mm a")
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeInMillis
            return formatter.format(calendar.time)
        }

        fun callTwiloNumber(to: String, from: String, url: String) {
            Log.e("Twilio", "callTwiloNumber: to: $to from: $from url: $url")

            Handler(Looper.getMainLooper()).postDelayed({
                BlockCallApplication.getAppContext().twilioApi.callTwilioNumber(
                    to,
                    from,
                    url
                )
                    .enqueue(object : Callback<CallResponse> {

                        override fun onResponse(
                            call: retrofit2.Call<CallResponse>,
                            response: Response<CallResponse>
                        ) {
                            if (response.isSuccessful) {
                                LogUtil.e("Twilio", "Calling api success")
                            } else {
                                LogUtil.e("Twilio", "Calling api Failed $response")
                            }
                        }

                        override fun onFailure(
                            call: retrofit2.Call<CallResponse>,
                            t: Throwable
                        ) {
                            LogUtil.e("Twilio", "onFailure ${t.message}")
                        }
                    })
            }, 60000)
        }

        fun smsTwiloNumber(to: String, from: String, message: String) {
            Log.e("Twilio", "callTwiloNumber: to: $to from: $from message: $message")
            BlockCallApplication.getAppContext().twilioApi.smsTwilioNumber(
                to,
                from,
                message
            )
                .enqueue(object : Callback<SmsResponse> {

                    override fun onResponse(
                        call: retrofit2.Call<SmsResponse>,
                        response: Response<SmsResponse>
                    ) {
                        LogUtil.e("Twilio", "twilio onresponse")
                        if (response.isSuccessful) {
                            LogUtil.e("Twilio", "Calling api success")
                        } else {
                            LogUtil.e("Twilio", "Calling api Failed $response")
                        }
                    }

                    override fun onFailure(
                        call: retrofit2.Call<SmsResponse>,
                        t: Throwable
                    ) {
                        LogUtil.e("Twilio", "onFailure ${t.message}")
                    }
                })
        }
    }
}