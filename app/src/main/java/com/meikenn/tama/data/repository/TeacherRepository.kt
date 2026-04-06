package com.meikenn.tama.data.repository

import android.util.Log
import com.meikenn.tama.data.remote.ExternalApiService
import com.meikenn.tama.domain.model.Teacher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeacherRepository @Inject constructor(
    private val externalApiService: ExternalApiService
) {
    companion object {
        private const val TAG = "TeacherRepo"
    }

    suspend fun getTeachers(): List<Teacher> {
        val json = externalApiService.getTeachers()

        if (!json.has("status") || !json.get("status").asBoolean) {
            throw IllegalStateException("Teacher API returned status=false")
        }

        val dataArray = json.getAsJsonArray("data")
            ?: throw IllegalStateException("Teacher API returned no data array")

        return dataArray.mapNotNull { element ->
            try {
                val obj = element.asJsonObject
                val name = obj.get("name")?.takeIf { !it.isJsonNull }?.asString ?: return@mapNotNull null
                val email = obj.get("email")?.takeIf { !it.isJsonNull }?.asString ?: return@mapNotNull null
                val furigana = obj.get("furigana")?.takeIf { !it.isJsonNull }?.asString

                Teacher(name = name, furigana = furigana, email = email)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse teacher: ${e.message}")
                null
            }
        }
    }
}
