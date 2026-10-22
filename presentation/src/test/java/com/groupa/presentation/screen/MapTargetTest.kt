package com.groupa.presentation.screen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MapTargetTest {

    @Test
    fun parsesLatitudeAndLongitude() {
        val target = parseMapTarget("37.7749,-122.4194")

        assertEquals(37.7749, target?.latitude ?: 0.0, 0.0001)
        assertEquals(-122.4194, target?.longitude ?: 0.0, 0.0001)
    }

    @Test
    fun rejectsInvalidCoordinates() {
        assertNull(parseMapTarget("Current location"))
        assertNull(parseMapTarget("91.0,0.0"))
        assertNull(parseMapTarget("0.0,181.0"))
    }
}
