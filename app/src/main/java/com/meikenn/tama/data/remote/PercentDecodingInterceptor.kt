package com.meikenn.tama.data.remote

import com.meikenn.tama.util.PercentDecoder
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject

class PercentDecodingInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val body = response.body ?: return response

        val contentType = body.contentType()
        val rawString = body.string()

        // Decode percent-encoded JSON response from university API
        val decoded = if (rawString.startsWith("%7B") || rawString.startsWith("%5B")) {
            PercentDecoder.decode(rawString)
        } else {
            rawString
        }

        val newBody = decoded.toResponseBody(contentType)
        return response.newBuilder().body(newBody).build()
    }
}
