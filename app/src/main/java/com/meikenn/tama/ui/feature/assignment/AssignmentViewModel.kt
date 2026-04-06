package com.meikenn.tama.ui.feature.assignment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meikenn.tama.data.repository.AssignmentRepository
import com.meikenn.tama.domain.model.Assignment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

enum class AssignmentFilter(val label: String) {
    ALL("すべて"),
    TODAY("今日"),
    THIS_WEEK("今週"),
    THIS_MONTH("今月"),
    OVERDUE("期限切れ")
}

data class AssignmentUiState(
    val assignments: List<Assignment> = emptyList(),
    val selectedFilter: AssignmentFilter = AssignmentFilter.ALL,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    /** Incremented every minute to force recomposition of remaining-time text. */
    val timeTick: Long = 0L
) {
    val filteredAssignments: List<Assignment>
        get() = when (selectedFilter) {
            AssignmentFilter.ALL -> assignments
            AssignmentFilter.TODAY -> todayAssignments
            AssignmentFilter.THIS_WEEK -> thisWeekAssignments
            AssignmentFilter.THIS_MONTH -> thisMonthAssignments
            AssignmentFilter.OVERDUE -> overdueAssignments
        }

    private val overdueAssignments: List<Assignment>
        get() = assignments
            .filter { it.isOverdue && it.isPending }
            .sortedByDescending { it.dueDate }

    private val todayAssignments: List<Assignment>
        get() {
            val cal = Calendar.getInstance()
            return assignments.filter { assignment ->
                !assignment.isOverdue && assignment.isPending && run {
                    cal.time = assignment.dueDate
                    val dueDay = cal.get(Calendar.DAY_OF_YEAR)
                    val dueYear = cal.get(Calendar.YEAR)
                    cal.time = Date()
                    dueDay == cal.get(Calendar.DAY_OF_YEAR) && dueYear == cal.get(Calendar.YEAR)
                }
            }.sortedBy { it.dueDate }
        }

    private val thisWeekAssignments: List<Assignment>
        get() {
            val cal = Calendar.getInstance()
            val todayStart = cal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            val endOfWeek = Calendar.getInstance().apply {
                time = todayStart
                add(Calendar.DAY_OF_YEAR, 7)
            }.time

            return assignments.filter { assignment ->
                !assignment.isOverdue && assignment.isPending
                    && !isToday(assignment.dueDate)
                    && !assignment.dueDate.after(endOfWeek)
            }.sortedBy { it.dueDate }
        }

    private val thisMonthAssignments: List<Assignment>
        get() {
            val cal = Calendar.getInstance()
            val todayStart = cal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            val endOfWeek = Calendar.getInstance().apply {
                time = todayStart
                add(Calendar.DAY_OF_YEAR, 7)
            }.time
            val endOfMonth = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.time

            return assignments.filter { assignment ->
                !assignment.isOverdue && assignment.isPending
                    && assignment.dueDate.after(endOfWeek)
                    && !assignment.dueDate.after(endOfMonth)
            }.sortedBy { it.dueDate }
        }

    private fun isToday(date: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date }
        val cal2 = Calendar.getInstance()
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
            && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}

@HiltViewModel
class AssignmentViewModel @Inject constructor(
    private val assignmentRepository: AssignmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssignmentUiState())
    val uiState: StateFlow<AssignmentUiState> = _uiState.asStateFlow()

    init {
        loadAssignments()
        startTimer()
    }

    fun loadAssignments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            assignmentRepository.fetchAssignments()
                .onSuccess { assignments ->
                    _uiState.update {
                        it.copy(
                            assignments = assignments,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = e.message
                        )
                    }
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            assignmentRepository.fetchAssignments()
                .onSuccess { assignments ->
                    _uiState.update {
                        it.copy(
                            assignments = assignments,
                            isRefreshing = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            error = e.message
                        )
                    }
                }
        }
    }

    fun selectFilter(filter: AssignmentFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    /** Tick every 60s so remaining-time labels refresh. */
    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                delay(60_000L)
                _uiState.update { it.copy(timeTick = it.timeTick + 1) }
            }
        }
    }
}
