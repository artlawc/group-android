package com.groupa.data.repository

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
import com.groupa.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class MockGroupRepository @Inject constructor() : GroupRepository {
    private val dealList = listOf(
        Deal(
            id = "burger",
            category = "dining",
            title = "Gourmet Burger Combo",
            image = "burger",
            rating = "4.8",
            reviews = 128,
            cuisine = "American Cuisine",
            price = 15.0,
            originalPrice = 25.0,
            discount = "40% OFF",
            distance = "0.5 miles",
            merchant = "Burger Haven Downtown",
            address = "123 Market Street, San Francisco, CA",
            latitude = 37.7749,
            longitude = -122.4194,
            hours = "Closes 10:00 PM",
            highlights = listOf(
                "Includes one premium gourmet burger of your choice (Beef, Chicken, or Veggie).",
                "Comes with a side of hand-cut fries and a house dipping sauce.",
                "Includes one fountain drink or iced tea.",
            ),
            about = "Experience the ultimate burger satisfaction at Burger Haven. Our patties are made from 100% locally sourced beef, grilled to perfection, and topped with the freshest ingredients. This exclusive combo deal is perfect for lunch or dinner.",
        ),
        Deal(
            id = "spa",
            category = "beauty",
            title = "Luxury Spa Day Package",
            image = "spa",
            rating = "4.9",
            reviews = 84,
            cuisine = "Spa & Wellness",
            price = 45.0,
            originalPrice = 100.0,
            discount = "55% OFF",
            distance = "1.2 miles",
            merchant = "Serenity Spa & Wellness",
            address = "488 Pine Avenue, San Francisco, CA",
            latitude = 37.7912,
            longitude = -122.4039,
            hours = "Closes 8:00 PM",
            highlights = listOf(
                "Full-day access to the sauna, steam room, and relaxation lounge.",
                "Includes a 60-minute signature aromatherapy massage.",
                "Complimentary herbal tea and light snacks included.",
            ),
            about = "Indulge in a full day of pure relaxation at Serenity Spa & Wellness. Your package includes unlimited access to premium facilities, a rejuvenating signature massage, and a calming atmosphere designed to melt stress away.",
        ),
        Deal(
            id = "gym",
            category = "activities",
            title = "1 Month Gym Access",
            image = "gym",
            rating = "4.5",
            reviews = 42,
            cuisine = "Fitness",
            price = 29.99,
            originalPrice = 42.99,
            discount = "30% OFF",
            distance = "2.0 miles",
            merchant = "FitZone Gym",
            address = "77 Howard Street, San Francisco, CA",
            latitude = 37.7833,
            longitude = -122.3964,
            hours = "Open 24 hours",
            highlights = listOf(
                "Unlimited gym access for a full 30 days.",
                "Includes one complimentary personal training session.",
                "Locker room and shower access included.",
            ),
            about = "Kickstart your fitness journey with a full month of access at FitZone Gym. Enjoy state-of-the-art equipment, group classes, and a motivating community.",
        ),
    )

    private val dealMap = dealList.associateBy(Deal::id)
    private val dealFlow = MutableStateFlow(dealList)
    private val cartFlow = MutableStateFlow(
        listOf(
            CartItem("burger", 1),
            CartItem("spa", 1),
        ),
    )
    private val orderFlow = MutableStateFlow(
        listOf(
            Order(
                orderId = "ORD-883920",
                dealId = "gym",
                title = "1 Month Gym Access",
                merchant = "FitZone Gym",
                address = "77 Howard Street, San Francisco, CA",
                code = "FZ-3920",
                total = 29.99,
                date = "Oct 1, 2026",
                status = "Completed",
                image = "gym",
            ),
        ),
    )

    private val couponFlow = MutableStateFlow(
        listOf(
            Coupon(
                id = "groupgo-10",
                title = "GroupGO New Member Coupon",
                code = "GROUPGO10",
                value = 10.0,
                minimumSpend = 50.0,
                expiresOn = "2026-12-31",
                isExpired = false,
                terms = listOf(
                    "Valid for first-time purchasers only.",
                    "Cannot be combined with other coupons.",
                ),
            ),
            Coupon(
                id = "spa-15",
                title = "Weekend Spa Coupon",
                code = "SPASAVE15",
                value = 15.0,
                minimumSpend = 80.0,
                expiresOn = "2026-11-20",
                isExpired = false,
                terms = listOf(
                    "Valid on weekend spa bookings.",
                    "One coupon per appointment.",
                ),
            ),
            Coupon(
                id = "pizza-8",
                title = "Family Pizza Coupon",
                code = "PIZZA8",
                value = 8.0,
                minimumSpend = 40.0,
                expiresOn = "2026-10-05",
                isExpired = true,
                terms = listOf(
                    "Valid for dine-in orders only.",
                    "This coupon can no longer be redeemed.",
                ),
            ),
            Coupon(
                id = "coffee-5",
                title = "Downtown Coffee Coupon",
                code = "COFFEE5",
                value = 5.0,
                minimumSpend = 25.0,
                expiresOn = "2026-09-01",
                isExpired = true,
                terms = listOf(
                    "Valid before 4 PM.",
                    "This coupon can no longer be redeemed.",
                ),
            ),
        ),
    )

    private val messageFlow = MutableStateFlow(
        listOf(
            GroupMessage("price-drop", "Price Drop!", "The Gourmet Burger Combo you liked is now 50% off for the next 2 hours.", "2m ago", false, true, "G"),
            GroupMessage("new-deal", "New Deal Near You", "Golden Spa just posted a new weekend special. Check it out before it's gone!", "15m ago", false, true, "G"),
            GroupMessage(
                id = "order-confirmed",
                title = "Order Confirmed",
                preview = "Your purchase for Yoga Classes - 5 Sessions was successful. View your receipt.",
                time = "2h ago",
                isChat = false,
                unread = false,
                avatarLabel = "O",
                orderId = "ORD-883920",
            ),
            GroupMessage("welcome-reward", "Welcome Reward", "Thanks for joining! Here is a $10 coupon for your first purchase.", "1d ago", false, false, "W"),
            GroupMessage("rate-experience", "Rate Your Experience", "How was your visit to Downtown Coffee? Leave a review to help others.", "2d ago", false, false, "R"),
            GroupMessage("deal-expiring", "Deal Expiring", "The Family Pizza Night deal saved in your wishlist expires in 24 hours.", "3d ago", false, false, "D"),
            GroupMessage(
                id = "body-muscles-update",
                title = "Body Muscles",
                preview = "Track intensity across your front and back muscle groups.",
                time = "3m ago",
                isChat = false,
                unread = true,
                avatarLabel = "M",
                bodyMuscleId = "head",
            ),
            GroupMessage(
                id = "stock-update",
                title = "Stock Update",
                preview = "GOUP is up 4.33% today. Tap to view the live chart.",
                time = "5m ago",
                isChat = false,
                unread = false,
                avatarLabel = "S",
                stockId = "goup",
            ),
            GroupMessage(
                id = "car-3d-dashboard",
                title = "Car 3D Dashboard",
                preview = "Explore the concept coupe in an interactive 3D dashboard.",
                time = "1m ago",
                isChat = false,
                unread = true,
                avatarLabel = "C",
                car3dId = "carconcept",
            ),
        ),
    )

    private val chatFlow = MutableStateFlow(
        listOf(
            ChatThread("gourmet-burger", "Gourmet Burger Bar", true, "G", unread = true),
            ChatThread("fitzone", "FitZone Gym", false, "F"),
        ),
    )

    private val chatMessageFlow = MutableStateFlow(
        listOf(
            ChatMessage("message-1", "Hi there! Thanks for asking about our burger deal. Yes, it includes fries.", "10:23 AM", false),
            ChatMessage("message-2", "Great! Is this available for dine-in only or takeout as well?", "10:25 AM", true),
            ChatMessage("message-3", "It's available for both! Just show the voucher code when you arrive.", "10:26 AM", false),
            ChatMessage("message-4", "Awesome, see you soon!", "10:28 AM", true),
        ),
    )

    private val defaultEmail = "tim@qq.com"

    private val contactFlow = MutableStateFlow(
        ('A'..'Z').mapIndexed { index, initial ->
            val sequence = (index + 1).toString().padStart(4, '0')
            Contact(
                id = "contact-$initial",
                name = "$initial Contact",
                phone = "+1 (555) 010-$sequence",
            )
        },
    )

    private val stockFlow = MutableStateFlow(
        listOf(
            StockInfo(
                id = "goup",
                symbol = "GOUP",
                name = "GroupGO Live Commerce",
                price = 28.46,
                change = 1.18,
                percentChange = 4.33,
                history = listOf(24.10, 24.80, 25.35, 25.10, 25.80, 26.20, 26.00, 26.70, 27.10, 27.40),
            ),
            StockInfo(
                id = "grub",
                symbol = "GRUB",
                name = "GroupGO Retail Group",
                price = 19.20,
                change = -0.35,
                percentChange = -1.79,
                history = listOf(20.10, 19.90, 20.20, 19.80, 19.60, 19.95, 19.70, 19.40, 19.25, 19.20),
            ),
        ),
    )
    private val defaultPassword = "time123"
    private var currentPassword = defaultPassword

    private val userFlow = MutableStateFlow(
        User(
            name = "Tim",
            email = "tim@qq.com",
            phone = "+1 (555) 123-4567",
            memberSince = "August 2023",
            totalSavings = 342.50,
            dealsSaved = 24,
            avatarInitial = "T",
            isSignedIn = false,
        ),
    )

    override val deals: Flow<List<Deal>> = dealFlow.asStateFlow()
    override val cart: Flow<List<CartItem>> = cartFlow.asStateFlow()
    override val orders: Flow<List<Order>> = orderFlow.asStateFlow()
    override val coupons: Flow<List<Coupon>> = couponFlow.asStateFlow()
    override val messages: Flow<List<GroupMessage>> = messageFlow.asStateFlow()
    override val chats: Flow<List<ChatThread>> = chatFlow.asStateFlow()
    override val chatMessages: Flow<List<ChatMessage>> = chatMessageFlow.asStateFlow()
    override val contacts: Flow<List<Contact>> = contactFlow.asStateFlow()
    override val stocks: Flow<List<StockInfo>> = stockFlow.asStateFlow()
    override val user: Flow<User> = userFlow.asStateFlow()

    override fun findDeal(id: String): Deal? = dealMap[id]

    override fun updateQuantity(dealId: String, quantity: Int) {
        if (quantity <= 0) {
            removeItem(dealId)
            return
        }
        cartFlow.update { items -> items.map { if (it.dealId == dealId) it.copy(quantity = quantity) else it } }
    }

    override fun removeItem(dealId: String) {
        cartFlow.update { items -> items.filterNot { it.dealId == dealId } }
    }

    override fun addToCart(dealId: String) {
        cartFlow.update { items ->
            val existing = items.firstOrNull { it.dealId == dealId }
            if (existing == null) {
                items + CartItem(dealId, 1)
            } else {
                items.map { if (it.dealId == dealId) it.copy(quantity = it.quantity + 1) else it }
            }
        }
    }

    override fun calculateTotals(promoApplied: Boolean): Totals {
        val items = cartFlow.value
        val subtotal = items.sumOf { item -> (dealMap[item.dealId]?.price ?: 0.0) * item.quantity }
        val savings = items.sumOf { item ->
            val deal = dealMap[item.dealId] ?: return@sumOf 0.0
            (deal.originalPrice - deal.price) * item.quantity
        }
        val taxes = subtotal * 0.075
        val promoDiscount = if (promoApplied) subtotal * 0.1 else 0.0
        return Totals(
            subtotal = subtotal,
            taxes = taxes,
            savings = savings,
            promoDiscount = promoDiscount,
            total = subtotal + taxes - promoDiscount,
        )
    }

    override fun placeOrder(promoApplied: Boolean): Order {
        val totals = calculateTotals(promoApplied)
        val primaryDeal = dealMap[cartFlow.value.firstOrNull()?.dealId] ?: dealList.first()
        val number = (100000..999999).random().toString()
        val initials = primaryDeal.title
            .split(" ")
            .filter { it.firstOrNull()?.isLetter() == true }
            .take(2)
            .joinToString("") { it.first().uppercase() }
        val order = Order(
            orderId = "ORD-$number",
            dealId = primaryDeal.id,
            title = primaryDeal.title,
            merchant = primaryDeal.merchant,
            address = primaryDeal.address,
            code = "$initials-${number.takeLast(4)}",
            total = totals.total.roundToIntHundredths(),
            date = "Today",
            status = "Completed",
            image = primaryDeal.image,
        )
        orderFlow.update { current -> listOf(order) + current }
        messageFlow.update { current ->
            listOf(
                GroupMessage(
                    id = "order-${order.orderId}",
                    title = "Order Confirmed",
                    preview = "Your purchase for ${order.title} was successful. View your receipt.",
                    time = "Just now",
                    isChat = false,
                    unread = true,
                    avatarLabel = order.title.firstOrNull()?.uppercase() ?: "O",
                    orderId = order.orderId,
                ),
            ) + current
        }
        cartFlow.value = emptyList()
        return order
    }

    override fun signIn(email: String, password: String): User {
        val normalizedEmail = email.trim()
        val normalizedPassword = password.trim()
        val isDefaultUser = normalizedEmail.equals(defaultEmail, ignoreCase = true)
        val isKnownPassword = normalizedPassword == defaultPassword || normalizedPassword == currentPassword
        if (!isDefaultUser || !isKnownPassword) {
            throw IllegalArgumentException("Enter tim@qq.com and the current password.")
        }
        val signedInUser = userFlow.value.copy(email = normalizedEmail, isSignedIn = true)
        userFlow.value = signedInUser
        return signedInUser
    }

    override fun updateProfile(name: String, email: String, phone: String): User {
        if (name.isBlank() || email.isBlank() || phone.isBlank()) {
            throw IllegalArgumentException("All profile fields are required.")
        }
        val updatedUser = userFlow.value.copy(
            name = name.trim(),
            email = email.trim(),
            phone = phone.trim(),
            avatarInitial = name.trim().firstOrNull()?.uppercase() ?: userFlow.value.avatarInitial,
        )
        userFlow.value = updatedUser
        return updatedUser
    }

    override fun updateAvatar(avatarUri: String): User {
        val normalizedAvatarUri = avatarUri.trim()
        if (normalizedAvatarUri.isEmpty()) {
            throw IllegalArgumentException("Choose a profile photo.")
        }
        val updatedUser = userFlow.value.copy(avatarUri = normalizedAvatarUri)
        userFlow.value = updatedUser
        return updatedUser
    }

    override fun changePassword(currentPassword: String, newPassword: String): Boolean {
        if (currentPassword != this.currentPassword) {
            throw IllegalArgumentException("Your current password is incorrect.")
        }
        val hasMinLength = newPassword.length >= 8
        val hasNumber = newPassword.any { it.isDigit() }
        val hasSpecialCharacter = newPassword.any { !it.isLetterOrDigit() }
        if (!hasMinLength || !hasNumber || !hasSpecialCharacter) {
            throw IllegalArgumentException("Password must have 8 characters, a number, and a special character.")
        }
        if (newPassword == currentPassword) {
            throw IllegalArgumentException("Your new password must be different.")
        }
        this.currentPassword = newPassword
        return true
    }

    override fun signInWithProvider(provider: String): User {
        if (provider.isBlank()) {
            throw IllegalArgumentException("Choose a sign-in provider.")
        }
        val signedInUser = userFlow.value.copy(email = "$provider-user@example.com", isSignedIn = true)
        userFlow.value = signedInUser
        return signedInUser
    }

    override fun signOut() {
        userFlow.value = userFlow.value.copy(email = "", isSignedIn = false)
    }

    override fun markAllMessagesRead() {
        messageFlow.update { messages -> messages.map { it.copy(unread = false) } }
        chatFlow.update { chats -> chats.map { it.copy(unread = false) } }
    }

    override fun markMessageRead(messageId: String) {
        messageFlow.update { messages ->
            messages.map { message ->
                if (message.id == messageId) message.copy(unread = false) else message
            }
        }
        chatFlow.update { chats ->
            chats.map { chat ->
                if (chat.id == messageId) chat.copy(unread = false) else chat
            }
        }
    }

    override fun addMessage(message: GroupMessage) {
        messageFlow.update { messages ->
            if (messages.any { it.id == message.id }) {
                messages
            } else {
                listOf(message) + messages
            }
        }
    }

    override fun deleteMessages(messageIds: Set<String>) {
        if (messageIds.isEmpty()) return

        messageFlow.update { messages -> messages.filterNot { it.id in messageIds } }
        chatFlow.update { chats -> chats.filterNot { it.id in messageIds } }
    }

    override fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        chatMessageFlow.update { messages ->
            messages + ChatMessage(
                id = "message-${messages.size + 1}",
                text = text.trim(),
                timestamp = "Now",
                fromUser = true,
            )
        }
    }

    override fun addContact(name: String, phone: String): Contact {
        val trimmedName = name.trim()
        val trimmedPhone = phone.trim()
        if (trimmedName.isEmpty() || trimmedPhone.isEmpty()) {
            throw IllegalArgumentException("Contact name and phone are required.")
        }
        val isDuplicate = contactFlow.value.any { contact ->
            contact.name.equals(trimmedName, ignoreCase = true) && contact.phone == trimmedPhone
        }
        if (isDuplicate) {
            throw IllegalArgumentException("This contact already exists.")
        }
        val contact = Contact(
            id = "contact-${System.currentTimeMillis()}",
            name = trimmedName,
            phone = trimmedPhone,
        )
        contactFlow.update { contacts ->
            (contacts + contact).sortedBy { it.name.lowercase() }
        }
        return contact
    }

    private fun Double.roundToIntHundredths(): Double = (this * 100.0).roundToInt() / 100.0
}
