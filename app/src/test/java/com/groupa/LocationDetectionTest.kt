package com.groupa

import android.Manifest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationDetectionTest {

    @Test
    fun permissionCallbackStartsDetectionWhenFineLocationIsGranted() {
        var didStartDetection = false
        var deniedLocation: DetectedLocation? = null

        onLocationPermissionsResult(
            permissions = mapOf(
                Manifest.permission.ACCESS_FINE_LOCATION to true,
                Manifest.permission.ACCESS_COARSE_LOCATION to false,
            ),
            permissionDeniedLabel = "Location unavailable",
            onDetectLocation = { didStartDetection = true },
            onPermissionDenied = { deniedLocation = it },
        )

        assertTrue(didStartDetection)
        assertNull(deniedLocation)
    }

    @Test
    fun permissionCallbackStartsDetectionWhenOnlyCoarseLocationIsGranted() {
        var didStartDetection = false

        onLocationPermissionsResult(
            permissions = mapOf(Manifest.permission.ACCESS_COARSE_LOCATION to true),
            permissionDeniedLabel = "Location unavailable",
            onDetectLocation = { didStartDetection = true },
            onPermissionDenied = {},
        )

        assertTrue(didStartDetection)
    }

    @Test
    fun permissionCallbackReportsDeniedLocationWhenAllPermissionsAreRejected() {
        var didStartDetection = false
        var deniedLocation: DetectedLocation? = null

        onLocationPermissionsResult(
            permissions = mapOf(
                Manifest.permission.ACCESS_FINE_LOCATION to false,
                Manifest.permission.ACCESS_COARSE_LOCATION to false,
            ),
            permissionDeniedLabel = "Location unavailable",
            onDetectLocation = { didStartDetection = true },
            onPermissionDenied = { deniedLocation = it },
        )

        assertFalse(didStartDetection)
        assertEquals("Location unavailable", deniedLocation?.label)
        assertNull(deniedLocation?.latitude)
        assertNull(deniedLocation?.longitude)
    }

    @Test
    fun locationCallbackSendsCoordinatesToHomeScreenImmediately() {
        var callbackLocation: DetectedLocation? = null

        val detectedLocation = onLocationResult(
            latitude = 31.230416,
            longitude = 121.473701,
            onLocationDetected = { callbackLocation = it },
        )

        assertEquals(detectedLocation, callbackLocation)
        assertEquals("31.2304, 121.4737", callbackLocation?.label)
        assertEquals(31.230416, callbackLocation?.latitude ?: 0.0, 0.0)
        assertEquals(121.473701, callbackLocation?.longitude ?: 0.0, 0.0)
    }
}
