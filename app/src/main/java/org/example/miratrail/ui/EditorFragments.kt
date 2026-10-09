package org.example.miratrail.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.example.miratrail.R
import org.example.miratrail.TrailApplication

abstract class EditorFragment : Fragment(R.layout.fragment_editor) {
    protected abstract val routeId: Int
    private val model: EditorViewModel by viewModels {
        EditorViewModelFactory((requireActivity().application as TrailApplication).repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (routeId != 0 && savedInstanceState == null) model.load(routeId)
        val input = model.state.value
        view.findViewById<TextView>(R.id.form_title)
            .setText(if (routeId == 0) R.string.create else R.string.edit)
        view.findViewById<ImageView>(R.id.editor_illustration).apply {
            setImageResource(if (routeId == 0) R.drawable.sunny_create else R.drawable.sunny_edit)
            layoutParams = layoutParams.apply {
                height = ((if (routeId == 0) 120 else 100) *
                    resources.displayMetrics.density).toInt()
            }
        }
        val title = view.findViewById<TextInputEditText>(R.id.title)
        val area = view.findViewById<TextInputEditText>(R.id.area)
        val description = view.findViewById<TextInputEditText>(R.id.description)
        val note = view.findViewById<TextInputEditText>(R.id.note)
        title.setText(input.title)
        area.setText(input.area)
        description.setText(input.description)
        note.setText(input.note)
        view.findViewById<View>(R.id.save).setOnClickListener {
            model.change(
                title.text?.toString().orEmpty(), area.text?.toString().orEmpty(),
                description.text?.toString().orEmpty(), note.text?.toString().orEmpty()
            )
            val saved = model.save(routeId)
            val errors = model.state.value
            view.findViewById<TextInputLayout>(R.id.title_layout).error = errors.titleError
            view.findViewById<TextInputLayout>(R.id.area_layout).error = errors.areaError
            if (saved != null) {
                findNavController().navigate(R.id.detailFragment, bundleOf("walkId" to saved.id))
            }
        }
    }
}

class CreateFragment : EditorFragment() {
    override val routeId: Int = 0
}

class EditFragment : EditorFragment() {
    override val routeId: Int get() = requireArguments().getInt("walkId")
}
