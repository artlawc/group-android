package com.groupa.presentation.screen

import com.groupa.data.repository.MockGroupRepository
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
}
