package com.groupa.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groupa.coreui.component.CircularIconButton
import com.groupa.coreui.component.DiscountPill
import com.groupa.coreui.component.dealImageRes
import com.groupa.coreui.theme.DividerGray
import com.groupa.coreui.theme.Gray400
import com.groupa.coreui.theme.Gray500
import com.groupa.coreui.theme.GroupBlue
import com.groupa.coreui.theme.Ink
import com.groupa.coreui.theme.SuccessGreen
import com.groupa.coreui.theme.White
import com.groupa.domain.model.Deal
import com.groupa.presentation.component.formatMoney

@Composable
fun DealDetailScreen(
    deal: Deal,
    onBack: () -> Unit,
    onBuyNow: (String) -> Unit,
    onOpenMap: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Box {
                Image(
                    painter = painterResource(dealImageRes(deal.image)),
                    contentDescription = deal.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    CircularIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBack,
                    )
                    CircularIconButton(
                        icon = Icons.Rounded.Favorite,
                        contentDescription = "Save deal",
                        onClick = {},
                        contentTint = GroupBlue,
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-36).dp),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = White,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = deal.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Ink,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = Color(0xFFFACC15),
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = deal.rating,
                                color = Ink,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                            )
                            Text(
                                text = "  (${deal.reviews})  •  ${deal.cuisine}",
                                color = Gray400,
                                fontSize = 13.sp,
                            )
                        }
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = formatMoney(deal.price),
                                    color = GroupBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 30.sp,
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = formatMoney(deal.originalPrice),
                                    color = Gray400,
                                    fontSize = 13.sp,
                                    textDecoration = TextDecoration.LineThrough,
                                    modifier = Modifier.padding(bottom = 4.dp),
                                )
                            }
                            DiscountPill(text = deal.discount)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF9FAFB),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray.copy(alpha = 0.7f)),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Text(
                                text = "What's included",
                                style = MaterialTheme.typography.titleMedium,
                                color = Ink,
                            )
                            deal.highlights.forEach { highlight ->
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = GroupBlue,
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(17.dp),
                                    )
                                    Text(
                                        text = highlight,
                                        color = Gray500,
                                        fontSize = 14.sp,
                                        lineHeight = 21.sp,
                                    )
                                }
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "About this deal",
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink,
                        )
                        Text(
                            text = deal.about,
                            color = Gray500,
                            fontSize = 14.sp,
                            lineHeight = 23.sp,
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Location",
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink,
                        )
                        Surface(
                            onClick = onOpenMap,
                            shape = RoundedCornerShape(20.dp),
                            color = White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocationOn,
                                        contentDescription = null,
                                        tint = GroupBlue,
                                    )
                                    Column {
                                        Text(
                                            text = deal.merchant,
                                            color = Ink,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                        )
                                        Text(
                                            text = deal.address,
                                            color = Gray500,
                                            fontSize = 13.sp,
                                        )
                                        Text(
                                            text = deal.distance,
                                            color = Gray400,
                                            fontSize = 12.sp,
                                        )
                                    }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Schedule,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = "Open now • ${deal.hours}",
                                        color = Gray500,
                                        fontSize = 13.sp,
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(110.dp))
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
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Column {
                    Text(
                        text = "Total",
                        color = Gray500,
                        fontSize = 12.sp,
                    )
                    Text(
                        text = formatMoney(deal.price),
                        color = Ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    )
                }
                Button(
                    onClick = { onBuyNow(deal.id) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GroupBlue),
                ) {
                    Text(
                        text = "Buy Now",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
