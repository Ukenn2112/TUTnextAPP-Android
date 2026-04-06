package com.meikenn.tama.ui.feature.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meikenn.tama.data.repository.TeacherRepository
import com.meikenn.tama.domain.model.Teacher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeacherEmailUiState(
    val teachers: List<Teacher> = emptyList(),
    val teachersBySection: Map<String, List<Teacher>> = emptyMap(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TeacherEmailListViewModel @Inject constructor(
    private val teacherRepository: TeacherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherEmailUiState())
    val uiState: StateFlow<TeacherEmailUiState> = _uiState.asStateFlow()

    /** Japanese 50-on section headings */
    private val japaneseSections = listOf("あ", "か", "さ", "た", "な", "は", "ま", "や", "ら", "わ")

    private val sectionCharMap = mapOf(
        "あ" to "あいうえお",
        "か" to "かきくけこがぎぐげご",
        "さ" to "さしすせそざじずぜぞ",
        "た" to "たちつてとだぢづでど",
        "な" to "なにぬねの",
        "は" to "はひふへほばびぶべぼぱぴぷぺぽ",
        "ま" to "まみむめも",
        "や" to "やゆよ",
        "ら" to "らりるれろ",
        "わ" to "わをん"
    )

    val sectionOrder: List<String>
        get() = japaneseSections + "その他"

    init {
        loadTeachers()
    }

    fun loadTeachers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val teachers = teacherRepository.getTeachers()
                val sections = organizeBySection(teachers)
                _uiState.update {
                    it.copy(
                        teachers = teachers,
                        teachersBySection = sections,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "読み込みに失敗しました")
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isEmpty()) {
                state.teachers
            } else {
                state.teachers.filter { teacher ->
                    teacher.name.contains(query, ignoreCase = true) ||
                        (teacher.furigana?.contains(query, ignoreCase = true) == true) ||
                        teacher.email.contains(query, ignoreCase = true)
                }
            }
            state.copy(
                searchQuery = query,
                teachersBySection = organizeBySection(filtered)
            )
        }
    }

    private fun organizeBySection(teachers: List<Teacher>): Map<String, List<Teacher>> {
        val sections = mutableMapOf<String, MutableList<Teacher>>()
        for (section in japaneseSections) {
            sections[section] = mutableListOf()
        }
        sections["その他"] = mutableListOf()

        for (teacher in teachers) {
            val furigana = teacher.furigana
            if (!furigana.isNullOrEmpty()) {
                val firstChar = furigana.first().toString()
                var assigned = false
                for (section in japaneseSections) {
                    val chars = sectionCharMap[section] ?: ""
                    if (chars.contains(firstChar)) {
                        sections[section]?.add(teacher)
                        assigned = true
                        break
                    }
                }
                if (!assigned) {
                    sections["その他"]?.add(teacher)
                }
            } else {
                sections["その他"]?.add(teacher)
            }
        }

        // Sort each section by furigana
        for ((key, list) in sections) {
            sections[key] = list.sortedBy { it.furigana ?: "" }.toMutableList()
        }

        return sections
    }
}
