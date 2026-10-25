package com.groupa.presentation.download

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CarModelDownloadManagerTest {
    @Test
    fun `reads total length from glb header rather than first chunk header`() {
        val file = File.createTempFile("model", ".glb")
        file.deleteOnExit()
        val header = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN)
            .put(byteArrayOf(0x47, 0x4C, 0x54, 0x46))
            .putInt(2)
            .putInt(11_778_688)
            .putInt(184_880)
            .array()
        file.writeBytes(header)

        assertEquals(11_778_688L, CarModelDownloadManager.readGlbTotalLength(file))
    }

    @Test
    fun `returns null when glb header is incomplete`() {
        val file = File.createTempFile("model", ".glb")
        file.deleteOnExit()
        file.writeBytes(byteArrayOf(0x47, 0x4C, 0x54))

        assertNull(CarModelDownloadManager.readGlbTotalLength(file))
    }

    @Test
    fun `accepts the standard gltf magic in a complete glb file`() {
        val file = File.createTempFile("model", ".glb")
        file.deleteOnExit()
        val glb = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN)
            .put(byteArrayOf(0x67, 0x6C, 0x54, 0x46))
            .putInt(2)
            .putInt(20)
            .array()
        file.writeBytes(glb)

        assertTrue(CarModelDownloadManager.isCompleteGlbFile(file))
    }
}
