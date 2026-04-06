package com.meikenn.tama.feature.home

import com.meikenn.tama.ui.feature.home.HomeViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeViewModelTest {

    @Test
    fun `initial state has correct default message`() {
        val viewModel = HomeViewModel()
        val state = viewModel.uiState.value
        assertEquals("Welcome to TUTnextAPP", state.message)
        assertEquals(false, state.isLoading)
    }
}
