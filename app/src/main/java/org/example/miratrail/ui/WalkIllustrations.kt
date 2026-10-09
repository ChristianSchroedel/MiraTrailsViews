package org.example.miratrail.ui

import androidx.annotation.DrawableRes
import org.example.miratrail.R

/** Decorative artwork for the fixed sample walks; newly created walks use generic scenery. */
object WalkIllustrations {
    @DrawableRes
    fun catalog(walkId: Int): Int = when (walkId) {
        1 -> R.drawable.catalog_river
        2 -> R.drawable.catalog_gardens
        3 -> R.drawable.catalog_hill
        4 -> R.drawable.catalog_market
        5 -> R.drawable.catalog_evening
        else -> R.drawable.sunny_overview
    }

    @DrawableRes
    fun collection(walkId: Int): Int = when (walkId) {
        1 -> R.drawable.collection_river
        2 -> R.drawable.collection_gardens
        3 -> R.drawable.collection_hill
        4 -> R.drawable.collection_market
        else -> catalog(walkId)
    }

    @DrawableRes
    fun detail(walkId: Int): Int =
        if (walkId == 1) R.drawable.sunny_detail else collection(walkId)
}
