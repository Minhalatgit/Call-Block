package com.blockcallnow.util

import android.util.Log
import com.blockcallnow.BuildConfig

class LogUtil {
    companion object {

        fun e(tag: String?, msg: String?) {
            if (BuildConfig.DEBUG)
                msg?.let {

                    Log.e(tag, msg)
                }
        }
    }

}