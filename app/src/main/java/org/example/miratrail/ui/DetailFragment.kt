package org.example.miratrail.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import org.example.miratrail.R
import org.example.miratrail.TrailApplication
import org.example.miratrail.data.Walk

class DetailFragment : Fragment(R.layout.fragment_detail) {
    private val model: WalkViewModel by activityViewModels {
        WalkViewModelFactory((requireActivity().application as TrailApplication).repository)
    }
    private val walkId get() = requireArguments().getInt("walkId")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.next_stage).setOnClickListener { model.completeNextStage(walkId) }
        view.findViewById<View>(R.id.favorite).setOnClickListener { model.toggleFavorite(walkId) }
        view.findViewById<View>(R.id.plan).setOnClickListener { model.togglePlanned(walkId) }
        view.findViewById<View>(R.id.edit).setOnClickListener {
            findNavController().navigate(R.id.editFragment, bundleOf("walkId" to walkId))
        }
        val repository = (requireActivity().application as TrailApplication).repository
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.walks.collect { list ->
                    list.firstOrNull { it.id == walkId }?.let { render(view, it) }
                }
            }
        }
    }

    private fun render(view: View, walk: Walk) {
        view.findViewById<TextView>(R.id.title).text = walk.title
        view.findViewById<TextView>(R.id.area).text = walk.area
        view.findViewById<TextView>(R.id.description).text = walk.description
        view.findViewById<StageProgressView>(R.id.stage_progress).show(walk.stages, walk.completedStages)
        view.findViewById<TextView>(R.id.stage_caption).text = "${walk.completedStages} von ${walk.stages.size} Etappen abgeschlossen"
        view.findViewById<TextView>(R.id.note).text = if (walk.note.isBlank()) "Noch keine Notiz." else "Notiz: ${walk.note}"
        view.findViewById<View>(R.id.next_stage).isEnabled = !walk.isComplete
        view.findViewById<TextView>(R.id.favorite).text = if (walk.favorite) "Aus Merkliste entfernen" else "Merken"
        view.findViewById<TextView>(R.id.plan).text = if (walk.planned) "Planung aufheben" else "Planen"
    }
}
