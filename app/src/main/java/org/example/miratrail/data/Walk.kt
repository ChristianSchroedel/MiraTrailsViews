package org.example.miratrail.data

data class Walk(
    val id: Int,
    val title: String,
    val area: String,
    val description: String,
    val stages: List<String>,
    val completedStages: Int = 0,
    val favorite: Boolean = false,
    val planned: Boolean = false,
    val note: String = ""
) {
    val isComplete: Boolean get() = stages.isNotEmpty() && completedStages == stages.size
}
