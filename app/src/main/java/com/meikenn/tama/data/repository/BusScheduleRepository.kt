package com.meikenn.tama.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.meikenn.tama.data.local.dao.BusScheduleDao
import com.meikenn.tama.data.local.entity.CachedBusScheduleEntity
import com.meikenn.tama.data.remote.ExternalApiService
import com.meikenn.tama.domain.model.*
import com.meikenn.tama.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BusScheduleRepository @Inject constructor(
    private val externalApiService: ExternalApiService,
    private val busScheduleDao: BusScheduleDao,
    private val gson: Gson
) {

    companion object {
        private const val TAG = "BusScheduleRepository"

        /** The first hour buses start running (used as offset base for array parsing). */
        private const val FIRST_BUS_HOUR = 7

        private val SPECIAL_NOTES = listOf(
            SpecialNote("◎", "印の付いた便は、永山駅経由学校行です。"),
            SpecialNote("*", "印のついた便は、永山駅経由聖蹟桜ヶ丘駅行です。"),
            SpecialNote("C", "中学生乗車限定"),
            SpecialNote("K", "高校生乗車限定"),
            SpecialNote("M", "大学生用マイクロバス（月〜木のみ）")
        )
    }

    fun fetchBusSchedule(): Flow<Result<BusSchedule>> = flow {
        // Emit cached data first if available
        val cached = busScheduleDao.getCachedBusSchedule()
        if (cached != null) {
            try {
                val cachedSchedule = gson.fromJson(cached.jsonData, BusSchedule::class.java)
                Log.d(TAG, "Emitting cached bus schedule (fetched at ${cached.lastFetchTime})")
                emit(Result.success(cachedSchedule))
            } catch (e: Exception) {
                Log.w(TAG, "Failed to deserialize cached data", e)
            }
        }

        // Fetch fresh data from API
        try {
            val response = externalApiService.getBusSchedule()
            val busSchedule = parseApiResponse(response)

            // Persist to Room
            val jsonData = gson.toJson(busSchedule)
            busScheduleDao.insertBusSchedule(
                CachedBusScheduleEntity(
                    jsonData = jsonData,
                    lastFetchTime = System.currentTimeMillis()
                )
            )
            Log.d(TAG, "Fresh bus schedule fetched and cached")

            emit(Result.success(busSchedule))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch bus schedule from API", e)

            // If cache is still valid (within TTL), don't emit error
            val isCacheValid = cached != null &&
                (System.currentTimeMillis() - cached.lastFetchTime) < Constants.CACHE_TTL_MS

            if (!isCacheValid && cached == null) {
                emit(Result.failure(e))
            } else if (!isCacheValid) {
                // Cache exists but expired, and API failed
                emit(Result.failure(e))
            }
            // Otherwise: cache is valid, we already emitted it, so silently ignore the API error
        }
    }

    private fun parseApiResponse(json: JsonObject): BusSchedule {
        // Parse messages
        val messages = json.getAsJsonArray("messages")?.map { element ->
            val obj = element.asJsonObject
            TemporaryMessage(
                title = obj.get("title")?.asString ?: "",
                url = obj.get("url")?.asString
            )
        } ?: emptyList()

        // Parse pin
        val pin = json.getAsJsonObject("pin")?.let { obj ->
            PinMessage(
                title = obj.get("title")?.asString ?: "",
                url = obj.get("url")?.asString
            )
        }

        // Parse data
        val data = json.getAsJsonObject("data")

        val weekdaySchedules = parseScheduleData(data.getAsJsonObject("weekday"), ScheduleType.WEEKDAY)
        val wednesdaySchedules = parseScheduleData(data.getAsJsonObject("wednesday"), ScheduleType.WEDNESDAY)
        val saturdaySchedules = parseScheduleData(data.getAsJsonObject("saturday"), ScheduleType.SATURDAY)

        return BusSchedule(
            weekdaySchedules = weekdaySchedules,
            saturdaySchedules = saturdaySchedules,
            wednesdaySchedules = wednesdaySchedules,
            specialNotes = SPECIAL_NOTES,
            temporaryMessages = messages,
            pin = pin
        )
    }

    private fun parseScheduleData(data: JsonObject?, scheduleType: ScheduleType): List<DaySchedule> {
        if (data == null) return emptyList()

        return RouteType.entries.mapNotNull { routeType ->
            val routeArray = data.getAsJsonArray(routeType.jsonKey) ?: return@mapNotNull null
            val hourSchedules = parseRouteArray(routeArray)
            if (hourSchedules.isEmpty()) return@mapNotNull null
            DaySchedule(
                routeType = routeType,
                scheduleType = scheduleType,
                hourSchedules = hourSchedules
            )
        }
    }

    /**
     * Parse a route's schedule array.
     *
     * The API returns the schedule as an array of arrays. Each element in the outer array
     * can be either:
     * - A JSON object with "hour" and "times" keys (structured format), or
     * - A JSON array of minute values (positional format, where index maps to hour offset).
     *
     * Minute values can be integers or strings with a special prefix (e.g., "◎30").
     */
    private fun parseRouteArray(routeArray: JsonArray): List<HourSchedule> {
        if (routeArray.size() == 0) return emptyList()

        // Detect format: if the first element is an object with "hour" key, it's structured
        val firstElement = routeArray[0]
        return if (firstElement.isJsonObject && firstElement.asJsonObject.has("hour")) {
            parseStructuredFormat(routeArray)
        } else {
            parsePositionalFormat(routeArray)
        }
    }

    /** Parse structured format: [{"hour": 8, "times": [{"hour":8,"minute":0,...}]}, ...] */
    private fun parseStructuredFormat(routeArray: JsonArray): List<HourSchedule> {
        return routeArray.mapNotNull { element ->
            val obj = element.asJsonObject
            val hour = obj.get("hour").asInt
            val timesArray = obj.getAsJsonArray("times") ?: return@mapNotNull null
            val times = timesArray.map { timeEl ->
                val timeObj = timeEl.asJsonObject
                TimeEntry(
                    hour = timeObj.get("hour").asInt,
                    minute = timeObj.get("minute").asInt,
                    isSpecial = timeObj.get("isSpecial")?.asBoolean ?: false,
                    specialNote = timeObj.get("specialNote")?.takeIf { !it.isJsonNull }?.asString
                )
            }
            HourSchedule(hour = hour, times = times)
        }
    }

    /** Parse positional format: [[min1, min2, ...], [min3, "◎min4", ...], ...] */
    private fun parsePositionalFormat(routeArray: JsonArray): List<HourSchedule> {
        val hourSchedules = mutableListOf<HourSchedule>()

        routeArray.forEachIndexed { index, element ->
            val hour = FIRST_BUS_HOUR + index
            val minutesArray = element.asJsonArray
            val times = parseMinuteEntries(hour, minutesArray)
            if (times.isNotEmpty()) {
                hourSchedules.add(HourSchedule(hour = hour, times = times))
            }
        }

        return hourSchedules
    }

    private fun parseMinuteEntries(hour: Int, minutesArray: JsonArray): List<TimeEntry> {
        return minutesArray.mapNotNull { entry ->
            parseMinuteEntry(hour, entry)
        }
    }

    private fun parseMinuteEntry(hour: Int, entry: JsonElement): TimeEntry? {
        return when {
            entry.isJsonPrimitive && entry.asJsonPrimitive.isNumber -> {
                TimeEntry(hour = hour, minute = entry.asInt)
            }
            entry.isJsonPrimitive && entry.asJsonPrimitive.isString -> {
                val raw = entry.asString
                parseSpecialMinuteString(hour, raw)
            }
            else -> null
        }
    }

    /**
     * Parse a special minute string like "◎30", "*15", "C20", "K10", "M45".
     * The prefix is the special note symbol, and the rest is the minute value.
     */
    private fun parseSpecialMinuteString(hour: Int, raw: String): TimeEntry? {
        if (raw.isBlank()) return null

        // Try to find where the numeric part starts
        val numericStartIndex = raw.indexOfFirst { it.isDigit() }
        if (numericStartIndex < 0) return null

        val symbol = raw.substring(0, numericStartIndex)
        val minute = raw.substring(numericStartIndex).toIntOrNull() ?: return null

        return if (symbol.isNotEmpty()) {
            TimeEntry(hour = hour, minute = minute, isSpecial = true, specialNote = symbol)
        } else {
            TimeEntry(hour = hour, minute = minute)
        }
    }
}
