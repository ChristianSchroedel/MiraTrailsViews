package org.example.miratrail.ui

import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import org.example.miratrail.R
import org.example.miratrail.TrailApplication

class DetailFragment : SunnyComposeFragment() {
    private val model: WalkViewModel by activityViewModels {
        WalkViewModelFactory((requireActivity().application as TrailApplication).repository)
    }
    private val walkId get() = requireArguments().getInt("walkId")

    @Composable
    override fun Screen() {
        val repository = (requireActivity().application as TrailApplication).repository
        val walk = repository.walks.screenValue(viewLifecycleOwner).firstOrNull { it.id == walkId }
        if (walk != null) DetailScreen(
            walk,
            onNext = { model.completeNextStage(walkId) },
            onFavorite = { model.toggleFavorite(walkId) },
            onPlan = { model.togglePlanned(walkId) },
            onEdit = {
                findNavController().navigate(
                    R.id.editFragment,
                    bundleOf("walkId" to walkId)
                )
            })
    }
}
