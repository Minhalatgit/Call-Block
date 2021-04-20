package com.call.blockcallnow.ui.base

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.call.blockcallnow.app.BlockCallApplication
import com.call.blockcallnow.data.model.UserDetail
import com.call.blockcallnow.data.preference.LoginPref
import com.call.blockcallnow.util.dialog.CustomProgressDialog

open class BaseFragment : Fragment() {

    lateinit var mContext: Context
    lateinit var mActivity: BaseActivity
    lateinit var myApp: BlockCallApplication
    lateinit var dialog: CustomProgressDialog
    lateinit var token: String
    var userDetail: UserDetail? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = activity as BaseActivity
        dialog = mActivity.dialog
        token = mActivity.token
        myApp = mActivity.myApp
        userDetail = LoginPref.getLoginObject(mContext)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
}