package com.groupa.domain.usecase

import com.groupa.domain.model.CartItem
import com.groupa.domain.model.Coupon
import com.groupa.domain.model.Deal
import com.groupa.domain.model.Order
import com.groupa.domain.model.Totals
import com.groupa.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDealsUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(): Flow<List<Deal>> = repository.deals
}

class GetDealUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(id: String): Deal? = repository.findDeal(id)
}

class GetCartUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(): Flow<List<CartItem>> = repository.cart
}

class GetOrdersUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(): Flow<List<Order>> = repository.orders
}

class GetCouponsUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(): Flow<List<Coupon>> = repository.coupons
}

class UpdateCartItemUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(dealId: String, quantity: Int) = repository.updateQuantity(dealId, quantity)
}

class RemoveCartItemUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(dealId: String) = repository.removeItem(dealId)
}

class AddToCartUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(dealId: String) = repository.addToCart(dealId)
}

class CalculateTotalsUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(promoApplied: Boolean): Totals = repository.calculateTotals(promoApplied)
}

class PlaceOrderUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(promoApplied: Boolean): Order = repository.placeOrder(promoApplied)
}
