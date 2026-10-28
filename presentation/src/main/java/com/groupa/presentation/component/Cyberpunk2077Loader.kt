package com.groupa.presentation.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private val CyberpunkBackground = Color(0xFF030305)
private val CyberpunkPanel = Color(0xFF0F141C)
private val CyberpunkBorder = Color(0xFF1A222D)
private val CyberpunkCyan = Color(0xFF00F0FF)
private val CyberpunkYellow = Color(0xFFFCEE0A)
private val CyberpunkCrimson = Color(0xFFFF003C)
private val CyberpunkLogText = Color(0xFF8FA3B3)

internal data class CyberpunkLoaderAction(
    val label: String,
    val onClick: () -> Unit,
)

@Composable
internal fun Cyberpunk2077Loader(
    title: String,
    panelTitle: String,
    status: String,
    progress: Float,
    detail: String,
    logs: List<String>,
    primaryAction: CyberpunkLoaderAction,
    modifier: Modifier = Modifier,
    secondaryAction: CyberpunkLoaderAction? = null,
    errorMessage: String? = null,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cyberpunkLoader")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.38f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cyberpunkStatusPulse",
    )
    val scanPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_200, easing = LinearEasing),
        ),
        label = "cyberpunkScanPhase",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberpunkBackground),
    ) {
        CyberpunkLoaderCanvas(
            progress = progress,
            phase = scanPhase,
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
        ) {
            CyberpunkHeader(
                status = status,
                pulse = pulse,
            )
            Spacer(modifier = Modifier.height(16.dp))
            CyberpunkWorkspace(
                title = title,
                logs = logs,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.height(16.dp))
            CyberpunkDataShard(
                panelTitle = panelTitle,
                progress = progress,
                detail = detail,
                logs = logs,
                errorMessage = errorMessage,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                primaryAction = primaryAction,
                secondaryAction = secondaryAction,
            )
        }
    }
}

@Composable
private fun CyberpunkHeader(
    status: String,
    pulse: Float,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xD90A0C10))
            .border(width = 1.dp, color = CyberpunkBorder)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "GROUPGO_NETLINK_V4.022 // SUB-NEURAL LAYER",
            color = CyberpunkCyan.copy(alpha = 0.78f),
            fontSize = 9.sp,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
        )
        Text(
            text = status,
            color = CyberpunkYellow.copy(alpha = pulse),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
        )
    }
}

@Composable
private fun CyberpunkWorkspace(
    title: String,
    logs: List<String>,
    modifier: Modifier = Modifier,
) {
    var echoMessages by remember {
        mutableStateOf(
            listOf("ECHO // NET_LINK: Establishing encrypted handshake..."),
        )
    }
    val echoScrollState = rememberScrollState()

    LaunchedEffect(logs) {
        var tick = 0
        while (true) {
            delay(900)
            echoMessages = (
                echoMessages + cyberpunkMonitorEchoMessage(logs = logs, tick = tick)
                ).takeLast(6)
            tick += 1
        }
    }

    LaunchedEffect(echoMessages) {
        echoScrollState.animateScrollTo(echoScrollState.maxValue)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp)
            .background(Color(0x66030305))
            .border(width = 1.dp, color = CyberpunkCyan.copy(alpha = 0.32f)),
    ) {
        Text(
            text = "WORKSPACE_FEED // MODEL_PIPELINE",
            color = Color.White.copy(alpha = 0.36f),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(12.dp),
        )
        Text(
            text = title,
            color = CyberpunkCyan.copy(alpha = 0.08f),
            fontSize = 46.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-2).sp,
            lineHeight = 46.sp,
            modifier = Modifier.align(Alignment.Center),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "MONITOR_ECHO // ROLLING_TELEMETRY",
                color = CyberpunkCyan.copy(alpha = 0.68f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp)
                    .background(Color(0xB2070A0E))
                    .border(width = 1.dp, color = Color(0xFF141C26)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(echoScrollState)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    echoMessages.forEach { message ->
                        Text(
                            text = message,
                            color = CyberpunkLogText,
                            fontSize = 8.sp,
                            lineHeight = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                        )
                    }
                }
            }
            Text(
                text = "WARNING: Model datashard streaming in progress. Runtime diagnostics loop until the chassis payload is verified.",
                color = CyberpunkCyan.copy(alpha = 0.8f),
                fontSize = 9.sp,
                lineHeight = 13.sp,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

internal fun cyberpunkMonitorEchoMessage(
    logs: List<String>,
    tick: Int,
): String {
    val source = logs.ifEmpty {
        listOf("BUFFER_TRACE: Awaiting model datashard packets")
    }
    val message = source[((tick % source.size) + source.size) % source.size]
    return "ECHO // $message"
}

@Composable
private fun CyberpunkDataShard(
    panelTitle: String,
    progress: Float,
    detail: String,
    logs: List<String>,
    errorMessage: String?,
    primaryAction: CyberpunkLoaderAction,
    secondaryAction: CyberpunkLoaderAction?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .widthIn(max = 420.dp)
            .clip(RoundedCornerShape(topEnd = 16.dp, bottomStart = 16.dp))
            .background(Color(0xE60F141C))
            .border(width = 1.dp, color = CyberpunkBorder)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = panelTitle,
            color = CyberpunkCrimson,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace,
        )
        CyberpunkProgressMetric(
            label = "DATA_STREAM_PROGRESS",
            progress = progress,
            color = CyberpunkCyan,
        )
        CyberpunkProgressMetric(
            label = "BUFFER_STABILITY_RATE",
            progress = (0.55f + progress * 0.4f).coerceIn(0f, 1f),
            color = CyberpunkCrimson,
        )
        Text(
            text = detail,
            color = CyberpunkLogText,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 2,
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = CyberpunkCrimson,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 3,
            )
        }
        CyberpunkDiagnosticLog(logs = logs)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CyberpunkActionButton(
                action = primaryAction,
                emphasized = true,
                modifier = Modifier.weight(1f),
            )
            if (secondaryAction != null) {
                CyberpunkActionButton(
                    action = secondaryAction,
                    emphasized = false,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CyberpunkProgressMetric(
    label: String,
    progress: Float,
    color: Color,
) {
    val safeProgress = progress.coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                color = CyberpunkLogText,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = "${(safeProgress * 100).roundToInt()}%",
                color = color,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color(0xFF11151C))
                .border(width = 1.dp, color = Color(0xFF232E3D)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(safeProgress)
                    .fillMaxHeight()
                    .background(color),
            )
        }
    }
}

@Composable
private fun CyberpunkDiagnosticLog(logs: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .background(Color(0xFF070A0E))
            .border(width = 1.dp, color = Color(0xFF141C26))
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        logs.takeLast(8).forEachIndexed { index, line ->
            Text(
                text = line,
                color = if (index % 4 == 3) {
                    CyberpunkYellow.copy(alpha = 0.9f)
                } else {
                    CyberpunkLogText
                },
                fontSize = 8.sp,
                lineHeight = 10.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun CyberpunkActionButton(
    action: CyberpunkLoaderAction,
    emphasized: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = if (emphasized) CyberpunkYellow else CyberpunkCyan
    val shape = CyberpunkButtonShape(chamfer = 10.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color.Transparent)
            .border(width = 1.dp, color = color, shape = shape)
            .clickable(onClick = action.onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(3.dp)
                .background(color)
                .align(Alignment.CenterStart),
        )
        Text(
            text = action.label,
            color = color,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Left,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, end = 15.dp, top = 6.dp, bottom = 6.dp),
        )
    }
}

private data class CyberpunkButtonShape(
    private val chamfer: androidx.compose.ui.unit.Dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val chamferPx = with(density) { chamfer.toPx() }
        val path = Path().apply {
            moveTo(x = 0f, y = 0f)
            lineTo(x = size.width - chamferPx, y = 0f)
            lineTo(x = size.width, y = chamferPx)
            lineTo(x = size.width, y = size.height)
            lineTo(x = chamferPx, y = size.height)
            lineTo(x = 0f, y = size.height - chamferPx)
            close()
        }
        return Outline.Generic(path = path)
    }
}

@Composable
private fun CyberpunkLoaderCanvas(
    progress: Float,
    phase: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .clipToBounds(),
    ) {
        val scanlineStep = 4.dp.toPx()
        var scanlineY = 0f
        while (scanlineY < size.height) {
            drawLine(
                color = Color.Black.copy(alpha = 0.24f),
                start = Offset(x = 0f, y = scanlineY),
                end = Offset(x = size.width, y = scanlineY),
                strokeWidth = 1f,
            )
            scanlineY += scanlineStep
        }

        val sweepWidth = size.width * 0.26f
        val sweepX = size.width * phase - sweepWidth / 2f
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    CyberpunkCyan.copy(alpha = 0.08f),
                    Color.Transparent,
                ),
                startX = sweepX,
                endX = sweepX + sweepWidth,
            ),
            topLeft = Offset(x = sweepX, y = 0f),
            size = Size(width = sweepWidth, height = size.height),
        )

        repeat(4) { index ->
            val glitchCenter = size.height * ((phase + index * 0.23f) % 1f)
            val glitchHeight = size.height * (0.005f + (index % 3) * 0.003f)
            val glitchColor = if (index % 2 == 0) {
                CyberpunkCyan.copy(alpha = 0.07f)
            } else {
                CyberpunkCrimson.copy(alpha = 0.06f)
            }
            drawRect(
                color = glitchColor,
                topLeft = Offset(x = 0f, y = glitchCenter),
                size = Size(width = size.width, height = glitchHeight),
            )
        }

        val decodeY = size.height * progress
        drawRect(
            color = CyberpunkCyan.copy(alpha = 0.05f),
            topLeft = Offset(x = 0f, y = decodeY),
            size = Size(width = size.width, height = 2.dp.toPx()),
        )
    }
}
