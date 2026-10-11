package org.example.miratrail

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModelProvider
import org.example.miratrail.ui.*

class MainActivity : ComponentActivity() {
    lateinit var navigation: TrailNavigation
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(
                SunnyColors.Cream.toArgb(), SunnyColors.Cream.toArgb()
            )
        )
        val repository = (application as TrailApplication).repository
        val model =
            ViewModelProvider(this, WalkViewModelFactory(repository))[WalkViewModel::class.java]
        setContent {
            val state = rememberTrailNavigation()
            SideEffect { navigation = state }
            SunnyTheme {
                AppShell(state.currentRoute, onBack = state::goBack) {
                    TrailNavDisplay(state, repository, model)
                }
            }
        }
    }
}
