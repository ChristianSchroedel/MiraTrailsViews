package org.example.miratrail

import org.example.miratrail.data.InMemoryWalkRepository
import org.example.miratrail.ui.EditorViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class EditorViewModelTest {
    @Test
    fun invalidInputDoesNotChangeRepository() {
        val repository = InMemoryWalkRepository()
        val model = EditorViewModel(repository)
        model.change("ab", "", "Beschreibung", "")
        assertNull(model.save())
        assertNotNull(model.state.value.titleError)
        assertNotNull(model.state.value.areaError)
        assertEquals(5, repository.walks.value.size)
    }

    @Test
    fun editingPreservesProgressAndFlags() {
        val repository = InMemoryWalkRepository()
        val model = EditorViewModel(repository)
        model.load(1)
        model.change("Am breiten Fluss", "Nordufer", "Neuer Text", "Neue Notiz")
        val saved = model.save(1)!!
        assertEquals(1, saved.completedStages)
        assertEquals(true, saved.favorite)
        assertEquals("Neue Notiz", saved.note)
    }
}
