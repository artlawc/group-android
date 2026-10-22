package com.groupa.navigation

import android.net.Uri

object GroupRoutes {
    const val SPLASH = "splash"
    const val SIGN_IN = "signIn"
    const val HOME = "home"
    const val DEAL_DETAIL = "deal/{dealId}"
    const val PURCHASES = "purchases"
    const val ORDERS = "orders"
    const val CHECKOUT = "checkout"
    const val CONFIRMATION = "confirmation/{orderId}"
    const val MESSAGES = "messages"
    const val CONTACTS = "contacts"
    const val PROFILE = "profile"
    const val ACCOUNT = "account"
    const val PAYMENT = "payment"
    const val EDIT_PROFILE = "editProfile"
    const val CHANGE_PASSWORD = "changePassword"
    const val LANGUAGE = "language"
    const val CURRENCY = "currency"
    const val COUPONS = "coupons"
    const val COUPON_DETAIL = "couponDetail/{couponId}"
    const val INVITE = "invite"
    const val SUPPORT = "support"
    const val CHAT = "chat/{threadId}"
    const val STOCK_INFO = "stockInfo/{stockId}"
    const val BODY_MUSCLES = "bodyMuscles/{muscleId}"
    const val CAR_3D = "car3d/{carId}"
    const val MAP = "map?target={target}&label={label}"

    val bottomRoutes = setOf(HOME, MESSAGES, PURCHASES, ORDERS, PROFILE, CONFIRMATION)

    fun dealDetail(dealId: String) = "deal/$dealId"
    fun confirmation(orderId: String) = "confirmation/$orderId"
    fun chat(threadId: String) = "chat/$threadId"
    fun stockInfo(stockId: String) = "stockInfo/$stockId"
    fun bodyMuscles(muscleId: String) = "bodyMuscles/$muscleId"
    fun car3d(carId: String) = "car3d/$carId"
    fun couponDetail(couponId: String) = "couponDetail/$couponId"
    fun map(target: String, label: String) =
        "map?target=${Uri.encode(target)}&label=${Uri.encode(label)}"
}