package com.meikenn.tama.data.model

import com.google.gson.annotations.SerializedName
import com.meikenn.tama.domain.model.Assignment
import com.meikenn.tama.domain.model.AssignmentStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class AssignmentResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("data") val data: List<ApiAssignment>?
)

data class ApiAssignment(
    @SerializedName("title") val title: String,
    @SerializedName("courseId") val courseId: String,
    @SerializedName("courseName") val courseName: String,
    @SerializedName("dueDate") val dueDate: String,
    @SerializedName("dueTime") val dueTime: String,
    @SerializedName("description") val description: String,
    @SerializedName("url") val url: String
) {
    fun toAssignment(): Assignment {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPAN)
        val date = try {
            dateFormat.parse("$dueDate $dueTime") ?: Date()
        } catch (_: Exception) {
            Date()
        }

        return Assignment(
            id = UUID.randomUUID().toString(),
            title = title,
            courseId = courseId,
            courseName = courseName,
            dueDate = date,
            description = description,
            status = AssignmentStatus.PENDING,
            url = url
        )
    }
}
