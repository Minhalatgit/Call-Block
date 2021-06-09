package com.call.blockcallnow.ui.menu.changepassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.call.blockcallnow.data.event.BaseNavEvent
import com.call.blockcallnow.data.model.LoginResponse
import com.call.blockcallnow.ui.base.BaseViewModel

class ChangePasswordViewModel : BaseViewModel() {
    private val navEvent = MutableLiveData<BaseNavEvent<LoginResponse?>>()
    val updatePasswordNavEvent: LiveData<BaseNavEvent<LoginResponse?>> = navEvent

    fun updatePassword(id: String, newPassword: String, newConfirmPassword: String) {
        when {
            newPassword.isBlank() -> navEvent.value =
                BaseNavEvent.ShowMessage("Please enter new password")
            newConfirmPassword.isBlank() -> navEvent.value =
                BaseNavEvent.ShowMessage("Please enter confirm password")
            //newPassword != newConfirmPassword -> BaseNavEvent.ShowMessage("Please enter confirm password")
            else -> makeRequest(
                api.updatePasswordAPI(id, newPassword, newConfirmPassword),
                navEvent
            )
        }
    }
}