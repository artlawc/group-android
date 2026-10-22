package com.groupa.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groupa.coreui.component.CircularIconButton
import com.groupa.coreui.component.StatusPill
import com.groupa.coreui.component.dealImageRes
import com.groupa.coreui.R
import com.groupa.coreui.theme.AppBackground
import com.groupa.coreui.theme.DividerGray
import com.groupa.coreui.theme.Gray400
import com.groupa.coreui.theme.Gray500
import com.groupa.coreui.theme.GroupBlue
import com.groupa.coreui.theme.Ink
import com.groupa.coreui.theme.SuccessGreen
import com.groupa.coreui.theme.White
import com.groupa.presentation.component.formatMoney
import com.groupa.presentation.viewmodel.GroupUiState

@Composable
fun PurchasesScreen(
    state: GroupUiState,
    showBack: Boolean,
    onBack: () -> Unit,
    onQuantityChange: (String, Int) -> Unit,
    onRemove: (String) -> Unit,
    onPromoInputChange: (String) -> Unit,
    onApplyPromo: () -> Unit,
    onCheckout: () -> Unit,
    onViewVoucher: (String) -> Unit,
    onBuyAgain: (String) -> Unit,
    onOpenDeal: (String) -> Unit,
    initialTab: String = "Cart",
) {
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }
    val dealsById = state.deals.associateBy { it.id }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 115.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    if (showBack) {
                        CircularIconButton(
                            icon = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back to Explore",
                            onClick = onBack,
                        )
                    }
                    Text(
                        text = stringResource(R.string.title_purchases),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Ink,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(White)
                        .border(1.dp, DividerGray, RoundedCornerShape(18.dp)),
                ) {
                    listOf(
                        stringResource(R.string.tab_cart_count, state.cart.size) to "Cart",
                        stringResource(R.string.tab_orders) to "Orders",
                    ).forEach { (label, tab) ->
                        val selected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (selected) GroupBlue else White)
                                .clickable { selectedTab = tab }
                                .padding(vertical = 13.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                        Text(
                            text = label,
                                color = if (selected) White else Gray400,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }
        }

        if (selectedTab == "Cart") {
            if (state.cart.isEmpty()) {
                item { EmptyCart() }
            } else {
                items(state.cart, key = { it.dealId }) { cartItem ->
                    val deal = dealsById[cartItem.dealId] ?: return@items
                    CartItemCard(
                        title = deal.title,
                        merchant = deal.merchant,
                        image = deal.image,
                        price = deal.price,
                        originalPrice = deal.originalPrice,
                        quantity = cartItem.quantity,
                        onRemove = { onRemove(deal.id) },
                        onQuantityChange = { quantity -> onQuantityChange(deal.id, quantity) },
                        onOpenDetail = { onOpenDeal(deal.id) },
                    )
                }
                item {
                    PromoCard(
                        value = state.promoInput,
                        applied = state.promoApplied,
                        onValueChange = onPromoInputChange,
                        onApply = onApplyPromo,
                    )
                }
                item {
                    SummaryCard(
                        totals = state.totals,
                        promoApplied = state.promoApplied,
                        onCheckout = onCheckout,
                    )
                }
            }
        } else {
            item {
                Text(
                    text = "ORDER HISTORY",
                    color = Gray400,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            items(state.orders, key = { it.orderId }) { order ->
                OrderCard(
                    orderId = order.orderId,
                    title = order.title,
                    image = order.image,
                    date = order.date,
                    status = order.status,
                    total = order.total,
                    onViewVoucher = { onViewVoucher(order.orderId) },
                    onBuyAgain = { onBuyAgain(order.dealId) },
                    onOpenDetail = { onOpenDeal(order.dealId) },
                )
            }
        }
    }
}

@Composable
private fun EmptyCart() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 90.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Your cart is empty",
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
        )
        Text(
            text = "Browse deals and add something you love.",
            color = Gray500,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun CartItemCard(
    title: String,
    merchant: String,
    image: String,
    price: Double,
    originalPrice: Double,
    quantity: Int,
    onRemove: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onOpenDetail: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = White,
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
                Image(
                    painter = painterResource(dealImageRes(image)),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(94.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpenDetail),
                )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onOpenDetail),
                    )
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Remove $title",
                        tint = Gray400,
                        modifier = Modifier
                            .padding(top = 3.dp, end = 3.dp)
                            .size(18.dp)
                            .clickable(onClick = onRemove),
                    )
                }
                Text(
                    text = merchant,
                    color = Gray500,
                    fontSize = 13.sp,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = formatMoney(price),
                            color = Ink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        )
                        Text(
                            text = formatMoney(originalPrice),
                            color = Gray400,
                            fontSize = 12.sp,
                            textDecoration = TextDecoration.LineThrough,
                        )
                    }
                    QuantityStepper(
                        quantity = quantity,
                        onQuantityChange = onQuantityChange,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuantityStepper(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppBackground)
            .border(1.dp, DividerGray, RoundedCornerShape(12.dp))
            .padding(horizontal = 5.dp),
    ) {
        IconButton(
            onClick = { onQuantityChange(quantity - 1) },
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Remove,
                contentDescription = "Decrease quantity",
                tint = Gray400,
                modifier = Modifier.size(17.dp),
            )
        }
        Text(
            text = quantity.toString(),
            color = Ink,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
        IconButton(
            onClick = { onQuantityChange(quantity + 1) },
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Increase quantity",
                tint = GroupBlue,
                modifier = Modifier.size(17.dp),
            )
        }
    }
}

@Composable
private fun PromoCard(
    value: String,
    applied: Boolean,
    onValueChange: (String) -> Unit,
    onApply: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = White,
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Promo Code",
                color = Gray500,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(text = "Enter code") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppBackground,
                        unfocusedContainerColor = AppBackground,
                    ),
                )
                Button(
                    onClick = onApply,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Ink),
                    modifier = Modifier.height(52.dp),
                ) {
                    Text(text = "Apply")
                }
            }
            if (applied) {
                Text(
                    text = "GROUPGO10 applied • 10% off subtotal",
                    color = SuccessGreen,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    totals: com.groupa.domain.model.Totals,
    promoApplied: Boolean,
    onCheckout: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = White,
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
            )
            SummaryRow("Subtotal", formatMoney(totals.subtotal))
            SummaryRow("Taxes & Fees", formatMoney(totals.taxes))
            SummaryRow(
                label = "Total Savings",
                value = "-${formatMoney(totals.savings)}",
                green = true,
            )
            if (promoApplied) {
                SummaryRow(
                    label = "Promo Discount",
                    value = "-${formatMoney(totals.promoDiscount)}",
                    green = true,
                )
            }
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(DividerGray),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                )
                Text(
                    text = formatMoney(totals.total),
                    color = Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }
            Button(
                onClick = onCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GroupBlue),
            ) {
                Text(
                    text = "Proceed to Checkout",
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, green: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = if (green) SuccessGreen else Gray500,
            fontSize = 14.sp,
        )
        Text(
            text = value,
            color = if (green) SuccessGreen else Gray500,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun OrderCard(
    orderId: String,
    title: String,
    image: String,
    date: String,
    status: String,
    total: Double,
    onViewVoucher: () -> Unit,
    onBuyAgain: () -> Unit,
    onOpenDetail: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = White,
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Ordered on $date",
                    color = Gray500,
                    fontSize = 12.sp,
                )
                StatusPill(status)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Image(
                    painter = painterResource(dealImageRes(image)),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenDetail),
                )
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        modifier = Modifier.clickable(onClick = onOpenDetail),
                    )
                    Text(
                        text = "#$orderId",
                        color = Gray500,
                        fontSize = 12.sp,
                    )
                    Text(
                        text = formatMoney(total),
                        color = Ink,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onViewVoucher,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = White),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = "View Voucher",
                        color = GroupBlue,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Button(
                    onClick = onBuyAgain,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GroupBlue),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = "Buy Again",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
