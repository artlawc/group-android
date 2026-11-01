package com.groupa.presentation.screen

import com.groupa.data.repository.MockGroupRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FlightScreenTest {

    @Test
    fun flightNotificationLinksToSeatMapData() = runBlocking {
        val repository = MockGroupRepository()
        val messages = repository.messages.first()
        val flightMessage = messages.firstOrNull { it.flightId != null }

        assertEquals("flightconcept", flightMessage?.flightId)
        assertTrue(flightMessage?.preview?.contains("seat", ignoreCase = true) == true)
    }

    @Test
    fun seatSelectionTogglesAvailableSeatsAndIgnoresTakenSeats() {
        val selected = toggleFlightSeatSelection(
            selectedSeats = emptySet(),
            seatName = "12A",
            isTaken = false,
        )

        assertEquals(setOf("12A"), selected)

        val deselected = toggleFlightSeatSelection(
            selectedSeats = selected,
            seatName = "12A",
            isTaken = false,
        )

        assertTrue(deselected.isEmpty())

        val takenAttempt = toggleFlightSeatSelection(
            selectedSeats = selected,
            seatName = "26E",
            isTaken = true,
        )

        assertEquals(selected, takenAttempt)
    }

    @Test
    fun seatStatusPrioritizesTakenThenSelectedThenHovered() {
        assertEquals(FlightSeatStatus.Taken, flightSeatStatus("26E", true, true, true))
        assertEquals(FlightSeatStatus.Selected, flightSeatStatus("12A", true, true, false))
        assertEquals(FlightSeatStatus.Hovered, flightSeatStatus("12A", true, false, false))
        assertEquals(FlightSeatStatus.Available, flightSeatStatus("12A", false, false, false))
    }

    @Test
    fun seatNameMatcherAcceptsFlightSeatLabelsOnly() {
        assertTrue(isFlightSeatName("26E"))
        assertTrue(isFlightSeatName("1A"))
        assertFalse(isFlightSeatName("body"))
        assertFalse(isFlightSeatName("text26E"))
    }
}
