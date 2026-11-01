package com.groupa.presentation.screen

import com.groupa.data.repository.MockGroupRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AirportScreenTest {

    @Test
    fun airportNotificationLinksToAirportDataAndFlightMap() = runBlocking {
        val repository = MockGroupRepository()
        val messages = repository.messages.first()
        val airportMessage = messages.firstOrNull { it.airportId != null }

        assertNotNull(airportMessage)
        assertEquals("iceland", airportMessage?.airportId)
        assertTrue(airportMessage?.preview?.contains("flight map", ignoreCase = true) == true)
    }

    @Test
    fun nearestAirportStationUsesTheTapHitThreshold() {
        val reykjavik = nearestAirportStation(x = 233f, y = 313f)

        assertEquals("reykjavik", reykjavik?.id)
        assertNull(nearestAirportStation(x = 900f, y = 500f))
    }

    @Test
    fun flightRoutePointFollowsTheBezierCurveEndpoints() {
        val from = icelandAirports[1]
        val to = icelandAirports[3]
        val control = flightRouteControlPoint(from = from, to = to)

        val start = flightRoutePoint(from, control, to, progress = 0f)
        val end = flightRoutePoint(from, control, to, progress = 1f)

        assertEquals(from.x, start.x, 0.001f)
        assertEquals(from.y, start.y, 0.001f)
        assertEquals(to.x, end.x, 0.001f)
        assertEquals(to.y, end.y, 0.001f)
    }
}
