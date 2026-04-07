package com.meikenn.tama.data.repository

import android.util.Log
import com.google.gson.JsonObject
import com.meikenn.tama.data.local.SecureStorage
import com.meikenn.tama.data.model.ApiRequestBody
import com.meikenn.tama.data.remote.ApiService
import com.meikenn.tama.domain.model.Announcement
import com.meikenn.tama.domain.model.Attendance
import com.meikenn.tama.domain.model.CourseDetail
import java.net.URLDecoder
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseDetailRepository @Inject constructor(
    private val apiService: ApiService,
    private val secureStorage: SecureStorage
) {
    companion object {
        private const val TAG = "CourseDetailRepo"
    }

    suspend fun getCourseDetail(
        jugyoCd: String,
        nendo: Int,
        kaikoNendo: Int,
        gakkiNo: Int,
        jugyoKbn: String,
        kaikoYobi: Int,
        jigenNo: Int
    ): Result<CourseDetail> {
        return try {
            val user = secureStorage.getUser()
                ?: return Result.failure(Exception("ユーザー認証情報がありません"))

            val requestData = JsonObject().apply {
                addProperty("jugyoCd", jugyoCd)
                addProperty("nendo", nendo)
                addProperty("kaikoNendo", kaikoNendo)
                addProperty("gakkiNo", gakkiNo)
                addProperty("jugyoKbn", jugyoKbn)
                addProperty("kaikoYobi", kaikoYobi)
                addProperty("jigenNo", jigenNo)
            }

            val body = ApiRequestBody(
                loginUserId = user.username,
                encryptedLoginPassword = user.encryptedPassword ?: "",
                data = requestData
            )

            val response = apiService.getCourseDetail(body)

            if (response.statusDto?.success != true) {
                val errorMsg = response.statusDto?.errorList?.firstOrNull()?.errorMsg
                    ?: "授業詳細の取得に失敗しました"
                return Result.failure(Exception(errorMsg))
            }

            val data = response.dataObject
                ?: return Result.failure(Exception("レスポンスデータがありません"))

            val courseDetail = parseCourseDetail(data)
            Result.success(courseDetail)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get course detail: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun saveMemo(jugyoCd: String, nendo: Int, memo: String): Result<Unit> {
        return try {
            val user = secureStorage.getUser()
                ?: return Result.failure(Exception("ユーザー認証情報がありません"))

            val encodedMemo = URLEncoder.encode(memo, "UTF-8")
            val requestData = JsonObject().apply {
                addProperty("jugyoCd", jugyoCd)
                addProperty("nendo", nendo)
                addProperty("jugyoMemo", encodedMemo)
            }

            val body = ApiRequestBody(
                loginUserId = user.username,
                encryptedLoginPassword = user.encryptedPassword ?: "",
                data = requestData
            )

            val response = apiService.saveMemo(body)

            if (response.statusDto?.success != true) {
                return Result.failure(Exception("メモの保存に失敗しました"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save memo: ${e.message}")
            Result.failure(e)
        }
    }

    private fun parseCourseDetail(data: JsonObject): CourseDetail {
        // Parse announcements
        val announcements = data.getAsJsonArray("keijiInfoDtoList")?.mapNotNull { elem ->
            try {
                val obj = elem.asJsonObject
                Announcement(
                    id = obj.get("keijiNo")?.asInt ?: 0,
                    title = obj.get("subject")?.asString ?: "",
                    date = obj.get("keijiAppendDate")?.asLong ?: 0L,
                    torkDate = obj.get("keijiTorkDate")?.let {
                        if (it.isJsonNull) null else it.asString
                    }
                )
            } catch (e: Exception) {
                null
            }
        } ?: emptyList()

        // Parse attendance
        val attList = data.getAsJsonArray("attInfoDtoList")
        val attendance = if (attList != null && attList.size() > 0) {
            val att = attList[0].asJsonObject
            Attendance(
                present = att.get("shusekiKaisu")?.asInt ?: 0,
                absent = att.get("kessekiKaisu")?.asInt ?: 0,
                late = att.get("chikokKaisu")?.asInt ?: 0,
                early = att.get("sotaiKaisu")?.asInt ?: 0,
                sick = att.get("koketsuKaisu")?.asInt ?: 0,
                unregistered = att.get("mitourokuKaisu")?.asInt ?: 0
            )
        } else {
            Attendance()
        }

        val rawMemo = data.get("jugyoMemo")?.let {
            if (it.isJsonNull) "" else it.asString
        } ?: ""
        val memo = try {
            URLDecoder.decode(rawMemo, "UTF-8")
        } catch (_: Exception) {
            rawMemo
        }

        val syllabusPubFlg = data.get("syllabusPubFlg")?.asBoolean ?: false
        val syuKetuKanriFlg = data.get("syuKetuKanriFlg")?.asBoolean ?: false

        return CourseDetail(
            announcements = announcements,
            attendance = attendance,
            memo = memo,
            syllabusPubFlg = syllabusPubFlg,
            syuKetuKanriFlg = syuKetuKanriFlg
        )
    }
}
