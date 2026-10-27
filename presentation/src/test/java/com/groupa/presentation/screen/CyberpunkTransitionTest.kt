package com.groupa.presentation.screen

import org.junit.Assert.assertEquals
import org.junit.Test

class CyberpunkTransitionTest {

    @Test
    fun transitionStartsFromAnUnchangedScreen() {
        val frame = cyberpunkTransitionFrame(0f)

        assertEquals(1f, frame.sourceAlpha, 0.001f)
        assertEquals(1f, frame.sourceScaleX, 0.001f)
        assertEquals(1f, frame.sourceScaleY, 0.001f)
        assertEquals(0f, frame.sourceSkewX, 0.001f)
        assertEquals(0f, frame.overlayAlpha, 0.001f)
    }

    @Test
    fun transitionCollapsesAndSkewsTheSourceScreen() {
        val frame = cyberpunkTransitionFrame(0.3f)

        assertEquals(1.3f, frame.sourceScaleX, 0.001f)
        assertEquals(0.9f, frame.sourceScaleY, 0.001f)
        assertEquals(-10f, frame.sourceSkewX, 0.001f)
        assertEquals(1f, frame.sourceAlpha, 0.001f)
        assertEquals(1f, frame.overlayAlpha, 0.001f)
    }

    @Test
    fun transitionReachesTheTearLineBeforeFading() {
        val frame = cyberpunkTransitionFrame(0.6f)

        assertEquals(1.3f, frame.sourceScaleX, 0.001f)
        assertEquals(30f, frame.sourceSkewX, 0.001f)
        assertEquals(1f, frame.sourceAlpha, 0.001f)
        assertEquals(1f, frame.overlayAlpha, 0.001f)
    }

    @Test
    fun transitionFinishesCollapsedAndClearsOverlay() {
        val frame = cyberpunkTransitionFrame(1f)

        assertEquals(2f, frame.sourceScaleX, 0.001f)
        assertEquals(0f, frame.sourceAlpha, 0.001f)
        assertEquals(0f, frame.overlayAlpha, 0.001f)
    }
}
