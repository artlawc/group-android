package com.groupa.presentation.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Cyberpunk2077LoaderTest {

    @Test
    fun monitorEchoCyclesThroughDiagnosticMessages() {
        val logs = listOf(
            "BUFFER_TRACE: Receiving packets",
            "SHADER_CACHE: Warming pipeline",
        )

        assertEquals(
            "ECHO // BUFFER_TRACE: Receiving packets",
            cyberpunkMonitorEchoMessage(logs = logs, tick = 0),
        )
        assertEquals(
            "ECHO // SHADER_CACHE: Warming pipeline",
            cyberpunkMonitorEchoMessage(logs = logs, tick = 1),
        )
        assertEquals(
            "ECHO // BUFFER_TRACE: Receiving packets",
            cyberpunkMonitorEchoMessage(logs = logs, tick = 2),
        )
    }

    @Test
    fun monitorEchoFallsBackWhenNoDiagnosticsAreAvailable() {
        val message = cyberpunkMonitorEchoMessage(logs = emptyList(), tick = -1)

        assertTrue(message.startsWith("ECHO // "))
        assertTrue(message.contains("Awaiting model datashard packets"))
    }
}
