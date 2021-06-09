package com.call.blockcallnow.ui.menu.changepassword

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.call.blockcallnow.R
import com.call.blockcallnow.data.event.BaseNavEvent
import com.call.blockcallnow.data.model.LoginResponse
import com.call.blockcallnow.data.preference.LoginPref
import com.call.blockcallnow.data.preference.PrefManager
import com.call.blockcallnow.databinding.ActivityChangePasswordBinding
import com.call.blockcallnow.ui.base.BaseActivity
import com.call.blockcallnow.ui.main.MainActivity
import com.call.blockcallnow.util.toast

class ChangePasswordActivity : BaseActivity() {

    private val binding: ActivityChangePasswordBinding by binding(R.layout.activity_change_password)
    lateinit var changePasswordViewModel: ChangePasswordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changePasswordViewModel = ViewModelProvider(this).get(ChangePasswordViewModel::class.java)

        binding.btnUpdatePassword.setOnClickListener {
            changePasswordViewModel.updatePassword(
                LoginPref.getLoginObject(this)?.id?.toString()!!,
                binding.etNewPassword.text.toString(),
                binding.etNewConfirmPassword.text.toString()
            )
        }

        changePasswordViewModel.updatePasswordNavEvent.observe(this, updatePasswordObserver)
    }

    private val updatePasswordObserver = Observer<BaseNavEvent<LoginResponse?>> {
        when (it) {
            is BaseNavEvent.StartLoading -> {
                dialog.show(mContext)
            }
            is BaseNavEvent.StopLoading -> {
                dialog.dialog.cancel()
            }
            is BaseNavEvent.Success -> {
                Log.e("ChangePassword", "Response: $it")
                toast(it.message ?: "Password updated Successfully")
                startActivity(
                    Intent(mContext, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )

                finish()

//                it.data?.data?.let { userDetail ->
//                    userDetail.accessToken?.let { token ->
//                        LoginPref.setApiToken(mContext, token)
//                    }
//                    userDetail.user?.let { detail ->
//                        LoginPref.setLoginObject(mContext, detail)
//
////                        Util.startRelevantActivity(detail, this)
//                        startActivity(
//                            Intent(mContext, MainActivity::class.java)
//                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                        )
//                    }
//
//                    finish()
//                }
            }

            is BaseNavEvent.Error -> {
                it.message?.let {
                    toast(it)
                }
            }
            is BaseNavEvent.ShowMessage -> {
                it.message?.let {
                    toast(it)
                }
            }
            is BaseNavEvent.JsonParseException -> {
                it.throwable?.message?.let { it1 ->
                    toast(it1)
                }
            }
            is BaseNavEvent.NetWorkException -> {
                it.throwable?.message?.let { it1 ->
                    toast(it1)
                }
            }
            is BaseNavEvent.UnKnownException -> {
                it.throwable?.message?.let { it1 -> toast(it1) }
            }
        }
    }
}