package com.meikenn.tama.util

object PasswordEncoder {
    fun encodePassword(password: String): String {
        return password
            .replace("/", "%2F")
            .replace("+", "%2B")
            .replace("=", "%3D")
    }
}
