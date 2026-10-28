package com.groupa.presentation.screen

import com.groupa.data.repository.MockGroupRepository
import com.groupa.presentation.download.CarModelDownloadStatus
import com.groupa.presentation.download.CarModelDownloadState
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Car3DScreenTest {

    @Test
    fun carNotificationLinksToTheCar3dDashboard() = runBlocking {
        val repository = MockGroupRepository()
        val messages = repository.messages.first()
        val carMessage = messages.firstOrNull { it.car3dId != null }

        assertNotNull(carMessage)
        assertEquals("carconcept", carMessage?.car3dId)
        assertTrue(carMessage?.preview?.contains("3D", ignoreCase = true) == true)
    }

    @Test
    fun downloadedModelFileIsIgnoredWhenCacheWasCleared() {
        val cacheDir = kotlin.io.path.createTempDirectory("car3d-cache").toFile()
        val modelFile = File(cacheDir, "CarConcept.glb")
        assertTrue(modelFile.createNewFile())

        val availableState = CarModelDownloadState(modelFile = modelFile)
        assertEquals(modelFile, resolveDownloadedModelFile(availableState))

        assertTrue(modelFile.delete())
        val clearedCacheState = CarModelDownloadState(modelFile = modelFile)

        assertEquals(null, resolveDownloadedModelFile(clearedCacheState))
        cacheDir.delete()
    }

    @Test
    fun carModelDownloadProgressIsClampedAndHandlesUnknownTotalSize() {
        val knownProgress = carModelDownloadProgress(
            CarModelDownloadState(
                status = CarModelDownloadStatus.Downloading,
                downloadedBytes = 5,
                totalBytes = 10,
            ),
        )
        val overflowProgress = carModelDownloadProgress(
            CarModelDownloadState(
                status = CarModelDownloadStatus.Downloading,
                downloadedBytes = 11,
                totalBytes = 10,
            ),
        )
        val unknownTotalProgress = carModelDownloadProgress(
            CarModelDownloadState(
                status = CarModelDownloadStatus.Downloading,
                downloadedBytes = 1024,
            ),
        )

        assertEquals(0.5f, knownProgress, 0.001f)
        assertEquals(1f, overflowProgress, 0.001f)
        assertEquals(0.08f, unknownTotalProgress, 0.001f)
    }

    @Test
    fun carModelDiagnosticsReactToProgressAndFailure() {
        val downloadingLogs = carModelDownloadDiagnosticLines(
            CarModelDownloadState(
                status = CarModelDownloadStatus.Downloading,
                downloadedBytes = 5,
                totalBytes = 10,
            ),
        )
        val failedLogs = carModelDownloadDiagnosticLines(
            CarModelDownloadState(
                status = CarModelDownloadStatus.Failed,
                downloadedBytes = 0,
                totalBytes = 10,
                error = "Connection reset",
            ),
        )

        assertTrue(downloadingLogs.any { it.contains("INTEGRITY") })
        assertTrue(failedLogs.any { it.contains("ALERT") })
    }
}
