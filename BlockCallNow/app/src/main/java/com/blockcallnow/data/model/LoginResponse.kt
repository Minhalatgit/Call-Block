package com.blockcallnow.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @field:SerializedName("access_token")
    val accessToken: String? = null,
    @field:SerializedName("expires_at")
    val expiresAt: String? = null,
    @field:SerializedName("token_type")
    val tokenType: String? = null,
    @field:SerializedName("user")
    val user: UserDetail? = null
)



