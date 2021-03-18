package com.blockcallnow.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.blockcallnow.data.event.BaseNavEvent
import com.blockcallnow.data.model.LoginResponse
import com.blockcallnow.ui.base.BaseViewModel

class LoginViewModel : BaseViewModel() {
    private val navEvent = MutableLiveData<BaseNavEvent<LoginResponse?>>()
    val loginNavEvent: LiveData<BaseNavEvent<LoginResponse?>> = navEvent

    fun login(phone: String, password: String) {
        when {
            phone.isBlank() -> navEvent.value = BaseNavEvent.ShowMessage("Please enter phone no")
            password.isBlank() -> navEvent.value = BaseNavEvent.ShowMessage("Please enter password")
            else -> makeRequest(api.loginAPI(phone, password, "android", "sdfdsaf"), navEvent)
        }
    }
}