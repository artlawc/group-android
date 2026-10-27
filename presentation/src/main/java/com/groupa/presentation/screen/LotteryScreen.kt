package com.groupa.presentation.screen

import android.graphics.Paint
import android.graphics.Typeface
import androidx.annotation.StringRes
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groupa.coreui.R
import com.groupa.presentation.component.ParticleCountdown
import com.groupa.presentation.component.formatParticleCountdown
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private data class LotteryPrize(
    val label: String,
    val color: Color,
)

internal data class LotteryPrizeDefinition(
    @StringRes val labelRes: Int,
    val color: Color,
)

internal val lotteryPrizeDefinitions = listOf(
    LotteryPrizeDefinition(R.string.lottery_prize_50_off, Color(0xFF2D1654)),
    LotteryPrizeDefinition(R.string.lottery_prize_plus, Color(0xFF00B3FF)),
    LotteryPrizeDefinition(R.string.lottery_prize_50_discount, Color(0xFF2D1654)),
    LotteryPrizeDefinition(R.string.lottery_prize_10_off, Color(0xFFFF007F)),
    LotteryPrizeDefinition(R.string.lottery_prize_mystery_gift, Color(0xFF2D1654)),
    LotteryPrizeDefinition(R.string.lottery_prize_20_voucher, Color(0xFFB300FF)),
    LotteryPrizeDefinition(R.string.lottery_prize_free_shipping, Color(0xFF2D1654)),
    LotteryPrizeDefinition(R.string.lottery_prize_5_coins, Color(0xFFFFD700)),
)

private val lotteryDarkPurple = Color(0xFF0B0318)
private val lotteryCardPurple = Color(0xFF2D1654)
private val lotteryNeonCyan = Color(0xFF00F3FF)
private val lotteryNeonPink = Color(0xFFFF007F)
private val lotteryMint = Color(0xFF00FFCC)
private val lotteryGold = Color(0xFFFFD700)
private val lotteryDisabled = Color(0xFF4A3B68)

@Composable
fun LotteryScreen(
    onBack: () -> Unit,
) {
    var coins by rememberSaveable { mutableIntStateOf(245) }
    var secondsRemaining by rememberSaveable { mutableIntStateOf(0) }
    var isSpinning by rememberSaveable { mutableStateOf(false) }
    var rotationTarget by rememberSaveable { mutableFloatStateOf(0f) }
    var rewardMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var flameIgnitionMode by remember { mutableStateOf(FlameIgnitionMode.TOP) }
    var flameSeed by remember { mutableStateOf(0) }
    val exitProgress = remember { androidx.compose.animation.core.Animatable(0f) }
    val rotation by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = tween(
            durationMillis = 4_000,
            easing = CubicBezierEasing(0.15f, 0.85f, 0.35f, 1f),
        ),
        label = "lotteryWheelRotation",
    )
    val coroutineScope = rememberCoroutineScope()
    val wonTemplate = stringResource(R.string.lottery_won, "%s")
    val boostedMessage = stringResource(R.string.lottery_time_boosted)
    val lotteryPrizes = lotteryPrizeDefinitions.map { definition ->
        LotteryPrize(
            label = stringResource(definition.labelRes),
            color = definition.color,
        )
    }
    val canSpin = !isSpinning && secondsRemaining == 0
    val canBoost = !isSpinning && secondsRemaining > 0 && coins >= 50

    fun exitWithFlame() {
        if (exitProgress.value > 0f) return
        flameIgnitionMode = randomFlameIgnitionMode()
        flameSeed = Random.nextInt()
        coroutineScope.launch {
            exitProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = FLAME_EXIT_TRANSITION_DURATION_MILLIS,
                    easing = CubicBezierEasing(0.15f, 0.85f, 0.3f, 1f),
                ),
            )
            onBack()
        }
    }

    BackHandler {
        exitWithFlame()
    }

    LaunchedEffect(secondsRemaining) {
        if (secondsRemaining > 0) {
            delay(1_000)
            secondsRemaining -= 1
        }
    }

    FlameExitTransition(
        progress = exitProgress.value,
        ignitionMode = flameIgnitionMode,
        background = Brush.verticalGradient(
            listOf(Color(0xFF2D124D), lotteryDarkPurple),
        ),
        seed = flameSeed,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF2D124D), lotteryDarkPurple),
                    ),
                )
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = ::exitWithFlame) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = Color.White,
                )
            }
            Text(
                text = "GroupGO",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f),
            )
            Surface(
                shape = CircleShape,
                color = lotteryGold.copy(alpha = 0.15f),
            ) {
                Text(
                    text = "$coins ${stringResource(R.string.lottery_coins)}",
                    color = lotteryGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                )
            }
        }

        Text(
            text = stringResource(R.string.title_lottery),
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
        )
        Text(
            text = stringResource(
                if (secondsRemaining == 0) {
                    R.string.lottery_status_available
                } else {
                    R.string.lottery_status_locked
                },
            ),
            color = Color(0xFFB3A1DF),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp),
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = lotteryCardPurple,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Column(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(
                        if (secondsRemaining == 0) {
                            R.string.lottery_status_available_label
                        } else {
                            R.string.lottery_next_free_spin
                        },
                    ),
                    color = Color(0xFFB3A1DF),
                    fontSize = 11.sp,
                )
                ParticleCountdown(
                    totalSeconds = secondsRemaining,
                    color = lotteryNeonCyan,
                    allDigitsBlink = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape)
                    .background(lotteryCardPurple),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = rotation
                        },
                ) {
                    val diameter = min(size.width, size.height)
                    val radius = diameter / 2f
                    val topLeft = Offset(
                        x = (size.width - diameter) / 2f,
                        y = (size.height - diameter) / 2f,
                    )
                    val sectorAngle = 360f / lotteryPrizes.size
                    val nativeCanvas = drawContext.canvas.nativeCanvas
                    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        textAlign = Paint.Align.RIGHT
                        textSize = 12.dp.toPx()
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }

                    lotteryPrizes.forEachIndexed { index, prize ->
                        val startAngle = index * sectorAngle
                        drawArc(
                            color = prize.color,
                            startAngle = startAngle,
                            sweepAngle = sectorAngle,
                            useCenter = true,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                        )
                        val boundaryAngle = Math.toRadians(startAngle.toDouble())
                        drawLine(
                            color = Color.White.copy(alpha = 0.15f),
                            start = Offset(size.width / 2f, size.height / 2f),
                            end = Offset(
                                x = size.width / 2f + radius * cos(boundaryAngle).toFloat(),
                                y = size.height / 2f + radius * sin(boundaryAngle).toFloat(),
                            ),
                            strokeWidth = 2f,
                        )

                        val labelPlacement = lotteryLabelPlacement(
                            cutLineAngle = startAngle,
                            wheelRadius = radius,
                            rimPadding = 18.dp.toPx(),
                            perpendicularInset = 12.dp.toPx(),
                        )
                        labelPaint.color = if (prize.color == lotteryCardPurple) {
                            Color.White.toArgb()
                        } else {
                            lotteryDarkPurple.toArgb()
                        }
                        nativeCanvas.save()
                        nativeCanvas.rotate(
                            labelPlacement.angleDegrees,
                            size.width / 2f,
                            size.height / 2f,
                        )
                        nativeCanvas.drawText(
                            prize.label,
                            size.width / 2f + labelPlacement.anchorRadius,
                            size.height / 2f + labelPlacement.perpendicularOffset,
                            labelPaint,
                        )
                        nativeCanvas.restore()
                    }
                }

                Canvas(
                    modifier = Modifier.size(46.dp),
                ) {
                    drawCircle(color = Color.White)
                    drawCircle(
                        color = lotteryNeonPink,
                        radius = size.minDimension / 2f - 5f,
                        style = Stroke(width = 5f),
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(width = 32.dp, height = 38.dp),
            ) {
                val pointer = Path().apply {
                    moveTo(size.width / 2f, size.height)
                    lineTo(0f, 0f)
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(pointer, lotteryNeonPink)
            }
        }

        if (rewardMessage != null) {
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = lotteryMint.copy(alpha = 0.95f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
            ) {
                Text(
                    text = rewardMessage.orEmpty(),
                    color = lotteryDarkPurple,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                if (!canSpin) return@Button
                val selectedIndex = Random.nextInt(lotteryPrizes.size)
                val selectedPrize = lotteryPrizes[selectedIndex]
                isSpinning = true
                rewardMessage = null
                rotationTarget = nextLotteryRotation(
                    currentRotation = rotationTarget,
                    selectedIndex = selectedIndex,
                    sectorCount = lotteryPrizes.size,
                )
                coroutineScope.launch {
                    delay(4_000)
                    isSpinning = false
                    secondsRemaining = 3_600
                    rewardMessage = wonTemplate.replace("%s", selectedPrize.label)
                    delay(2_500)
                    if (rewardMessage == wonTemplate.replace("%s", selectedPrize.label)) {
                        rewardMessage = null
                    }
                }
            },
            enabled = canSpin,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (canSpin) lotteryMint else lotteryDisabled,
                contentColor = if (canSpin) lotteryDarkPurple else Color(0xFF8C7BA8),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(54.dp),
        ) {
            Text(
                text = stringResource(R.string.lottery_spin_now),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            onClick = {
                if (!canBoost) return@Surface
                coins -= 50
                secondsRemaining = (secondsRemaining - 900).coerceAtLeast(0)
                rewardMessage = boostedMessage
                coroutineScope.launch {
                    delay(2_500)
                    if (rewardMessage == boostedMessage) {
                        rewardMessage = null
                    }
                }
            },
            enabled = canBoost,
            shape = RoundedCornerShape(14.dp),
            color = if (canBoost) lotteryNeonPink else Color(0xFF23123A),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.lottery_speed_up),
                    color = if (canBoost) Color.White else Color(0xFF5D4B7C),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.lottery_speed_up_hint),
                    color = if (canBoost) Color.White.copy(alpha = 0.85f) else Color(0xFF5D4B7C),
                    fontSize = 11.sp,
                )
            }
        }
    }
    }
}

internal fun formatLotteryCountdown(totalSeconds: Int): String {
    return formatParticleCountdown(totalSeconds)
}

internal fun nextLotteryRotation(
    currentRotation: Float,
    selectedIndex: Int,
    sectorCount: Int,
): Float {
    require(sectorCount > 0) { "sectorCount must be positive" }
    require(selectedIndex in 0 until sectorCount) { "selectedIndex is out of range" }
    val sectorAngle = 360f / sectorCount
    val targetRemainder = (270f - selectedIndex * sectorAngle - sectorAngle / 2f).mod(360f)
    val currentRemainder = currentRotation.mod(360f)
    return currentRotation + 5f * 360f + (targetRemainder - currentRemainder).mod(360f)
}

internal data class LotteryLabelPlacement(
    val angleDegrees: Float,
    val anchorRadius: Float,
    val perpendicularOffset: Float,
)

internal fun lotteryLabelPlacement(
    cutLineAngle: Float,
    wheelRadius: Float,
    rimPadding: Float,
    perpendicularInset: Float,
): LotteryLabelPlacement {
    require(wheelRadius > rimPadding) { "wheelRadius must be greater than rimPadding" }
    require(perpendicularInset >= 0f) { "perpendicularInset must not be negative" }
    return LotteryLabelPlacement(
        angleDegrees = cutLineAngle,
        anchorRadius = wheelRadius - rimPadding,
        perpendicularOffset = perpendicularInset,
    )
}
