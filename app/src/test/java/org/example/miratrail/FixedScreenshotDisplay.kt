package org.example.miratrail

import org.junit.rules.ExternalResource
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowDisplay

/** Set exact pixels before the Compose test rule launches its Activity. */
class FixedScreenshotDisplay : ExternalResource() {
    override fun before() {
        shadowOf(ShadowDisplay.getDefaultDisplay()).apply {
            setHeight(2992)
            setRealHeight(2992)
        }
    }
}
