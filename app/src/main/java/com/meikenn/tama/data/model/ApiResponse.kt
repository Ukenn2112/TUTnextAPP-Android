package com.meikenn.tama.data.model

import com.google.gson.JsonElement
import com.google.gson.JsonObject

data class ApiResponse(
    val statusDto: StatusDto? = null,
    val data: JsonElement? = null
) {
    val dataObject: JsonObject?
        get() = if (data != null && data.isJsonObject) data.asJsonObject else null
}

data class StatusDto(
    val success: Boolean = false,
    val messageList: List<String>? = null,
    val errorList: List<ErrorItem>? = null
)

data class ErrorItem(
    val errorCd: String? = null,
    val errorMsg: String? = null
)
