package org.example.miratrail.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.example.miratrail.R
import org.example.miratrail.TrailApplication
import org.example.miratrail.data.Walk

abstract class CollectionFragment : Fragment(R.layout.fragment_collection) {
    protected abstract val heading: String
    protected abstract val subtitle: String
    protected abstract val emptyText: String
    protected abstract fun accepts(walk: Walk): Boolean

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<TextView>(R.id.heading).text = heading
        view.findViewById<TextView>(R.id.subheading).text = subtitle
        val empty = view.findViewById<TextView>(R.id.empty)
        empty.text = emptyText
        val adapter = WalkAdapter(onOpen = { findNavController().navigate(R.id.detailFragment, bundleOf("walkId" to it.id)) })
        view.findViewById<RecyclerView>(R.id.walks).apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
        val repository = (requireActivity().application as TrailApplication).repository
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.walks.collect { walks ->
                    val filtered = walks.filter(::accepts)
                    adapter.submitList(filtered)
                    empty.isVisible = filtered.isEmpty()
                }
            }
        }
    }
}

class FavoritesFragment : CollectionFragment() {
    override val heading = "Merkliste"
    override val subtitle = "Wege, die du wiederfinden möchtest."
    override val emptyText = "Noch kein Weg gemerkt. Öffne einen Weg und tippe auf Merken."
    override fun accepts(walk: Walk) = walk.favorite
}

class PlannedFragment : CollectionFragment() {
    override val heading = "Geplant"
    override val subtitle = "Deine nächsten Spaziergänge."
    override val emptyText = "Noch nichts geplant. Plane einen Weg in seiner Detailansicht."
    override fun accepts(walk: Walk) = walk.planned && !walk.isComplete
}

class CompletedFragment : CollectionFragment() {
    override val heading = "Abgeschlossen"
    override val subtitle = "Wege mit vollständig erledigten Etappen."
    override val emptyText = "Noch kein Weg abgeschlossen."
    override fun accepts(walk: Walk) = walk.isComplete
}

class NotesFragment : CollectionFragment() {
    override val heading = "Notizen"
    override val subtitle = "Wege mit persönlichen Beobachtungen."
    override val emptyText = "Noch keine Notiz. Bearbeite einen Weg, um eine zu schreiben."
    override fun accepts(walk: Walk) = walk.note.isNotBlank()
}
