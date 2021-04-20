package com.call.blockcallnow.data.model

import com.google.gson.annotations.SerializedName

data class BlockNoListResponse(
    @field:SerializedName("block_list")
    val block_list: List<BlockNoDetails>? = null
)