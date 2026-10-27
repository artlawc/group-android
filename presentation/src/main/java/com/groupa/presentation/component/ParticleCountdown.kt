package com.groupa.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private const val PARTICLE_MORPH_DURATION_MILLIS = 800
private const val PARTICLE_STEPS_PER_SEGMENT = 16
private const val SLOT_DESIGN_WIDTH = 75f
private const val SLOT_DESIGN_HEIGHT = 120f

private enum class CountdownSegment {
    TOP,
    TOP_RIGHT,
    BOTTOM_RIGHT,
    BOTTOM,
    BOTTOM_LEFT,
    TOP_LEFT,
    MIDDLE,
}

private data class SegmentLine(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
)

private val segmentCoordinates = mapOf(
    CountdownSegment.TOP to SegmentLine(10f, 10f, 65f, 10f),
    CountdownSegment.TOP_RIGHT to SegmentLine(65f, 10f, 65f, 55f),
    CountdownSegment.BOTTOM_RIGHT to SegmentLine(65f, 55f, 65f, 100f),
    CountdownSegment.BOTTOM to SegmentLine(10f, 100f, 65f, 100f),
    CountdownSegment.BOTTOM_LEFT to SegmentLine(10f, 55f, 10f, 100f),
    CountdownSegment.TOP_LEFT to SegmentLine(10f, 10f, 10f, 55f),
    CountdownSegment.MIDDLE to SegmentLine(10f, 55f, 65f, 55f),
)

private val characterSegments = mapOf(
    '0' to listOf(
        CountdownSegment.TOP,
        CountdownSegment.TOP_RIGHT,
        CountdownSegment.BOTTOM_RIGHT,
        CountdownSegment.BOTTOM,
        CountdownSegment.BOTTOM_LEFT,
        CountdownSegment.TOP_LEFT,
    ),
    '1' to listOf(CountdownSegment.TOP_RIGHT, CountdownSegment.BOTTOM_RIGHT),
    '2' to listOf(
        CountdownSegment.TOP,
        CountdownSegment.TOP_RIGHT,
        CountdownSegment.MIDDLE,
        CountdownSegment.BOTTOM_LEFT,
        CountdownSegment.BOTTOM,
    ),
    '3' to listOf(
        CountdownSegment.TOP,
        CountdownSegment.TOP_RIGHT,
        CountdownSegment.MIDDLE,
        CountdownSegment.BOTTOM_RIGHT,
        CountdownSegment.BOTTOM,
    ),
    '4' to listOf(
        CountdownSegment.TOP_LEFT,
        CountdownSegment.MIDDLE,
        CountdownSegment.TOP_RIGHT,
        CountdownSegment.BOTTOM_RIGHT,
    ),
    '5' to listOf(
        CountdownSegment.TOP,
        CountdownSegment.TOP_LEFT,
        CountdownSegment.MIDDLE,
        CountdownSegment.BOTTOM_RIGHT,
        CountdownSegment.BOTTOM,
    ),
    '6' to listOf(
        CountdownSegment.TOP,
        CountdownSegment.TOP_LEFT,
        CountdownSegment.BOTTOM_LEFT,
        CountdownSegment.BOTTOM,
        CountdownSegment.BOTTOM_RIGHT,
        CountdownSegment.MIDDLE,
    ),
    '7' to listOf(
        CountdownSegment.TOP,
        CountdownSegment.TOP_RIGHT,
        CountdownSegment.BOTTOM_RIGHT,
    ),
    '8' to CountdownSegment.entries,
    '9' to listOf(
        CountdownSegment.TOP,
        CountdownSegment.TOP_RIGHT,
        CountdownSegment.BOTTOM_RIGHT,
        CountdownSegment.BOTTOM,
        CountdownSegment.TOP_LEFT,
        CountdownSegment.MIDDLE,
    ),
    ':' to emptyList(),
)

internal data class ParticleCountdownPoint(
    val x: Float,
    val y: Float,
    val visible: Boolean = true,
)

internal data class ParticleCountdownParticle(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val startAlpha: Float,
    val endAlpha: Float,
    val burstX: Float,
    val burstY: Float,
    val radius: Float,
)

internal data class ParticleCountdownSlot(
    val sourceCharacter: Char,
    val targetCharacter: Char,
    val particles: List<ParticleCountdownParticle>,
)

internal fun formatParticleCountdown(totalSeconds: Int): String {
    require(totalSeconds >= 0) { "totalSeconds must not be negative" }
    val hours = totalSeconds / 3_600
    val minutes = totalSeconds % 3_600 / 60
    val seconds = totalSeconds % 60
    return listOf(hours, minutes, seconds).joinToString(separator = ":") { value ->
        value.toString().padStart(2, '0')
    }
}

internal fun particleCountdownTargetPoints(
    character: Char,
    random: Random,
): List<ParticleCountdownPoint> {
    if (character == ':') {
        return buildList {
            repeat(15) {
                add(
                    ParticleCountdownPoint(
                        x = 20f + random.nextFloat() * 6f,
                        y = 35f + random.nextFloat() * 6f,
                    ),
                )
            }
            repeat(15) {
                add(
                    ParticleCountdownPoint(
                        x = 20f + random.nextFloat() * 6f,
                        y = 75f + random.nextFloat() * 6f,
                    ),
                )
            }
        }
    }

    val points = characterSegments[character].orEmpty().flatMap { segment ->
        val line = segmentCoordinates.getValue(segment)
        (0..PARTICLE_STEPS_PER_SEGMENT).map { step ->
            val progress = step.toFloat() / PARTICLE_STEPS_PER_SEGMENT
            ParticleCountdownPoint(
                x = line.startX +
                    (line.endX - line.startX) * progress +
                    (random.nextFloat() - 0.5f) * 2f,
                y = line.startY +
                    (line.endY - line.startY) * progress +
                    (random.nextFloat() - 0.5f) * 2f,
            )
        }
    }

    return points.ifEmpty {
        listOf(ParticleCountdownPoint(x = 37.5f, y = 55f, visible = false))
    }
}

internal fun particleCountdownMorphParticles(
    sourceCharacter: Char,
    targetCharacter: Char,
    random: Random,
): List<ParticleCountdownParticle> {
    val sourcePoints = particleCountdownTargetPoints(sourceCharacter, random)
    val targetPoints = particleCountdownTargetPoints(targetCharacter, random)
    val particleCount = maxOf(sourcePoints.size, targetPoints.size)

    return List(particleCount) { index ->
        val source = sourcePoints[index % sourcePoints.size]
        val target = targetPoints[index % targetPoints.size]
        ParticleCountdownParticle(
            startX = source.x,
            startY = source.y,
            endX = target.x,
            endY = target.y,
            startAlpha = if (source.visible) 1f else 0f,
            endAlpha = if (target.visible) 1f else 0f,
            burstX = (random.nextFloat() - 0.5f) * 25f,
            burstY = (random.nextFloat() - 0.5f) * 25f,
            radius = 1.5f + random.nextFloat() * 2f,
        )
    }
}

internal fun particleCountdownSettledParticles(
    character: Char,
    random: Random,
): List<ParticleCountdownParticle> =
    particleCountdownTargetPoints(character, random).map { point ->
        ParticleCountdownParticle(
            startX = point.x,
            startY = point.y,
            endX = point.x,
            endY = point.y,
            startAlpha = if (point.visible) 1f else 0f,
            endAlpha = if (point.visible) 1f else 0f,
            burstX = 0f,
            burstY = 0f,
            radius = 1.5f + random.nextFloat() * 2f,
        )
    }

private fun initialParticleCountdownSlots(
    text: String,
): List<ParticleCountdownSlot> = text.map { character ->
    ParticleCountdownSlot(
        sourceCharacter = character,
        targetCharacter = character,
        particles = particleCountdownSettledParticles(character, Random),
    )
}

internal fun updatedParticleCountdownSlots(
    sourceCharacters: List<Char>,
    targetText: String,
    allDigitsBlink: Boolean,
    random: Random,
) = targetText.mapIndexed { index, targetCharacter ->
    val sourceCharacter = sourceCharacters[index]
    val shouldBlink = allDigitsBlink ||
        sourceCharacter != targetCharacter ||
        targetCharacter == ':'
    ParticleCountdownSlot(
        sourceCharacter = sourceCharacter,
        targetCharacter = targetCharacter,
        particles = if (shouldBlink) {
            particleCountdownMorphParticles(sourceCharacter, targetCharacter, random)
        } else {
            particleCountdownSettledParticles(targetCharacter, random)
        },
    )
}

@Composable
internal fun ParticleCountdown(
    totalSeconds: Int,
    color: Color,
    modifier: Modifier = Modifier,
    allDigitsBlink: Boolean = false,
) {
    val countdownText = formatParticleCountdown(totalSeconds)
    var slots by remember {
        mutableStateOf(initialParticleCountdownSlots(countdownText))
    }
    val morphProgress = remember { Animatable(1f) }

    LaunchedEffect(countdownText) {
        val sourceCharacters = slots.map { slot -> slot.targetCharacter }
        if (sourceCharacters == countdownText.toList()) return@LaunchedEffect

        slots = updatedParticleCountdownSlots(
            sourceCharacters = sourceCharacters,
            targetText = countdownText,
            allDigitsBlink = allDigitsBlink,
            random = Random,
        )
        morphProgress.snapTo(0f)
        morphProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = PARTICLE_MORPH_DURATION_MILLIS),
        )
    }

    Canvas(modifier = modifier) {
        val slotCount = slots.size.coerceAtLeast(1)
        val slotWidth = size.width / slotCount
        val scale = min(
            slotWidth / SLOT_DESIGN_WIDTH,
            size.height / SLOT_DESIGN_HEIGHT,
        )
        val digitWidth = SLOT_DESIGN_WIDTH * scale
        val digitHeight = SLOT_DESIGN_HEIGHT * scale
        val topPadding = (size.height - digitHeight) / 2f
        val progress = morphProgress.value
        val easedProgress = easeOutQuart(progress)
        val burstFactor = sin(progress * Math.PI.toFloat())

        slots.forEachIndexed { index, slot ->
            val leftPadding = index * slotWidth + (slotWidth - digitWidth) / 2f
            slot.particles.forEach { particle ->
                val xDelta = particle.endX - particle.startX
                val yDelta = particle.endY - particle.startY
                val x = particle.startX +
                    xDelta * easedProgress +
                    particle.burstX * burstFactor * (1f - progress)
                val y = particle.startY +
                    yDelta * easedProgress +
                    particle.burstY * burstFactor * (1f - progress)
                val alpha = particle.startAlpha +
                    (particle.endAlpha - particle.startAlpha) * easedProgress
                if (alpha <= 0.01f) return@forEach

                val center = Offset(
                    x = leftPadding + x * scale,
                    y = topPadding + y * scale,
                )
                val radius = particle.radius * scale
                drawCircle(
                    color = color.copy(alpha = alpha * 0.18f),
                    radius = radius * 2.4f,
                    center = center,
                )
                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = radius,
                    center = center,
                )
            }
        }
    }
}

private fun easeOutQuart(progress: Float): Float {
    val remaining = 1f - progress
    return 1f - remaining * remaining * remaining * remaining
}
