package com.call.blockcallnow.data.preference

import android.content.Context
import com.call.blockcallnow.data.model.UserDetail
import com.call.blockcallnow.data.preference.PrefManager.Companion.getSharedPreferences
import com.call.blockcallnow.util.LogUtil
import com.google.gson.Gson
import com.call.blockcallnow.data.preference.PrefManager.Companion.getSharedPreferencesEditor
import com.call.blockcallnow.data.preference.PrefManager.PreferenceConstant.KEY_CALL_ENABLE
import com.call.blockcallnow.data.preference.PrefManager.PreferenceConstant.KEY_FCM_TOKEN
import com.call.blockcallnow.data.preference.PrefManager.PreferenceConstant.KEY_SMS_ENABLE
import com.call.blockcallnow.data.preference.PrefManager.PreferenceConstant.USER_OBJECT_KEY

object LoginPref {
    private val TAG = LoginPref::class.java.simpleName
    fun setFcmToken(context: Context, fcmToken: String) {
        LogUtil.e(TAG, "setFcmToken $fcmToken")
        getSharedPreferencesEditor(context).putString(KEY_FCM_TOKEN, fcmToken).commit()
    }

    fun getFcmToken(context: Context): String {
        val fcmToken: String = getSharedPreferences(context).getString(KEY_FCM_TOKEN, "") ?: ""
        LogUtil.e(TAG, "getFcmToken $fcmToken")
        return fcmToken
    }

    fun getLoginStatus(context: Context): Boolean {
        val isLoggedIn: Boolean =
            getSharedPreferences(context).getBoolean(
                PrefManager.PreferenceConstant.LOGIN_KEY,
                false
            )
        LogUtil.e(TAG, "getLoginStatus $isLoggedIn")
        return isLoggedIn
    }

    fun setLoginStatus(context: Context, status: Boolean) {
        getSharedPreferencesEditor(context)
            .putBoolean(PrefManager.PreferenceConstant.LOGIN_KEY, status).apply()
    }

    fun getLoginObject(context: Context): UserDetail? {
        val myObject: String? =
            getSharedPreferences(context).getString(USER_OBJECT_KEY, null)
        LogUtil.e(TAG, "getLoginObject $myObject")
        return Gson().fromJson(myObject, UserDetail::class.java)
    }

    fun setLoginObject(context: Context, userObj: UserDetail) {
//        userObj.token?.let {
//            setApiToken(context, it)
//        }
        getSharedPreferencesEditor(context)
            .putString(USER_OBJECT_KEY, Gson().toJson(userObj))
            .commit()
    }

    fun setApiToken(context: Context, apiToken: String) {
        LogUtil.e("LoginPref", "token $apiToken")
        getSharedPreferencesEditor(context)
            .putString(PrefManager.PreferenceConstant.KEY_TOKEN, apiToken)
            .commit()
    }

    fun getApiToken(context: Context): String? {
        return getSharedPreferences(context).getString(PrefManager.PreferenceConstant.KEY_TOKEN, "")
    }

    fun removeLoginDetail(context: Context) {
        getSharedPreferencesEditor(context).remove(USER_OBJECT_KEY).commit()
    }

    fun setSmsEnable(context: Context, smsEnable: Boolean) {
        LogUtil.e("LoginPref", "Sms enable $smsEnable")
        getSharedPreferencesEditor(context)
            .putBoolean(KEY_SMS_ENABLE, smsEnable)
            .commit()
    }

    fun getSmsEnable(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_SMS_ENABLE, false)
    }

    fun setCallEnable(context: Context, callEnable: Boolean) {
        LogUtil.e("LoginPref", "Call enable $callEnable")
        getSharedPreferencesEditor(context)
            .putBoolean(KEY_CALL_ENABLE, callEnable)
            .commit()
    }

    fun getCallEnable(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_CALL_ENABLE, false)
    }
}