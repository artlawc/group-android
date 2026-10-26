package com.groupa.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.groupa.domain.model.CartItem
import com.groupa.domain.model.ChatMessage
import com.groupa.domain.model.ChatThread
import com.groupa.domain.model.Contact
import com.groupa.domain.model.Coupon
import com.groupa.domain.model.Deal
import com.groupa.domain.model.GroupMessage
import com.groupa.domain.model.Order
import com.groupa.domain.model.StockInfo
import com.groupa.domain.model.Totals
import com.groupa.domain.model.User
import com.groupa.domain.usecase.AddMessageUseCase
import com.groupa.domain.usecase.AddToCartUseCase
import com.groupa.domain.usecase.AddContactUseCase
import com.groupa.domain.usecase.CalculateTotalsUseCase
import com.groupa.domain.usecase.GetCartUseCase
import com.groupa.domain.usecase.GetChatMessagesUseCase
import com.groupa.domain.usecase.GetChatsUseCase
import com.groupa.domain.usecase.GetContactsUseCase
import com.groupa.domain.usecase.GetStocksUseCase
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
import com.groupa.domain.usecase.ChangePasswordUseCase
import com.groupa.domain.usecase.DeleteMessagesUseCase
import com.groupa.domain.usecase.UpdateProfileUseCase
import com.groupa.domain.usecase.UpdateAvatarUseCase
import com.groupa.domain.usecase.SignInWithProviderUseCase
import com.groupa.domain.usecase.SignOutUseCase
import com.groupa.domain.usecase.UpdateCartItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupUiState(
    val deals: List<Deal> = emptyList(),
    val cart: List<CartItem> = emptyList(),
    val orders: List<Order> = emptyList(),
    val coupons: List<Coupon> = emptyList(),
    val messages: List<GroupMessage> = emptyList(),
    val chats: List<ChatThread> = emptyList(),
    val chatMessages: List<ChatMessage> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val stocks: List<StockInfo> = emptyList(),
    val user: User = User("", "", "", 0.0, 0, "", false),
    val promoInput: String = "",
    val promoApplied: Boolean = false,
    val totals: Totals = Totals(0.0, 0.0, 0.0, 0.0, 0.0),
) {
    val hasUnreadMessages: Boolean
        get() = messages.any { it.unread } || chats.any { it.unread }
}

private data class CommerceState(
    val deals: List<Deal>,
    val cart: List<CartItem>,
    val orders: List<Order>,
    val coupons: List<Coupon>,
    val promoInput: String,
    val promoApplied: Boolean,
)

private data class AccountState(
    val user: User,
    val messages: List<GroupMessage>,
    val chats: List<ChatThread>,
    val chatMessages: List<ChatMessage>,
    val contacts: List<Contact>,
)

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val getDeals: GetDealsUseCase,
    getCart: GetCartUseCase,
    getOrders: GetOrdersUseCase,
    getCoupons: GetCouponsUseCase,
    getUser: GetUserUseCase,
    getMessages: GetMessagesUseCase,
    getChats: GetChatsUseCase,
    getChatMessages: GetChatMessagesUseCase,
    getContacts: GetContactsUseCase,
    getStocks: GetStocksUseCase,
    private val addToCart: AddToCartUseCase,
    private val updateCartItem: UpdateCartItemUseCase,
    private val removeCartItem: RemoveCartItemUseCase,
    private val calculateTotals: CalculateTotalsUseCase,
    private val placeOrder: PlaceOrderUseCase,
    private val signInUseCase: SignInUseCase,
    private val signInWithProviderUseCase: SignInWithProviderUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val updateAvatarUseCase: UpdateAvatarUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val markAllMessagesReadUseCase: MarkAllMessagesReadUseCase,
    private val markMessageReadUseCase: MarkMessageReadUseCase,
    private val addMessageUseCase: AddMessageUseCase,
    private val deleteMessagesUseCase: DeleteMessagesUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val addContactUseCase: AddContactUseCase,
) : ViewModel() {

    private val promoInputFlow = MutableStateFlow("")
    private val promoAppliedFlow = MutableStateFlow(false)

    private val commerceInputsFlow: Flow<Triple<String, Boolean, List<Coupon>>> = combine(
        promoInputFlow,
        promoAppliedFlow,
        getCoupons(),
    ) { promoInput, promoApplied, coupons ->
        Triple(promoInput, promoApplied, coupons)
    }

    private val commerceFlow: Flow<CommerceState> = combine(
        getDeals(),
        getCart(),
        getOrders(),
        commerceInputsFlow,
    ) { deals, cart, orders, commerceInputs ->
        CommerceState(
            deals = deals,
            cart = cart,
            orders = orders,
            coupons = commerceInputs.third,
            promoInput = commerceInputs.first,
            promoApplied = commerceInputs.second,
        )
    }

    private val accountFlow: Flow<AccountState> = combine(
        getUser(),
        getMessages(),
        getChats(),
        getChatMessages(),
        getContacts(),
    ) { user, messages, chats, chatMessages, contacts ->
        AccountState(user, messages, chats, chatMessages, contacts)
    }

    private val stocksFlow: Flow<List<StockInfo>> = getStocks()

    val state: StateFlow<GroupUiState> = combine(
        commerceFlow,
        accountFlow,
        stocksFlow,
    ) { commerce, account, stockList ->
        GroupUiState(
            deals = commerce.deals,
            cart = commerce.cart,
            orders = commerce.orders,
            coupons = commerce.coupons,
            messages = account.messages,
            chats = account.chats,
            chatMessages = account.chatMessages,
            contacts = account.contacts,
            stocks = stockList,
            user = account.user,
            promoInput = commerce.promoInput,
            promoApplied = commerce.promoApplied,
            totals = calculateTotals(commerce.promoApplied),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GroupUiState(),
    )

    fun onPromoInputChange(value: String) {
        promoInputFlow.value = value
        if (value.isNotBlank() && value.equals(PROMO_CODE, ignoreCase = true)) {
            promoAppliedFlow.value = true
        }
    }

    fun applyPromo() {
        promoAppliedFlow.value = promoInputFlow.value.equals(PROMO_CODE, ignoreCase = true)
    }

    fun addDealToCart(dealId: String) {
        addToCart(dealId)
    }

    fun changeQuantity(dealId: String, quantity: Int) {
        updateCartItem(dealId, quantity)
    }

    fun removeItem(dealId: String) {
        removeCartItem(dealId)
    }

    fun purchaseCart(): Order {
        val order = placeOrder(promoAppliedFlow.value)
        promoAppliedFlow.value = false
        promoInputFlow.value = ""
        return order
    }

    fun signIn(email: String, password: String): User? {
        return runCatching { signInUseCase(email, password) }.getOrNull()
    }

    fun signInWithProvider(provider: String): User? {
        return runCatching { signInWithProviderUseCase(provider) }.getOrNull()
    }

    fun updateProfile(name: String, email: String, phone: String): Boolean {
        return runCatching { updateProfileUseCase(name, email, phone) }.isSuccess
    }

    fun updateAvatar(
        avatarUri: String,
        copyPickedAvatar: (suspend () -> String?)? = null,
    ): Boolean {
        val result = runCatching { updateAvatarUseCase(avatarUri) }.isSuccess
        if (result && copyPickedAvatar != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val storedAvatarUri = copyPickedAvatar()
                if (!storedAvatarUri.isNullOrBlank()) {
                    runCatching { updateAvatarUseCase(storedAvatarUri) }
                }
            }
        }
        return result
    }

    fun changePassword(currentPassword: String, newPassword: String): Boolean {
        return runCatching { changePasswordUseCase(currentPassword, newPassword) }.isSuccess
    }

    fun signOutUser() {
        signOutUseCase()
    }

    fun markAllMessagesAsRead() {
        markAllMessagesReadUseCase()
    }

    fun markMessageAsRead(messageId: String) {
        markMessageReadUseCase(messageId)
    }

    fun addMessage(message: GroupMessage) {
        addMessageUseCase(message)
    }

    fun deleteMessages(messageIds: Set<String>) {
        deleteMessagesUseCase(messageIds)
    }

    fun sendChat(text: String) {
        sendChatMessageUseCase(text)
    }

    fun addContact(name: String, phone: String): Boolean {
        return runCatching { addContactUseCase(name, phone) }.isSuccess
    }

    private companion object {
        const val PROMO_CODE = "GROUPGO10"
    }
}
