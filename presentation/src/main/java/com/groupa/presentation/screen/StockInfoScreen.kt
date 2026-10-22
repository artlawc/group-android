package com.groupa.presentation.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groupa.coreui.R
import com.groupa.coreui.theme.AppBackground
import com.groupa.coreui.theme.CheckoutBlue
import com.groupa.coreui.theme.DividerGray
import com.groupa.coreui.theme.Gray500
import com.groupa.coreui.theme.Ink
import com.groupa.coreui.theme.SuccessGreen
import com.groupa.coreui.theme.White
import com.groupa.domain.model.StockInfo
import kotlin.math.max
import kotlin.math.min

internal fun buildStockChartPoints(
    history: List<Double>,
    width: Float,
    height: Float,
): List<Offset> {
    if (history.isEmpty()) return emptyList()

    val lowest = history.min()
    val highest = history.max()
    val range = max(highest - lowest, 0.01)

    return history.mapIndexed { index, value ->
        val x = if (history.size <= 1) {
            0f
        } else {
            index * width / (history.size - 1)
        }
        val y = height - ((value - lowest) / range * height).toFloat()
        Offset(x, y)
    }
}

@Composable
fun StockInfoScreen(
    stock: StockInfo,
    onBack: () -> Unit,
) {
    val chartProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1200),
        label = "stockChartProgress",
    )
    val trendColor = if (stock.change >= 0) SuccessGreen else Color(0xFFE5484D)
    val chartPoints = remember(stock.id) {
        buildStockChartPoints(
            history = stock.history,
            width = 1f,
            height = 1f,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            androidx.compose.material3.IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = Ink,
                )
            }
            Text(
                text = stringResource(R.string.title_stock_info),
                style = MaterialTheme.typography.headlineMedium,
                color = Ink,
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = White,
            border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(
                    text = stock.symbol,
                    color = CheckoutBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                Text(
                    text = stock.name,
                    color = Ink,
                    style = MaterialTheme.typography.titleLarge,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.label_stock_price),
                            color = Gray500,
                            fontSize = 12.sp,
                        )
                        Text(
                            text = "$${stock.price}",
                            color = Ink,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.label_stock_change),
                            color = Gray500,
                            fontSize = 12.sp,
                        )
                        Text(
                            text = "${stock.change} (${stock.percentChange}%)",
                            color = trendColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.label_stock_chart),
                    color = Gray500,
                    fontWeight = FontWeight.SemiBold,
                )
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                ) {
                    val points = chartPoints
                    if (points.isEmpty()) return@Canvas

                    val chartPath = Path()
                    points.forEachIndexed { index, point ->
                        if (index == 0) {
                            chartPath.moveTo(point.x * size.width, point.y * size.height)
                        } else {
                            chartPath.lineTo(point.x * size.width, point.y * size.height)
                        }
                    }

                    clipRect(
                        right = size.width * chartProgress,
                    ) {
                        drawPath(
                            path = chartPath,
                            color = trendColor,
                            style = Stroke(width = 3.dp.toPx()),
                        )
                    }

                    val currentPointIndex = ((points.size - 1) * chartProgress).toInt()
                    val currentPoint = points[currentPointIndex]
                    drawCircle(
                        color = trendColor,
                        radius = 5.dp.toPx(),
                        center = Offset(
                            x = currentPoint.x * size.width,
                            y = currentPoint.y * size.height,
                        ),
                    )
                }
            }
        }
    }
}
