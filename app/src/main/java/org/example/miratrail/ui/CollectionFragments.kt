package org.example.miratrail.ui

import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import org.example.miratrail.R
import org.example.miratrail.TrailApplication
import org.example.miratrail.data.Walk

abstract class CollectionFragment : SunnyComposeFragment() {
    protected abstract val heading: String
    protected abstract val subtitle: String
    protected abstract val emptyText: String
    protected abstract fun accepts(walk: Walk): Boolean

    @Composable
    override fun Screen() {
        val repository = (requireActivity().application as TrailApplication).repository
        CollectionScreen(
            heading,
            subtitle,
            emptyText,
            repository.walks.screenValue(viewLifecycleOwner).filter(::accepts),
            completed = this is CompletedFragment,
            onOpen = {
                findNavController().navigate(
                    R.id.detailFragment,
                    bundleOf("walkId" to it.id)
                )
            })
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
