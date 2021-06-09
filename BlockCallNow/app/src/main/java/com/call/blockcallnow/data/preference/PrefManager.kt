package com.call.blockcallnow.data.preference

import android.content.Context
import android.content.SharedPreferences

class PrefManager {
    fun getFloat(
        context: Context,
        key: String?,
        defaultValue: Float?
    ): Float {
        return getSharedPreferences(context).getFloat(key, defaultValue!!)
    }

    fun getBoolean(
        context: Context,
        key: String?,
        defaultValue: Boolean?
    ): Boolean {
        return getSharedPreferences(context).getBoolean(key, defaultValue!!)
    }

    companion object {
        fun getSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(
                PreferenceConstant.PREF_NAME,
                Context.MODE_PRIVATE
            )
        }

        fun getSharedPreferencesEditor(context: Context): SharedPreferences.Editor {
            return getSharedPreferences(context).edit()
        }

        fun setInt(
            context: Context,
            key: String?,
            value: Int
        ) {
            getSharedPreferencesEditor(context).putInt(key, value).apply()
        }

        fun getInt(context: Context, key: String?, defaultValue: Int): Int {
            return getSharedPreferences(context).getInt(key, defaultValue)
        }

        fun setString(
            context: Context,
            key: String?,
            value: String?
        ) {
            getSharedPreferencesEditor(context).putString(key, value)
        }

        fun getString(
            context: Context,
            key: String?,
            defaultValue: String?
        ): String? {
            return getSharedPreferences(context).getString(key, defaultValue)
        }

        fun setFloat(
            context: Context,
            key: String?,
            value: Float?
        ) {
            getSharedPreferencesEditor(context).putFloat(key, value!!)
        }

        fun setBoolean(
            context: Context,
            key: String?,
            value: Boolean?
        ) {
            getSharedPreferencesEditor(context).putBoolean(key, value!!)
        }
    }

    object PreferenceConstant {
        const val LOGIN_KEY = "key_login"
        const val PREF_NAME = "key_pref"
        const val USER_OBJECT_KEY = "key_user_object"
        const val KEY_TOKEN = "key_user_Token"
        const val KEY_DEVICE_ID = "key_device_id"
        const val KEY_FCM_TOKEN = "key_fcm_token"
        const val KEY_SMS_ENABLE = "key_fcm_token"
        const val KEY_CALL_ENABLE = "key_fcm_token"

        //        blocking preference constants
        const val KEY_CALLS_LIST = "key_calls_list"
        const val KEY_PVT_NUM = "key_pvt_num"
        const val KEY_UNKNOWN_NUM = "key_unknown"
        const val KEY_MSG_UNKNOWN_NUM = "key_msg_unknown"
        const val KEY_MSG_NON_NUMERIC = "key_msg_non_numeric"
        const val KEY_SPAM = "key_spam"
        const val KEY_INTERNATIONAL_NUM = "key_international"
        const val KEY_BLOCK_ALL_WITHOUT_CONTACTS = "key_block_all_without_contacts"
        const val KEY_BLOCK_ALL = "key_block_all"
        const val KEY_BLOCK_ALL_VOIP_CONNECTED = "key_block_all_voip_connected"
    }
}
