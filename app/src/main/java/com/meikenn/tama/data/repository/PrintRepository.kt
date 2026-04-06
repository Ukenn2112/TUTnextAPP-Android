package com.meikenn.tama.data.repository

import com.meikenn.tama.data.local.dao.PrintRecordDao
import com.meikenn.tama.data.local.entity.PrintRecordEntity
import com.meikenn.tama.domain.model.NUpType
import com.meikenn.tama.domain.model.PlexType
import com.meikenn.tama.domain.model.PrintResult
import com.meikenn.tama.domain.model.PrintSettings
import com.meikenn.tama.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrintRepository @Inject constructor(
    private val printRecordDao: PrintRecordDao
) {
    // Separate client for print system (manages its own cookies)
    private val client = OkHttpClient.Builder()
        .cookieJar(okhttp3.JavaNetCookieJar(java.net.CookieManager()))
        .build()

    suspend fun login(): Boolean = withContext(Dispatchers.IO) {
        val formBody = okhttp3.FormBody.Builder()
            .add("id", Constants.PRINT_CREDENTIALS_ID)
            .add("password", Constants.PRINT_CREDENTIALS_PASSWORD)
            .add("lang", "ja")
            .build()

        val request = Request.Builder()
            .url("${Constants.PRINT_BASE_URL}/guestweb/login")
            .post(formBody)
            .build()

        val response = client.newCall(request).execute()
        response.isSuccessful
    }

    suspend fun uploadFile(
        fileData: ByteArray,
        fileName: String,
        contentType: String,
        settings: PrintSettings
    ): PrintResult = withContext(Dispatchers.IO) {
        val fileBody = fileData.toRequestBody(contentType.toMediaType())

        val multipartBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", fileName, fileBody)
            .addFormDataPart("title", fileName)
            .addFormDataPart("isGlobal", "true")
            .addFormDataPart("colorMode", "auto")
            .addFormDataPart("plex", settings.plex.apiValue)
            .addFormDataPart("nUp", settings.nUp.apiValue)
            .addFormDataPart("startPage", settings.startPage.toString())
            .addFormDataPart("autoNetprint", "false")

        if (!settings.pin.isNullOrEmpty()) {
            multipartBuilder.addFormDataPart("pin", settings.pin)
        }

        val requestBody = multipartBuilder.build()

        val request = Request.Builder()
            .url("${Constants.PRINT_BASE_URL}${Constants.PRINT_TENANT_PATH}")
            .post(requestBody)
            .header("X-CSRF-Token", "token")
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string()
            ?: throw IllegalStateException("Empty response from print upload")

        val json = JSONObject(body)
        val id = json.getString("id")

        // Poll for print details (prCode)
        val result = fetchPrintDetails(id)

        // Save to Room
        printRecordDao.insert(
            PrintRecordEntity(
                printNumber = result.printNumber,
                fileName = result.fileName,
                expiryDate = result.expiryDate,
                pageCount = result.pageCount,
                duplex = result.duplex,
                fileSize = result.fileSize,
                nUp = result.nUp
            )
        )

        result
    }

    private suspend fun fetchPrintDetails(id: String): PrintResult {
        val url = "${Constants.PRINT_BASE_URL}${Constants.PRINT_TENANT_PATH}$id"

        repeat(50) { attempt ->
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: throw IllegalStateException("Empty response")

            val json = JSONObject(body)
            val prCode = json.optString("prCode", "")

            if (prCode.isNotEmpty()) {
                val expiresAt = json.getString("expiresAt")
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                // Try ISO 8601 with timezone offset too
                val expiryDate = try {
                    sdf.parse(expiresAt)?.time
                } catch (_: Exception) {
                    val sdf2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
                    sdf2.parse(expiresAt)?.time
                } ?: System.currentTimeMillis()

                val plexValue = json.optString("plex", "simplex")
                val nUpValue = json.optInt("nUp", 1)

                return PrintResult(
                    printNumber = prCode,
                    fileName = json.optString("title", ""),
                    expiryDate = expiryDate,
                    pageCount = json.optInt("pages", 0),
                    duplex = PlexType.entries.find { it.apiValue == plexValue }?.displayName ?: "片面",
                    fileSize = "${json.optInt("size", 0)} KB",
                    nUp = NUpType.entries.find { it.apiValue == nUpValue.toString() }?.displayName ?: "しない"
                )
            }

            if (attempt < 49) {
                delay(1000)
            }
        }

        throw IllegalStateException("Failed to get prCode after 50 retries")
    }

    suspend fun getRecentUploads(): List<PrintResult> {
        printRecordDao.clearExpired()
        return printRecordDao.getValidRecords().map { entity ->
            PrintResult(
                printNumber = entity.printNumber,
                fileName = entity.fileName,
                expiryDate = entity.expiryDate,
                pageCount = entity.pageCount,
                duplex = entity.duplex,
                fileSize = entity.fileSize,
                nUp = entity.nUp
            )
        }
    }

    fun getContentType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "pdf" -> "application/pdf"
            "jpg", "jpeg", "jpe" -> "image/jpeg"
            "png" -> "image/png"
            "tif", "tiff" -> "image/tiff"
            "rtf" -> "application/rtf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "xdw" -> "application/vnd.fujifilm.xdw"
            "xbd" -> "application/vnd.fujifilm.xbd"
            "xps" -> "application/vnd.ms-xpsdocument"
            "oxps" -> "application/oxps"
            else -> "application/octet-stream"
        }
    }
}
