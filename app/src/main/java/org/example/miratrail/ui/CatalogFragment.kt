package org.example.miratrail.ui

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import org.example.miratrail.R
import org.example.miratrail.TrailApplication

class CatalogFragment : Fragment(R.layout.fragment_catalog) {
    private val model: WalkViewModel by activityViewModels {
        WalkViewModelFactory((requireActivity().application as TrailApplication).repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val search = view.findViewById<TextInputEditText>(R.id.search)
        val favorites = view.findViewById<MaterialCheckBox>(R.id.favorites_filter)
        val list = view.findViewById<RecyclerView>(R.id.walks)
        val message = view.findViewById<TextView>(R.id.message)
        val loading = view.findViewById<ProgressBar>(R.id.loading)
        val summary = view.findViewById<TextView>(R.id.list_summary)
        val selection = view.findViewById<TextView>(R.id.selection)
        val adapter = WalkAdapter(
            onOpen = {
                findNavController().navigate(
                    R.id.detailFragment,
                    bundleOf("walkId" to it.id)
                )
            },
            onSelect = { model.select(it.id) }
        )
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = adapter
        search.setText(model.state.value.query)
        favorites.isChecked = model.state.value.favoritesOnly
        search.doAfterTextChanged { model.setQuery(it?.toString().orEmpty()) }
        favorites.setOnCheckedChangeListener { _, checked -> model.setFavoritesOnly(checked) }
        view.findViewById<View>(R.id.refresh).setOnClickListener { model.load() }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.state.collect { state ->
                    summary.text = "${state.walks.size} passende Wege"
                    loading.isVisible = state.phase == CatalogPhase.LOADING
                    list.isVisible = state.phase == CatalogPhase.CONTENT
                    message.isVisible =
                        state.phase == CatalogPhase.EMPTY || state.phase == CatalogPhase.ERROR
                    message.setText(if (state.phase == CatalogPhase.ERROR) R.string.load_error else R.string.empty_list)
                    adapter.submitList(state.walks)
                    val selected = state.walks.firstOrNull { it.id == state.selectedId }
                    selection.isVisible = selected != null && state.phase == CatalogPhase.CONTENT
                    selection.text =
                        selected?.let { "Ausgewählt: ${it.title}. Antippen öffnet Details." }
                            .orEmpty()
                }
            }
        }
        model.load()
    }
}
