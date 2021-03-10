package com.blockcallnow.data.network

class ApiConstant {
    companion object {
        const val BASE_URL = "https://callblock.thesupportonline.net/"
        const val TWILIO_BASE_URL =
            "https://api.twilio.com/2010-04-01/Accounts/ACc215d55fa3243b7699d2fdae7a30cdfe/"
        const val API_LOGIN = "api/auth/login"
        const val API_REGISTER = "api/auth/signup"
        const val API_BLOCK_NO = "api/auth/block-no"
        const val API_AUDIO = "api/auth/audio"
        const val API_BLOCK_NO_DETAIL = "api/auth/block-no/show"
        const val API_HISTORY = "api/auth/history"
        const val API_USER = "api/auth/user"
        const val MAKE_CALL = "Calls.json"
        const val SEND_SMS = "Messages.json"
        const val TWILIO_NUMBER = "+15615719060"
        const val TWILIO_ACCOUNT_SID = "ACc215d55fa3243b7699d2fdae7a30cdfe"
        const val TWILIO_AUTH_TOKEN = "1c88f7b927baebe07d41125428f1c4b4"
    }
}