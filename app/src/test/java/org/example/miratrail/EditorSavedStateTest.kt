package org.example.miratrail

import androidx.lifecycle.SavedStateHandle
import org.example.miratrail.data.InMemoryWalkRepository
import org.example.miratrail.ui.EditorViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class EditorSavedStateTest {
    @Test
    fun restoredDraftIncludingEmptyFieldsAndErrorsWinsOverRepository() {
        val repository = InMemoryWalkRepository()
        val saved = SavedStateHandle()
        val editor = EditorViewModel(repository, saved, walkId = 1)
        editor.change("", "", "Neue Beschreibung", "Neue Notiz")
        assertNull(editor.save(1))
        val restored = SavedStateHandle(saved.keys().associateWith { saved.get<Any?>(it) })
        val recreated = EditorViewModel(repository, restored, walkId = 1)
        assertEquals(editor.state.value, recreated.state.value)
        assertNotNull(recreated.state.value.titleError)
        assertNotNull(recreated.state.value.areaError)
        assertEquals("Am kleinen Fluss", repository.find(1)!!.title)
    }
}
