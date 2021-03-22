package com.blockcallnow.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.blockcallnow.data.event.BaseNavEvent
import com.blockcallnow.data.model.LoginResponse
import com.blockcallnow.ui.base.BaseViewModel


class RegisterViewModel : BaseViewModel() {
    private val navEvent = MutableLiveData<BaseNavEvent<LoginResponse?>>()
    val registerNavEvent: LiveData<BaseNavEvent<LoginResponse?>> = navEvent

    fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String
    ) {
        when {
            name.isBlank() -> navEvent.value = BaseNavEvent.ShowMessage("Please your full name")
            email.isBlank() -> navEvent.value = BaseNavEvent.ShowMessage("Please enter email")
            phone.isBlank() -> navEvent.value = BaseNavEvent.ShowMessage("Please enter phone no")
            password.isBlank() -> navEvent.value =
                BaseNavEvent.ShowMessage("Please enter password")
            confirmPassword.isBlank() -> navEvent.value =
                BaseNavEvent.ShowMessage("Please enter confirm password")
            password != confirmPassword -> navEvent.value =
                BaseNavEvent.ShowMessage("Password and Confirm Password does not match")
            else -> makeRequest(
                api.register(name, email, phone, password, confirmPassword),
                navEvent
            )
        }
    }
}