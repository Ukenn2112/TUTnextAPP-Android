package com.meikenn.tama.data.repository

import android.provider.Settings
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.meikenn.tama.data.local.SecureStorage
import com.meikenn.tama.data.model.ApiRequestBody
import com.meikenn.tama.data.remote.ApiService
import com.meikenn.tama.data.remote.CookieJarImpl
import com.meikenn.tama.domain.model.User
import com.meikenn.tama.util.PasswordEncoder
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val secureStorage: SecureStorage,
    private val cookieJar: CookieJarImpl,
    private val gson: Gson
) {

    suspend fun login(account: String, password: String): Result<User> {
        return try {
            val deviceId = UUID.randomUUID().toString()
            val loginData = JsonObject().apply {
                addProperty("loginUserId", account)
                addProperty("plainLoginPassword", password)
                addProperty("judgeLoginPossibleFlg", false)
                addProperty("deviceId", deviceId)
                addProperty("autoLoginAuthCd", "")
            }
            val body = ApiRequestBody(
                loginUserId = account,
                data = loginData
            )

            val response = apiService.login(body)

            if (response.statusDto?.success != true) {
                val errorMsg = response.statusDto?.errorList?.firstOrNull()?.errorMsg
                    ?: response.statusDto?.messageList?.firstOrNull()
                    ?: "ログインに失敗しました"
                return Result.failure(Exception(errorMsg))
            }

            val data = response.data ?: return Result.failure(Exception("レスポンスデータがありません"))
            val user = createUserFromResponse(account, data)
                ?: return Result.failure(Exception("ユーザー情報の解析に失敗しました"))

            secureStorage.saveUser(user)

            // Call firstSetting after login
            try {
                firstSetting(user, deviceId)
            } catch (e: Exception) {
                // firstSetting failure is non-fatal
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun firstSetting(user: User, deviceId: String) {
        val settingData = JsonObject().apply {
            addProperty("deviceId", deviceId)
            addProperty("token", "")
            addProperty("keijiNoticeFlg", false)
            addProperty("jugyoNoticeFlg", false)
            val productIniList = JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("productCd", "AP")
                    addProperty("section", "ATTEND_PUSH")
                    addProperty("key", "PUSH_USE_FLAG")
                })
                add(JsonObject().apply {
                    addProperty("productCd", "AP")
                    addProperty("section", "ATTEND_PUSH")
                    addProperty("key", "PUSH_USE_FLAG_PARENT")
                })
            }
            add("productIniFileDtoList", productIniList)
        }
        val body = ApiRequestBody(
            loginUserId = user.username,
            encryptedLoginPassword = user.encryptedPassword ?: "",
            data = settingData
        )
        apiService.firstSetting(body)
    }

    suspend fun logout(): Result<Unit> {
        return try {
            val user = secureStorage.getUser()
            if (user != null) {
                val body = ApiRequestBody(
                    loginUserId = user.username,
                    encryptedLoginPassword = user.encryptedPassword ?: ""
                )
                apiService.logout(body)
            }
            secureStorage.clearAll()
            cookieJar.clearCookies()
            Result.success(Unit)
        } catch (e: Exception) {
            // Clear local data even if API call fails
            secureStorage.clearAll()
            cookieJar.clearCookies()
            Result.success(Unit)
        }
    }

    fun getCurrentUser(): User? = secureStorage.getUser()

    fun isLoggedIn(): Boolean {
        return secureStorage.getUser() != null && cookieJar.hasSessionCookies()
    }

    fun updateUser(user: User) {
        secureStorage.saveUser(user)
    }

    private fun createUserFromResponse(account: String, data: JsonObject): User? {
        return try {
            val userId = data.get("userId")?.asString ?: account
            val userName = data.get("userName")?.asString ?: ""
            val gaksekiCd = data.get("gaksekiCd")?.asString
                ?: data.get("jinjiCd")?.asString
                ?: userId
            val rawPassword = data.get("encryptedPassword")?.asString ?: ""
            val encodedPassword = PasswordEncoder.encodePassword(rawPassword)
            val allKeijiMidokCnt = data.get("allKeijiMidokCnt")?.asInt
            val maxJigenNo = data.get("maxJigenNo")?.asInt

            User(
                id = gaksekiCd,
                username = userId,
                fullName = userName,
                encryptedPassword = encodedPassword,
                allKeijiMidokCnt = allKeijiMidokCnt,
                maxJigenNo = maxJigenNo
            )
        } catch (e: Exception) {
            null
        }
    }
}
