package com.meikenn.tama.ui.feature.bus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meikenn.tama.data.repository.BusScheduleRepository
import com.meikenn.tama.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class BusScheduleUiState(
    val schedule: BusSchedule? = null,
    val selectedRoute: RouteType = RouteType.FROM_SEISEKI_TO_SCHOOL,
    val selectedScheduleType: ScheduleType = ScheduleType.WEEKDAY,
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
    val currentMinute: Int = Calendar.getInstance().get(Calendar.MINUTE)
)

@HiltViewModel
class BusScheduleViewModel @Inject constructor(
    private val repository: BusScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BusScheduleUiState())
    val uiState: StateFlow<BusScheduleUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        detectScheduleType()
        fetchBusSchedule()
        startTimerUpdate()
    }

    fun fetchBusSchedule() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.schedule == null, error = null) }

            repository.fetchBusSchedule().collect { result ->
                result.fold(
                    onSuccess = { schedule ->
                        _uiState.update {
                            it.copy(
                                schedule = schedule,
                                isLoading = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { _ ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = if (it.schedule == null) {
                                    "時刻表の読み込みに失敗しました。\nネットワーク接続を確認してください。"
                                } else null
                            )
                        }
                    }
                )
            }
        }
    }

    fun selectRoute(route: RouteType) {
        _uiState.update { it.copy(selectedRoute = route) }
    }

    fun selectScheduleType(type: ScheduleType) {
        _uiState.update { it.copy(selectedScheduleType = type) }
    }

    fun getFilteredSchedule(): DaySchedule? {
        val state = _uiState.value
        val schedule = state.schedule ?: return null

        val schedules = when (state.selectedScheduleType) {
            ScheduleType.WEEKDAY -> schedule.weekdaySchedules
            ScheduleType.SATURDAY -> schedule.saturdaySchedules
            ScheduleType.WEDNESDAY -> schedule.wednesdaySchedules
        }

        return schedules.firstOrNull { it.routeType == state.selectedRoute }
            ?: schedules.firstOrNull()
    }

    fun getNextBus(): TimeEntry? {
        val state = _uiState.value
        val daySchedule = getFilteredSchedule() ?: return null
        val currentHour = state.currentHour
        val currentMinute = state.currentMinute

        // Look in the current hour first
        daySchedule.hourSchedules
            .firstOrNull { it.hour == currentHour }
            ?.times
            ?.firstOrNull { it.minute > currentMinute }
            ?.let { return it }

        // Look in subsequent hours
        for (hour in (currentHour + 1)..23) {
            daySchedule.hourSchedules
                .firstOrNull { it.hour == hour }
                ?.times
                ?.firstOrNull()
                ?.let { return it }
        }

        return null
    }

    fun isNextBusHour(hour: Int): Boolean {
        val nextBus = getNextBus() ?: return false
        return nextBus.hour == hour
    }

    fun isNextBus(time: TimeEntry): Boolean {
        val nextBus = getNextBus() ?: return false
        return time.hour == nextBus.hour && time.minute == nextBus.minute
    }

    private fun detectScheduleType() {
        val weekday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val type = when (weekday) {
            Calendar.WEDNESDAY -> ScheduleType.WEDNESDAY
            Calendar.SATURDAY -> ScheduleType.SATURDAY
            else -> ScheduleType.WEEKDAY
        }
        _uiState.update { it.copy(selectedScheduleType = type) }
    }

    private fun startTimerUpdate() {
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(60_000L) // Update every minute
                val calendar = Calendar.getInstance()
                _uiState.update {
                    it.copy(
                        currentHour = calendar.get(Calendar.HOUR_OF_DAY),
                        currentMinute = calendar.get(Calendar.MINUTE)
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
