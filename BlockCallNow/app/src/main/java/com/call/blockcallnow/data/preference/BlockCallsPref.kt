package com.call.blockcallnow.data.preference

import android.content.Context
import com.call.blockcallnow.data.preference.PrefManager.Companion.getSharedPreferences
import com.call.blockcallnow.data.preference.PrefManager.Companion.getSharedPreferencesEditor
import com.call.blockcallnow.data.preference.PrefManager.PreferenceConstant.KEY_BLOCK_ALL
import com.call.blockcallnow.data.preference.PrefManager.PreferenceConstant.KEY_BLOCK_ALL_VOIP_CONNECTED
import com.call.blockcallnow.data.preference.PrefManager.PreferenceConstant.KEY_BLOCK_ALL_WITHOUT_CONTACTS
import com.call.blockcallnow.data.preference.PrefManager.PreferenceConstant.KEY_INTERNATIONAL_NUM
import com.call.blockcallnow.data.preference.PrefManager.PreferenceConstant.KEY_MSG_NON_NUMERIC
import com.call.blockcallnow.data.preference.PrefManager.PreferenceConstant.KEY_MSG_UNKNOWN_NUM
import com.call.blockcallnow.data.preference.PrefManager.PreferenceConstant.KEY_PVT_NUM
import com.call.blockcallnow.data.preference.PrefManager.PreferenceConstant.KEY_SPAM
import com.call.blockcallnow.data.preference.PrefManager.PreferenceConstant.KEY_UNKNOWN_NUM

object BlockCallsPref {
    private val TAG = BlockCallsPref::class.java.simpleName

    //    fun setCallListEnable(context: Context, isEnable: Boolean) {
//        getSharedPreferencesEditor(context).putBoolean(KEY_CALLS_LIST, isEnable)
//            .commit()
//    }
//
//
//    fun getCallListEnable(context: Context): Boolean {
//        return getSharedPreferences(context).getBoolean(KEY_CALLS_LIST, false)
//
//    }
    fun setMsgUnknownNumber(context: Context, isEnable: Boolean) {
        getSharedPreferencesEditor(context).putBoolean(KEY_MSG_UNKNOWN_NUM, isEnable).commit()
    }

    fun getMsgUnknownNumber(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_MSG_UNKNOWN_NUM, false)
    }

    fun setMsgNonNumericNUmber(context: Context, isEnable: Boolean) {
        getSharedPreferencesEditor(context).putBoolean(KEY_MSG_NON_NUMERIC, isEnable).commit()
    }

    fun getMsgNonNumericNUmber(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_MSG_NON_NUMERIC, false)
    }

    fun setPvtNumOption(context: Context, isEnable: Boolean) {
        getSharedPreferencesEditor(context).putBoolean(KEY_PVT_NUM, isEnable).commit()
    }

    fun getPvtNumOption(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_PVT_NUM, false)
    }

    fun setUnknownNum(context: Context, isEnable: Boolean) {
        getSharedPreferencesEditor(context).putBoolean(KEY_UNKNOWN_NUM, isEnable).commit()
    }

    fun getUnknownNum(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_UNKNOWN_NUM, false)
    }

    fun setSpamOption(context: Context, isEnable: Boolean) {
        getSharedPreferencesEditor(context).putBoolean(KEY_SPAM, isEnable).commit()
    }

    fun getSpamOption(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_SPAM, false)
    }

    fun setInternationalOption(context: Context, isEnable: Boolean) {
        getSharedPreferencesEditor(context).putBoolean(KEY_INTERNATIONAL_NUM, isEnable).commit()
    }

    fun getInternationalOption(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_INTERNATIONAL_NUM, false)
    }

    fun setBlockAllExceptContactsOption(context: Context, isEnable: Boolean) {
        getSharedPreferencesEditor(context).putBoolean(KEY_BLOCK_ALL_WITHOUT_CONTACTS, isEnable)
            .commit()
    }

    fun getBlockAllExceptContactsOption(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_BLOCK_ALL_WITHOUT_CONTACTS, false)
    }

    fun setBlockAllOption(context: Context, isEnable: Boolean) {
        getSharedPreferencesEditor(context).putBoolean(KEY_BLOCK_ALL, isEnable).commit()
    }

    fun getBlockAllOption(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_BLOCK_ALL, false)
    }

    fun setBlockAllVoipConnected(context: Context, isEnable: Boolean) {
        getSharedPreferencesEditor(context).putBoolean(KEY_BLOCK_ALL_VOIP_CONNECTED, isEnable)
            .commit()
    }

    fun getBlockAllVoipConnected(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_BLOCK_ALL_VOIP_CONNECTED, false)
    }
}