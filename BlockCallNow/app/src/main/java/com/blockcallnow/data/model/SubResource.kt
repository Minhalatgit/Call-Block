package com.blockcallnow.data.model

import com.google.gson.annotations.SerializedName

data class SubResource(
    @SerializedName("notifications") val notifications: String,
    @SerializedName("recordings") val recordings: String,
    @SerializedName("payments") val payments: String,
    @SerializedName("feedback") val feedback: String,
    @SerializedName("events") val events: String,
    @SerializedName("feedback_summaries") val feedback_summaries: String
)