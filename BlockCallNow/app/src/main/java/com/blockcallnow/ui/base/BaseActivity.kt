package com.blockcallnow.ui.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.blockcallnow.app.BlockCallApplication
import com.blockcallnow.data.model.UserDetail
import com.blockcallnow.data.preference.LoginPref
import com.blockcallnow.util.dialog.CustomProgressDialog

open class BaseActivity : AppCompatActivity() {

    val dialog = CustomProgressDialog()
    var userDetail: UserDetail? = null

    lateinit var myApp: BlockCallApplication

    protected inline fun <reified T : ViewDataBinding> binding(
            @LayoutRes resId: Int
    ): Lazy<T> = lazy { DataBindingUtil.setContentView<T>(this, resId) }

    val token by lazy {
        "Bearer " + LoginPref.getApiToken(mContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myApp = application as BlockCallApplication
        setUserDetail()
    }

    fun setUserDetail() {
        userDetail = LoginPref.getLoginObject(mContext)
    }

    val mContext by lazy {
        this
    }
}