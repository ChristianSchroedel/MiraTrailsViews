package org.example.miratrail.ui

import androidx.compose.runtime.Composable
import androidx.fragment.app.activityViewModels
import org.example.miratrail.TrailApplication

class SettingsFragment : SunnyComposeFragment() {
    private val model: WalkViewModel by activityViewModels {
        WalkViewModelFactory((requireActivity().application as TrailApplication).repository)
    }

    @Composable
    override fun Screen() {
        SettingsScreen(model.simulateLoadError, model::setSimulateLoadError, model::reset)
    }
}
