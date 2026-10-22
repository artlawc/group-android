package com.groupa.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groupa.coreui.component.CircularIconButton
import com.groupa.coreui.component.dealImageRes
import com.groupa.coreui.theme.CheckoutBlue
import com.groupa.coreui.theme.DividerGray
import com.groupa.coreui.theme.Gray500
import com.groupa.coreui.theme.Ink
import com.groupa.coreui.theme.Slate500
import com.groupa.coreui.theme.SuccessGreen
import com.groupa.coreui.theme.White
import com.groupa.coreui.R
import com.groupa.domain.model.Order
import com.groupa.presentation.component.formatMoney

@Composable
fun ConfirmationScreen(
    order: Order,
    onBackToExplore: () -> Unit,
    onViewVoucher: () -> Unit,
    onShareVoucher: () -> Unit,
) {
    val steps = listOf(
        Triple(1, "Open the merchant page", "Find ${order.merchant} in GroupGO and show this voucher."),
        Triple(2, "Show your voucher code", "Present code ${order.code} at checkout or redemption."),
        Triple(3, "Enjoy your deal", "The merchant will validate the code and apply your purchase."),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 150.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBackToExplore,
                )
                Text(
                    text = stringResource(R.string.confirmation_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF6F7F8))
                    .padding(horizontal = 20.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(Color(0xFFDCFCE7), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(45.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.confirmation_success),
                    color = Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.confirmation_voucher_ready),
                    color = Slate500,
                    fontSize = 15.sp,
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                ) {
                    Column {
                        Box {
                            Image(
                                painter = painterResource(dealImageRes(order.image)),
                                contentDescription = order.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(152.dp),
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                                        ),
                                    ),
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp),
                            ) {
                                Text(
                                    text = order.merchant,
                                    color = White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                )
                                Text(
                                    text = order.title,
                                    color = White.copy(alpha = 0.86f),
                                    fontSize = 13.sp,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.label_voucher_code),
                                    color = Gray500,
                                    fontSize = 12.sp,
                                )
                                Text(
                                    text = order.code,
                                    color = Ink,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                )
                            }
            Surface(
                modifier = Modifier.clickable(onClick = onShareVoucher),
                shape = CircleShape,
                color = Color(0xFFF1F5F9),
                            ) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = stringResource(R.string.action_share_voucher),
                                    tint = Slate500,
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .size(20.dp),
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(text = "Order ID", color = Gray500, fontSize = 12.sp)
                    Text(
                        text = "#${order.orderId}",
                        color = Ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
                Spacer(
                    Modifier
                        .width(1.dp)
                        .height(38.dp)
                        .background(DividerGray),
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Total Paid", color = Gray500, fontSize = 12.sp)
                    Text(
                        text = formatMoney(order.total),
                        color = Ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(
                    text = "How to Redeem",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                )
                steps.forEach { (number, title, description) ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(29.dp)
                                .background(CheckoutBlue.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = number.toString(),
                                color = CheckoutBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = title,
                                color = Ink,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                            )
                            Text(
                                text = description,
                                color = Slate500,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = White,
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(20.dp),
            ) {
                Button(
                    onClick = onViewVoucher,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CheckoutBlue),
                ) {
                    Text(
                        text = stringResource(R.string.action_view_voucher),
                        fontWeight = FontWeight.Bold,
                    )
                }
                OutlinedButton(
                    onClick = onBackToExplore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(15.dp),
                ) {
                    Text(
                        text = stringResource(R.string.action_back_to_explore),
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                    )
                }
            }
        }
    }
}
