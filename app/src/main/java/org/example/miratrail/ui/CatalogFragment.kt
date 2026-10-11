package org.example.miratrail.ui

import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import android.os.Bundle
import android.view.View
import org.example.miratrail.R
import org.example.miratrail.TrailApplication

class CatalogFragment : SunnyComposeFragment() {
    private val model: WalkViewModel by activityViewModels {
        WalkViewModelFactory((requireActivity().application as TrailApplication).repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.load()
    }

    @Composable
    override fun Screen() {
        CatalogScreen(
            model.state.screenValue(viewLifecycleOwner),
            model::setQuery,
            model::setFavoritesOnly,
            model::load,
            onOpen = {
                findNavController().navigate(
                    R.id.detailFragment,
                    bundleOf("walkId" to it.id)
                )
            },
            onSelect = { model.select(it.id) })
    }
}
