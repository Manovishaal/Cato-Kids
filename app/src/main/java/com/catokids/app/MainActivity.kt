package com.catokids.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.catokids.app.ui.navigation.CatoApp
import com.catokids.app.ui.theme.CatoKidsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as CatoKidsApplication).container

        setContent {
            val profile by container.auth.profile.collectAsState()
            CatoKidsTheme(role = profile?.role) {
                CatoApp(container)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (application as? CatoKidsApplication)?.container?.speech?.shutdown()
    }
}
