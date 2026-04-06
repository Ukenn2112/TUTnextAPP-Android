package com.meikenn.tama.data.remote

import android.content.SharedPreferences
import android.util.Base64
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import javax.inject.Inject
import javax.inject.Named

class CookieJarImpl @Inject constructor(
    @Named("cookie_prefs") private val prefs: SharedPreferences
) : CookieJar {

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val editor = prefs.edit()
        val existing = loadAll().toMutableSet()
        for (cookie in cookies) {
            existing.removeAll { it.name == cookie.name && it.domain == cookie.domain }
            existing.add(cookie)
        }
        val serialized = existing.map { serializeCookie(it) }.toSet()
        editor.putStringSet("cookies", serialized)
        editor.apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return loadAll().filter { it.matches(url) }
    }

    fun clearCookies() {
        prefs.edit().remove("cookies").apply()
    }

    fun hasSessionCookies(domain: String = "next.tama.ac.jp"): Boolean {
        return loadAll().any { it.domain.contains(domain) }
    }

    private fun loadAll(): List<Cookie> {
        val serialized = prefs.getStringSet("cookies", emptySet()) ?: emptySet()
        return serialized.mapNotNull { deserializeCookie(it) }
    }

    private fun encode(value: String): String {
        return Base64.encodeToString(value.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

    private fun decode(value: String): String {
        return String(Base64.decode(value, Base64.NO_WRAP), Charsets.UTF_8)
    }

    private fun serializeCookie(cookie: Cookie): String {
        return "${encode(cookie.name)}|${encode(cookie.value)}|${encode(cookie.domain)}|${encode(cookie.path)}|${cookie.expiresAt}|${cookie.secure}|${cookie.httpOnly}"
    }

    private fun deserializeCookie(str: String): Cookie? {
        return try {
            val parts = str.split("|")
            if (parts.size < 7) return null
            Cookie.Builder()
                .name(decode(parts[0]))
                .value(decode(parts[1]))
                .domain(decode(parts[2]))
                .path(decode(parts[3]))
                .expiresAt(parts[4].toLong())
                .apply {
                    if (parts[5].toBoolean()) secure()
                    if (parts[6].toBoolean()) httpOnly()
                }
                .build()
        } catch (e: Exception) {
            null
        }
    }
}
