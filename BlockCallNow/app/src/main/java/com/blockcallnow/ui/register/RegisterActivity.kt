package com.blockcallnow.ui.register

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blockcallnow.ui.main.MainActivity
import com.blockcallnow.R
import com.blockcallnow.data.event.BaseNavEvent
import com.blockcallnow.data.model.LoginResponse
import com.blockcallnow.data.preference.LoginPref
import com.blockcallnow.databinding.ActivityRegisterBinding
import com.blockcallnow.ui.base.BaseActivity
import com.blockcallnow.util.toast

class RegisterActivity : BaseActivity() {

    private val binding: ActivityRegisterBinding by binding(R.layout.activity_register)
    lateinit var registerViewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerViewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)
        registerViewModel.registerNavEvent.observe(this, registerObserver)

        binding.btnSingup.setOnClickListener {

            registerViewModel.register(
                    binding.etName.text.toString(),
                    binding.etEmail.text.toString(),
                    binding.etPhone.text.toString(),

                    binding.etPassword.text.toString(),
                    binding.etConfirmPassword.text.toString()
            )
        }
    }

    private val registerObserver = Observer<BaseNavEvent<LoginResponse?>> {
        when (it) {
            is BaseNavEvent.StartLoading -> {
                dialog.show(mContext)
            }
            is BaseNavEvent.StopLoading -> {
                dialog.dialog.cancel()
            }
            is BaseNavEvent.Success -> {
                it.data?.data?.let { userDetail ->
                    userDetail.accessToken?.let { token ->
                        LoginPref.setApiToken(mContext, token)
                    }
                    userDetail.user?.let { detail ->
                        LoginPref.setLoginObject(mContext, detail)

//                        Util.startRelevantActivity(detail, this)
                        startActivity(Intent(mContext, MainActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    }

                    finish()
                }

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
                it.throwable?.message?.let { it1 -> toast(it1) }
            }
            is BaseNavEvent.NetWorkException -> {
                it.throwable?.message?.let { it1 -> toast(it1) }
            }
            is BaseNavEvent.UnKnownException -> {
                it.throwable?.message?.let { it1 -> toast(it1) }
            }
        }
    }
}