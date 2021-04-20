package com.call.blockcallnow.data.network

import androidx.lifecycle.MutableLiveData
import com.call.blockcallnow.data.event.BaseNavEvent
import com.call.blockcallnow.data.model.BaseResponse
import com.call.blockcallnow.util.LogUtil
import com.google.gson.JsonParseException
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkHelper {

    private val TAG = NetworkHelper::class.java.simpleName

    //    Consumer<BaseResponse<T>> onSuccess, Consumer<Throwable> onError,
    fun <T> makeRequest(
            single: Single<BaseResponse<T>?>?,
            liveData: MutableLiveData<BaseNavEvent<T>>
    ): Disposable {
        return single!!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    liveData.value = BaseNavEvent.StartLoading()
                }
                .subscribe(
                        { tBaseResponse: BaseResponse<T>? ->
                            liveData.value = BaseNavEvent.StopLoading()
                            if (tBaseResponse != null) {
                                LogUtil.e(TAG, "response $tBaseResponse")
                                if (tBaseResponse.success) {
                                    liveData.value = BaseNavEvent.Success(tBaseResponse)
                                }
//                        else if (tBaseResponse.getMessageCode().equals("09")) {
//
//                            // token is expire get the token
////                                    responseListener.onError(tBaseResponse);
//                            responseListener.onTokenExpired(tBaseResponse)
//                        }
                                else liveData.value =
                                        BaseNavEvent.Error(tBaseResponse.message, tBaseResponse)
                            }
                            liveData.value = BaseNavEvent.Response(tBaseResponse)
                        }
                ) { throwable: Throwable ->
                    liveData.value = BaseNavEvent.StopLoading()
                    throwable.printStackTrace()
                    LogUtil.e(TAG, "error " + throwable.message)
                    liveData.value = BaseNavEvent.Failure(throwable)
                    if (throwable is UnknownHostException || throwable is SocketTimeoutException) {
                        liveData.value = BaseNavEvent.NetWorkException(throwable)
                    } else if (throwable is JsonParseException) {
                        liveData.value = BaseNavEvent.JsonParseException(throwable)
                    } else {
                        liveData.value = BaseNavEvent.UnKnownException(throwable)
                    }
                }
    }

    fun <T> makeRequestInBackground(
            single: Single<BaseResponse<T>?>?,
            liveData: MutableLiveData<BaseNavEvent<T>>
    ): Disposable {
        return single!!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    liveData.postValue(BaseNavEvent.StartLoading())
                }
                .subscribe(
                        { tBaseResponse: BaseResponse<T>? ->
                            liveData.postValue(BaseNavEvent.StopLoading())
                            if (tBaseResponse != null) {
                                LogUtil.e(TAG, "response $tBaseResponse")
                                if (tBaseResponse.success) {
                                    liveData.postValue(BaseNavEvent.Success(tBaseResponse))
                                }
//                        else if (tBaseResponse.getMessageCode().equals("09")) {
//
//                            // token is expire get the token
////                                    responseListener.onError(tBaseResponse);
//                            responseListener.onTokenExpired(tBaseResponse)
//                        }
                                else liveData.postValue(
                                        BaseNavEvent.Error(tBaseResponse.message, tBaseResponse)
                                )
                            }
                            liveData.postValue(BaseNavEvent.Response(tBaseResponse))
                        }
                ) { throwable: Throwable ->
                    liveData.postValue(BaseNavEvent.StopLoading())
                    throwable.printStackTrace()
                    LogUtil.e(TAG, "error " + throwable.message)
                    liveData.postValue(BaseNavEvent.Failure(throwable))
                    if (throwable is UnknownHostException || throwable is SocketTimeoutException) {
                        liveData.postValue(BaseNavEvent.NetWorkException(throwable))
                    } else if (throwable is JsonParseException) {
                        liveData.postValue(BaseNavEvent.JsonParseException(throwable))
                    } else {
                        liveData.postValue(BaseNavEvent.UnKnownException(throwable))
                    }
                }
    }
}