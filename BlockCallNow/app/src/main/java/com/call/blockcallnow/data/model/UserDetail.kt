package com.call.blockcallnow.data.model

import com.google.gson.annotations.SerializedName

data class UserDetail(
        @field:SerializedName("phone_no")
        val phoneNo: String? = null,
        @field:SerializedName("name")
        val name: String? = null,
        @field:SerializedName("id")
        val id: Int? = null,
        @field:SerializedName("email")
        val email: String? = null,

        @field:SerializedName("paywhirl_customer_id")
        val paywhirl_customer_id: Int? = null,
        @field:SerializedName("paywhirl_plan_id")
        val paywhirl_plan_id: Int? = null,
        @field:SerializedName("paywhirl_subscription_id")
        val paywhirl_subscription_id: Int? = null,
        @field:SerializedName("paywhirl_current_period_end")
        val paywhirl_current_period_end: String? = null,

        @field:SerializedName("avatar")
        val avatar: String? = null,
        @field:SerializedName("is_expired")
        val is_expired: Boolean = true,
        @field:SerializedName("renewal_url")
        val renewal_url: String? = null
)