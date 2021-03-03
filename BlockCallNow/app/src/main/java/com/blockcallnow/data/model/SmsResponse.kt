package com.blockcallnow.data.model

import com.google.gson.annotations.SerializedName

data class SmsResponse(
    @SerializedName("sid") val sid: String,
    @SerializedName("date_created") val date_created: String,
    @SerializedName("date_updated") val date_updated: String,
    @SerializedName("date_sent") val date_sent: String,
    @SerializedName("account_sid") val account_sid: String,
    @SerializedName("to") val to: String,
    @SerializedName("from") val from: String,
    @SerializedName("messaging_service_sid") val messaging_service_sid: String,
    @SerializedName("body") val body: String,
    @SerializedName("status") val status: String,
    @SerializedName("num_segments") val num_segments: String,
    @SerializedName("num_media") val num_media: String,
    @SerializedName("direction") val direction: String,
    @SerializedName("api_version") val api_version: String,
    @SerializedName("price") val price: String,
    @SerializedName("price_unit") val price_unit: String,
    @SerializedName("error_code") val error_code: String,
    @SerializedName("error_message") val error_message: String,
    @SerializedName("uri") val uri: String,
    @SerializedName("subresource_uris") val subresource_uris: SubResourceSms
)