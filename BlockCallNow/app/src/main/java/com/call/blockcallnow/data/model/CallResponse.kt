package com.call.blockcallnow.data.model

import com.google.gson.annotations.SerializedName

data class CallResponse(
    @SerializedName("date_updated") val date_updated: String,
    @SerializedName("price_unit") val price_unit: String,
    @SerializedName("parent_call_sid") val parent_call_sid: String,
    @SerializedName("caller_name") val caller_name: String,
    @SerializedName("duration") val duration: String,
    @SerializedName("from") val from: String,
    @SerializedName("to") val to: String,
    @SerializedName("annotation") val annotation: String,
    @SerializedName("answered_by") val answered_by: String,
    @SerializedName("sid") val sid: String,
    @SerializedName("queue_time") val queue_time: String,
    @SerializedName("price") val price: String,
    @SerializedName("api_version") val api_version: String,
    @SerializedName("status") val status: String,
    @SerializedName("direction") val direction: String,
    @SerializedName("start_time") val start_time: String,
    @SerializedName("date_created") val date_created: String,
    @SerializedName("from_formatted") val from_formatted: String,
    @SerializedName("group_sid") val group_sid: String,
    @SerializedName("trunk_sid") val trunk_sid: String,
    @SerializedName("forwarded_from") val forwarded_from: String,
    @SerializedName("uri") val uri: String,
    @SerializedName("account_sid") val account_sid: String,
    @SerializedName("end_time") val end_time: String,
    @SerializedName("to_formatted") val to_formatted: String,
    @SerializedName("phone_number_sid") val phone_number_sid: String,
    @SerializedName("subresource_uris") val subresource_uris: SubResource
)