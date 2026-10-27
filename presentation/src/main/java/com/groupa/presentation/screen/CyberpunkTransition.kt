package com.groupa.presentation.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.tan

internal const val CYBERPUNK_TRANSITION_DURATION_MILLIS = 600

private val cyberpunkCyan = Color(0xFF00F0FF)
private val cyberpunkMagenta = Color(0xFFFF0055)
private val cyberpunkYellow = Color(0xFFFCEE0A)

internal data class CyberpunkTransitionFrame(
    val sourceAlpha: Float,
    val sourceScaleX: Float,
    val sourceScaleY: Float,
    val sourceSkewX: Float,
    val overlayAlpha: Float,
)

internal fun cyberpunkTransitionFrame(progress: Float): CyberpunkTransitionFrame {
    require(progress in 0f..1f) { "progress must be between 0 and 1" }
    val collapsePhase = (progress / 0.3f).coerceIn(0f, 1f)
    val tearPhase = ((progress - 0.3f) / 0.3f).coerceIn(0f, 1f)
    val fadePhase = ((progress - 0.6f) / 0.4f).coerceIn(0f, 1f)
    val scaleXAfterCollapse = interpolate(1.3f, 2f, fadePhase)

    return CyberpunkTransitionFrame(
        sourceAlpha = interpolate(1f, 0f, fadePhase),
        sourceScaleX = if (progress <= 0.3f) {
            interpolate(1f, 1.3f, collapsePhase)
        } else {
            scaleXAfterCollapse
        },
        sourceScaleY = interpolate(1f, 0.9f, collapsePhase),
        sourceSkewX = if (progress <= 0.3f) {
            interpolate(0f, -10f, collapsePhase)
        } else {
            interpolate(-10f, 30f, tearPhase)
        },
        overlayAlpha = if (progress <= 0f) 0f else interpolate(1f, 0f, fadePhase),
    )
}

internal fun Modifier.cyberpunkSourceTransform(progress: Float): Modifier {
    val frame = cyberpunkTransitionFrame(progress)

    return graphicsLayer {
        alpha = frame.sourceAlpha
    }
        .clipToBounds()
        .drawWithContent {
            val content = this@drawWithContent
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val skewX = tan(Math.toRadians(frame.sourceSkewX.toDouble())).toFloat()
            val matrix = Matrix()
            matrix.values[Matrix.ScaleX] = frame.sourceScaleX
            matrix.values[Matrix.ScaleY] = frame.sourceScaleY
            matrix.values[Matrix.SkewX] = skewX
            matrix.values[Matrix.TranslateX] =
                centerX - frame.sourceScaleX * centerX - skewX * centerY
            matrix.values[Matrix.TranslateY] = centerY - frame.sourceScaleY * centerY

            withTransform(
                transformBlock = { transform(matrix) },
            ) {
                content.drawContent()
            }
        }
}

@Composable
fun CyberpunkScreenEntry(
    content: @Composable () -> Unit,
) {
    val progress = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = CYBERPUNK_TRANSITION_DURATION_MILLIS,
                easing = CubicBezierEasing(0.15f, 0.85f, 0.3f, 1f),
            ),
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .cyberpunkSourceTransform(progress.value),
        ) {
            content()
        }
        if (progress.value > 0f) {
            CyberpunkGlitchOverlay(progress = progress.value)
        }
    }
}

@Composable
internal fun CyberpunkGlitchOverlay(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val frame = cyberpunkTransitionFrame(progress)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds(),
    ) {
        if (frame.overlayAlpha <= 0f) return@Canvas

        val bands = listOf(
            CyberpunkTearBand(0.12f, 0.75f, cyberpunkMagenta),
            CyberpunkTearBand(0.40f, 0.42f, cyberpunkCyan),
            CyberpunkTearBand(0.70f, 0.15f, cyberpunkYellow),
            CyberpunkTearBand(0.85f, 0.02f, Color.White),
        )

        bands.forEachIndexed { index, band ->
            val phase = ((progress * 4f + index) % 1f)
            val direction = if (index % 2 == 0) -1f else 1f
            val horizontalShift = direction * size.width * 0.06f * (1f - phase)
            val bandHeight = size.height * (band.topInset + band.bottomInset)

            drawRect(
                color = band.color.copy(alpha = frame.overlayAlpha * 0.30f),
                topLeft = Offset(
                    x = horizontalShift,
                    y = size.height * band.topInset,
                ),
                size = Size(
                    width = size.width,
                    height = bandHeight,
                ),
                blendMode = BlendMode.Screen,
            )
        }

        drawRect(
            color = cyberpunkCyan.copy(alpha = frame.overlayAlpha * 0.22f),
            topLeft = Offset(x = 0f, y = size.height * 0.48f),
            size = Size(width = size.width, height = size.height * 0.04f),
            blendMode = BlendMode.Screen,
        )
    }
}

private data class CyberpunkTearBand(
    val topInset: Float,
    val bottomInset: Float,
    val color: Color,
)

private fun interpolate(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}
