package com.blockcallnow.data.model

import com.google.gson.annotations.SerializedName

data class UploadAudioResponse(
    val audio: Audio? = null
)

data class Audio(
    @field:SerializedName("phone_no")
    val phoneNo: String? = null,
    @field:SerializedName("file_url")
    val fileUrl: String? = null,
    @field:SerializedName("user_id")
    val userId: Int? = null,
    @field:SerializedName("name")
    val name: String? = null,
    @field:SerializedName("id")
    val id: Int? = null,
    @field:SerializedName("title")
    val title: String? = null,
    @field:SerializedName("type")
    val type: String? = null,
)

