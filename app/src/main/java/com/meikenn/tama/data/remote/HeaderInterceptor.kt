package com.meikenn.tama.data.remote

import com.meikenn.tama.util.Constants
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class HeaderInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Content-Type", Constants.CONTENT_TYPE_JSON)
            .header("User-Agent", Constants.USER_AGENT)
            .build()
        return chain.proceed(request)
    }
}
