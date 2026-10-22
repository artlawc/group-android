package com.groupa.presentation.screen

import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.groupa.coreui.R

internal data class SwipeDeleteAction(
    val labelRes: Int,
    val labelColor: Color,
    val backgroundColor: Color,
    val contentAlignment: Alignment,
    val areaAlignment: Alignment,
)

internal val leftSwipeDeleteAction = SwipeDeleteAction(
    labelRes = R.string.action_confirm_delete,
    labelColor = Color.White,
    backgroundColor = Color(0xFFDC2626),
    contentAlignment = Alignment.Center,
    areaAlignment = Alignment.CenterEnd,
)
