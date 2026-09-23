package org.example.miratrail

import org.example.miratrail.data.InMemoryWalkRepository
import org.example.miratrail.domain.WalkRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class WalkRulesTest {
    @Test fun nameAndPlaceAreRequired() {
        assertNotNull(WalkRules.titleError(" "))
        assertNotNull(WalkRules.titleError("ab"))
        assertNull(WalkRules.titleError(" Weg "))
        assertNotNull(WalkRules.areaError(" "))
    }

    @Test fun searchMatchesNameAndPlaceIgnoringCase() {
        val walks = InMemoryWalkRepository().walks.value
        assertEquals(listOf(1), WalkRules.filter(walks, "NORDUFER", false).map { it.id })
        assertEquals(listOf(1, 2), WalkRules.filter(walks, "", true).map { it.id })
    }
}
