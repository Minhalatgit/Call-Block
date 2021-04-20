package com.call.blockcallnow.data.model

import com.google.gson.annotations.SerializedName

data class PhoneNoDetailResponse(
    @field:SerializedName("statusCode")
    val statusCode: String? = null,
    @field:SerializedName("recordFound")
    val recordFound: String? = null,
    @field:SerializedName("phoneNumber")
    val phoneNumber: String? = null,
    @field:SerializedName("timestamp")
    val timestamp: String? = null,
    @field:SerializedName("spamRisk")
    val spamRisk: SpamRisk? = null


//	@field:SerializedName("ReportedCallerName")
//	val reportedCallerName: String? = null,
//	@field:SerializedName("Confidence")
//	val confidence: Int? = null,
//	@field:SerializedName("CallType")
//	val callType: String? = null,
//	@field:SerializedName("IsSpam")
//	val isSpam: Boolean? = null,
//	@field:SerializedName("LastComplaintDate")
//	val lastComplaintDate: String? = null,
//	@field:SerializedName("Tags")
//	val tags: List<String?>? = null
)

data class SpamRisk(

    @field:SerializedName("level")
    val level: Int? = null
)

