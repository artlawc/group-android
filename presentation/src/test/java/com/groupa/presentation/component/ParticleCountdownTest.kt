package com.groupa.presentation.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ParticleCountdownTest {

    @Test
    fun formatsSecondsAsHoursMinutesAndSeconds() {
        assertEquals("00:00:00", formatParticleCountdown(0))
        assertEquals("00:00:59", formatParticleCountdown(59))
        assertEquals("00:59:59", formatParticleCountdown(3_599))
        assertEquals("01:00:00", formatParticleCountdown(3_600))
    }

    @Test
    fun digitParticlesFollowSevenSegmentGeometry() {
        val zero = particleCountdownTargetPoints('0', Random(1))
        val one = particleCountdownTargetPoints('1', Random(2))

        assertEquals(6 * 17, zero.size)
        assertEquals(2 * 17, one.size)
        assertTrue(zero.all { it.visible })
        assertTrue(one.all { it.x in 63f..67f })
    }

    @Test
    fun colonUsesTwoParticleClusters() {
        val colon = particleCountdownTargetPoints(':', Random(3))

        assertEquals(30, colon.size)
        assertTrue(colon.take(15).all { it.y in 34f..42f })
        assertTrue(colon.drop(15).all { it.y in 74f..82f })
        assertTrue(colon.all { it.x in 19f..27f })
    }

    @Test
    fun morphParticlesCoverBothTheLargerSourceAndTargetShapes() {
        val particles = particleCountdownMorphParticles(
            sourceCharacter = '0',
            targetCharacter = '1',
            random = Random(4),
        )

        assertEquals(102, particles.size)
        assertTrue(particles.all { it.startAlpha == 1f && it.endAlpha == 1f })
        assertTrue(particles.all { it.endX in 63f..67f })
    }

    @Test
    fun morphingToAnUnknownCharacterHidesTheDigit() {
        val particles = particleCountdownMorphParticles(
            sourceCharacter = '8',
            targetCharacter = ' ',
            random = Random(5),
        )

        assertTrue(particles.all { it.startAlpha == 1f && it.endAlpha == 0f })
    }

    @Test
    fun onlyChangedDigitsAndTheColonBlinkByDefault() {
        val slots = updatedParticleCountdownSlots(
            sourceCharacters = formatParticleCountdown(0).toList(),
            targetText = formatParticleCountdown(1),
            allDigitsBlink = false,
            random = Random(6),
        )

        assertFalse(slots[0].particles.any { it.burstX != 0f || it.burstY != 0f })
        assertTrue(slots[5].particles.any { it.burstX != 0f || it.burstY != 0f })
        assertTrue(slots[7].sourceCharacter == '0')
        assertTrue(slots[7].targetCharacter == '1')
        assertTrue(slots[7].particles.any { it.burstX != 0f || it.burstY != 0f })
    }

    @Test
    fun allDigitsCanBlinkWhenTheFlagIsEnabled() {
        val slots = updatedParticleCountdownSlots(
            sourceCharacters = formatParticleCountdown(0).toList(),
            targetText = formatParticleCountdown(1),
            allDigitsBlink = true,
            random = Random(7),
        )

        assertTrue(slots.all { slot ->
            slot.particles.any { it.burstX != 0f || it.burstY != 0f }
        })
    }
}
