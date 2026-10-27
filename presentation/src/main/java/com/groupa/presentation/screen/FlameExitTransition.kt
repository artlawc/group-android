package com.groupa.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

internal const val FLAME_EXIT_TRANSITION_DURATION_MILLIS = 900

private val flameCharred = Color(0xFF141210)
private val flameDeepOrange = Color(0xFFEA580C)
private val flameBrightOrange = Color(0xFFF97316)

internal enum class FlameIgnitionMode {
    TOP,
    BOTTOM,
    CENTER,
}

internal fun randomFlameIgnitionMode(
    random: Random = Random.Default,
): FlameIgnitionMode {
    return FlameIgnitionMode.entries[random.nextInt(FlameIgnitionMode.entries.size)]
}

internal data class FlameBladePoint(
    val x: Float,
    val y: Float,
)

internal data class FlameParticle(
    val x: Float,
    val y: Float,
    val alpha: Float,
    val rotationDegrees: Float,
    val radius: Float,
    val seed: Float,
)

internal fun flameFrontierY(
    height: Float,
    progress: Float,
): Float {
    require(height >= 0f) { "height must not be negative" }
    require(progress in 0f..1f) { "progress must be between 0 and 1" }
    return height * progress
}

internal fun flameFrontierRadius(
    width: Float,
    height: Float,
    progress: Float,
): Float {
    require(width >= 0f) { "width must not be negative" }
    require(height >= 0f) { "height must not be negative" }
    require(progress in 0f..1f) { "progress must be between 0 and 1" }
    return sqrt(width * width + height * height) / 2f * progress
}

internal fun flameBladePoints(
    progress: Float,
    width: Float,
    height: Float,
    ignitionMode: FlameIgnitionMode,
    bladeCount: Int = 10,
): List<FlameBladePoint> {
    require(progress in 0f..1f) { "progress must be between 0 and 1" }
    require(width >= 0f) { "width must not be negative" }
    require(height >= 0f) { "height must not be negative" }
    require(bladeCount >= 3) { "bladeCount must be at least 3" }
    if (width == 0f || height == 0f) return emptyList()

    return when (ignitionMode) {
        FlameIgnitionMode.TOP -> {
            val frontierY = flameFrontierY(height, progress)
            val amplitude = height * 0.045f * sin(PI * progress).toFloat().coerceAtLeast(0f)
            List(bladeCount + 1) { index ->
                val x = width * index / bladeCount
                val y = frontierY +
                    amplitude * sin(index * 2.4f + progress * 9f) +
                    amplitude * 0.45f * cos(index * 5.1f + progress * 13f)
                FlameBladePoint(x = x, y = y.coerceIn(0f, height))
            }
        }

        FlameIgnitionMode.BOTTOM -> {
            val frontierY = height * (1f - progress)
            val amplitude = height * 0.045f * sin(PI * progress).toFloat().coerceAtLeast(0f)
            List(bladeCount + 1) { index ->
                val x = width * index / bladeCount
                val y = frontierY -
                    amplitude * sin(index * 2.4f + progress * 9f) -
                    amplitude * 0.45f * cos(index * 5.1f + progress * 13f)
                FlameBladePoint(x = x, y = y.coerceIn(0f, height))
            }
        }

        FlameIgnitionMode.CENTER -> {
            val frontierRadius = flameFrontierRadius(width, height, progress)
            List(bladeCount + 1) { index ->
                val angle = 2f * PI * index / bladeCount
                val bladeWave = 1f +
                    0.08f * sin(index * 3.1f + progress * 10f) +
                    0.05f * cos(index * 5.7f + progress * 14f)
                val radius = frontierRadius * bladeWave
                FlameBladePoint(
                    x = width / 2f + radius * cos(angle).toFloat(),
                    y = height / 2f + radius * sin(angle).toFloat(),
                )
            }
        }
    }
}

internal fun flameParticles(
    progress: Float,
    width: Float,
    height: Float,
    ignitionMode: FlameIgnitionMode,
    seed: Int = 0,
    particleCount: Int = 36,
): List<FlameParticle> {
    require(progress in 0f..1f) { "progress must be between 0 and 1" }
    require(width >= 0f) { "width must not be negative" }
    require(height >= 0f) { "height must not be negative" }
    require(particleCount >= 0) { "particleCount must not be negative" }
    if (width == 0f || height == 0f || particleCount == 0) return emptyList()

    val random = Random(seed)
    return List(particleCount) { index ->
        val upwardDrift = progress * (18f + random.nextFloat() * 34f)
        val position = when (ignitionMode) {
            FlameIgnitionMode.TOP -> {
                val frontierY = flameFrontierY(height, progress)
                Offset(
                    x = random.nextFloat() * width,
                    y = frontierY - upwardDrift + (random.nextFloat() - 0.5f) * 10f,
                )
            }

            FlameIgnitionMode.BOTTOM -> {
                val frontierY = height * (1f - progress)
                Offset(
                    x = random.nextFloat() * width,
                    y = frontierY - upwardDrift + (random.nextFloat() - 0.5f) * 10f,
                )
            }

            FlameIgnitionMode.CENTER -> {
                val frontierRadius = flameFrontierRadius(width, height, progress)
                val angle = random.nextFloat() * 2f * PI
                val boundaryRadius = frontierRadius * (0.78f + random.nextFloat() * 0.24f)
                Offset(
                    x = width / 2f + boundaryRadius * cos(angle).toFloat(),
                    y = height / 2f + boundaryRadius * sin(angle).toFloat() - upwardDrift,
                )
            }
        }

        FlameParticle(
            x = position.x,
            y = position.y,
            alpha = (1f - progress * 1.15f).coerceIn(0f, 1f),
            rotationDegrees = progress * (90f + random.nextFloat() * 180f),
            radius = 3f + random.nextFloat() * 8f,
            seed = random.nextFloat(),
        )
    }
}

@Composable
internal fun FlameExitTransition(
    progress: Float,
    ignitionMode: FlameIgnitionMode,
    background: Brush,
    seed: Int,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        content()
        FlameExitOverlay(
            progress = progress,
            ignitionMode = ignitionMode,
            background = background,
            seed = seed,
        )
    }
}

@Composable
private fun FlameExitOverlay(
    progress: Float,
    ignitionMode: FlameIgnitionMode,
    background: Brush,
    seed: Int,
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds(),
    ) {
        if (progress <= 0f) return@Canvas

        val bladePoints = flameBladePoints(
            progress = progress,
            width = size.width,
            height = size.height,
            ignitionMode = ignitionMode,
        )
        if (bladePoints.isEmpty()) return@Canvas

        drawPath(
            path = flameBurnPath(
                width = size.width,
                height = size.height,
                ignitionMode = ignitionMode,
                bladePoints = bladePoints,
            ),
            brush = background,
        )
        drawFlameBlades(ignitionMode, bladePoints)
        drawAshParticles(progress, ignitionMode, seed)
    }
}

private fun flameBurnPath(
    width: Float,
    height: Float,
    ignitionMode: FlameIgnitionMode,
    bladePoints: List<FlameBladePoint>,
): Path {
    val path = Path()

    when (ignitionMode) {
        FlameIgnitionMode.TOP -> {
            path.moveTo(0f, 0f)
            path.lineTo(width, 0f)
            path.lineTo(bladePoints.last().x, bladePoints.last().y)
            appendFlameCurve(path, bladePoints.reversed())
            path.lineTo(0f, 0f)
            path.close()
        }

        FlameIgnitionMode.BOTTOM -> {
            path.moveTo(0f, height)
            path.lineTo(width, height)
            path.lineTo(bladePoints.last().x, bladePoints.last().y)
            appendFlameCurve(path, bladePoints.reversed())
            path.lineTo(0f, height)
            path.close()
        }

        FlameIgnitionMode.CENTER -> {
            appendFlameCurve(path, bladePoints)
            path.close()
        }
    }

    return path
}

private fun DrawScope.drawFlameBlades(
    ignitionMode: FlameIgnitionMode,
    bladePoints: List<FlameBladePoint>,
) {
    val edgePath = Path()
    appendFlameCurve(edgePath, bladePoints)
    if (ignitionMode == FlameIgnitionMode.CENTER) {
        edgePath.close()
    }

    val edgeWidth = (size.minDimension * 0.0045f).coerceAtLeast(2.5f)
    drawPath(
        path = edgePath,
        color = flameDeepOrange.copy(alpha = 0.32f),
        style = Stroke(width = edgeWidth * 5.5f),
    )
    drawPath(
        path = edgePath,
        color = flameCharred.copy(alpha = 0.98f),
        style = Stroke(width = edgeWidth * 3f),
    )
    drawPath(
        path = edgePath,
        color = flameDeepOrange.copy(alpha = 0.95f),
        style = Stroke(width = edgeWidth * 1.7f),
    )
    drawPath(
        path = edgePath,
        color = flameBrightOrange,
        style = Stroke(width = edgeWidth),
    )
    drawPath(
        path = edgePath,
        color = flameDeepOrange.copy(alpha = 0.42f),
        style = Stroke(width = edgeWidth * 0.7f),
    )
}

private fun appendFlameCurve(
    path: Path,
    points: List<FlameBladePoint>,
) {
    if (points.isEmpty()) return
    path.moveTo(points.first().x, points.first().y)

    for (index in 1 until points.size) {
        val current = points[index - 1]
        val next = points[index]
        val deltaX = next.x - current.x
        val deltaY = next.y - current.y
        val segmentLength = sqrt(deltaX * deltaX + deltaY * deltaY).coerceAtLeast(1f)
        val normalX = -deltaY / segmentLength
        val normalY = deltaX / segmentLength
        val bend = segmentLength * 0.22f * if (index % 2 == 0) 1f else -1f

        path.cubicTo(
            x1 = current.x + deltaX * 0.25f + normalX * bend,
            y1 = current.y + deltaY * 0.25f + normalY * bend,
            x2 = current.x + deltaX * 0.75f - normalX * bend,
            y2 = current.y + deltaY * 0.75f - normalY * bend,
            x3 = next.x,
            y3 = next.y,
        )
    }
}

private fun DrawScope.drawAshParticles(
    progress: Float,
    ignitionMode: FlameIgnitionMode,
    seed: Int,
) {
    flameParticles(
        progress = progress,
        width = size.width,
            height = size.height,
            ignitionMode = ignitionMode,
            seed = seed,
        ).forEach { particle ->
        if (particle.alpha <= 0f) return@forEach
        val particlePath = Path()
        val angleStep = 360f / 6f
        for (vertexIndex in 0 until 6) {
            val angle = Math.toRadians(
                (particle.rotationDegrees + vertexIndex * angleStep).toDouble(),
            )
            val vertexRadius = particle.radius * (
                0.55f + 0.45f * sin(vertexIndex * 2.1f + particle.seed * 6.28f)
                )
            val vertexX = particle.x + vertexRadius * cos(angle).toFloat()
            val vertexY = particle.y + vertexRadius * sin(angle).toFloat()
            if (vertexIndex == 0) {
                particlePath.moveTo(vertexX, vertexY)
            } else {
                particlePath.lineTo(vertexX, vertexY)
            }
        }
        particlePath.close()
        drawPath(
            path = particlePath,
            color = Color(0xFF201D1B).copy(alpha = particle.alpha),
        )
        drawPath(
            path = particlePath,
            color = flameDeepOrange.copy(alpha = particle.alpha * 0.5f),
            style = Stroke(width = 1.2f),
        )
    }
}
