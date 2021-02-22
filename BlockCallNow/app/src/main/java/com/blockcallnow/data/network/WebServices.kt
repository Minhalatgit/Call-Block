package com.blockcallnow.data.network

import com.blockcallnow.data.model.*
import com.blockcallnow.data.network.ApiConstant.Companion.API_AUDIO
import com.blockcallnow.data.network.ApiConstant.Companion.API_BLOCK_NO
import com.blockcallnow.data.network.ApiConstant.Companion.API_BLOCK_NO_DETAIL
import com.blockcallnow.data.network.ApiConstant.Companion.API_HISTORY

import com.blockcallnow.data.network.ApiConstant.Companion.API_LOGIN
import com.blockcallnow.data.network.ApiConstant.Companion.API_REGISTER
import com.blockcallnow.data.network.ApiConstant.Companion.API_USER
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
    )
            : Single<BaseResponse<LoginResponse?>?>?

    @FormUrlEncoded
    @POST(API_REGISTER)
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("phone_no") phone_no: String,
        @Field("password") password: String,
        @Field("password_confirmation") password_confirmation: String
    )
            : Single<BaseResponse<LoginResponse?>?>?

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
    )
            : Single<BaseResponse<Void?>?>?

    @GET(API_BLOCK_NO)
    fun getBlockNoList(
        @Header("Authorization") token: String
    )
            : Single<BaseResponse<BlockNoListResponse?>?>?

    @GET(API_BLOCK_NO_DETAIL)
    fun getBlockNoDetail(
        @Header("Authorization") token: String,
        @Query("phone_no") phoneNo: String
    )
            : Single<BaseResponse<BlockNoDetail?>?>?
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
        @Part("title") title: RequestBody,
        @Part audio: MultipartBody.Part?
    )
            : Single<BaseResponse<UploadAudioResponse?>?>?

    @DELETE(API_AUDIO)
    fun deleteAudio(
        @Header("Authorization") token: String,
        @Query("phone_no") phone: String
    )
            : Single<BaseResponse<Void?>?>?

    @FormUrlEncoded
    @POST(API_HISTORY)
    fun addToHistory(
        @Header("Authorization") token: String,
        @Field("block_no") phone: String?
    )
            : Single<BaseResponse<Nothing?>?>?

    @GET
    fun getPhoneNoDetail(
        @Url url: String,
        @Header("DataApiKey") api: String,
        @Header("DataApiSid") sid: String
    )
            : Call<PhoneNoDetailResponse>

    @GET(API_USER)
    fun getUser(@Header("Authorization") token: String)
            : Single<BaseResponse<LoginResponse?>?>?

    //
//    @GET(API_VISA_TYPE_LIST)
//    fun getVisaTypes(@Query("country_id") countryId: Int?): Single<BaseResponse<VisaTypeResponse?>?>?
//
//    @GET(API_PROFILE)
//    fun getProfile(@Header("Authorization") token: String): Single<BaseResponse<ProfileResponse?>?>?
//
//    @GET(API_SERVICE_COUNTRIES)
//    fun getServiceCountries(): Single<BaseResponse<ServiceCountryResponse?>?>?
//
//    @GET(API_LOC_COUNTRIES)
//    fun getLocCountries(): Single<BaseResponse<CountryResponse?>?>?
//
//    @GET(API_CITIES)
//    fun getCities(@Query("country_id") countryId: Int?): Single<BaseResponse<CityResponse?>?>?
//
//    @GET(API_LANGUAGES)
//    fun getLangs(): Single<BaseResponse<LanguagesResponse?>?>?
//
//    @Multipart
//    @POST(API_UPLOAD_DOCUMENTS)
//    fun uploadDocuments(
//        @Header("Authorization") token: String,
//        @Part documents: Array<MultipartBody.Part>
//    ): Single<BaseResponse<List<Document>?>?>?
//
//    @FormUrlEncoded
//    @POST(API_DELETE_DOCUMENTS)
//    fun deleteDocument(
//        @Header("Authorization") token: String,
//        @Field("document_id") id: Int?
//    ): Single<BaseResponse<Nothing?>?>?
//
//    @Multipart
//    @POST(API_SEND_QUOTATION)
//    fun sendQuotation(
//        @Header("Authorization") token: String,
//        @Part("customer_id") customerId: RequestBody,
//        @Part("country_id") countryId: RequestBody,
//        @Part("visatype_id") visatypeId: RequestBody,
//        @Part("is_milestone") isMilestone: RequestBody,
//        @Part("milestones") data: RequestBody
//    ): Single<BaseResponse<Nothing?>?>?
//
//    @GET(API_PACKAGES)
//    fun getPackages(@Header("Authorization") token: String): Single<BaseResponse<SubscriptionResponse?>?>?
//
//    @GET(API_ORDERS_LIST)
//    fun getOrders(
//        @Header("Authorization") token: String,
//        @Query("order_type") orderType: String
//    ): Single<BaseResponse<OrderListResponse?>?>?
//
//    @GET(API_ORDERS_DETAIL)
//    fun getOrderDetail(
//        @Header("Authorization") token: String,
//        @Query("order_id") orderIdd: Int
//    ): Single<BaseResponse<OrderDetailResponse?>?>?
//
//    @GET(API_PAYMENT_DASHBOARD)
//    fun getPaymentSummary(
//        @Header("Authorization") toke: String
//    ): Single<BaseResponse<PaymentDashBoardResponse?>?>?
//
//    @Multipart
//    @POST(API_DELIVER_MILESTONE)
//    fun deliverMilestone(
//        @Header("Authorization") token: String,
//        @Part("milestone_id") milestoneId: RequestBody?,
//        @Part("description") description: RequestBody?,
//        @Part documents: Array<MultipartBody.Part>?
//    ): Single<BaseResponse<Nothing?>?>?
//
//    @FormUrlEncoded
//    @POST(API_ADD_RATING)
//    fun addRating(
//        @Header("Authorization") token: String,
//        @Field("order_id") orderId: String?,
//        @Field("user_id") userId: String?,
//        @Field("rate") rate: String,
//        @Field("description") description: String
//    ): Single<BaseResponse<Nothing?>?>?
//
//    @FormUrlEncoded
//    @POST(API_CHANGE_PASSWORD)
//    fun changePassword(
//        @Header("Authorization") token: String,
//        @Field("old_password") oldPassword: String,
//        @Field("new_password") newPassword: String
//    ): Single<BaseResponse<Nothing?>?>?
//    https://api.callcontrol.com/api/2015-11-01/Reputation/18008472911?api_key=demo

}