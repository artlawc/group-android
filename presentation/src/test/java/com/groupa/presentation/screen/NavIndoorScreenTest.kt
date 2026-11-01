package com.groupa.presentation.screen

import com.groupa.data.repository.MockGroupRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavIndoorScreenTest {

    @Test
    fun navIndoorNotificationLinksToIndoorNavigationData() = runBlocking {
        val repository = MockGroupRepository()
        val messages = repository.messages.first()
        val navIndoorMessage = messages.firstOrNull { it.navIndoorId != null }

        assertNotNull(navIndoorMessage)
        assertEquals("macodrum-lv5", navIndoorMessage?.navIndoorId)
        assertTrue(navIndoorMessage?.preview?.contains("floor plan", ignoreCase = true) == true)
    }

    @Test
    fun routeLengthUsesEveryFloorPlanSegment() {
        val points = listOf(
            NavIndoorPathPoint(0f, 0f),
            NavIndoorPathPoint(3f, 4f),
            NavIndoorPathPoint(3f, 4f),
        )

        assertEquals(5f, navIndoorRouteLength(points), 0.001f)
    }

    @Test
    fun routePointReturnsSegmentEndpointAtBoundaryDistance() {
        val point = navIndoorRoutePointAtDistance(navIndoorRoutePoints, distance = 0f)

        assertEquals(navIndoorRoutePoints.first().x, point.x, 0.001f)
        assertEquals(navIndoorRoutePoints.first().y, point.y, 0.001f)
        assertEquals(0f, navIndoorRouteProgress(navIndoorRoutePoints, 0f), 0.001f)
        assertEquals(1f, navIndoorRouteProgress(navIndoorRoutePoints, 10_000f), 0.001f)
    }
}
