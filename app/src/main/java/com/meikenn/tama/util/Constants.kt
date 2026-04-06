package com.meikenn.tama.util

object Constants {
    const val BASE_URL = "https://next.tama.ac.jp/uprx/webapi/"
    const val EXTERNAL_API_BASE_URL = "https://tama.qaq.tw/"
    const val PRINT_BASE_URL = "https://cloudodp.fujifilm.com/"

    const val USER_AGENT = "UNIPA/1.1.35 CFNetwork/3826.500.62.2.1 Darwin/24.4.0"
    const val CONTENT_TYPE_JSON = "application/json"

    const val PRODUCT_CD = "ap"
    const val SUB_PRODUCT_CD = "apa"
    const val LANG_CD = "ja"

    const val CACHE_TTL_MS = 12 * 60 * 60 * 1000L // 12 hours
    const val ROOM_CHANGE_EXPIRY_MS = 48 * 60 * 60 * 1000L // 48 hours

    const val PRINT_CREDENTIALS_ID = "836-tamauniv01"
    const val PRINT_CREDENTIALS_PASSWORD = "tama1989"
}
