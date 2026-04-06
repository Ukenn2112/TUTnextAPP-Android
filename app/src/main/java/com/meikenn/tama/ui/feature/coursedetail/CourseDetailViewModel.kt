package com.meikenn.tama.ui.feature.coursedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val memoSaved: Boolean = false
)

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    private val courseDetailRepository: CourseDetailRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseDetailUiState())
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    private var currentJugyoCd: String = ""
    private var currentNendo: Int = 0

    fun loadCourseDetail(
        courseName: String,
        jugyoCd: String,
        nendo: Int,
        kaikoNendo: Int,
        gakkiNo: Int,
        jugyoKbn: String,
        kaikoYobi: Int,
        jigenNo: Int
    ) {
        currentJugyoCd = jugyoCd
        currentNendo = nendo
        _uiState.value = _uiState.value.copy(courseName = courseName, isLoading = true, error = null)

        viewModelScope.launch {
            val result = courseDetailRepository.getCourseDetail(
                jugyoCd, nendo, kaikoNendo, gakkiNo, jugyoKbn, kaikoYobi, jigenNo
            )
            result.fold(
                onSuccess = { detail ->
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
        _uiState.value = _uiState.value.copy(memoText = text, memoSaved = false)
    }

    fun saveMemo() {
        val memo = _uiState.value.memoText
        _uiState.value = _uiState.value.copy(isSavingMemo = true)

        viewModelScope.launch {
            val result = courseDetailRepository.saveMemo(currentJugyoCd, currentNendo, memo)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSavingMemo = false, memoSaved = true)
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
}
