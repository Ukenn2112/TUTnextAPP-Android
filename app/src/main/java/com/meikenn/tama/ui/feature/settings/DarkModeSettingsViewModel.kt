package com.meikenn.tama.ui.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meikenn.tama.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DarkModeSettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val darkMode: StateFlow<Int> = preferencesManager.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreferencesManager.DARK_MODE_SYSTEM)

    fun setDarkMode(mode: Int) {
        viewModelScope.launch {
            preferencesManager.setDarkMode(mode)
        }
    }
}
