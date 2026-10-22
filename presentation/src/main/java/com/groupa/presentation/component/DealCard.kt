package com.groupa.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groupa.coreui.component.DiscountPill
import com.groupa.coreui.component.dealImageRes
import com.groupa.coreui.theme.DividerGray
import com.groupa.coreui.theme.Gray400
import com.groupa.coreui.theme.Gray500
import com.groupa.coreui.theme.GroupBlue
import com.groupa.coreui.theme.Ink
import com.groupa.coreui.theme.White
import com.groupa.domain.model.Deal

@Composable
fun DealCard(
    deal: Deal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = White,
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray.copy(alpha = 0.7f)),
    ) {
        Column {
            Box {
                androidx.compose.foundation.Image(
                    painter = painterResource(dealImageRes(deal.image)),
                    contentDescription = deal.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                )
                DiscountPill(
                    text = deal.discount,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                )
            }
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = deal.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color(0xFFFACC15),
                        modifier = Modifier.size(17.dp),
                    )
                    Text(
                        text = deal.rating,
                        color = Ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                    Text(
                        text = "(${deal.reviews})",
                        color = Gray400,
                        fontSize = 13.sp,
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = GroupBlue,
                        modifier = Modifier.size(17.dp),
                    )
                    Text(
                        text = deal.distance,
                        color = Gray500,
                        fontSize = 13.sp,
                    )
                }
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = formatMoney(deal.price),
                        color = GroupBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                    )
                    Text(
                        text = formatMoney(deal.originalPrice),
                        color = Gray400,
                        fontSize = 13.sp,
                        textDecoration = TextDecoration.LineThrough,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
                Text(
                    text = deal.cuisine,
                    color = Gray500,
                    fontSize = 13.sp,
                )
            }
        }
    }
}
