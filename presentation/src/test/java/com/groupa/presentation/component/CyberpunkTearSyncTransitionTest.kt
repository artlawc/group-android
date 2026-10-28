package com.groupa.presentation.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CyberpunkTearSyncTransitionTest {

    @Test
    fun entryTransitionPlaysOnceWhenTheModelIsReady() {
        assertTrue(
            cyberpunkEntryTransitionShouldPlay(
                modelReady = true,
                hasPlayed = false,
            ),
        )
        assertFalse(
            cyberpunkEntryTransitionShouldPlay(
                modelReady = true,
                hasPlayed = true,
            ),
        )
        assertFalse(
            cyberpunkEntryTransitionShouldPlay(
                modelReady = false,
                hasPlayed = false,
            ),
        )
    }

    @Test
    fun dissolveThresholdMatchesTheReferenceShader() {
        assertEquals(0f, cyberpunkTearSyncThreshold(0f), 0.001f)
        assertEquals(0.75f, cyberpunkTearSyncThreshold(0.5f), 0.001f)
        assertEquals(1.5f, cyberpunkTearSyncThreshold(1f), 0.001f)
    }

    @Test
    fun glitchIntensityIsViolentAtStartAndFadesOut() {
        assertEquals(0.85f, cyberpunkTearSyncGlitchIntensity(0f), 0.001f)
        assertEquals(0.425f, cyberpunkTearSyncGlitchIntensity(0.5f), 0.001f)
        assertEquals(0f, cyberpunkTearSyncGlitchIntensity(1f), 0.001f)
    }

    @Test
    fun shaderHashIsStableAndNormalized() {
        val first = cyberpunkHash(x = 3f, y = 7f)
        val second = cyberpunkHash(x = 3f, y = 7f)

        assertEquals(first, second, 0.001f)
        assertTrue(first in 0f..1f)
    }

}
