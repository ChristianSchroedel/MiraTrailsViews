package org.example.miratrail.domain

import org.example.miratrail.data.Walk

object WalkRules {
    fun titleError(title: String): String? = when {
        title.isBlank() -> "Bitte einen Namen eingeben."
        title.trim().length < 3 -> "Der Name muss mindestens drei Zeichen haben."
        else -> null
    }

    fun areaError(area: String): String? = if (area.isBlank()) "Bitte einen Ort eingeben." else null

    fun filter(walks: List<Walk>, query: String, onlyFavorites: Boolean): List<Walk> =
        walks.filter {
            (!onlyFavorites || it.favorite) && (query.isBlank() ||
                it.title.contains(query.trim(), ignoreCase = true) ||
                it.area.contains(query.trim(), ignoreCase = true))
        }
}
