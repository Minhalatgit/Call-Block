package com.blockcallnow.data.network

class ApiConstant {
    companion object {
        const val BASE_URL = "https://callblock.thesupportonline.net/"
        const val TWILIO_BASE_URL =
            "https://api.twilio.com/2010-04-01/Accounts/AC33015d168d35f91c2d89b062683355bd/"
        const val API_LOGIN = "api/auth/login"
        const val API_REGISTER = "api/auth/signup"
        const val API_BLOCK_NO = "api/auth/block-no"
        const val API_AUDIO = "api/auth/audio"
        const val API_BLOCK_NO_DETAIL = "api/auth/block-no/show"
        const val API_HISTORY = "api/auth/history"
        const val API_USER = "api/auth/user"
        const val MAKE_CALL = "Calls.json"
        const val SEND_SMS = "Messages.json"
        const val TWILIO_ACCOUNT_SID = "AC33015d168d35f91c2d89b062683355bd"
        const val TWILIO_AUTH_TOKEN = "72f60752d601cacce5a49169d8b6b81b"
    }
}