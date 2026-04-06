package com.meikenn.tama.ui.navigation

import androidx.lifecycle.ViewModel
import com.meikenn.tama.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AppNavigationViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(authRepository.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userInitials = MutableStateFlow(computeInitials())
    val userInitials: StateFlow<String> = _userInitials.asStateFlow()

    fun refreshLoginState() {
        _isLoggedIn.value = authRepository.isLoggedIn()
        _userInitials.value = computeInitials()
    }

    private fun computeInitials(): String {
        val user = authRepository.getCurrentUser() ?: return "?"
        val fullName = user.fullName
        // Split by full-width space (Japanese names) and take first 2 chars of first part
        val parts = fullName.split("\u3000") // full-width space
        return if (parts.isNotEmpty()) {
            parts.first().take(2)
        } else {
            fullName.take(2).ifEmpty { "?" }
        }
    }
}
