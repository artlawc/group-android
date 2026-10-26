package com.groupa.domain.repository

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
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    val deals: Flow<List<Deal>>
    val cart: Flow<List<CartItem>>
    val orders: Flow<List<Order>>
    val coupons: Flow<List<Coupon>>
    val messages: Flow<List<GroupMessage>>
    val chats: Flow<List<ChatThread>>
    val chatMessages: Flow<List<ChatMessage>>
    val contacts: Flow<List<Contact>>
    val stocks: Flow<List<StockInfo>>
    val user: Flow<User>

    fun findDeal(id: String): Deal?
    fun updateQuantity(dealId: String, quantity: Int)
    fun removeItem(dealId: String)
    fun addToCart(dealId: String)
    fun calculateTotals(promoApplied: Boolean): Totals
    fun placeOrder(promoApplied: Boolean): Order
    fun signIn(email: String, password: String): User
    fun signInWithProvider(provider: String): User
    fun updateProfile(name: String, email: String, phone: String): User
    fun updateAvatar(avatarUri: String): User
    fun changePassword(currentPassword: String, newPassword: String): Boolean
    fun signOut()
    fun markAllMessagesRead()
    fun markMessageRead(messageId: String)
    fun addMessage(message: GroupMessage)
    fun deleteMessages(messageIds: Set<String>)
    fun sendChatMessage(text: String)
    fun addContact(name: String, phone: String): Contact
}
