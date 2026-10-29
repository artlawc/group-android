package com.groupa.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
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
import kotlin.math.abs

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
private fun StockIndicatorPanel(
    candles: List<StockCandle>,
    rangeMetrics: StockRangeMetrics?,
) {
    val activeCandle = candles.lastOrNull()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = StockControlBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, StockBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.stock_indicator_candle_range),
                        color = StockLabelGray,
                        fontSize = 12.sp,
                        maxLines = 1,
                    )
                    if (rangeMetrics != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            StockMetricColumn(
                                label = stringResource(R.string.stock_range_high),
                                value = formatStockMoney(rangeMetrics.high),
                                valueColor = StockUp,
                                modifier = Modifier.weight(1f),
                            )
                            StockMetricColumn(
                                label = stringResource(R.string.stock_range_low),
                                value = formatStockMoney(rangeMetrics.low),
                                valueColor = StockDown,
                                modifier = Modifier.weight(1f),
                            )
                            StockMetricColumn(
                                label = stringResource(R.string.stock_label_net),
                                value = formatStockPercent(rangeMetrics.netPercent),
                                valueColor = if (rangeMetrics.netPercent >= 0.0) {
                                    StockUp
                                } else {
                                    StockDown
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    } else if (activeCandle != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            StockMetricColumn(
                                label = stringResource(R.string.stock_label_open),
                                value = formatStockAmount(activeCandle.open),
                                valueColor = StockText,
                                modifier = Modifier.weight(1f),
                            )
                            StockMetricColumn(
                                label = stringResource(R.string.stock_label_high),
                                value = formatStockAmount(activeCandle.high),
                                valueColor = StockUp,
                                modifier = Modifier.weight(1f),
                            )
                            StockMetricColumn(
                                label = stringResource(R.string.stock_label_low),
                                value = formatStockAmount(activeCandle.low),
                                valueColor = StockDown,
                                modifier = Modifier.weight(1f),
                            )
                            StockMetricColumn(
                                label = stringResource(R.string.stock_label_close),
                                value = formatStockAmount(activeCandle.close),
                                valueColor = StockText,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.stock_indicator_alerts_tracks),
                        color = StockLabelGray,
                        fontSize = 12.sp,
                        maxLines = 1,
                    )
                    if (rangeMetrics != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            StockMetricColumn(
                                label = stringResource(R.string.stock_range_alerts),
                                value = stringResource(
                                    R.string.stock_counted_buy_alerts,
                                    rangeMetrics.buyAlerts,
                                ),
                                valueColor = StockUp,
                                modifier = Modifier.weight(1f),
                            )
                            StockMetricColumn(
                                label = "",
                                value = stringResource(
                                    R.string.stock_counted_sell_alerts,
                                    rangeMetrics.sellAlerts,
                                ),
                                valueColor = StockDown,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    } else if (activeCandle != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            StockMetricColumn(
                                label = stringResource(R.string.stock_track_signal),
                                value = formatStockAmount(activeCandle.rsi),
                                valueColor = StockAccent,
                                modifier = Modifier.weight(1f),
                            )
                            StockMetricColumn(
                                label = stringResource(R.string.stock_label_close),
                                value = formatStockAmount(activeCandle.macd),
                                valueColor = if (activeCandle.macd >= 0.0) {
                                    StockUp
                                } else {
                                    StockDown
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Text(
                            text = activeCandle.alert?.let {
                                stringResource(R.string.stock_track_crossover, it)
                            } ?: stringResource(R.string.stock_track_no_trigger),
                            color = if (activeCandle.alert == "SELL") {
                                StockDown
                            } else {
                                StockUp
                            },
                            fontSize = 11.sp,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StockMetricColumn(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            color = StockLabelGray,
            fontSize = 10.sp,
            maxLines = 1,
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun StockNavigationBar(
    title: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                tint = StockText,
            )
        }
        Text(
            text = title,
            color = StockText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun StockHeroSection(
    symbolText: String,
    description: String,
    stockName: String,
    priceText: String,
    changeText: String,
    percentText: String,
    isPriceUp: Boolean,
) {
    val trendColor = if (isPriceUp) StockUp else StockDown

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = StockDashboard,
        border = androidx.compose.foundation.BorderStroke(1.dp, StockBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = symbolText,
                color = StockText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                text = description,
                color = StockLabelGray,
                fontSize = 12.sp,
                maxLines = 2,
            )
            Text(
                text = stockName,
                color = StockLabelGray,
                fontSize = 12.sp,
                maxLines = 1,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = priceText,
                    color = trendColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "$changeText ($percentText)",
                    color = trendColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun StockChartSection(
    candles: List<StockCandle>,
    chartTransform: StockChartTransform,
    drawMode: Boolean,
    firstAnchorIndex: Int?,
    secondAnchorIndex: Int?,
    textMeasurer: TextMeasurer,
    chartViewportWidthPx: Float,
    hintText: String,
    onTransformChange: ((StockChartTransform) -> StockChartTransform) -> Unit,
    onAnchorSelected: (Int?) -> Unit,
    onChartWidthChanged: (Float) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = StockDashboard,
        border = androidx.compose.foundation.BorderStroke(1.dp, StockBorder),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(StockDashboard),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .background(StockDashboard)
                    .onSizeChanged { size -> onChartWidthChanged(size.width.toFloat()) }
                    .pointerInput(
                        chartViewportWidthPx,
                        candles.size,
                    ) {
                        detectTransformGestures(panZoomLock = true) { centroid, pan, zoom, _ ->
                            onTransformChange { current ->
                                applyStockChartGesture(
                                    transform = current,
                                    panX = pan.x,
                                    zoom = zoom,
                                    focusX = centroid.x,
                                    viewportWidth = chartViewportWidthPx,
                                )
                            }
                        }
                    }
                    .pointerInput(
                        chartViewportWidthPx,
                        candles.size,
                    ) {
                        detectTapGestures(
                            onDoubleTap = { offset ->
                                onAnchorSelected(
                                    stockCandleIndexAt(
                                        x = offset.x,
                                        transform = chartTransform,
                                        candleCount = candles.size,
                                        graphWidth = chartViewportWidthPx,
                                    ),
                                )
                            },
                        )
                    },
            ) {
                drawStockChart(
                    candles = candles,
                    transform = chartTransform,
                    drawMode = drawMode,
                    firstAnchorIndex = firstAnchorIndex,
                    secondAnchorIndex = secondAnchorIndex,
                    textMeasurer = textMeasurer,
                )
            }

            Text(
                text = hintText,
                color = StockLabelGray,
                fontSize = 10.sp,
                maxLines = 2,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
            )
        }
    }
}

private fun DrawScope.drawStockChart(
    textMeasurer: TextMeasurer,
    candles: List<StockCandle>,
    transform: StockChartTransform,
    drawMode: Boolean,
    firstAnchorIndex: Int?,
    secondAnchorIndex: Int?,
) {
    if (candles.isEmpty()) return

    val rightAxisWidth = 56.dp.toPx()
    val graphWidth = size.width - rightAxisWidth
    if (graphWidth <= 0f) return

    val panelPadding = 12.dp.toPx()
    val priceTop = 12.dp.toPx()
    val priceHeight = size.height * 0.56f
    val macdTop = priceTop + priceHeight + panelPadding
    val macdHeight = size.height * 0.20f
    val rsiTop = macdTop + macdHeight + panelPadding
    val rsiHeight = size.height - rsiTop - panelPadding
    val candleCount = candles.size

    val minPrice = candles.minOf { it.low }
    val maxPrice = candles.maxOf { it.high }
    val priceRange = max(maxPrice - minPrice, 0.01)
    val pricePadding = priceRange * 0.05
    val priceLower = minPrice - pricePadding
    val priceUpper = maxPrice + pricePadding

    val macdExtent = candles.maxOf {
        max(max(abs(it.macd), abs(it.signal)), abs(it.histogram))
    }
    val macdBound = max(macdExtent * 1.1, 0.01)

    val slotWidth = graphWidth / candleCount * transform.zoom
    val bodyWidth = max(slotWidth * 0.68f, 1.5f)
    val totalWidth = graphWidth * transform.zoom
    val firstVisible = (
        (-transform.panX) / totalWidth * candleCount
        ).toInt() - 1
    val lastVisible = (
        (graphWidth - transform.panX) / totalWidth * candleCount
        ).toInt() + 1
    val visibleStart = firstVisible.coerceIn(0, candleCount - 1)
    val visibleEnd = lastVisible.coerceIn(0, candleCount - 1)

    fun priceToY(value: Double): Float {
        val normalized = (value - priceLower) / (priceUpper - priceLower)
        return priceTop + (1f - normalized.toFloat()).coerceIn(0f, 1f) * priceHeight
    }

    fun macdToY(value: Double): Float {
        val normalized = (value + macdBound) / (2.0 * macdBound)
        return macdTop + (1f - normalized.toFloat()).coerceIn(0f, 1f) * macdHeight
    }

    fun rsiToY(value: Double): Float {
        return rsiTop + (1f - (value / 100.0).coerceIn(0.0, 1.0).toFloat()) * rsiHeight
    }

    fun candleToX(index: Int): Float {
        return transform.panX + (
            index.toFloat() / candleCount.toFloat()
            ) * totalWidth + slotWidth / 2f
    }

    val labelStyle = TextStyle(
        color = StockLabelGray,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
    )
    val alertStyle = labelStyle.copy(
        color = StockText,
        fontSize = 8.sp,
        fontWeight = FontWeight.Bold,
    )

    fun drawAxisLabel(text: String, y: Float) {
        drawText(
            textMeasurer = textMeasurer,
            text = text,
            topLeft = Offset(x = size.width - rightAxisWidth + 4.dp.toPx(), y = y),
            style = labelStyle,
        )
    }

    for (gridIndex in 0..4) {
        val y = priceTop + (gridIndex / 4f) * priceHeight
        drawLine(
            color = StockGrid,
            start = Offset(x = 0f, y = y),
            end = Offset(x = graphWidth, y = y),
            strokeWidth = 1f,
        )
        val value = maxPrice - (gridIndex / 4f) * priceRange
        drawAxisLabel(formatStockAmount(value), y)
    }

    for (gridIndex in 0..2) {
        val y = macdTop + (gridIndex / 2f) * macdHeight
        val value = macdBound - (gridIndex / 2f) * (2.0 * macdBound)
        drawLine(
            color = if (gridIndex == 1) StockControlBorder else StockGridSoft,
            start = Offset(x = 0f, y = y),
            end = Offset(x = graphWidth, y = y),
            strokeWidth = 1f,
        )
        drawAxisLabel(formatStockAmount(value), y)
    }

    listOf(100.0, 70.0, 50.0, 30.0, 0.0).forEach { level ->
        val y = rsiToY(level)
        val lineColor = when (level) {
            50.0 -> StockControlBorder
            30.0, 70.0 -> StockAccent.copy(alpha = 0.20f)
            else -> StockGridSoft
        }
        drawLine(
            color = lineColor,
            start = Offset(x = 0f, y = y),
            end = Offset(x = graphWidth, y = y),
            strokeWidth = 1f,
        )
        drawAxisLabel(level.toInt().toString(), y)
    }

    if (firstAnchorIndex != null && secondAnchorIndex != null) {
        val start = min(firstAnchorIndex, secondAnchorIndex)
        val end = max(firstAnchorIndex, secondAnchorIndex)
        val xStart = candleToX(start) - slotWidth / 2f
        val xEnd = candleToX(end) + slotWidth / 2f

        clipRect(left = 0f, top = priceTop, right = graphWidth, bottom = size.height) {
            drawRect(
                color = StockAccent.copy(alpha = 0.06f),
                topLeft = Offset(x = xStart, y = priceTop),
                size = Size(width = xEnd - xStart, height = size.height - priceTop),
            )
            drawLine(
                color = StockAccent.copy(alpha = 0.45f),
                start = Offset(x = xStart, y = priceTop),
                end = Offset(x = xStart, y = size.height),
                strokeWidth = 1f,
            )
            drawLine(
                color = StockAccent.copy(alpha = 0.45f),
                start = Offset(x = xEnd, y = priceTop),
                end = Offset(x = xEnd, y = size.height),
                strokeWidth = 1f,
            )
        }
    }

    clipRect(left = 0f, top = priceTop, right = graphWidth, bottom = priceTop + priceHeight) {
        for (index in visibleStart..visibleEnd) {
            val candle = candles[index]
            val x = candleToX(index)
            val openY = priceToY(candle.open)
            val closeY = priceToY(candle.close)
            val highY = priceToY(candle.high)
            val lowY = priceToY(candle.low)
            val candleColor = if (candle.isBullish) StockUp else StockDown

            drawLine(
                color = candleColor,
                start = Offset(x = x, y = highY),
                end = Offset(x = x, y = lowY),
                strokeWidth = max(slotWidth * 0.08f, 1f),
            )
            drawRect(
                color = candleColor,
                topLeft = Offset(x = x - bodyWidth / 2f, y = min(openY, closeY)),
                size = Size(
                    width = bodyWidth,
                    height = max(abs(openY - closeY), 1.5f),
                ),
            )

            if (candle.alert != null) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = candle.alert,
                    topLeft = Offset(
                        x = x - 12.dp.toPx(),
                        y = if (candle.alert == "BUY") lowY else highY - 10.sp.toPx(),
                    ),
                    style = alertStyle.copy(
                        color = if (candle.alert == "BUY") StockUp else StockDown,
                    ),
                )
            }
        }
    }

    clipRect(left = 0f, top = macdTop, right = graphWidth, bottom = macdTop + macdHeight) {
        for (index in visibleStart..visibleEnd) {
            val candle = candles[index]
            val x = candleToX(index)
            val histogramY = macdToY(candle.histogram)
            val zeroY = macdToY(0.0)
            val histogramColor = if (candle.histogram >= 0.0) {
                StockUp.copy(alpha = 0.55f)
            } else {
                StockDown.copy(alpha = 0.55f)
            }

            drawRect(
                color = histogramColor,
                topLeft = Offset(x = x - bodyWidth / 2f, y = min(zeroY, histogramY)),
                size = Size(
                    width = max(slotWidth * 0.68f, 1.5f),
                    height = max(abs(zeroY - histogramY), 1f),
                ),
            )
        }
    }

    if (drawMode) {
        clipRect(left = 0f, top = macdTop, right = graphWidth, bottom = size.height) {
            val macdPath = Path()
            val signalPath = Path()
            val rsiPath = Path()
            var macdStarted = false
            var signalStarted = false
            var rsiStarted = false

            for (index in visibleStart..visibleEnd) {
                val candle = candles[index]
                val x = candleToX(index)
                if (index >= 25) {
                    val y = macdToY(candle.macd)
                    if (!macdStarted) {
                        macdPath.moveTo(x, y)
                        macdStarted = true
                    } else {
                        macdPath.lineTo(x, y)
                    }
                }
                if (index >= 33) {
                    val y = macdToY(candle.signal)
                    if (!signalStarted) {
                        signalPath.moveTo(x, y)
                        signalStarted = true
                    } else {
                        signalPath.lineTo(x, y)
                    }
                }
                if (index >= 14) {
                    val y = rsiToY(candle.rsi)
                    if (!rsiStarted) {
                        rsiPath.moveTo(x, y)
                        rsiStarted = true
                    } else {
                        rsiPath.lineTo(x, y)
                    }
                }
            }

            drawPath(
                path = macdPath,
                color = StockLineBlue,
                style = Stroke(width = 2f),
            )
            drawPath(
                path = signalPath,
                color = StockLineOrange,
                style = Stroke(width = 2f),
            )
            drawPath(
                path = rsiPath,
                color = StockLinePurple,
                style = Stroke(width = 2f),
            )
        }
    }

    firstAnchorIndex?.let { index ->
        drawLine(
            color = StockAccent,
            start = Offset(x = candleToX(index), y = priceTop),
            end = Offset(x = candleToX(index), y = size.height - panelPadding),
            strokeWidth = 2f,
        )
    }
    secondAnchorIndex?.let { index ->
        drawLine(
            color = StockAccent,
            start = Offset(x = candleToX(index), y = priceTop),
            end = Offset(x = candleToX(index), y = size.height - panelPadding),
            strokeWidth = 2f,
        )
    }
}

@Composable
private fun StockControlBar(
    selectedTimeframe: StockChartTimeframe,
    onSelectTimeframe: (StockChartTimeframe) -> Unit,
    drawMode: Boolean,
    onToggleDrawMode: () -> Unit,
    drawStateText: String,
    waveRunning: Boolean,
    onToggleWaveCycle: () -> Unit,
    onReset: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = StockDashboard,
        border = androidx.compose.foundation.BorderStroke(1.dp, StockBorder),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StockActionButton(
                    text = stringResource(R.string.stock_action_wave_cycle),
                    active = waveRunning,
                    onClick = onToggleWaveCycle,
                    modifier = Modifier.weight(1f),
                )
                StockActionButton(
                    text = stringResource(
                        R.string.stock_action_draw_mode,
                        drawStateText,
                    ),
                    active = drawMode,
                    onClick = onToggleDrawMode,
                    modifier = Modifier.weight(1f),
                )
                StockActionButton(
                    text = stringResource(R.string.stock_action_clear_reset),
                    active = false,
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StockControlBackground, RoundedCornerShape(6.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                StockChartTimeframe.entries.forEach { timeframe ->
                    StockActionButton(
                        text = timeframe.label,
                        active = selectedTimeframe == timeframe,
                        onClick = { onSelectTimeframe(timeframe) },
                        modifier = Modifier.weight(1f),
                        compact = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun StockActionButton(
    text: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val textColor = if (active) StockAccent else StockLabelGray

    Surface(
        onClick = onClick,
        modifier = modifier.height(if (compact) 34.dp else 38.dp),
        shape = RoundedCornerShape(6.dp),
        color = if (active) StockControlSelected else androidx.compose.ui.graphics.Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (active) StockAccent else StockControlBorder,
        ),
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = if (compact) 12.sp else 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

private val StockBackground = Color(0xFF0F1115)
private val StockDashboard = Color(0xFF161A22)
private val StockBorder = Color(0xFF2A2F3A)
private val StockControlBackground = Color(0xFF0B0E11)
private val StockControlSelected = Color(0xFF2B3139)
private val StockControlBorder = Color(0xFF474F59)
private val StockLabelGray = Color(0xFF848E9C)
private val StockText = Color(0xFFE4E6EB)
private val StockUp = Color(0xFF26A69A)
private val StockDown = Color(0xFFEF5350)
private val StockAccent = Color(0xFFF0B90B)
private val StockLineBlue = Color(0xFF3B82F6)
private val StockLineOrange = Color(0xFFF97316)
private val StockLinePurple = Color(0xFFA855F7)
private val StockGrid = Color(0xFF222630)
private val StockGridSoft = Color(0xFF1F242E)

private const val STOCK_WAVE_TICK_LIMIT = 70

@Composable
fun StockInfoScreen(
    stock: StockInfo,
    onBack: () -> Unit,
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val scrollState = rememberScrollState()

    val datasets = remember(stock.id) {
        generateStockDatasets(stock.history)
    }

    var selectedTimeframe by remember(stock.id) {
        mutableStateOf(StockChartTimeframe.DAY)
    }
    val chartTransformState = remember { mutableStateOf(StockChartTransform()) }
    var chartTransform by chartTransformState
    var drawMode by remember { mutableStateOf(false) }
    var waveRunning by remember { mutableStateOf(false) }
    var waveTicks by remember { mutableIntStateOf(0) }
    var firstAnchorIndex by remember { mutableStateOf<Int?>(null) }
    var secondAnchorIndex by remember { mutableStateOf<Int?>(null) }
    var chartWidthPx by remember { mutableFloatStateOf(0f) }

    val baseCandles = remember(datasets, selectedTimeframe) {
        datasets.getValue(selectedTimeframe)
    }

    LaunchedEffect(waveRunning, stock.id) {
        if (!waveRunning) return@LaunchedEffect
        if (waveTicks >= STOCK_WAVE_TICK_LIMIT) waveTicks = 0
        while (waveTicks < STOCK_WAVE_TICK_LIMIT) {
            delay(100)
            waveTicks += 1
        }
    }

    val displayCandles = remember(baseCandles, waveTicks) {
        applyStockWaveCycle(baseCandles, waveTicks)
    }

    val rangeMetrics = remember(
        displayCandles,
        firstAnchorIndex,
        secondAnchorIndex,
    ) {
        calculateStockRangeMetrics(
            candles = displayCandles,
            firstAnchorIndex = firstAnchorIndex,
            secondAnchorIndex = secondAnchorIndex,
        )
    }

    val rightAxisWidthPx = with(density) { 56.dp.toPx() }
    val chartViewportWidthPx = (chartWidthPx - rightAxisWidthPx).coerceAtLeast(1f)
    LaunchedEffect(baseCandles, chartViewportWidthPx) {
        chartTransform = defaultStockChartTransform(
            candleCount = baseCandles.size,
            viewportWidth = chartViewportWidthPx,
        )
    }
    val lastCandle = displayCandles.lastOrNull()
    val previousClose = displayCandles.getOrNull(displayCandles.lastIndex - 1)?.close
        ?: stock.price
    val livePrice = lastCandle?.close ?: stock.price
    val liveChange = lastCandle?.let { it.close - it.open } ?: stock.change
    val livePercent = stock.percentChange
    val isPriceUp = livePrice >= previousClose

    fun requestAnchor(tappedIndex: Int?) {
        tappedIndex ?: return
        when {
            firstAnchorIndex == null -> {
                firstAnchorIndex = tappedIndex
            }
            secondAnchorIndex == null -> {
                secondAnchorIndex = tappedIndex
            }
            else -> {
                firstAnchorIndex = tappedIndex
                secondAnchorIndex = null
            }
        }
    }

    fun resetChartView() {
        chartTransform = defaultStockChartTransform(
            candleCount = displayCandles.size,
            viewportWidth = chartViewportWidthPx,
        )
        firstAnchorIndex = null
        secondAnchorIndex = null
    }

    fun selectTimeframe(timeframe: StockChartTimeframe) {
        if (selectedTimeframe != timeframe) {
            selectedTimeframe = timeframe
            chartTransform = defaultStockChartTransform(
                candleCount = datasets.getValue(timeframe).size,
                viewportWidth = chartViewportWidthPx,
            )
            firstAnchorIndex = null
            secondAnchorIndex = null
            waveTicks = 0
            waveRunning = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StockBackground)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StockNavigationBar(
            title = stringResource(R.string.title_stock_info),
            onBack = onBack,
        )

        StockHeroSection(
            symbolText = stringResource(R.string.stock_symbol_pair, stock.symbol),
            description = stringResource(R.string.stock_tool_description),
            stockName = stock.name,
            priceText = formatStockMoney(livePrice),
            changeText = formatStockDelta(liveChange),
            percentText = formatStockPercent(livePercent),
            isPriceUp = isPriceUp,
        )

        StockControlBar(
            selectedTimeframe = selectedTimeframe,
            onSelectTimeframe = ::selectTimeframe,
            drawMode = drawMode,
            onToggleDrawMode = { drawMode = !drawMode },
            drawStateText = stringResource(
                if (drawMode) R.string.stock_state_on else R.string.stock_state_off,
            ),
            waveRunning = waveRunning,
            onToggleWaveCycle = { waveRunning = !waveRunning },
            onReset = ::resetChartView,
        )

        StockChartSection(
            candles = displayCandles,
            chartTransform = chartTransform,
            drawMode = drawMode,
            firstAnchorIndex = firstAnchorIndex,
            secondAnchorIndex = secondAnchorIndex,
            textMeasurer = textMeasurer,
            onTransformChange = { update ->
                chartTransformState.value = update(chartTransformState.value)
            },
            onAnchorSelected = ::requestAnchor,
            onChartWidthChanged = { width -> chartWidthPx = width },
            chartViewportWidthPx = chartViewportWidthPx,
            hintText = stringResource(R.string.stock_chart_hint),
        )

        StockIndicatorPanel(
            candles = displayCandles,
            rangeMetrics = rangeMetrics,
        )
    }
}
