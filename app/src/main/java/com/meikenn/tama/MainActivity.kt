package com.meikenn.tama

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.meikenn.tama.data.local.PreferencesManager
import com.meikenn.tama.ui.navigation.AppNavigation
import com.meikenn.tama.ui.theme.TUTnextAPPTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkModeValue by preferencesManager.darkMode.collectAsState(initial = PreferencesManager.DARK_MODE_SYSTEM)
            val isDark = when (darkModeValue) {
                PreferencesManager.DARK_MODE_LIGHT -> false
                PreferencesManager.DARK_MODE_DARK -> true
                else -> isSystemInDarkTheme()
            }

            TUTnextAPPTheme(darkTheme = isDark) {
                AppNavigation()
            }
        }
    }
}
