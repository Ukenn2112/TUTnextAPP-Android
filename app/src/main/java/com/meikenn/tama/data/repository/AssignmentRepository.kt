package com.meikenn.tama.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.meikenn.tama.data.local.PreferencesManager
import com.meikenn.tama.data.local.SecureStorage
import com.meikenn.tama.data.model.AssignmentResponse
import com.meikenn.tama.data.remote.ExternalApiService
import com.meikenn.tama.domain.model.Assignment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssignmentRepository @Inject constructor(
    private val externalApiService: ExternalApiService,
    private val secureStorage: SecureStorage,
    private val preferencesManager: PreferencesManager,
    private val gson: Gson
) {
    suspend fun fetchAssignments(): Result<List<Assignment>> = runCatching {
        val user = secureStorage.getUser()
            ?: throw IllegalStateException("ユーザー情報が見つかりません")

        val body = JsonObject().apply {
            addProperty("username", user.username)
            addProperty("encryptedPassword", user.encryptedPassword)
        }

        val responseJson = externalApiService.getAssignments(body)
        val response = gson.fromJson(responseJson, AssignmentResponse::class.java)

        if (!response.status) {
            throw IllegalStateException("APIエラー: データの取得に失敗しました")
        }

        val assignments = response.data
            ?.map { it.toAssignment() }
            ?.sortedBy { it.dueDate }
            ?: emptyList()

        // Update fingerprints for new-assignment detection
        val currentFingerprints = assignments.map { it.fingerprint }.toSet()
        preferencesManager.saveAssignmentFingerprints(currentFingerprints)

        assignments
    }
}
