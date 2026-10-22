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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groupa.coreui.R
import com.groupa.coreui.component.CircularIconButton
import com.groupa.coreui.theme.AppBackground
import com.groupa.coreui.theme.CheckoutBlue
import com.groupa.coreui.theme.DividerGray
import com.groupa.coreui.theme.Gray400
import com.groupa.coreui.theme.Gray500
import com.groupa.coreui.theme.GroupBlue
import com.groupa.coreui.theme.Ink
import com.groupa.coreui.theme.SuccessGreen
import com.groupa.coreui.theme.White
import com.groupa.domain.model.Coupon
import com.groupa.presentation.component.formatMoney

@Composable
fun CouponsScreen(
    coupons: List<Coupon>,
    onBack: () -> Unit,
    onOpenCoupon: (String) -> Unit,
) {
    var selectedFilter by rememberSaveable { mutableStateOf("Active") }
    var selectedSort by rememberSaveable { mutableStateOf("Value") }
    val filteredCoupons = coupons.filter {
        if (selectedFilter == "Active") !it.isExpired else it.isExpired
    }
    val sortedCoupons = when (selectedSort) {
        "Date" -> filteredCoupons.sortedByDescending { it.expiresOn }
        else -> filteredCoupons.sortedByDescending { it.value }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                ScreenHeader(
                    title = stringResource(R.string.profile_my_coupons),
                    onBack = onBack,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(White)
                        .border(1.dp, DividerGray, RoundedCornerShape(18.dp)),
                ) {
                    SelectionButton(
                        label = stringResource(R.string.coupon_filter_active),
                        selected = selectedFilter == "Active",
                        onClick = { selectedFilter = "Active" },
                        modifier = Modifier.weight(1f),
                    )
                    SelectionButton(
                        label = stringResource(R.string.coupon_filter_outdated),
                        selected = selectedFilter == "Outdated",
                        onClick = { selectedFilter = "Outdated" },
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(White)
                        .border(1.dp, DividerGray, RoundedCornerShape(18.dp)),
                ) {
                    SelectionButton(
                        label = stringResource(R.string.coupon_sort_value),
                        selected = selectedSort == "Value",
                        onClick = { selectedSort = "Value" },
                        modifier = Modifier.weight(1f),
                    )
                    SelectionButton(
                        label = stringResource(R.string.coupon_sort_date),
                        selected = selectedSort == "Date",
                        onClick = { selectedSort = "Date" },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        items(sortedCoupons, key = { it.id }) { coupon ->
            CouponCard(
                coupon = coupon,
                onClick = { onOpenCoupon(coupon.id) },
            )
        }
    }
}

@Composable
fun CouponDetailScreen(
    coupon: Coupon,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ScreenHeader(
            title = stringResource(R.string.title_coupon_detail),
            onBack = onBack,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = White,
            border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ConfirmationNumber,
                        contentDescription = null,
                        tint = GroupBlue,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = coupon.title,
                        color = Ink,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    ExpiryPill(coupon = coupon)
                }
                Text(
                    text = stringResource(R.string.coupon_value, formatMoney(coupon.value)),
                    color = CheckoutBlue,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
                InfoRow(
                    label = stringResource(R.string.label_coupon_code),
                    value = coupon.code,
                )
                InfoRow(
                    label = stringResource(R.string.label_minimum_spend),
                    value = formatMoney(coupon.minimumSpend),
                )
                InfoRow(
                    label = stringResource(R.string.label_expires_on),
                    value = coupon.expiresOn,
                )
            }
        }
        Text(
            text = stringResource(R.string.label_coupon_terms),
            color = Gray500,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
        coupon.terms.forEach { term ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = White,
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
            ) {
                Text(
                    text = term,
                    color = Gray500,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(14.dp),
                )
            }
        }
    }
}

@Composable
fun InviteFriendsScreen(
    onBack: () -> Unit,
    onInviteWeChat: () -> Unit,
    onInviteTwitter: () -> Unit,
    onInviteDouyin: () -> Unit,
    onInviteTaobao: () -> Unit,
    onInviteAlipay: () -> Unit,
    onInvitePinduoduo: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ScreenHeader(
            title = stringResource(R.string.profile_invite_friends),
            onBack = onBack,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = GroupBlue,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.invite_code_label),
                    color = White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                )
                Text(
                    text = "TIM-2026",
                    color = White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.invite_message),
                    color = White,
                    fontSize = 14.sp,
                )
            }
        }
        Button(
            onClick = onInviteWeChat,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF07C160)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {
            Text(
                text = stringResource(R.string.action_invite_wechat),
                fontWeight = FontWeight.SemiBold,
            )
        }
        OutlinedButton(
            onClick = onInviteTwitter,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {
            Text(
                text = stringResource(R.string.action_invite_twitter),
                fontWeight = FontWeight.SemiBold,
            )
        }
        OutlinedButton(
            onClick = onInviteDouyin,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {
            Text(
                text = stringResource(R.string.action_invite_douyin),
                fontWeight = FontWeight.SemiBold,
            )
        }
        OutlinedButton(
            onClick = onInviteTaobao,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {
            Text(
                text = stringResource(R.string.action_invite_taobao),
                fontWeight = FontWeight.SemiBold,
            )
        }
        OutlinedButton(
            onClick = onInviteAlipay,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {
            Text(
                text = stringResource(R.string.action_invite_alipay),
                fontWeight = FontWeight.SemiBold,
            )
        }
        OutlinedButton(
            onClick = onInvitePinduoduo,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {
            Text(
                text = stringResource(R.string.action_invite_pinduoduo),
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = stringResource(R.string.invite_hint),
            color = Gray400,
            fontSize = 12.sp,
        )
    }
}

@Composable
fun SupportScreen(
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ScreenHeader(
            title = stringResource(R.string.profile_support),
            onBack = onBack,
        )
        Text(
            text = stringResource(R.string.support_intro),
            color = Gray500,
            fontSize = 14.sp,
        )
        SupportCard(
            title = stringResource(R.string.support_service_scope),
            value = stringResource(R.string.support_service_scope_value),
            icon = Icons.Rounded.ConfirmationNumber,
        )
        SupportCard(
            title = stringResource(R.string.support_email),
            value = "support@groupgo.com",
            icon = Icons.Rounded.Email,
        )
        SupportCard(
            title = stringResource(R.string.support_phone),
            value = "+1 (555) 010-2026",
            icon = Icons.Rounded.Call,
        )
    }
}

@Composable
private fun ScreenHeader(
    title: String,
    onBack: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CircularIconButton(
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = stringResource(R.string.action_back),
            onClick = onBack,
        )
        Text(
            text = title,
            color = Ink,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SelectionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) GroupBlue else White)
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) White else Gray400,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun CouponCard(
    coupon: Coupon,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = White,
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .background(
                        if (coupon.isExpired) Color(0xFFF3F4F6) else GroupBlue.copy(alpha = 0.1f),
                        RoundedCornerShape(18.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        text = formatMoney(coupon.value),
                        color = if (coupon.isExpired) Gray400 else CheckoutBlue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.coupon_value_suffix),
                        color = if (coupon.isExpired) Gray400 else GroupBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = coupon.title,
                        color = Ink,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    ExpiryPill(coupon = coupon)
                }
                Box(
                    modifier = Modifier
                        .background(
                            if (coupon.isExpired) Color(0xFFF3F4F6) else GroupBlue.copy(alpha = 0.08f),
                            RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = coupon.code,
                        color = if (coupon.isExpired) Gray400 else GroupBlue,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = stringResource(R.string.coupon_expires, coupon.expiresOn),
                    color = if (coupon.isExpired) Color(0xFFDC2626) else SuccessGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = Gray400,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun ExpiryPill(coupon: Coupon) {
    val expired = stringResource(R.string.coupon_filter_outdated)
    val active = stringResource(R.string.coupon_filter_active)

    Box(
        modifier = Modifier
            .background(
                if (coupon.isExpired) Color(0xFFFEE2E2) else Color(0xFFECFDF5),
                RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = if (coupon.isExpired) expired else active,
            color = if (coupon.isExpired) Color(0xFFDC2626) else SuccessGreen,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = Gray500,
            fontSize = 13.sp,
        )
        Text(
            text = value,
            color = Ink,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SupportCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = White,
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = GroupBlue.copy(alpha = 0.1f),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GroupBlue,
                    modifier = Modifier
                        .padding(9.dp)
                        .size(20.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = title,
                    color = Gray500,
                    fontSize = 12.sp,
                )
                Text(
                    text = value,
                    color = Ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
