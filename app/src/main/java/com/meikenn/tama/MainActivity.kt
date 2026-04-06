package com.meikenn.tama

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.meikenn.tama.ui.navigation.AppNavigation
import com.meikenn.tama.ui.theme.TUTnextAPPTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TUTnextAPPTheme {
                AppNavigation()
            }
        }
    }
}
