package com.groupa.presentation.screen

import kotlin.math.sqrt
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlameExitTransitionTest {

    @Test
    fun topFlameFrontierSweepsFromTopToBottom() {
        assertEquals(0f, flameFrontierY(100f, 0f), 0.001f)
        assertEquals(50f, flameFrontierY(100f, 0.5f), 0.001f)
        assertEquals(100f, flameFrontierY(100f, 1f), 0.001f)
    }

    @Test
    fun centerFlameFrontierGrowsToCoverTheScreenCorners() {
        val frontierRadius = flameFrontierRadius(
            width = 300f,
            height = 400f,
            progress = 1f,
        )

        assertEquals(sqrt(300f * 300f + 400f * 400f) / 2f, frontierRadius, 0.001f)
    }

    @Test
    fun topFlameUsesMultipleWaveBladesInsteadOfAStraightLine() {
        val points = flameBladePoints(
            progress = 0.5f,
            width = 200f,
            height = 500f,
            ignitionMode = FlameIgnitionMode.TOP,
        )

        assertEquals(11, points.size)
        assertTrue(points.any { it.y < 250f })
        assertTrue(points.any { it.y > 250f })
    }

    @Test
    fun bottomFlameBurnsFromBottomToTopWithWaveBlades() {
        val points = flameBladePoints(
            progress = 0.5f,
            width = 200f,
            height = 500f,
            ignitionMode = FlameIgnitionMode.BOTTOM,
        )

        assertEquals(11, points.size)
        assertTrue(points.any { it.y < 250f })
        assertTrue(points.any { it.y > 250f })
    }

    @Test
    fun centerFlameBladesFormACircleAroundTheScreenCenter() {
        val points = flameBladePoints(
            progress = 0.5f,
            width = 200f,
            height = 500f,
            ignitionMode = FlameIgnitionMode.CENTER,
        )

        assertEquals(11, points.size)
        assertTrue(points.any { it.x < 100f })
        assertTrue(points.any { it.x > 100f })
        assertTrue(points.any { it.y < 250f })
        assertTrue(points.any { it.y > 250f })
    }

    @Test
    fun topFlameParticlesStartAtTheFrontierAndDriftUpward() {
        val particles = flameParticles(
            progress = 0.4f,
            width = 200f,
            height = 500f,
            ignitionMode = FlameIgnitionMode.TOP,
        )

        assertEquals(36, particles.size)
        assertTrue(particles.all { it.y <= 200f })
        assertTrue(particles.all { it.alpha > 0f })
        assertTrue(particles.all { it.radius >= 3f })
    }

    @Test
    fun bottomFlameParticlesStartNearTheFrontierAndDriftUpward() {
        val particles = flameParticles(
            progress = 0.4f,
            width = 200f,
            height = 500f,
            ignitionMode = FlameIgnitionMode.BOTTOM,
        )

        assertEquals(36, particles.size)
        assertTrue(particles.all { it.y <= 300f })
        assertTrue(particles.all { it.alpha > 0f })
    }

    @Test
    fun centerFlameParticlesSpreadAroundTheCoreFrontier() {
        val particles = flameParticles(
            progress = 0.5f,
            width = 200f,
            height = 500f,
            ignitionMode = FlameIgnitionMode.CENTER,
        )

        assertEquals(36, particles.size)
        assertTrue(particles.any { it.x < 100f })
        assertTrue(particles.any { it.x > 100f })
        assertTrue(particles.any { it.y < 250f })
        assertTrue(particles.any { it.y > 250f })
    }

    @Test
    fun flameParticlesFadeOutByTheEndOfTheTransition() {
        val modes = FlameIgnitionMode.entries

        assertTrue(modes.all { mode ->
            flameParticles(
                progress = 1f,
                width = 200f,
                height = 500f,
                ignitionMode = mode,
            ).all { it.alpha <= 0f }
        })
    }

    @Test
    fun exitRandomlySelectsASupportedIgnitionMode() {
        val modes = List(100) { index ->
            randomFlameIgnitionMode(Random(seed = index))
        }.toSet()

        assertTrue(modes.all { it in FlameIgnitionMode.entries })
        assertEquals(3, modes.size)
    }
}
