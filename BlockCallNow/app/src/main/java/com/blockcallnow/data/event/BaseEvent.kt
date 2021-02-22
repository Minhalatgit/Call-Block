package com.blockcallnow.data.event

import com.blockcallnow.data.model.BaseResponse


open class BaseNavEvent<T> {
    private var hasBeenHandled = false
    var data: BaseResponse<T>? = null
    var message: String? = null
    var throwable: Throwable? = null

    constructor (
        data: BaseResponse<T>? = null,
        message: String? = null
    ) {
        this.data = data
        this.message = message
    }

    constructor(throwable: Throwable? = null) {
        this.throwable = throwable
    }

    constructor(message: String) {
        this.message = message
    }


    class Success<T>(data: BaseResponse<T>? = null) : BaseNavEvent<T>(data)
    class StartLoading<T>(data: BaseResponse<T>? = null) : BaseNavEvent<T>(data)
    class StopLoading<T>(data: BaseResponse<T>? = null) : BaseNavEvent<T>(data)
    class Error<T>(message: String, data: BaseResponse<T>? = null) : BaseNavEvent<T>(data, message)
    class Response<T>(data: BaseResponse<T>? = null) : BaseNavEvent<T>(data)
    class Failure<T>(throwable: Throwable) : BaseNavEvent<T>(throwable)
    class NetWorkException<T>(throwable: Throwable) : BaseNavEvent<T>(throwable)
    class JsonParseException<T>(throwable: Throwable) : BaseNavEvent<T>(throwable)
    class UnKnownException<T>(throwable: Throwable) : BaseNavEvent<T>(throwable)
    class ShowMessage<T>(val msg: String) : BaseNavEvent<T>(msg)
}