package com.call.blockcallnow.ui.base

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.call.blockcallnow.app.BlockCallApplication
import com.call.blockcallnow.data.event.BaseNavEvent
import com.call.blockcallnow.data.model.BaseResponse
import com.call.blockcallnow.data.network.NetworkHelper
import com.call.blockcallnow.data.network.WebServices


import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable

open class BaseViewModel : ViewModel() {

    //    private lateinit var mCompositeDisposable: CompositeDisposable
//
//
//    protected open fun getCompositeDisposable(): CompositeDisposable? {
//        if (mCompositeDisposable == null) mCompositeDisposable = CompositeDisposable()
//        return mCompositeDisposable
//    }
    var api: WebServices = BlockCallApplication.getAppContext().api

    val mDisposable by lazy {
        CompositeDisposable()
    }

    override fun onCleared() {
        mDisposable.clear()
        super.onCleared()
    }

    fun <T> makeRequest(
        single: Single<BaseResponse<T>?>?,
        liveData: MutableLiveData<BaseNavEvent<T>>
    ) {
        mDisposable.add(NetworkHelper.makeRequest(single, liveData))
    }
}