package com.meikenn.tama.data.repository

import com.meikenn.tama.data.remote.ExternalApiService
import com.meikenn.tama.domain.model.Teacher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeacherRepository @Inject constructor(
    private val externalApiService: ExternalApiService
) {
    suspend fun getTeachers(): List<Teacher> {
        val json = externalApiService.getTeachers()

        if (!json.has("status") || !json.get("status").asBoolean) {
            throw IllegalStateException("Teacher API returned status=false")
        }

        val dataArray = json.getAsJsonArray("data")
        return dataArray.map { element ->
            val obj = element.asJsonObject
            Teacher(
                name = obj.get("name").asString,
                furigana = obj.get("furigana")?.takeIf { !it.isJsonNull }?.asString,
                email = obj.get("email").asString
            )
        }
    }
}
