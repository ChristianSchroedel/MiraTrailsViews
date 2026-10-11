package org.example.miratrail.ui

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import org.example.miratrail.R
import org.example.miratrail.TrailApplication

abstract class EditorFragment : SunnyComposeFragment() {
    protected abstract val routeId: Int
    private val model: EditorViewModel by viewModels {
        EditorViewModelFactory((requireActivity().application as TrailApplication).repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (routeId != 0 && savedInstanceState == null) model.load(routeId)
        super.onViewCreated(view, savedInstanceState)
    }

    @Composable
    override fun Screen() {
        EditorScreen(
            model.state.screenValue(viewLifecycleOwner), editing = routeId != 0,
            onChange = { input ->
                model.change(
                    input.title,
                    input.area,
                    input.description,
                    input.note
                )
            },
            onSave = {
                model.save(routeId)
                    ?.let {
                        findNavController().navigate(
                            R.id.detailFragment,
                            bundleOf("walkId" to it.id)
                        )
                    }
            })
    }
}

class CreateFragment : EditorFragment() {
    override val routeId: Int = 0
}

class EditFragment : EditorFragment() {
    override val routeId: Int get() = requireArguments().getInt("walkId")
}
