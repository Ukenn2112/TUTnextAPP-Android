package com.meikenn.tama.ui.feature.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meikenn.tama.data.local.dao.CourseColorDao
import com.meikenn.tama.data.repository.TimetableRepository
import com.meikenn.tama.domain.model.Course
import com.meikenn.tama.domain.model.Semester
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimetableUiState(
    val courses: Map<String, Map<String, Course>> = emptyMap(),
    val semester: Semester? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    /** Check if a given period exists in any weekday. */
    fun hasPeriod(period: String): Boolean {
        return courses.values.any { it.containsKey(period) }
    }

    /** Get weekdays to display (Mon-Fri, plus Sat if courses exist). */
    fun getWeekdays(): List<String> {
        val base = listOf("1", "2", "3", "4", "5")
        val satCourses = courses["6"]
        return if (!satCourses.isNullOrEmpty()) base + "6" else base
    }

    /** Get periods to display (trim 6th/7th if not needed). */
    fun getPeriods(): List<PeriodInfo> {
        val all = PeriodInfo.all
        val has7th = hasPeriod("7")
        val has6th = hasPeriod("6")
        return when {
            has7th -> all
            has6th -> all.take(6)
            else -> all.take(5)
        }
    }
}

data class PeriodInfo(
    val number: String,
    val startTime: String,
    val endTime: String
) {
    companion object {
        val all = listOf(
            PeriodInfo("1", "9:00", "10:30"),
            PeriodInfo("2", "10:40", "12:10"),
            PeriodInfo("3", "13:00", "14:30"),
            PeriodInfo("4", "14:40", "16:10"),
            PeriodInfo("5", "16:20", "17:50"),
            PeriodInfo("6", "18:00", "19:30"),
            PeriodInfo("7", "19:40", "21:10")
        )
    }
}

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val timetableRepository: TimetableRepository,
    private val courseColorDao: CourseColorDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimetableUiState())
    val uiState: StateFlow<TimetableUiState> = _uiState.asStateFlow()

    init {
        loadTimetable()
        observeColorChanges()
    }

    private fun observeColorChanges() {
        viewModelScope.launch {
            courseColorDao.getAllAsFlow().collect { colors ->
                val colorMap = colors.associate { it.jugyoCd to it.colorIndex }
                val currentCourses = _uiState.value.courses
                if (currentCourses.isEmpty()) return@collect

                val updated = currentCourses.mapValues { (_, dayMap) ->
                    dayMap.mapValues { (_, course) ->
                        val newColor = course.jugyoCd?.let { colorMap[it] }
                        if (newColor != null && newColor != course.colorIndex) {
                            course.copy(colorIndex = newColor)
                        } else course
                    }
                }
                if (updated != currentCourses) {
                    _uiState.value = _uiState.value.copy(courses = updated)
                }
            }
        }
    }

    fun loadTimetable() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val semester = timetableRepository.getSemester()
            _uiState.value = _uiState.value.copy(semester = semester)

            timetableRepository.fetchTimetable(semester.year, semester.termNo)
                .collect { result ->
                    result.fold(
                        onSuccess = { data ->
                            _uiState.value = _uiState.value.copy(
                                courses = data,
                                isLoading = false,
                                isRefreshing = false,
                                error = null
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = error.message
                            )
                        }
                    )
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)

            val semester = timetableRepository.getSemester()
            timetableRepository.fetchTimetable(semester.year, semester.termNo)
                .collect { result ->
                    result.fold(
                        onSuccess = { data ->
                            _uiState.value = _uiState.value.copy(
                                courses = data,
                                isRefreshing = false,
                                error = null
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isRefreshing = false,
                                error = error.message
                            )
                        }
                    )
                }
        }
    }

    fun updateCourseColor(day: String, period: String, colorIndex: Int) {
        val course = _uiState.value.courses[day]?.get(period) ?: return
        val jugyoCd = course.jugyoCd ?: return

        // Update in-memory state
        val updatedCourses = _uiState.value.courses.toMutableMap()
        val dayMap = updatedCourses[day]?.toMutableMap() ?: return
        dayMap[period] = course.copy(colorIndex = colorIndex)
        updatedCourses[day] = dayMap
        _uiState.value = _uiState.value.copy(courses = updatedCourses)

        // Persist to database
        viewModelScope.launch {
            timetableRepository.saveCourseColor(jugyoCd, colorIndex)
        }
    }
}
