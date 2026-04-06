package com.meikenn.tama.data.local

import android.content.SharedPreferences
import com.google.gson.Gson
import com.meikenn.tama.domain.model.User
import javax.inject.Inject
import javax.inject.Named

class SecureStorage @Inject constructor(
    @Named("secure_prefs") private val prefs: SharedPreferences,
    private val gson: Gson
) {
    companion object {
        private const val KEY_USER = "currentUser"
        private const val KEY_DEVICE_TOKEN = "deviceToken"
    }

    fun saveUser(user: User) {
        prefs.edit().putString(KEY_USER, gson.toJson(user)).apply()
    }

    fun getUser(): User? {
        val json = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(json, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun clearUser() {
        prefs.edit().remove(KEY_USER).apply()
    }

    fun saveDeviceToken(token: String) {
        prefs.edit().putString(KEY_DEVICE_TOKEN, token).apply()
    }

    fun getDeviceToken(): String? {
        return prefs.getString(KEY_DEVICE_TOKEN, null)
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
