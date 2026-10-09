package org.example.miratrail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.example.miratrail.data.Walk
import org.example.miratrail.data.WalkRepository
import org.example.miratrail.domain.WalkRules

data class EditorState(
    val title: String = "",
    val area: String = "",
    val description: String = "",
    val note: String = "",
    val titleError: String? = null,
    val areaError: String? = null
)

class EditorViewModel(private val repository: WalkRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = mutableState

    fun load(id: Int) {
        val walk = repository.find(id) ?: return
        mutableState.value = EditorState(walk.title, walk.area, walk.description, walk.note)
    }

    fun change(title: String, area: String, description: String, note: String) {
        mutableState.update {
            it.copy(
                title = title,
                area = area,
                description = description,
                note = note,
                titleError = null,
                areaError = null
            )
        }
    }

    fun save(id: Int = 0): Walk? {
        val input = mutableState.value
        val titleError = WalkRules.titleError(input.title)
        val areaError = WalkRules.areaError(input.area)
        if (titleError != null || areaError != null) {
            mutableState.update { it.copy(titleError = titleError, areaError = areaError) }
            return null
        }
        val old = repository.find(id)
        return repository.save(
            (old ?: Walk(0, "", "", "", listOf("Start", "Mitte", "Ziel"))).copy(
                title = input.title.trim(), area = input.area.trim(),
                description = input.description.trim(), note = input.note.trim()
            )
        )
    }
}

class EditorViewModelFactory(private val repository: WalkRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = EditorViewModel(repository) as T
}
