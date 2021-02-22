package com.blockcallnow.data.event


open class SecondaryEvent<T> {
    private var hasBeenHandled = false
    var data: T? = null
    var message: String? = null
    var throwable: Throwable? = null

    constructor (
        data: T? = null,
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

    class Success<T>(data: T? = null) : SecondaryEvent<T>(data)
    class StartLoading<T>(data: T? = null) : SecondaryEvent<T>(data)
    class StopLoading<T>(data: T? = null) : SecondaryEvent<T>(data)
    class Error<T>(message: String, data: T? = null) : SecondaryEvent<T>(data, message)
    class Response<T>(data: T? = null) : SecondaryEvent<T>(data)
    class Failure<T>(throwable: Throwable) : SecondaryEvent<T>(throwable)
    class NetWorkException<T>(throwable: Throwable) : SecondaryEvent<T>(throwable)
    class JsonParseException<T>(throwable: Throwable) : SecondaryEvent<T>(throwable)
    class UnKnownException<T>(throwable: Throwable) : SecondaryEvent<T>(throwable)
    class ShowMessage<T>(val msg: String) : SecondaryEvent<T>(msg)
}