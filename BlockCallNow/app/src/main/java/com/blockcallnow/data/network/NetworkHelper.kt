package com.blockcallnow.data.network

import androidx.lifecycle.MutableLiveData
import com.blockcallnow.data.event.BaseNavEvent
import com.blockcallnow.data.event.SecondaryEvent
import com.blockcallnow.data.model.BaseResponse
import com.blockcallnow.util.LogUtil
import com.google.gson.JsonParseException
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/*
object NetworkHelper {
    private val TAG = NetworkHelper::class.java.simpleName

    //    Consumer<BaseResponse<T>> onSuccess, Consumer<Throwable> onError,
    fun <T> makeRequest(
        single: Single<BaseResponse<T>?>,
        responseListener: NetworkResponseListener<BaseResponse<T>?>
    ): Disposable {
        return single
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { tBaseResponse: BaseResponse<T>? ->
                    if (tBaseResponse != null) {
                        LogUtil.e(
                            TAG,
                            "response $tBaseResponse"
                        )
                        if (tBaseResponse.success) {
                            responseListener.onSuccess(tBaseResponse)
                        }
//                        else if (tBaseResponse.getMessageCode().equals("09")) {
//
//                            // token is expire get the token
////                                    responseListener.onError(tBaseResponse);
//                            responseListener.onTokenExpired(tBaseResponse)
//                        }
                        else responseListener.onError(tBaseResponse)
                    }
                    responseListener.onResponse(tBaseResponse)
                }
            ) { throwable: Throwable ->
                throwable.printStackTrace()
                LogUtil.e(TAG, "error " + throwable.message)
                responseListener.onFailure(throwable)
                if (throwable is UnknownHostException || throwable is SocketTimeoutException) {
                    responseListener.netWorkException(throwable)
                } else if (throwable is JsonParseException) {
                    responseListener.jsonParseException(throwable)
                } else {
                    responseListener.unKnownException(throwable)
                }
            }
    } //
    //    public static void generateSessionToken(SessionNavigator navigator, Map<String, String> sessionParams) {
    //        //                            Util.isSuccessResponse(response);
    ////                            LoginPref.setApiToken(mContext, response.getData().getToken());
    ////                            setApiToken();
    //        //                            Util.showToastError(mContext, throwable);
    //        GotoApp.getApi().getSessionToken(sessionParams)
    //                .subscribeOn(Schedulers.io())
    //                .observeOn(AndroidSchedulers.mainThread())
    //                .subscribe(
    //
    //                        navigator::handleSuccessResponse, navigator::handleError);
    //    }
    //
    //    public static void tryReLogin(Map<String, String> loginParams, UserDetailNavigator navigator) {
    //        GotoApp.getApi().relogin(loginParams)
    //                .subscribeOn(Schedulers.io())
    //                .observeOn(AndroidSchedulers.mainThread())
    //                .subscribe(
    //                        navigator::handleSuccessResponse, navigator::handleError);
    //    }
}
*/
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

    fun <T> makeSecondaryRequestInBackground(
            single: Single<T?>?,
            liveData: MutableLiveData<SecondaryEvent<T>>
    ): Disposable {
        return single!!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    liveData.postValue(SecondaryEvent.StartLoading())
                }
                .subscribe(
                        { tBaseResponse: T? ->
                            liveData.postValue(SecondaryEvent.StopLoading())
                            if (tBaseResponse != null) {
                                LogUtil.e(
                                        TAG,
                                        "response $tBaseResponse"
                                )
//                        if (tBaseResponse.success) {
                                liveData.postValue(SecondaryEvent.Success(tBaseResponse))
//                        }
//                        else if (tBaseResponse.getMessageCode().equals("09")) {
//
//                            // token is expire get the token
////                                    responseListener.onError(tBaseResponse);
//                            responseListener.onTokenExpired(tBaseResponse)
//                        }
//                        else liveData.postValue(
//                            BaseNavEvent.Error(tBaseResponse.message, tBaseResponse))
                            }
//                    liveData.postValue( BaseNavEvent.Response(tBaseResponse))
                        }
                ) { throwable: Throwable ->
                    liveData.postValue(SecondaryEvent.StopLoading())


                    throwable.printStackTrace()
                    LogUtil.e(TAG, "error " + throwable.message)
                    liveData.postValue(SecondaryEvent.Failure(throwable))
                    if (throwable is UnknownHostException || throwable is SocketTimeoutException) {
                        liveData.postValue(SecondaryEvent.NetWorkException(throwable))
                    } else if (throwable is JsonParseException) {
                        liveData.postValue(SecondaryEvent.JsonParseException(throwable))
                    } else {
                        liveData.postValue(SecondaryEvent.UnKnownException(throwable))
                    }

                }

        //
        //    public static void generateSessionToken(SessionNavigator navigator, Map<String, String> sessionParams) {
        //        //                            Util.isSuccessResponse(response);
        ////                            LoginPref.setApiToken(mContext, response.getData().getToken());
        ////                            setApiToken();
        //        //                            Util.showToastError(mContext, throwable);
        //        GotoApp.getApi().getSessionToken(sessionParams)
        //                .subscribeOn(Schedulers.io())
        //                .observeOn(AndroidSchedulers.mainThread())
        //                .subscribe(
        //
        //                        navigator::handleSuccessResponse, navigator::handleError);
        //    }
        //
        //    public static void tryReLogin(Map<String, String> loginParams, UserDetailNavigator navigator) {
        //        GotoApp.getApi().relogin(loginParams)
        //                .subscribeOn(Schedulers.io())
        //                .observeOn(AndroidSchedulers.mainThread())
        //                .subscribe(
        //                        navigator::handleSuccessResponse, navigator::handleError);
        //    }
    }
}