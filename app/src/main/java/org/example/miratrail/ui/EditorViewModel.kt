package org.example.miratrail.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
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

class EditorViewModel(
    private val repository: WalkRepository,
    private val savedState: SavedStateHandle = SavedStateHandle(),
    walkId: Int = 0
) : ViewModel() {
    private val existing = repository.find(walkId)
    private val mutableState = MutableStateFlow(
        EditorState(
            title = savedState["title"] ?: existing?.title.orEmpty(),
            area = savedState["area"] ?: existing?.area.orEmpty(),
            description = savedState["description"] ?: existing?.description.orEmpty(),
            note = savedState["note"] ?: existing?.note.orEmpty(),
            titleError = savedState["titleError"],
            areaError = savedState["areaError"]
        )
    )
    val state: StateFlow<EditorState> = mutableState

    fun load(id: Int) {
        val walk = repository.find(id) ?: return
        mutableState.value = EditorState(walk.title, walk.area, walk.description, walk.note)
        rememberInput()
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
        rememberInput()
    }

    fun save(id: Int = 0): Walk? {
        val input = mutableState.value
        val titleError = WalkRules.titleError(input.title)
        val areaError = WalkRules.areaError(input.area)
        if (titleError != null || areaError != null) {
            mutableState.update { it.copy(titleError = titleError, areaError = areaError) }
            rememberInput()
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

    private fun rememberInput() {
        val input = mutableState.value
        savedState["title"] = input.title
        savedState["area"] = input.area
        savedState["description"] = input.description
        savedState["note"] = input.note
        savedState["titleError"] = input.titleError
        savedState["areaError"] = input.areaError
    }
}

class EditorViewModelFactory(
    private val repository: WalkRepository,
    private val walkId: Int = 0
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        EditorViewModel(repository, walkId = walkId) as T

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
        EditorViewModel(repository, extras.createSavedStateHandle(), walkId) as T
}
