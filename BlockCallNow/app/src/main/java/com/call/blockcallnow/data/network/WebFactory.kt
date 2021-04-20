package com.call.blockcallnow.data.network

import com.call.blockcallnow.BuildConfig
import com.call.blockcallnow.data.network.ApiConstant.Companion.BASE_URL
import com.call.blockcallnow.data.network.ApiConstant.Companion.TWILIO_BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class WebFactory {
    companion object {

        fun getApiService(): WebServices {
            val interceptor = HttpLoggingInterceptor()
            interceptor.apply { interceptor.level = HttpLoggingInterceptor.Level.BODY }
            val builder = OkHttpClient.Builder()
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
            if (BuildConfig.DEBUG) {
                builder.addInterceptor(interceptor)
            }

            val retrofit = Retrofit.Builder()
                    .client(builder.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .baseUrl(BASE_URL)
                    .build()

            return retrofit.create(WebServices::class.java)
        }

        fun getApiService2(): WebServices {
            val interceptor = HttpLoggingInterceptor()
            interceptor.apply { interceptor.level = HttpLoggingInterceptor.Level.BODY }
            val builder = OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
            if (BuildConfig.DEBUG) {
                builder.addInterceptor(interceptor)
            }

            val retrofit = Retrofit.Builder()
                    .client(builder.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(BASE_URL)
                    .build()

            return retrofit.create(WebServices::class.java)
        }

        fun getTwilioService(): WebServices {
            val interceptor = HttpLoggingInterceptor()
            interceptor.apply { interceptor.level = HttpLoggingInterceptor.Level.BODY }
            val builder = OkHttpClient.Builder()
                // username: accountSid
                // password: auth token
                .addInterceptor(
                    BasicAuthInterceptor(
                        "ACc215d55fa3243b7699d2fdae7a30cdfe",
                        "1c88f7b927baebe07d41125428f1c4b4"
                    )
                )
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
            if (BuildConfig.DEBUG) {
                builder.addInterceptor(interceptor)
            }

            val retrofit = Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(TWILIO_BASE_URL)
                .build()

            return retrofit.create(WebServices::class.java)
        }
    }
}