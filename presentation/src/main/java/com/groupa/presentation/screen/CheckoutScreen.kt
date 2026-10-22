package com.groupa.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groupa.coreui.component.CircularIconButton
import com.groupa.coreui.theme.CheckoutBlue
import com.groupa.coreui.theme.DividerGray
import com.groupa.coreui.theme.Gray400
import com.groupa.coreui.theme.Gray500
import com.groupa.coreui.theme.Ink
import com.groupa.coreui.theme.SuccessBackground
import com.groupa.coreui.theme.SuccessBorder
import com.groupa.coreui.theme.SuccessGreen
import com.groupa.coreui.theme.White
import com.groupa.presentation.component.formatMoney
import com.groupa.presentation.viewmodel.GroupUiState

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CheckoutScreen(
    state: GroupUiState,
    onBack: () -> Unit,
    onConfirmAndPay: () -> Unit,
) {
    var paymentMethod by rememberSaveable { mutableStateOf("visa") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
    ) {
        Surface(color = White, shadowElevation = 3.dp) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    CircularIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBack,
                    )
                    Text(
                        text = "Checkout",
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    listOf("Shipping", "Payment", "Review").forEachIndexed { index, step ->
                        val active = index == 1
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(
                                        when {
                                            index == 0 -> SuccessGreen
                                            active -> CheckoutBlue
                                            else -> DividerGray
                                        },
                                        CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (index <= 1) {
                                    Icon(
                                        imageVector = if (index == 0) Icons.Rounded.Check else Icons.Rounded.Payments,
                                        contentDescription = null,
                                        tint = White,
                                        modifier = Modifier.size(16.dp),
                                    )
                                } else {
                                    Text(text = "3", color = White, fontSize = 12.sp)
                                }
                            }
                            Text(
                                text = step,
                                color = if (index <= 1) Ink else Gray400,
                                fontSize = 12.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                SectionCard(title = "Delivery Method") {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(SuccessBackground, RoundedCornerShape(8.dp))
                                    .border(1.dp, SuccessBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 9.dp, vertical = 3.dp),
                            ) {
                                Text(
                                    text = "SELECTED",
                                    color = SuccessGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(13.dp),
                            ) {
                                Surface(shape = CircleShape, color = CheckoutBlue.copy(alpha = 0.09f)) {
                                    Icon(
                                        imageVector = Icons.Rounded.Wallet,
                                        contentDescription = null,
                                        tint = CheckoutBlue,
                                        modifier = Modifier
                                            .padding(11.dp)
                                            .size(22.dp),
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Text(text = "Digital Voucher", color = Ink, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Instant delivery to your email & app wallet.",
                                        color = Gray500,
                                        fontSize = 13.sp,
                                    )
                                    Text(text = "Free", color = SuccessGreen, fontSize = 13.sp)
                                }
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = CheckoutBlue,
                                    modifier = Modifier.size(23.dp),
                                )
                            }
                        }
                    }
                }
            }
            item {
                SectionCard(title = "Payment Method") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PaymentOption(
                            title = "Visa",
                            detail = "•••• 1234 • Expires 12/25",
                            selected = paymentMethod == "visa",
                            onClick = { paymentMethod = "visa" },
                        )
                        PaymentOption(
                            title = "Apple Pay",
                            detail = "Pay with your Apple Wallet",
                            selected = paymentMethod == "apple",
                            onClick = { paymentMethod = "apple" },
                        )
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
                        ) {
                            Row(
                                modifier = Modifier.padding(15.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(13.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFF3F4F6), CircleShape)
                                        .padding(10.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = null,
                                        tint = Gray400,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                                Text(
                                    text = "Add New Card",
                                    color = Gray500,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    modifier = Modifier.padding(bottom = 4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = Gray400,
                        modifier = Modifier.size(17.dp),
                    )
                    Text(
                        text = "Payments are secure and encrypted",
                        color = Gray400,
                        fontSize = 13.sp,
                    )
                }
            }
        }

        Surface(color = White, shadowElevation = 12.dp) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Column {
                    Text(text = "Total Amount", color = Gray500, fontSize = 12.sp)
                    Text(
                        text = formatMoney(state.totals.total),
                        color = Ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    )
                }
                Button(
                    onClick = onConfirmAndPay,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CheckoutBlue),
                ) {
                    Text(
                        text = "Confirm and Pay",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = Ink)
        content()
    }
}

@Composable
private fun PaymentOption(
    title: String,
    detail: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = White,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (selected) CheckoutBlue else DividerGray,
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(15.dp)
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(34.dp)
                    .background(
                        if (selected) CheckoutBlue else Color(0xFF111827),
                        RoundedCornerShape(7.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (title == "Visa") "VISA" else "Pay",
                    color = White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(text = title, color = Ink, fontWeight = FontWeight.SemiBold)
                Text(text = detail, color = Gray500, fontSize = 13.sp)
            }
            Box(
                modifier = Modifier
                    .size(21.dp)
                    .border(2.dp, if (selected) CheckoutBlue else DividerGray, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(CheckoutBlue, CircleShape),
                    )
                }
            }
        }
    }
}
