package com.meikenn.tama.ui.feature.coursedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meikenn.tama.data.local.dao.CourseColorDao
import com.meikenn.tama.data.local.entity.CourseColorEntity
import com.meikenn.tama.data.repository.CourseDetailRepository
import com.meikenn.tama.domain.model.CourseDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CourseDetailUiState(
    val courseName: String = "",
    val courseDetail: CourseDetail? = null,
    val isLoading: Boolean = false,
    val isSavingMemo: Boolean = false,
    val error: String? = null,
    val memoText: String = "",
    val memoSaved: Boolean = false,
    val memoHasChanges: Boolean = false,
    val colorIndex: Int = 0,
    val periodInfo: String = "",
    val teacherName: String = "",
    val roomName: String = ""
)

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    private val courseDetailRepository: CourseDetailRepository,
    private val courseColorDao: CourseColorDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseDetailUiState())
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    private var currentJugyoCd: String = ""
    private var currentNendo: Int = 0
    private var originalMemo: String = ""

    fun loadCourseDetail(
        courseName: String,
        jugyoCd: String,
        nendo: Int,
        kaikoNendo: Int,
        gakkiNo: Int,
        jugyoKbn: String,
        kaikoYobi: Int,
        jigenNo: Int,
        teacherName: String = "",
        roomName: String = ""
    ) {
        currentJugyoCd = jugyoCd
        currentNendo = nendo

        // Build period info from navigation params
        val weekdays = listOf("月", "火", "水", "木", "金", "土", "日")
        val periodInfo = if (kaikoYobi in 1..7 && jigenNo > 0) {
            "${weekdays[kaikoYobi - 1]}曜日 ${jigenNo}限"
        } else ""

        _uiState.value = _uiState.value.copy(
            courseName = courseName,
            isLoading = true,
            error = null,
            periodInfo = periodInfo,
            teacherName = teacherName,
            roomName = roomName
        )

        viewModelScope.launch {
            // Load color preference
            val colorEntity = courseColorDao.getColor(jugyoCd)
            _uiState.value = _uiState.value.copy(colorIndex = colorEntity?.colorIndex ?: 0)

            val result = courseDetailRepository.getCourseDetail(
                jugyoCd, nendo, kaikoNendo, gakkiNo, jugyoKbn, kaikoYobi, jigenNo
            )
            result.fold(
                onSuccess = { detail ->
                    originalMemo = detail.memo
                    _uiState.value = _uiState.value.copy(
                        courseDetail = detail,
                        memoText = detail.memo,
                        isLoading = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            )
        }
    }

    fun onMemoChanged(text: String) {
        _uiState.value = _uiState.value.copy(
            memoText = text,
            memoSaved = false,
            memoHasChanges = text != originalMemo
        )
    }

    fun saveMemo() {
        val memo = _uiState.value.memoText
        _uiState.value = _uiState.value.copy(isSavingMemo = true)

        viewModelScope.launch {
            val result = courseDetailRepository.saveMemo(currentJugyoCd, currentNendo, memo)
            result.fold(
                onSuccess = {
                    originalMemo = memo
                    _uiState.value = _uiState.value.copy(
                        isSavingMemo = false,
                        memoSaved = true,
                        memoHasChanges = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isSavingMemo = false,
                        error = e.message
                    )
                }
            )
        }
    }

    fun onColorSelected(index: Int) {
        _uiState.value = _uiState.value.copy(colorIndex = index)
        viewModelScope.launch {
            courseColorDao.insertColor(CourseColorEntity(jugyoCd = currentJugyoCd, colorIndex = index))
        }
    }
}
