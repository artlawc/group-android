package com.groupa.presentation.screen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import androidx.activity.result.contract.ActivityResultContracts

class AvatarSelectionTest {

    @Test
    fun avatarPickerUsesSystemPhotoPickerContract() {
        val contract = createAvatarPickerContract()

        assertEquals(
            ActivityResultContracts.PickVisualMedia::class.java,
            contract.javaClass,
        )
    }

    @Test
    fun avatarPickerRequestUsesGalleryImagesOnly() {
        val request = buildAvatarPickerRequest()

        assertEquals(
            ActivityResultContracts.PickVisualMedia.ImageOnly,
            request.mediaType,
        )
    }

    @Test
    fun galleryPickerCallbackUpdatesAvatarBeforeInternalCopyFinishes() {
        val pickedUri = "content://media/external/images/123"
        val changedAvatars = mutableListOf<String>()

        val selectedAvatarUri = handleAvatarPickerResult(
            pickedUri = pickedUri,
            storedUri = "",
            onAvatarChanged = changedAvatars::add,
        )

        assertEquals(pickedUri, selectedAvatarUri)
        assertEquals(listOf(pickedUri), changedAvatars)
    }

    @Test
    fun galleryPickerCallbackReplacesTemporarySelectionWithInternalPhoto() {
        val pickedUri = "content://media/external/images/123"
        val storedUri = "file:///data/user/0/com.groupa/files/profile_avatar_123.jpg"
        val changedAvatars = mutableListOf<String>()

        val selectedAvatarUri = handleAvatarPickerResult(
            pickedUri = pickedUri,
            storedUri = storedUri,
            onAvatarChanged = changedAvatars::add,
        )

        assertEquals(storedUri, selectedAvatarUri)
        assertEquals(listOf(storedUri), changedAvatars)
    }

    @Test
    fun pickedPhotoUpdatesImmediatelyBeforeInternalCopyFinishes() {
        val pickedUri = "content://media/external/images/123"

        val result = resolvePickedAvatarUri(pickedUri, storedUri = "")

        assertEquals(pickedUri, result)
    }

    @Test
    fun copiedInternalPhotoReplacesTemporaryPickedPhoto() {
        val pickedUri = "content://media/external/images/123"
        val storedUri = "file:///data/user/0/com.groupa/files/profile_avatar_1.jpg"

        val result = resolvePickedAvatarUri(pickedUri, storedUri)

        assertEquals(storedUri, result)
    }

    @Test
    fun noPhotoSelectionKeepsAvatarUnchanged() {
        assertNull(resolvePickedAvatarUri(null, storedUri = ""))
    }

    @Test
    fun pickerCallbackSendsPickedPhotoImmediately() {
        val pickedUri = "content://media/external/images/123"
        val changedAvatars = mutableListOf<String>()

        val selectedAvatarUri = handleAvatarPickerResult(
            pickedUri = pickedUri,
            storedUri = "",
            onAvatarChanged = changedAvatars::add,
        )

        assertEquals(pickedUri, selectedAvatarUri)
        assertEquals(listOf(pickedUri), changedAvatars)
    }

    @Test
    fun pickerCallbackReplacesPickedPhotoWithCopiedInternalPhoto() {
        val pickedUri = "content://media/external/images/123"
        val storedUri = "file:///data/user/0/com.groupa/files/profile_avatar_123.jpg"
        val changedAvatars = mutableListOf<String>()

        val selectedAvatarUri = handleAvatarPickerResult(
            pickedUri = pickedUri,
            storedUri = storedUri,
            onAvatarChanged = changedAvatars::add,
        )

        assertEquals(storedUri, selectedAvatarUri)
        assertEquals(listOf(storedUri), changedAvatars)
    }

    @Test
    fun pickerCallbackDoesNothingWhenPhotoSelectionIsCancelled() {
        val changedAvatars = mutableListOf<String>()

        val selectedAvatarUri = handleAvatarPickerResult(
            pickedUri = null,
            storedUri = "",
            onAvatarChanged = changedAvatars::add,
        )

        assertNull(selectedAvatarUri)
        assertTrue(changedAvatars.isEmpty())
    }
}
