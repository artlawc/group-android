package com.groupa.coreui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groupa.coreui.theme.DividerGray
import com.groupa.coreui.theme.GroupBlue
import com.groupa.coreui.theme.Ink
import com.groupa.coreui.theme.SuccessBackground
import com.groupa.coreui.theme.SuccessBorder
import com.groupa.coreui.theme.SuccessGreen
import com.groupa.coreui.theme.White

@Composable
fun DiscountPill(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = SuccessBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, SuccessBorder),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = Color(0xFF15803D),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CircularIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    containerColor: Color = White.copy(alpha = 0.92f),
    contentTint: Color = Ink,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray.copy(alpha = 0.5f)),
        modifier = Modifier.size(42.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentTint,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
        )
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                color = GroupBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .background(SuccessBackground, RoundedCornerShape(8.dp))
                    .border(1.dp, SuccessBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
fun StatusPill(text: String) {
    Box(
        modifier = Modifier
            .background(SuccessBackground, RoundedCornerShape(6.dp))
            .border(1.dp, SuccessBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            color = SuccessGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
