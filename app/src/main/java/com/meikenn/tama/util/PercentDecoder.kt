package com.meikenn.tama.util

import java.net.URLDecoder

object PercentDecoder {
    fun decode(input: String): String {
        return try {
            val decoded = URLDecoder.decode(input, "UTF-8")
            decoded.replace("\u3000", " ")
        } catch (e: Exception) {
            input
        }
    }
}
