package com.groupa.presentation.viewmodel

import com.groupa.data.repository.MockGroupRepository
import com.groupa.domain.usecase.AddContactUseCase
import com.groupa.domain.usecase.AddToCartUseCase
import com.groupa.domain.usecase.CalculateTotalsUseCase
import com.groupa.domain.usecase.ChangePasswordUseCase
import com.groupa.domain.usecase.DeleteMessagesUseCase
import com.groupa.domain.usecase.GetCartUseCase
import com.groupa.domain.usecase.GetChatMessagesUseCase
import com.groupa.domain.usecase.GetChatsUseCase
import com.groupa.domain.usecase.GetContactsUseCase
import com.groupa.domain.usecase.GetCouponsUseCase
import com.groupa.domain.usecase.GetDealsUseCase
import com.groupa.domain.usecase.GetMessagesUseCase
import com.groupa.domain.usecase.GetOrdersUseCase
import com.groupa.domain.usecase.GetStocksUseCase
import com.groupa.domain.usecase.GetUserUseCase
import com.groupa.domain.usecase.MarkAllMessagesReadUseCase
import com.groupa.domain.usecase.MarkMessageReadUseCase
import com.groupa.domain.usecase.PlaceOrderUseCase
import com.groupa.domain.usecase.RemoveCartItemUseCase
import com.groupa.domain.usecase.SendChatMessageUseCase
import com.groupa.domain.usecase.SignInUseCase
import com.groupa.domain.usecase.SignInWithProviderUseCase
import com.groupa.domain.usecase.SignOutUseCase
import com.groupa.domain.usecase.UpdateCartItemUseCase
import com.groupa.domain.usecase.UpdateAvatarUseCase
import com.groupa.domain.usecase.UpdateProfileUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GroupViewModelSignInTest {

    private val repository = MockGroupRepository()
    private val viewModel = GroupViewModel(
        getDeals = GetDealsUseCase(repository),
        getCart = GetCartUseCase(repository),
        getOrders = GetOrdersUseCase(repository),
        getCoupons = GetCouponsUseCase(repository),
        getUser = GetUserUseCase(repository),
        getMessages = GetMessagesUseCase(repository),
        getChats = GetChatsUseCase(repository),
        getChatMessages = GetChatMessagesUseCase(repository),
        getContacts = GetContactsUseCase(repository),
        getStocks = GetStocksUseCase(repository),
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
        deleteMessagesUseCase = DeleteMessagesUseCase(repository),
        sendChatMessageUseCase = SendChatMessageUseCase(repository),
        addContactUseCase = AddContactUseCase(repository),
    )

    @Test
    fun onSignInAcceptsDefaultCredentials() = runBlocking {
        val user = viewModel.signIn("tim@qq.com", "time123")

        assertNotNull(user)
        assertTrue(user!!.isSignedIn)
        assertEquals("tim@qq.com", user.email)
        assertTrue(repository.user.first().isSignedIn)
    }

    @Test
    fun onUpdateAvatarChangesSelectedPhoto() = runBlocking {
        val avatarUri = "file:///data/user/0/com.groupa/files/profile_avatar_123.jpg"

        assertTrue(viewModel.updateAvatar(avatarUri))
        assertEquals(avatarUri, repository.user.first().avatarUri)

        assertFalse(viewModel.updateAvatar(" "))
    }

    @Test
    fun onSignInTrimsSurroundingWhitespace() = runBlocking {
        val user = viewModel.signIn(" tim@qq.com ", " time123 ")

        assertNotNull(user)
        assertEquals("tim@qq.com", user!!.email)
    }

    @Test
    fun onSignInAcceptsEmailCaseInsensitively() = runBlocking {
        val user = viewModel.signIn("TIM@qq.COM", "time123")

        assertNotNull(user)
    }

    @Test
    fun onSignInRejectsIncorrectPassword() {
        val user = viewModel.signIn("tim@qq.com", "wrong-password")

        assertNull(user)
    }

    @Test
    fun onSignInRejectsIncorrectEmail() {
        val user = viewModel.signIn("other@qq.com", "time123")

        assertNull(user)
    }

    @Test
    fun onSignInKeepsDefaultCredentialsAvailableAfterPasswordChange() = runBlocking {
        assertTrue(viewModel.changePassword("time123", "NewPassword1!"))

        assertNotNull(viewModel.signIn("tim@qq.com", "NewPassword1!"))
        assertNotNull(viewModel.signIn("tim@qq.com", "time123"))
    }

    @Test
    fun changePasswordRejectsWeakPasswords() {
        assertFalse(viewModel.changePassword("time123", "password"))
        assertFalse(viewModel.changePassword("time123", "Password1"))
        assertFalse(viewModel.changePassword("time123", "Password!"))
    }

    @Test
    fun changePasswordRejectsReusingCurrentPassword() {
        assertTrue(viewModel.changePassword("time123", "Current1!"))
        assertFalse(viewModel.changePassword("Current1!", "Current1!"))
    }

    @Test
    fun markMessageAsReadClearsIndividualUnreadSigns() = runBlocking {
        assertTrue(repository.chats.first().any { it.unread })
        assertTrue(repository.messages.first().any { it.unread })

        viewModel.markMessageAsRead("gourmet-burger")
        viewModel.markMessageAsRead("price-drop")

        assertFalse(repository.chats.first().any { it.id == "gourmet-burger" && it.unread })
        assertFalse(repository.messages.first().any { it.id == "price-drop" && it.unread })
        assertTrue(repository.messages.first().any { it.id == "new-deal" && it.unread })
    }

    @Test
    fun markAllMessagesAsReadClearsEveryUnreadSign() = runBlocking {
        viewModel.markAllMessagesAsRead()

        assertFalse(repository.chats.first().any { it.unread })
        assertFalse(repository.messages.first().any { it.unread })
    }

    @Test
    fun readingLastUnreadMessageClearsBottomBarUnreadState() = runBlocking {
        val initialState = GroupUiState(
            messages = repository.messages.first(),
            chats = repository.chats.first(),
        )

        assertTrue(initialState.hasUnreadMessages)

        viewModel.markMessageAsRead("gourmet-burger")
        viewModel.markMessageAsRead("price-drop")
        viewModel.markMessageAsRead("new-deal")
        viewModel.markMessageAsRead("body-muscles-update")

        val finalState = GroupUiState(
            messages = repository.messages.first(),
            chats = repository.chats.first(),
        )

        assertFalse(finalState.messages.any { it.unread })
        assertFalse(finalState.chats.any { it.unread })
        assertFalse(finalState.hasUnreadMessages)
    }

    @Test
    fun deleteMessagesRemovesSingleNotification() = runBlocking {
        viewModel.deleteMessages(setOf("price-drop"))

        assertFalse(repository.messages.first().any { it.id == "price-drop" })
    }

    @Test
    fun deleteMessagesRemovesSelectedBatchAcrossChatsAndNotifications() = runBlocking {
        viewModel.deleteMessages(setOf("price-drop", "gourmet-burger", "order-confirmed"))

        val messages = repository.messages.first()
        val chats = repository.chats.first()

        assertFalse(messages.any { it.id in setOf("price-drop", "order-confirmed") })
        assertFalse(chats.any { it.id == "gourmet-burger" })
    }

    @Test
    fun deletingUnreadMessagesClearsBottomBarUnreadState() = runBlocking {
        viewModel.deleteMessages(setOf("price-drop", "new-deal", "body-muscles-update", "gourmet-burger"))

        val state = GroupUiState(
            messages = repository.messages.first(),
            chats = repository.chats.first(),
        )

        assertFalse(state.hasUnreadMessages)
    }

    @Test
    fun bodyMuscleNotificationLinksToBodyMuscleScreen() = runBlocking {
        val message = repository.messages.first().first { it.id == "body-muscles-update" }

        assertEquals("head", message.bodyMuscleId)
    }

    @Test
    fun updateProfileUpdatesRepositoryUser() = runBlocking {
        viewModel.signIn("tim@qq.com", "time123")

        val result = viewModel.updateProfile(
            name = "Tim Updated",
            email = "updated@qq.com",
            phone = "+1 (555) 987-6543",
        )
        val user = repository.user.first()

        assertTrue(result)
        assertEquals("Tim Updated", user.name)
        assertEquals("updated@qq.com", user.email)
        assertEquals("+1 (555) 987-6543", user.phone)
        assertEquals("T", user.avatarInitial)
    }

    @Test
    fun updateProfileRejectsBlankFields() {
        val result = viewModel.updateProfile(
            name = " ",
            email = "updated@qq.com",
            phone = "+1 (555) 987-6543",
        )

        assertFalse(result)
    }
}
