package com.call.blockcallnow.data.network

interface NetworkResponseListener<T> {
    /*
     * onSuccess will be called when response.getResult==1
     * */
    fun onSuccess(response: T) {}

    /*
     * onError will be called when response.getResult==0
     * */
    fun onError(response: T) {}

    /*
     * onResponse will be called when api will be successfully completed
     * same as retrofit2 onResponse
     * */
    fun onResponse(response: T) {}

    /*
     * onFailure will be called when api will be successfully completed
     *same as retrofit2 onFailure
     * */
    fun onFailure(error: Throwable?) {}
    fun onTokenExpired(error: T) {}
    fun jsonParseException(error: Throwable?) {}
    fun netWorkException(error: Throwable?) {}
    fun unKnownException(error: Throwable?) {}
}