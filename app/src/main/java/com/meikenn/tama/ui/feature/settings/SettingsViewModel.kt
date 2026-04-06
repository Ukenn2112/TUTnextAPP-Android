package com.meikenn.tama.ui.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meikenn.tama.data.local.PreferencesManager
import com.meikenn.tama.data.repository.AuthRepository
import com.meikenn.tama.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val user: User? = null,
    val isLoggingOut: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val darkMode: StateFlow<Int> = preferencesManager.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreferencesManager.DARK_MODE_SYSTEM)

    init {
        loadUserData()
    }

    private fun loadUserData() {
        _uiState.value = _uiState.value.copy(user = authRepository.getCurrentUser())
    }

    fun getInitials(): String {
        val fullName = _uiState.value.user?.fullName ?: return "?"
        val parts = fullName.split("\u3000") // full-width space
        return if (parts.isNotEmpty()) {
            parts.first().take(2)
        } else {
            fullName.take(2).ifEmpty { "?" }
        }
    }

    fun getDarkModeText(mode: Int): String = when (mode) {
        PreferencesManager.DARK_MODE_LIGHT -> "ライト"
        PreferencesManager.DARK_MODE_DARK -> "ダーク"
        else -> "システムに従う"
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoggingOut = true)
            authRepository.logout()
            _uiState.value = _uiState.value.copy(isLoggingOut = false)
            onComplete()
        }
    }
}
