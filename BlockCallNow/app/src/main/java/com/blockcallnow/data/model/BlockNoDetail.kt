package com.blockcallnow.data.model

import com.google.gson.annotations.SerializedName

data class BlockNoDetail(

    @field:SerializedName("block_no_details")
    val blockNoDetails: BlockNoDetails? = null,
    @field:SerializedName("audio")
    val audio: Audio? = null
)

data class BlockNoDetails(

    @field:SerializedName("phone_no")
    val phoneNo: String? = null,

    @field:SerializedName("updated_at")
    val updatedAt: String? = null,

    @field:SerializedName("user_id")
    val userId: Int? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("deleted_at")
    val deletedAt: String? = null,

    @field:SerializedName("status")
    val status: String? = null,

    @field:SerializedName("formatted_phone_no")
    val formatted_phone_no: String? = null,

    @field:SerializedName("is_generic_text")
    val is_generic_text: Int? = null,

    @field:SerializedName("set_voice_gender")
    val set_voice_gender: String? = null,

    @field:SerializedName("set_voice_lang")
    val set_voice_lang: String? = null,

    @field:SerializedName("message")
    val message: String? = null
)
