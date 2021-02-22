package com.blockcallnow.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blockcallnow.app.BlockCallApplication
import com.blockcallnow.data.event.BaseNavEvent
import com.blockcallnow.data.model.BaseResponse
import com.blockcallnow.data.network.NetworkHelper
import com.blockcallnow.data.network.WebServices


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