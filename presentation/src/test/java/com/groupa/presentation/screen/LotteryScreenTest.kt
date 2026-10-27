package com.groupa.presentation.screen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LotteryScreenTest {

    @Test
    fun countdownFormatsHoursMinutesAndSeconds() {
        assertEquals("00:00:00", formatLotteryCountdown(0))
        assertEquals("00:00:59", formatLotteryCountdown(59))
        assertEquals("01:00:00", formatLotteryCountdown(3_600))
    }

    @Test
    fun spinRotationPlacesTheSelectedSectorUnderThePointer() {
        val rotation = nextLotteryRotation(
            currentRotation = 0f,
            selectedIndex = 0,
            sectorCount = 8,
        )

        assertEquals(2_047.5f, rotation, 0.001f)
        assertTrue(rotation > 5f * 360f)
    }

    @Test
    fun spinRotationMovesForwardFromAnyCurrentRotation() {
        val currentRotation = 1_111f
        val rotation = nextLotteryRotation(
            currentRotation = currentRotation,
            selectedIndex = 3,
            sectorCount = 8,
        )
        val sectorAngle = 360f / 8f
        val selectedCenter = 270f - 3f * sectorAngle - sectorAngle / 2f

        assertTrue(rotation > currentRotation + 5f * 360f)
        assertEquals(selectedCenter, rotation.mod(360f), 0.001f)
    }

    @Test
    fun wheelLabelsFollowTheirCutLinesAndStayNearTheRim() {
        val sectorAngle = 360f / 8f

        repeat(8) { index ->
            val placement = lotteryLabelPlacement(
                cutLineAngle = index * sectorAngle,
                wheelRadius = 150f,
                rimPadding = 18f,
                perpendicularInset = 12f,
            )

            assertEquals(index * sectorAngle, placement.angleDegrees, 0.001f)
            assertEquals(132f, placement.anchorRadius, 0.001f)
            assertEquals(12f, placement.perpendicularOffset, 0.001f)
        }
    }

    @Test
    fun wheelWordingIsExtractedToDistinctLocalizationResources() {
        val labelResources = lotteryPrizeDefinitions.map { it.labelRes }

        assertEquals(8, labelResources.size)
        assertEquals(labelResources.size, labelResources.distinct().size)
        assertTrue(labelResources.all { it != 0 })
    }
}
