package com.shivdhikshith.medsafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.shivdhikshith.medsafe.presentation.navigation.MedSafeNavHost
import com.shivdhikshith.medsafe.presentation.theme.MedSafeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as MedSafeApplication
        val container = app.container

        setContent {
            val userSettings by container.observeSettingsUseCase().collectAsState(
                initial = com.shivdhikshith.medsafe.domain.model.UserSettings()
            )

            val isDarkTheme = when (userSettings.theme) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            MedSafeTheme(darkTheme = isDarkTheme) {
                MedSafeNavHost(container = container)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val app = application as? MedSafeApplication
        app?.triggerExpiryCheck()
    }
}
