package org.example.miratrail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.miratrail.data.Walk
import org.example.miratrail.data.WalkRepository
import org.example.miratrail.domain.WalkRules

enum class CatalogPhase { LOADING, CONTENT, EMPTY, ERROR }

data class CatalogState(
    val walks: List<Walk> = emptyList(),
    val query: String = "",
    val favoritesOnly: Boolean = false,
    val selectedId: Int? = null,
    val phase: CatalogPhase = CatalogPhase.LOADING
)

class WalkViewModel(private val repository: WalkRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(CatalogState())
    val state: StateFlow<CatalogState> = mutableState
    var simulateLoadError: Boolean = false
        private set

    init {
        viewModelScope.launch {
            repository.walks.collect { updateList() }
        }
    }

    fun find(id: Int): Walk? = repository.find(id)

    fun load() {
        mutableState.update { it.copy(phase = CatalogPhase.LOADING) }
        viewModelScope.launch {
            delay(450)
            updateList()
        }
    }

    fun setQuery(query: String) {
        mutableState.update { it.copy(query = query) }
        updateList()
    }

    fun setFavoritesOnly(enabled: Boolean) {
        mutableState.update { it.copy(favoritesOnly = enabled) }
        updateList()
    }

    fun select(id: Int) {
        mutableState.update { it.copy(selectedId = id) }
    }

    fun setSimulateLoadError(enabled: Boolean) {
        simulateLoadError = enabled
        updateList()
    }

    fun toggleFavorite(id: Int) = update(id) { it.copy(favorite = !it.favorite) }
    fun togglePlanned(id: Int) = update(id) { it.copy(planned = !it.planned) }
    fun completeNextStage(id: Int) = update(id) {
        it.copy(completedStages = (it.completedStages + 1).coerceAtMost(it.stages.size))
    }

    fun reset() {
        repository.reset(); simulateLoadError = false; updateList()
    }

    private fun update(id: Int, transform: (Walk) -> Walk) {
        repository.find(id)?.let { repository.save(transform(it)) }
    }

    private fun updateList() {
        val previous = mutableState.value
        val filtered =
            WalkRules.filter(repository.walks.value, previous.query, previous.favoritesOnly)
        val phase = when {
            simulateLoadError -> CatalogPhase.ERROR
            filtered.isEmpty() -> CatalogPhase.EMPTY
            else -> CatalogPhase.CONTENT
        }
        mutableState.value = previous.copy(walks = filtered, phase = phase)
    }
}

class WalkViewModelFactory(private val repository: WalkRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = WalkViewModel(repository) as T
}
