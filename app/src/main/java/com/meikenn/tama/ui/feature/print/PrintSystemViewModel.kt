package com.meikenn.tama.ui.feature.print

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meikenn.tama.data.repository.PrintRepository
import com.meikenn.tama.domain.model.NUpType
import com.meikenn.tama.domain.model.PlexType
import com.meikenn.tama.domain.model.PrintResult
import com.meikenn.tama.domain.model.PrintSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SelectedFile(
    val uri: Uri,
    val name: String,
    val size: Long,
    val data: ByteArray
)

data class PrintUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val selectedFile: SelectedFile? = null,
    val printSettings: PrintSettings = PrintSettings(),
    val pinCode: String = "",
    val printResult: PrintResult? = null,
    val showResult: Boolean = false,
    val recentUploads: List<PrintResult> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class PrintSystemViewModel @Inject constructor(
    private val printRepository: PrintRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrintUiState())
    val uiState: StateFlow<PrintUiState> = _uiState.asStateFlow()

    init {
        loginAndLoadRecent()
    }

    private fun loginAndLoadRecent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val success = printRepository.login()
                val uploads = printRepository.getRecentUploads()
                _uiState.update {
                    it.copy(isLoading = false, isLoggedIn = success, recentUploads = uploads)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "ログインに失敗しました: ${e.message}")
                }
            }
        }
    }

    fun handleFileSelection(uri: Uri) {
        viewModelScope.launch {
            try {
                val contentResolver = appContext.contentResolver
                val cursor = contentResolver.query(uri, null, null, null, null)
                var fileName = "unknown"
                var fileSize = 0L

                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIdx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIdx = it.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIdx >= 0) fileName = it.getString(nameIdx)
                        if (sizeIdx >= 0) fileSize = it.getLong(sizeIdx)
                    }
                }

                val inputStream = contentResolver.openInputStream(uri)
                val data = inputStream?.readBytes() ?: throw IllegalStateException("Cannot read file")
                inputStream.close()

                _uiState.update {
                    it.copy(
                        selectedFile = SelectedFile(uri, fileName, fileSize, data),
                        errorMessage = null,
                        printResult = null,
                        showResult = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "ファイルの読み込みに失敗しました: ${e.message}")
                }
            }
        }
    }

    fun updatePlex(plex: PlexType) {
        _uiState.update { it.copy(printSettings = it.printSettings.copy(plex = plex)) }
    }

    fun updateNUp(nUp: NUpType) {
        _uiState.update { it.copy(printSettings = it.printSettings.copy(nUp = nUp)) }
    }

    fun updateStartPage(page: Int) {
        if (page in 1..999) {
            _uiState.update { it.copy(printSettings = it.printSettings.copy(startPage = page)) }
        }
    }

    fun updatePinCode(pin: String) {
        val filtered = pin.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(pinCode = filtered) }
    }

    fun uploadFile() {
        val state = _uiState.value
        val file = state.selectedFile ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val settings = state.printSettings.copy(
                    pin = state.pinCode.ifEmpty { null }
                )
                val contentType = printRepository.getContentType(file.name)
                val result = printRepository.uploadFile(file.data, file.name, contentType, settings)
                val uploads = printRepository.getRecentUploads()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        printResult = result,
                        showResult = true,
                        recentUploads = uploads
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "アップロードに失敗しました: ${e.message}")
                }
            }
        }
    }

    fun reset() {
        _uiState.update {
            it.copy(
                selectedFile = null,
                printSettings = PrintSettings(),
                pinCode = "",
                printResult = null,
                showResult = false,
                errorMessage = null
            )
        }
    }

    fun formattedFileSize(bytes: Long): String {
        return when {
            bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576.0)
            bytes >= 1024 -> String.format("%.0f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
