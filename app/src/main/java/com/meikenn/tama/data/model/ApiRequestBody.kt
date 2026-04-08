package com.meikenn.tama.data.model

import com.google.gson.JsonObject
import com.meikenn.tama.util.Constants

data class ApiRequestBody(
    val productCd: String = Constants.PRODUCT_CD,
    val subProductCd: String = Constants.SUB_PRODUCT_CD,
    val loginUserId: String = "",
    val encryptedLoginPassword: String = "",
    val langCd: String = Constants.LANG_CD,
    val data: JsonObject = JsonObject()
)
