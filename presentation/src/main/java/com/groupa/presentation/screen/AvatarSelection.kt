package com.groupa.presentation.screen

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

internal fun createAvatarPickerContract() =
    ActivityResultContracts.PickVisualMedia()

internal fun buildAvatarPickerRequest(): PickVisualMediaRequest =
    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

internal fun resolvePickedAvatarUri(
    pickedUri: String?,
    storedUri: String?,
): String? {
    if (pickedUri == null) return null
    return storedUri?.takeIf { it.isNotBlank() } ?: pickedUri
}

internal fun handleAvatarPickerResult(
    pickedUri: String?,
    storedUri: String?,
    onAvatarChanged: (String) -> Unit,
): String? {
    val effectiveAvatarUri = resolvePickedAvatarUri(pickedUri, storedUri)
    effectiveAvatarUri?.let(onAvatarChanged)
    return effectiveAvatarUri
}
