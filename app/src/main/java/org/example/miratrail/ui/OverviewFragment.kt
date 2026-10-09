package org.example.miratrail.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import org.example.miratrail.R
import org.example.miratrail.TrailApplication

class OverviewFragment : Fragment(R.layout.fragment_overview) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val destinations = mapOf(
            R.id.open_catalog to R.id.catalogFragment,
            R.id.open_create to R.id.createFragment,
            R.id.open_favorites to R.id.favoritesFragment,
            R.id.open_planned to R.id.plannedFragment,
            R.id.open_completed to R.id.completedFragment,
            R.id.open_notes to R.id.notesFragment,
            R.id.open_settings to R.id.settingsFragment
        )
        destinations.forEach { (button, destination) ->
            view.findViewById<View>(button)
                .setOnClickListener { findNavController().navigate(destination) }
        }
        val repository = (requireActivity().application as TrailApplication).repository
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.walks.collect { walks ->
                    view.findViewById<TextView>(R.id.summary).text =
                        "${walks.size} Wege · ${walks.count { it.planned }} geplant · ${walks.count { it.isComplete }} abgeschlossen"
                }
            }
        }
    }
}
