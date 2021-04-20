package com.call.blockcallnow.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.call.blockcallnow.R
import com.call.blockcallnow.data.event.BaseNavEvent
import com.call.blockcallnow.data.model.LoginResponse
import com.call.blockcallnow.data.preference.LoginPref
import com.call.blockcallnow.databinding.ActivityLoginBinding
import com.call.blockcallnow.ui.base.BaseActivity
import com.call.blockcallnow.ui.main.MainActivity
import com.call.blockcallnow.ui.register.RegisterActivity
import com.call.blockcallnow.util.toast

class LoginActivity : BaseActivity() {

    private val binding: ActivityLoginBinding by binding(R.layout.activity_login)
    lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        loginViewModel.loginNavEvent.observe(this, loginObserver)

        binding.btnSigin.setOnClickListener {
            loginViewModel.login(
                binding.etPhone.text.toString(),
                binding.etPassword.text.toString()
            )
        }
        binding.tvCreateAccount.setOnClickListener {
            startActivity(Intent(mContext, RegisterActivity::class.java))
//            startActivity(Intent(Intent.ACTION_VIEW))
//            Utils.openLink(mContext,"https://www.blockcallsnow.com/pricing/")
        }
    }

    private val loginObserver = Observer<BaseNavEvent<LoginResponse?>> {
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
                        startActivity(
                            Intent(mContext, MainActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        )
                    }

                    finish()
                }
            }

            is BaseNavEvent.Error -> {
                it.message?.let {
                    toast(it)
                    Log.e("LoginActivity",  it)
                }
            }
            is BaseNavEvent.ShowMessage -> {
                it.message?.let {
                    toast(it)
                    Log.e("LoginActivity",  it)
                }
            }
            is BaseNavEvent.JsonParseException -> {
                it.throwable?.message?.let { it1 -> toast(it1)
                    Log.e("LoginActivity",  it1)
                }
            }
            is BaseNavEvent.NetWorkException -> {
                it.throwable?.message?.let { it1 -> toast(it1)
                    Log.e("LoginActivity",  it1)
                }
            }
            is BaseNavEvent.UnKnownException -> {
                it.throwable?.message?.let { it1 -> toast(it1) }
            }
        }
    }
}