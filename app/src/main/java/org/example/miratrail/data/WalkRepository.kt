package org.example.miratrail.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface WalkRepository {
    val walks: StateFlow<List<Walk>>
    fun find(id: Int): Walk?
    fun save(walk: Walk): Walk
    fun reset()
}

class InMemoryWalkRepository : WalkRepository {
    private val entries = MutableStateFlow(seed())
    override val walks: StateFlow<List<Walk>> = entries

    override fun find(id: Int): Walk? = entries.value.firstOrNull { it.id == id }

    override fun save(walk: Walk): Walk {
        val stored = if (walk.id == 0) walk.copy(id = (entries.value.maxOfOrNull { it.id }
            ?: 0) + 1) else walk
        entries.value = if (entries.value.any { it.id == stored.id }) {
            entries.value.map { if (it.id == stored.id) stored else it }
        } else entries.value + stored
        return stored
    }

    override fun reset() {
        entries.value = seed()
    }

    private fun seed() = listOf(
        Walk(
            1,
            "Am kleinen Fluss",
            "Nordufer",
            "Ein ruhiger Weg am Wasser. Der Rundgang führt über den alten Steg, an einer offenen Wiese vorbei und durch die schmale Baumallee zurück zum Ausgangspunkt. Nach Regen kann der Abschnitt am Ufer feucht sein; die Brücke bleibt gut begehbar.",
            listOf("Alter Steg", "Wiese", "Baumallee"),
            1,
            favorite = true,
            planned = true,
            note = "Die Wiese ist am Morgen besonders ruhig."
        ),
        Walk(
            2,
            "Runde durch die Gärten",
            "Westgärten",
            "Kurzer Rundgang zwischen Gemeinschaftsbeeten und schattigen Wegen.",
            listOf("Tor", "Beete", "Brunnen"),
            favorite = true
        ),
        Walk(
            3,
            "Hügelblick",
            "Südhöhe",
            "Ein Anstieg mit weitem Blick über die Stadt.",
            listOf("Treppe", "Aussicht", "Rückweg"),
            planned = true
        ),
        Walk(
            4,
            "Markt und Mauer",
            "Altviertel",
            "Vom Platz entlang der alten Mauer bis zum kleinen Hof.",
            listOf("Markt", "Mauer", "Hof"),
            3,
            note = "Am Hof gibt es Sitzplätze."
        ),
        Walk(
            5,
            "Abendrunde",
            "Ostpark",
            "Eine kurze Runde durch den Park.",
            listOf("Eingang", "Teich", "Ausgang")
        )
    )
}
