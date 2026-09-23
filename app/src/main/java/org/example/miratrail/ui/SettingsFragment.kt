package org.example.miratrail.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.example.miratrail.R
import org.example.miratrail.TrailApplication

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private val model: WalkViewModel by activityViewModels {
        WalkViewModelFactory((requireActivity().application as TrailApplication).repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val error = view.findViewById<MaterialCheckBox>(R.id.simulate_error)
        error.isChecked = model.simulateLoadError
        error.setOnCheckedChangeListener { _, checked -> model.setSimulateLoadError(checked) }
        view.findViewById<View>(R.id.reset).setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.reset)
                .setMessage(R.string.reset_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.reset) { _, _ ->
                    model.reset()
                    error.isChecked = false
                }
                .show()
        }
    }
}
