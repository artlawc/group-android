package com.groupa.presentation.screen

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import java.util.Locale

enum class StockChartTimeframe(
    val label: String,
    val candleCount: Int,
) {
    DAY("1D", 60),
    FIVE_DAY("5D", 110),
    MONTH("1M", 180),
}

data class StockCandle(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val rsi: Double = 50.0,
    val macd: Double = 0.0,
    val signal: Double = 0.0,
    val histogram: Double = 0.0,
    val alert: String? = null,
) {
    val isBullish: Boolean get() = close >= open
}

data class StockChartTransform(
    val zoom: Float = 1f,
    val panX: Float = 0f,
)

data class StockRangeMetrics(
    val high: Double,
    val low: Double,
    val netPercent: Double,
    val buyAlerts: Int,
    val sellAlerts: Int,
    val candleCount: Int,
)

internal fun stockHash(
    seed: Int,
    index: Int,
): Double {
    val value = sin(seed * 12.9898 + index * 78.233) * 43_758.5453
    return abs(value - floor(value))
}

internal fun sampleStockHistory(
    history: List<Double>,
    count: Int,
): List<Double> {
    if (history.isEmpty()) return emptyList()
    if (count <= 0) return emptyList()
    if (history.size == 1) return List(count) { history.first() }

    return List(count) { index ->
        if (count == 1) {
            history.first()
        } else {
            val position = index * (history.size - 1) / (count - 1).toFloat()
            val lowerIndex = floor(position).toInt().coerceIn(0, history.size - 2)
            val fraction = position - lowerIndex
            history[lowerIndex] * (1f - fraction) + history[lowerIndex + 1] * fraction
        }
    }
}

internal fun generateStockCandles(
    history: List<Double>,
    count: Int,
    seedOffset: Int = 0,
): List<StockCandle> {
    val closes = sampleStockHistory(history, count)
    if (closes.isEmpty()) return emptyList()

    return closes.mapIndexed { index, close ->
        val seed = index + seedOffset
        val previous = if (index == 0) {
            close * 0.995
        } else {
            closes[index - 1]
        }
        val open = previous * (1.0 + (stockHash(seed, 1) - 0.5) * 0.018)
        val high = max(open, close) * (
            1.002 + stockHash(seed, 2) * 0.005
            )
        val low = min(open, close) * (
            0.998 - stockHash(seed, 3) * 0.005
            )
        StockCandle(
            open = open,
            high = high,
            low = low,
            close = close,
        )
    }
}

internal fun generateStockDatasets(
    history: List<Double>,
): Map<StockChartTimeframe, List<StockCandle>> =
    StockChartTimeframe.entries.associateWith { timeframe ->
        enrichStockIndicators(
            generateStockCandles(
                history = history,
                count = timeframe.candleCount,
                seedOffset = timeframe.ordinal * 97,
            ),
        )
    }

internal fun calculateStockEma(
    values: List<Double>,
    period: Int,
): List<Double> {
    if (values.isEmpty()) return emptyList()
    val safePeriod = max(period, 1)
    val result = MutableList(values.size) { 0.0 }
    val initialCount = min(safePeriod, values.size)
    var sum = 0.0
    for (index in 0 until initialCount) sum += values[index]
    result[initialCount - 1] = sum / initialCount

    val multiplier = 2.0 / (safePeriod + 1.0)
    for (index in safePeriod until values.size) {
        result[index] = values[index] * multiplier + result[index - 1] * (1 - multiplier)
    }
    return result
}

internal fun calculateStockRsi(values: List<Double>): List<Double> {
    if (values.size < 2) return values.map { 50.0 }

    val result = MutableList(values.size) { 50.0 }
    var gains = 0.0
    var losses = 0.0
    val initialBound = min(14, values.size - 1)
    for (index in 1..initialBound) {
        val difference = values[index] - values[index - 1]
        if (difference >= 0.0) gains += difference else losses -= difference
    }

    var averageGain = gains / initialBound
    var averageLoss = losses / initialBound
    for (index in 1 until values.size) {
        if (index > initialBound) {
            val difference = values[index] - values[index - 1]
            averageGain = (
                averageGain * (initialBound - 1) +
                    if (difference >= 0.0) difference else 0.0
                ) / initialBound
            averageLoss = (
                averageLoss * (initialBound - 1) +
                    if (difference < 0.0) -difference else 0.0
                ) / initialBound
        }

        result[index] = if (averageLoss <= 0.000001) {
            100.0
        } else {
            100.0 - 100.0 / (1.0 + averageGain / averageLoss)
        }
    }
    return result
}

internal fun enrichStockIndicators(
    candles: List<StockCandle>,
): List<StockCandle> {
    if (candles.isEmpty()) return emptyList()
    if (candles.size < 35) return candles

    val closes = candles.map { it.close }
    val ema12 = calculateStockEma(closes, 12)
    val ema26 = calculateStockEma(closes, 26)
    val macdLine = closes.indices.map { index ->
        ema12[index] - ema26[index]
    }
    val signalLine = MutableList(closes.size) { 0.0 }
    var macdSignalSeed = 0.0
    for (index in 25..33) macdSignalSeed += macdLine[index]
    signalLine[33] = macdSignalSeed / 9.0

    val signalMultiplier = 2.0 / 10.0
    for (index in 34 until closes.size) {
        signalLine[index] = macdLine[index] * signalMultiplier +
            signalLine[index - 1] * (1.0 - signalMultiplier)
    }

    val rsiValues = calculateStockRsi(closes)
    return candles.mapIndexed { index, candle ->
        val histogram = macdLine[index] - signalLine[index]
        val previousHistogram = if (index > 33) {
            macdLine[index - 1] - signalLine[index - 1]
        } else {
            null
        }
        val alert = when {
            previousHistogram != null && previousHistogram <= 0.0 && histogram > 0.0 -> "BUY"
            previousHistogram != null && previousHistogram >= 0.0 && histogram < 0.0 -> "SELL"
            else -> null
        }

        candle.copy(
            rsi = rsiValues[index],
            macd = macdLine[index],
            signal = signalLine[index],
            histogram = histogram,
            alert = alert,
        )
    }
}

internal fun clampStockChartTransform(
    transform: StockChartTransform,
    viewportWidth: Float,
): StockChartTransform {
    if (viewportWidth <= 0f) return transform.copy(zoom = 1f, panX = 0f)

    val zoom = transform.zoom.coerceIn(1f, 25f)
    val contentWidth = viewportWidth * zoom
    val minimumPan = -(contentWidth - viewportWidth)
    val panX = transform.panX.coerceIn(minimumPan, 0f)
    return StockChartTransform(zoom = zoom, panX = panX)
}

internal fun stockChartZoomTransform(
    transform: StockChartTransform,
    focusX: Float,
    zoomFactor: Float,
    viewportWidth: Float,
): StockChartTransform {
    val previous = clampStockChartTransform(
        transform = transform,
        viewportWidth = viewportWidth,
    )

    val zoom = (previous.zoom * zoomFactor).coerceIn(1f, 25f)
    val focusedPan = focusX - (
        focusX - previous.panX
        ) * (zoom / previous.zoom)

    return clampStockChartTransform(
        transform = StockChartTransform(zoom = zoom, panX = focusedPan),
        viewportWidth = viewportWidth,
    )
}

internal fun stockChartPanTransform(
    transform: StockChartTransform,
    panDeltaX: Float,
    viewportWidth: Float,
): StockChartTransform {
    val clamped = clampStockChartTransform(
        transform = transform,
        viewportWidth = viewportWidth,
    )
    return clampStockChartTransform(
        transform = clamped.copy(panX = clamped.panX + panDeltaX),
        viewportWidth = viewportWidth,
    )
}

internal fun defaultStockChartTransform(
    candleCount: Int,
    viewportWidth: Float,
): StockChartTransform {
    if (candleCount <= 0 || viewportWidth <= 0f) return StockChartTransform()

    val zoom = (candleCount / 24.0f).coerceIn(1.4f, 8.0f)
    val contentWidth = viewportWidth * zoom
    val centeredPan = -(contentWidth - viewportWidth) / 2f

    return clampStockChartTransform(
        transform = StockChartTransform(zoom = zoom, panX = centeredPan),
        viewportWidth = viewportWidth,
    )
}

internal fun applyStockChartGesture(
    transform: StockChartTransform,
    panX: Float,
    zoom: Float,
    focusX: Float,
    viewportWidth: Float,
): StockChartTransform {
    if (viewportWidth <= 0f) return transform

    var updated = clampStockChartTransform(
        transform = transform,
        viewportWidth = viewportWidth,
    )
    if (panX != 0f) {
        updated = stockChartPanTransform(
            transform = updated,
            panDeltaX = panX,
            viewportWidth = viewportWidth,
        )
    }
    if (zoom != 1f) {
        updated = stockChartZoomTransform(
            transform = updated,
            focusX = focusX,
            zoomFactor = zoom,
            viewportWidth = viewportWidth,
        )
    }
    return updated
}

internal fun stockCandleIndexAt(
    x: Float,
    transform: StockChartTransform,
    candleCount: Int,
    graphWidth: Float,
): Int? {
    if (candleCount <= 0 || graphWidth <= 0f) return null
    if (x < 0f || x > graphWidth) return null

    val totalWidth = graphWidth * transform.zoom
    val index = ((x - transform.panX) / totalWidth * candleCount).toInt()
    return index.takeIf { it in 0 until candleCount }
}

internal fun applyStockWaveCycle(
    candles: List<StockCandle>,
    ticks: Int,
): List<StockCandle> {
    if (ticks <= 0 || candles.isEmpty()) return candles

    var shifted = candles.toMutableList()
    val wave = sin(ticks / 35.0 * Math.PI) * shifted.last().close * 0.006

    val insertCount = ticks / 5
    repeat(insertCount) { shiftIndex ->
        val nextOpen = shifted.last().close
        shifted.removeAt(index = 0)
        shifted.add(
            StockCandle(
                open = nextOpen,
                high = nextOpen,
                low = nextOpen,
                close = nextOpen,
            ),
        )
    }

    val animated = shifted.mapIndexed { index, candle ->
        val targetClose = if (index == shifted.lastIndex) candle.close + wave else candle.close
        val targetHigh = if (index == shifted.lastIndex) {
            max(candle.high, targetClose)
        } else {
            candle.high
        }
        val targetLow = if (index == shifted.lastIndex) {
            min(candle.low, targetClose)
        } else {
            candle.low
        }

        StockCandle(
            open = candle.open + (targetClose - candle.open) * 0.22,
            high = targetHigh,
            low = targetLow,
            close = targetClose,
        )
    }

    return enrichStockIndicators(animated)
}

internal fun calculateStockRangeMetrics(
    candles: List<StockCandle>,
    firstAnchorIndex: Int?,
    secondAnchorIndex: Int?,
): StockRangeMetrics? {
    if (firstAnchorIndex == null || secondAnchorIndex == null) return null
    if (candles.isEmpty()) return null

    val startIndex = min(firstAnchorIndex, secondAnchorIndex)
        .coerceIn(0, candles.lastIndex)
    val endIndex = max(firstAnchorIndex, secondAnchorIndex)
        .coerceIn(0, candles.lastIndex)

    val subset = candles.subList(startIndex, endIndex + 1)
    if (subset.isEmpty()) return null

    val openingPrice = subset.first().open
    val closingPrice = subset.last().close
    val netPercent = if (openingPrice == 0.0) {
        0.0
    } else {
        (closingPrice - openingPrice) / openingPrice * 100.0
    }

    return StockRangeMetrics(
        high = subset.maxOf { it.high },
        low = subset.minOf { it.low },
        netPercent = netPercent,
        buyAlerts = subset.count { it.alert == "BUY" },
        sellAlerts = subset.count { it.alert == "SELL" },
        candleCount = subset.size,
    )
}

internal fun formatStockMoney(value: Double): String =
    String.format(Locale.US, "$%.2f", value)

internal fun formatStockDelta(value: Double): String =
    String.format(Locale.US, "%+.2f", value)

internal fun formatStockPercent(value: Double): String =
    String.format(Locale.US, "%+.2f%%", value)

internal fun formatStockAmount(value: Double): String =
    String.format(Locale.US, "%.2f", value)
