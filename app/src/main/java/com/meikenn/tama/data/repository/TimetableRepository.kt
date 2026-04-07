package com.meikenn.tama.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.meikenn.tama.data.local.PreferencesManager
import com.meikenn.tama.data.local.SecureStorage
import com.meikenn.tama.data.local.dao.CourseColorDao
import com.meikenn.tama.data.local.dao.RoomChangeDao
import com.meikenn.tama.data.local.dao.TimetableDao
import com.meikenn.tama.data.local.entity.CachedTimetableEntity
import com.meikenn.tama.data.local.entity.CourseColorEntity
import com.meikenn.tama.data.model.ApiRequestBody
import com.meikenn.tama.data.remote.ApiService
import com.meikenn.tama.domain.model.Course
import com.meikenn.tama.domain.model.Semester
import com.meikenn.tama.util.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimetableRepository @Inject constructor(
    private val apiService: ApiService,
    private val timetableDao: TimetableDao,
    private val courseColorDao: CourseColorDao,
    private val roomChangeDao: RoomChangeDao,
    private val secureStorage: SecureStorage,
    private val preferencesManager: PreferencesManager,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "TimetableRepo"
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 2000L
    }

    /**
     * Fetch timetable data with cache-first strategy.
     * Emits cached data first (if available), then fetches from API and emits again.
     */
    fun fetchTimetable(year: Int, termNo: Int): Flow<Result<Map<String, Map<String, Course>>>> = flow {
        // Try to emit cached data first
        val cached = loadFromCache()
        if (cached != null) {
            Log.d(TAG, "Emitting cached timetable data")
            emit(Result.success(cached))
        }

        // Fetch from API with retries
        val apiResult = fetchFromApiWithRetry(year, termNo)
        apiResult.fold(
            onSuccess = { data ->
                // Save to cache
                saveToCache(data)
                emit(Result.success(data))
            },
            onFailure = { error ->
                Log.e(TAG, "API fetch failed: ${error.message}")
                // Only emit error if we had no cached data
                if (cached == null) {
                    emit(Result.failure(error))
                }
            }
        )
    }

    suspend fun saveCourseColor(jugyoCd: String, colorIndex: Int) {
        courseColorDao.insertColor(CourseColorEntity(jugyoCd, colorIndex))
    }

    suspend fun cleanupExpiredRoomChanges() {
        roomChangeDao.clearExpired()
    }

    // --- Private helpers ---

    private suspend fun loadFromCache(): Map<String, Map<String, Course>>? {
        return try {
            val entity = timetableDao.getCachedTimetable() ?: return null
            val now = System.currentTimeMillis()
            if (now - entity.lastFetchTime > Constants.CACHE_TTL_MS) {
                Log.d(TAG, "Cache expired")
                return null
            }
            deserializeTimetable(entity.jsonData)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load cache: ${e.message}")
            null
        }
    }

    private suspend fun saveToCache(data: Map<String, Map<String, Course>>) {
        try {
            val json = gson.toJson(data)
            timetableDao.insertTimetable(
                CachedTimetableEntity(
                    jsonData = json,
                    lastFetchTime = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save cache: ${e.message}")
        }
    }

    private fun deserializeTimetable(json: String): Map<String, Map<String, Course>>? {
        return try {
            val type = object : TypeToken<Map<String, Map<String, Course>>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deserialize timetable: ${e.message}")
            null
        }
    }

    private suspend fun fetchFromApiWithRetry(year: Int, termNo: Int): Result<Map<String, Map<String, Course>>> {
        var lastError: Exception? = null

        repeat(MAX_RETRIES) { attempt ->
            try {
                val result = fetchFromApi(year, termNo)
                if (result.isSuccess) return result
                lastError = result.exceptionOrNull() as? Exception
            } catch (e: Exception) {
                lastError = e
            }

            if (attempt < MAX_RETRIES - 1) {
                Log.d(TAG, "Retry ${attempt + 1}/$MAX_RETRIES")
                delay(RETRY_DELAY_MS)
            }
        }

        return Result.failure(lastError ?: Exception("時間割の取得に失敗しました"))
    }

    private suspend fun fetchFromApi(year: Int, termNo: Int): Result<Map<String, Map<String, Course>>> {
        val user = secureStorage.getUser()
            ?: return Result.failure(Exception("ユーザー認証情報がありません"))

        val requestData = JsonObject().apply {
            addProperty("kaikoNendo", year)
            addProperty("gakkiNo", termNo)
        }

        val body = ApiRequestBody(
            loginUserId = user.username,
            encryptedLoginPassword = user.encryptedPassword ?: "",
            data = requestData
        )

        val response = apiService.getTimetable(body)

        if (response.statusDto?.success != true) {
            val errorMsg = response.statusDto?.errorList?.firstOrNull()?.errorMsg
                ?: response.statusDto?.messageList?.firstOrNull()
                ?: "時間割の取得に失敗しました"
            return Result.failure(Exception(errorMsg))
        }

        val data = response.dataObject
            ?: return Result.failure(Exception("レスポンスデータがありません"))

        // Extract and save semester info
        val semesterYear = data.get("nendo")?.asInt ?: 0
        val semesterTermNo = data.get("gakkiNo")?.asInt ?: 0
        val semesterName = data.get("gakkiName")?.asString ?: ""
        if (semesterYear > 0) {
            preferencesManager.saveSemester(semesterYear, semesterTermNo, semesterName)
        }

        // Parse course list
        val courseListJson = data.getAsJsonArray("jgkmDtoList")
            ?: return Result.failure(Exception("授業データが見つかりません"))

        // Load color preferences and room changes
        val colorMap = courseColorDao.getAll().associate { it.jugyoCd to it.colorIndex }
        val roomChanges = roomChangeDao.getValidChanges()
            .associate { it.courseName to it.newRoom }

        val timetableData = mutableMapOf<String, MutableMap<String, Course>>()

        for (i in 0 until courseListJson.size()) {
            val courseJson = courseListJson[i].asJsonObject
            val course = parseCourse(courseJson, colorMap, roomChanges) ?: continue

            val dayKey = course.weekday?.toString() ?: continue
            val periodKey = course.period?.toString() ?: continue

            timetableData.getOrPut(dayKey) { mutableMapOf() }[periodKey] = course
        }

        return Result.success(timetableData)
    }

    private fun parseCourse(
        json: JsonObject,
        colorMap: Map<String, Int>,
        roomChanges: Map<String, String>
    ): Course? {
        return try {
            val name = json.get("jugyoName")?.asString ?: return null
            val teacher = json.get("kyoinName")?.asString ?: return null
            val weekday = json.get("kaikoYobi")?.asInt ?: return null
            val period = json.get("jigenNo")?.asInt ?: return null
            val academicYear = json.get("nendo")?.asInt ?: return null
            val courseYear = json.get("kaikoNendo")?.asInt ?: return null
            val courseTerm = json.get("gakkiNo")?.asInt ?: return null
            val keijiMidokCnt = json.get("keijiMidokCnt")?.asInt ?: 0

            // Room name: may be null
            val rawRoom = json.get("kyostName")?.let {
                if (it.isJsonNull) "" else it.asString
            } ?: ""

            // jugyoCd can be Int or String
            val jugyoCd = json.get("jugyoCd")?.let { elem ->
                when {
                    elem.isJsonPrimitive && elem.asJsonPrimitive.isString -> elem.asString
                    elem.isJsonPrimitive && elem.asJsonPrimitive.isNumber -> elem.asInt.toString()
                    else -> null
                }
            } ?: return null

            // jugyoKbn can be Int or String
            val jugyoKbn = json.get("jugyoKbn")?.let { elem ->
                when {
                    elem.isJsonNull -> ""
                    elem.isJsonPrimitive && elem.asJsonPrimitive.isString -> elem.asString
                    elem.isJsonPrimitive && elem.asJsonPrimitive.isNumber -> elem.asInt.toString()
                    else -> ""
                }
            } ?: ""

            // Start/end time: HHMM integer -> "HH:MM" string
            val startTime = parseTime(json.get("jugyoStartTime"))
            val endTime = parseTime(json.get("jugyoEndTime"))

            // Color index from saved preferences, default 1
            val colorIndex = colorMap[jugyoCd] ?: 1

            // Apply room changes, remove "教室" suffix
            var room = rawRoom.replace("教室", "")
            roomChanges[name]?.let { newRoom -> room = newRoom }

            Course(
                name = name,
                room = room,
                teacher = teacher,
                startTime = startTime,
                endTime = endTime,
                colorIndex = colorIndex,
                weekday = weekday,
                period = period,
                jugyoCd = jugyoCd,
                jugyoKbn = jugyoKbn,
                academicYear = academicYear,
                courseYear = courseYear,
                courseTerm = courseTerm,
                keijiMidokCnt = keijiMidokCnt
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse course: ${e.message}")
            null
        }
    }

    private fun parseTime(element: com.google.gson.JsonElement?): String {
        if (element == null || element.isJsonNull) return ""
        return try {
            if (element.isJsonPrimitive) {
                val prim = element.asJsonPrimitive
                if (prim.isString) {
                    prim.asString
                } else if (prim.isNumber) {
                    val t = prim.asInt
                    "%02d:%02d".format(t / 100, t % 100)
                } else ""
            } else ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Get the current semester info from preferences.
     */
    suspend fun getSemester(): Semester {
        return Semester(
            year = preferencesManager.getSemesterYear(),
            termNo = preferencesManager.getSemesterTermNo(),
            termName = preferencesManager.getSemesterTermName()
        )
    }
}
