package com.groupa

import android.Manifest
import java.util.Locale

internal data class DetectedLocation(
    val label: String,
    val latitude: Double?,
    val longitude: Double?,
)

internal fun onLocationPermissionsResult(
    permissions: Map<String, Boolean>,
    permissionDeniedLabel: String,
    onDetectLocation: () -> Unit,
    onPermissionDenied: (DetectedLocation) -> Unit,
) {
    val isFineLocationGranted =
        permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    val isCoarseLocationGranted =
        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

    if (isFineLocationGranted || isCoarseLocationGranted) {
        onDetectLocation()
    } else {
        onPermissionDenied(
            DetectedLocation(
                label = permissionDeniedLabel,
                latitude = null,
                longitude = null,
            ),
        )
    }
}

internal fun onLocationResult(
    latitude: Double,
    longitude: Double,
    onLocationDetected: (DetectedLocation) -> Unit,
): DetectedLocation {
    val detectedLocation = DetectedLocation(
        label = String.format(Locale.US, "%.4f, %.4f", latitude, longitude),
        latitude = latitude,
        longitude = longitude,
    )
    onLocationDetected(detectedLocation)
    return detectedLocation
}
