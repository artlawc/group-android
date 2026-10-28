package com.groupa.presentation.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.floor
import kotlin.math.sin

private val TearSyncBackground = Color(0xFF030407)
private val TearSyncCyan = Color(0xFF00F0FF)
private val TearSyncCrimson = Color(0xFFFF003C)
private val TearSyncYellow = Color(0xFFFCEE0A)

internal fun cyberpunkEntryTransitionShouldPlay(
    modelReady: Boolean,
    hasPlayed: Boolean,
): Boolean = modelReady && !hasPlayed

internal fun cyberpunkTearSyncThreshold(progress: Float): Float {
    require(progress in 0f..1f) { "progress must be between 0 and 1" }
    return progress * 1.5f
}

internal fun cyberpunkTearSyncGlitchIntensity(progress: Float): Float {
    require(progress in 0f..1f) { "progress must be between 0 and 1" }
    return 0.85f * (1f - progress)
}

internal fun cyberpunkHash(
    x: Float,
    y: Float,
): Float {
    val value = sin(x * 127.1f + y * 311.7f) * 43_758.545f
    return value - floor(value)
}

internal fun cyberpunkTearSyncRevealPath(
    width: Float,
    height: Float,
    progress: Float,
): Path {
    require(width > 0f && height > 0f) { "width and height must be positive" }
    val threshold = cyberpunkTearSyncThreshold(progress)
    val path = Path()

    path.moveTo(x = 0f, y = 0f)
    if (threshold <= 1f) {
        path.lineTo(x = width * threshold, y = 0f)
        path.lineTo(x = 0f, y = height * threshold * 2f)
    } else {
        path.lineTo(x = width, y = 0f)
        path.lineTo(x = width, y = height * (threshold - 1f) * 2f)
        path.lineTo(x = width * (threshold - 0.5f), y = height)
        path.lineTo(x = 0f, y = height)
    }
    path.close()
    return path
}

@Composable
internal fun CyberpunkTearSyncTransition(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    if (progress >= 1f) return

    val infiniteTransition = rememberInfiniteTransition(label = "tearSyncTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
        ),
        label = "tearSyncTime",
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds(),
    ) {
        val safeProgress = progress.coerceIn(0f, 1f)
        val intensity = cyberpunkTearSyncGlitchIntensity(safeProgress)
        val threshold = cyberpunkTearSyncThreshold(safeProgress)

        drawRect(color = TearSyncBackground)
        drawPath(
            path = cyberpunkTearSyncRevealPath(
                width = size.width,
                height = size.height,
                progress = safeProgress,
            ),
            color = Color.Transparent,
            blendMode = BlendMode.Clear,
        )

        drawRgbTearing(time = time, intensity = intensity)
        drawDissolveBoundary(
            threshold = threshold,
            intensity = intensity,
            time = time,
        )
        drawScanlines(alpha = 0.2f * (1f - safeProgress))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRgbTearing(
    time: Float,
    intensity: Float,
) {
    val redOffset = 0.015f * intensity
    val blueOffset = -0.010f * intensity

    repeat(3) { stripeIndex ->
        val x = (
            (time * 0.8f + stripeIndex * 0.33f) % 1f
            ) * size.width
        drawRect(
            color = Color(0xFFFF0040).copy(alpha = 0.10f * intensity),
            topLeft = Offset(x = x + redOffset * size.width, y = 0f),
            size = Size(width = size.width * 0.04f, height = size.height),
        )
        drawRect(
            color = Color(0xFF00F0FF).copy(alpha = 0.08f * intensity),
            topLeft = Offset(x = x, y = 0f),
            size = Size(width = size.width * 0.04f, height = size.height),
        )
        drawRect(
            color = Color(0xFF4060FF).copy(alpha = 0.10f * intensity),
            topLeft = Offset(x = x + blueOffset * size.width, y = 0f),
            size = Size(width = size.width * 0.04f, height = size.height),
        )
    }

    val sliceCount = 25
    val sliceHeight = size.height / sliceCount
    val timeSlot = floor(time * 15f)
    for (sliceIndex in 0 until sliceCount) {
        val sliceHash = cyberpunkHash(
            x = sliceIndex.toFloat(),
            y = timeSlot,
        )
        if (sliceHash < intensity * 0.7f) {
            val offset = (
                cyberpunkHash(sliceIndex.toFloat(), time) - 0.5f
                ) * 0.12f * size.width * intensity
            drawRect(
                color = if (sliceIndex % 2 == 0) {
                    TearSyncCyan.copy(alpha = 0.14f * intensity)
                } else {
                    TearSyncCrimson.copy(alpha = 0.12f * intensity)
                },
                topLeft = Offset(x = offset, y = sliceIndex * sliceHeight),
                size = Size(width = size.width, height = sliceHeight),
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDissolveBoundary(
    threshold: Float,
    intensity: Float,
    time: Float,
) {
    if (intensity <= 0.01f) return

    val boundaryPath = Path()
    var started = false
    for (step in 0..80) {
        val progress = step / 80f
        val x = size.width * progress
        val y = size.height * 2f * (threshold - progress)
        if (y !in 0f..size.height) continue
        if (!started) {
            boundaryPath.moveTo(x = x, y = y)
            started = true
        } else {
            boundaryPath.lineTo(x = x, y = y)
        }
    }

    drawPath(
        path = boundaryPath,
        color = TearSyncCyan.copy(alpha = 0.75f * intensity),
        style = Stroke(width = 3.dp.toPx()),
    )
    drawPath(
        path = boundaryPath,
        color = TearSyncCrimson.copy(alpha = 0.55f * intensity),
        style = Stroke(width = 7.dp.toPx()),
    )

    repeat(24) { index ->
        val x = cyberpunkHash(index.toFloat(), time) * size.width
        val y = size.height * 2f * (
            threshold - x / size.width
            ) + (cyberpunkHash(time, index.toFloat()) - 0.5f) * 20.dp.toPx()
        if (y !in 0f..size.height) return@repeat
        val color = when (index % 3) {
            0 -> TearSyncCyan
            1 -> TearSyncCrimson
            else -> TearSyncYellow
        }
        val sizePx = (2f + cyberpunkHash(index.toFloat(), threshold) * 5f).dp.toPx()
        drawRect(
            color = color.copy(alpha = 0.75f * intensity),
            topLeft = Offset(x = x, y = y),
            size = Size(width = sizePx, height = sizePx),
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawScanlines(
    alpha: Float,
) {
    if (alpha <= 0.01f) return
    val step = 4.dp.toPx()
    var y = 0f
    while (y < size.height) {
        drawLine(
            color = Color.Black.copy(alpha = alpha),
            start = Offset(x = 0f, y = y),
            end = Offset(x = size.width, y = y),
            strokeWidth = 1f,
        )
        y += step
    }
}
