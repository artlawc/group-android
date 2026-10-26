package com.groupa.presentation.screen

import android.graphics.Bitmap
import android.net.Uri

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.groupa.coreui.R
import com.groupa.data.repository.MockGroupRepository
import com.groupa.domain.usecase.AddMessageUseCase
import com.groupa.domain.usecase.AddToCartUseCase
import com.groupa.domain.usecase.CalculateTotalsUseCase
import com.groupa.domain.usecase.ChangePasswordUseCase
import com.groupa.domain.usecase.DeleteMessagesUseCase
import com.groupa.domain.usecase.GetCartUseCase
import com.groupa.domain.usecase.GetChatMessagesUseCase
import com.groupa.domain.usecase.GetChatsUseCase
import com.groupa.domain.usecase.GetCouponsUseCase
import com.groupa.domain.usecase.GetDealsUseCase
import com.groupa.domain.usecase.GetMessagesUseCase
import com.groupa.domain.usecase.GetOrdersUseCase
import com.groupa.domain.usecase.GetUserUseCase
import com.groupa.domain.usecase.MarkAllMessagesReadUseCase
import com.groupa.domain.usecase.MarkMessageReadUseCase
import com.groupa.domain.usecase.PlaceOrderUseCase
import com.groupa.domain.usecase.RemoveCartItemUseCase
import com.groupa.domain.usecase.SendChatMessageUseCase
import com.groupa.domain.usecase.SignInUseCase
import com.groupa.domain.usecase.SignInWithProviderUseCase
import com.groupa.domain.usecase.SignOutUseCase
import com.groupa.domain.usecase.UpdateAvatarUseCase
import com.groupa.domain.usecase.UpdateCartItemUseCase
import com.groupa.domain.usecase.UpdateProfileUseCase
import com.groupa.presentation.viewmodel.GroupViewModel
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenAvatarPickerTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

    @Test
    fun profileScreenChangesAvatarWhenGalleryReturnsPhoto() {
        val fakeRegistry = FakeAvatarPickerRegistry()
        val changedAvatars = mutableListOf<String>()

        composeTestRule.setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalActivityResultRegistryOwner provides FakeActivityResultRegistryOwner(fakeRegistry),
            ) {
                ProfileScreen(
                    name = "Tim",
                    email = "tim@qq.com",
                    memberSince = "2024",
                    totalSavings = 0.0,
                    dealsSaved = 0,
                    avatarUri = "",
                    version = "v1.1.0",
                    onOpenAccountSettings = {},
                    onOpenPaymentMethods = {},
                    onOpenCoupons = {},
                    onOpenInviteFriends = {},
                    onOpenSupport = {},
                    onChangeAvatar = { avatarUri, _ -> changedAvatars.add(avatarUri) },
                    onLogout = {},
                )
            }
        }

        val changeAvatarText = composeTestRule.activity.getString(R.string.profile_change_avatar)
        composeTestRule
            .onNodeWithText(changeAvatarText)
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) { changedAvatars.isNotEmpty() }

        assertEquals(1, changedAvatars.size)
        assertEquals(
            ActivityResultContracts.PickVisualMedia::class.java,
            fakeRegistry.launchedContract.get()?.javaClass,
        )
        assertEquals(
            ActivityResultContracts.PickVisualMedia.ImageOnly,
            fakeRegistry.launchedRequest.get()?.mediaType,
        )
    }

    @Test
    fun viewModelCompletesAvatarCopyAfterProfileScreenLeaves() {
        val repository = MockGroupRepository()
        val viewModel = GroupViewModel(
            getDeals = GetDealsUseCase(repository),
            getCart = GetCartUseCase(repository),
            getOrders = GetOrdersUseCase(repository),
            getCoupons = GetCouponsUseCase(repository),
            getUser = GetUserUseCase(repository),
            getMessages = GetMessagesUseCase(repository),
            getChats = GetChatsUseCase(repository),
            getChatMessages = GetChatMessagesUseCase(repository),
            addToCart = AddToCartUseCase(repository),
            updateCartItem = UpdateCartItemUseCase(repository),
            removeCartItem = RemoveCartItemUseCase(repository),
            calculateTotals = CalculateTotalsUseCase(repository),
            placeOrder = PlaceOrderUseCase(repository),
            signInUseCase = SignInUseCase(repository),
            signInWithProviderUseCase = SignInWithProviderUseCase(repository),
            updateProfileUseCase = UpdateProfileUseCase(repository),
            updateAvatarUseCase = UpdateAvatarUseCase(repository),
            changePasswordUseCase = ChangePasswordUseCase(repository),
            signOutUseCase = SignOutUseCase(repository),
            markAllMessagesReadUseCase = MarkAllMessagesReadUseCase(repository),
            markMessageReadUseCase = MarkMessageReadUseCase(repository),
            addMessageUseCase = AddMessageUseCase(repository),
            deleteMessagesUseCase = DeleteMessagesUseCase(repository),
            sendChatMessageUseCase = SendChatMessageUseCase(repository),
        )
        val pickedUri = "content://media/external/images/123"
        val storedUri = "file:///data/user/0/com.groupa/files/profile_avatar_123.jpg"

        assertTrue(
            viewModel.updateAvatar(pickedUri) {
                delay(250)
                storedUri
            },
        )
        assertEquals(pickedUri, runBlocking { repository.user.first().avatarUri })

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            runBlocking { repository.user.first().avatarUri } == storedUri
        }
        val finalState = runBlocking {
            viewModel.state.first { it.user.avatarUri == storedUri }
        }

        assertEquals(storedUri, finalState.user.avatarUri)
    }

    @Test
    fun internalFileAvatarUriDecodesToBitmap() {
        val avatarFile = File(composeTestRule.activity.cacheDir, "profile_avatar_test.jpg")
        val sourceBitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        avatarFile.outputStream().use { outputStream ->
            sourceBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }

        val decodedBitmap = decodeAvatarBitmap(
            context = composeTestRule.activity,
            avatarUri = Uri.fromFile(avatarFile).toString(),
        )

        assertNotNull(decodedBitmap)
        assertEquals(16, decodedBitmap!!.width)
        assertEquals(16, decodedBitmap.height)

        avatarFile.delete()
        sourceBitmap.recycle()
        decodedBitmap.recycle()
    }

    private class FakeActivityResultRegistryOwner(
        private val registry: ActivityResultRegistry,
    ) : ActivityResultRegistryOwner {
        override val activityResultRegistry: ActivityResultRegistry
            get() = registry
    }

    private class FakeAvatarPickerRegistry : ActivityResultRegistry() {
        val launchedContract = AtomicReference<ActivityResultContract<*, *>>(null)
        val launchedRequest = AtomicReference<PickVisualMediaRequest>(null)

        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?,
        ) {
            launchedContract.set(contract)
            launchedRequest.set(input as PickVisualMediaRequest)
            dispatchResult(
                requestCode,
                android.net.Uri.parse("content://media/external/images/123") as O,
            )
        }
    }
}
