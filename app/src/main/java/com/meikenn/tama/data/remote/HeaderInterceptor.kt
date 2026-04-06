package com.meikenn.tama.data.remote

import com.meikenn.tama.util.Constants
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class HeaderInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
            .header("User-Agent", Constants.USER_AGENT)

        // Only add Content-Type for requests with a body (POST, PUT, PATCH)
        if (original.body != null) {
            builder.header("Content-Type", Constants.CONTENT_TYPE_JSON)
        }

        return chain.proceed(builder.build())
    }
}
