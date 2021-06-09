package com.call.blockcallnow.data.network

import com.call.blockcallnow.data.model.*
import com.call.blockcallnow.data.network.ApiConstant.Companion.API_AUDIO
import com.call.blockcallnow.data.network.ApiConstant.Companion.API_BLOCK_NO
import com.call.blockcallnow.data.network.ApiConstant.Companion.API_BLOCK_NO_DETAIL
import com.call.blockcallnow.data.network.ApiConstant.Companion.API_HISTORY

import com.call.blockcallnow.data.network.ApiConstant.Companion.API_LOGIN
import com.call.blockcallnow.data.network.ApiConstant.Companion.API_REGISTER
import com.call.blockcallnow.data.network.ApiConstant.Companion.API_UPDATE_PASSWORD
import com.call.blockcallnow.data.network.ApiConstant.Companion.API_USER
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface WebServices {

    @FormUrlEncoded
    @POST(API_LOGIN)
    fun loginAPI(
        @Field("phone_no") email: String,
        @Field("password") password: String,
        @Field("device_type") device_type: String,
        @Field("device_token") device_token: String
    ): Single<BaseResponse<LoginResponse?>?>?

    @FormUrlEncoded
    @POST(API_UPDATE_PASSWORD)
    fun updatePasswordAPI(
        @Query("id") id: String,
        @Field("password") password: String,
        @Field("password_confirmation") confirmPassword: String
    ): Single<BaseResponse<LoginResponse?>?>?

    @FormUrlEncoded
    @POST(API_REGISTER)
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("phone_no") phone_no: String,
        @Field("password") password: String,
        @Field("password_confirmation") password_confirmation: String
    ): Single<BaseResponse<LoginResponse?>?>?

    @FormUrlEncoded
    @POST(API_BLOCK_NO)
    fun blockNo(
        @Header("Authorization") token: String,
        @Field("phone_no") phoneNo: String?,
        @Field("name") name: String?,
        @Field("formatted_phone_no") formatted_phone_no: String?,
        @Field("status") status: String,
        @Field("set_voice_gender") gender: String,
        @Field("is_generic_text") is_generic_text: Int,
        @Field("set_voice_lang") voice_lng: String,
        @Field("message") message: String?
    ): Single<BaseResponse<Void?>?>?

    @GET(API_BLOCK_NO)
    fun getBlockNoList(
        @Header("Authorization") token: String
    ): Single<BaseResponse<BlockNoListResponse?>?>?

    @GET(API_BLOCK_NO_DETAIL)
    fun getBlockNoDetail(
        @Header("Authorization") token: String,
        @Query("phone_no") phoneNo: String
    ): Single<BaseResponse<BlockNoDetail?>?>?

    @GET(API_BLOCK_NO_DETAIL)
    fun getBlockNoDetailForAudio(
        @Header("Authorization") token: String,
        @Query("phone_no") phoneNo: String
    ): Call<BaseResponse<BlockNoDetail>>

    //    @FormUrlEncoded
//    @POST(API_VERIFY_EMAIL)
//    fun verifyCode(
//        @Header("Authorization") token: String,
//        @Field("email_code") otp: String
//    ): Single<BaseResponse<Nothing?>?>?
    @Multipart
    @POST(API_AUDIO)
    fun uploadAudio(
        @Header("Authorization") token: String,
        @Part("phone_no") phoneNo: RequestBody,
        @Part("formatted_phone_no") blockNo: RequestBody,
        @Part("title") title: RequestBody,
        @Part audio: MultipartBody.Part?
    ): Single<BaseResponse<UploadAudioResponse?>?>?

    @DELETE(API_AUDIO)
    fun deleteAudio(
        @Header("Authorization") token: String,
        @Query("formatted_phone_no") phone: String
    ): Single<BaseResponse<Void?>?>?

    @FormUrlEncoded
    @POST(API_HISTORY)
    fun addToHistory(
        @Header("Authorization") token: String,
        @Field("block_no") phone: String?
    ): Single<BaseResponse<Nothing?>?>?

    @GET
    fun getPhoneNoDetail(
        @Url url: String,
        @Header("DataApiKey") api: String,
        @Header("DataApiSid") sid: String
    ): Call<PhoneNoDetailResponse>

    @GET(API_USER)
    fun getUser(@Header("Authorization") token: String): Single<BaseResponse<LoginResponse?>?>?

    @FormUrlEncoded
    @POST(ApiConstant.MAKE_CALL)
    fun callTwilioNumber(
        @Field("To") to: String,
        @Field("From") from: String,
        @Field("Url") voiceUrl: String
    ): Call<CallResponse>

    @FormUrlEncoded
    @POST(ApiConstant.SEND_SMS)
    fun smsTwilioNumber(
        @Field("To") to: String,
        @Field("From") from: String,
        @Field("Body") message: String
    ): Call<SmsResponse>
}