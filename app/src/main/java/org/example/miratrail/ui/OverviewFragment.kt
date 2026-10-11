package org.example.miratrail.ui

import androidx.compose.runtime.Composable
import androidx.navigation.fragment.findNavController
import org.example.miratrail.TrailApplication

class OverviewFragment : SunnyComposeFragment() {
    @Composable
    override fun Screen() {
        val repository = (requireActivity().application as TrailApplication).repository
        val walks = repository.walks.screenValue(viewLifecycleOwner)
        OverviewScreen(
            summary = "${walks.size} Wege · ${walks.count { it.planned }} geplant · ${walks.count { it.isComplete }} abgeschlossen",
            onNavigate = { findNavController().navigate(it) }
        )
    }
}
