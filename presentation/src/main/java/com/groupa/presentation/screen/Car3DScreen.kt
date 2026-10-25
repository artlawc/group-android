package com.groupa.presentation.screen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groupa.presentation.download.CarModelDownloadManager
import com.groupa.presentation.download.CarModelDownloadStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.groupa.presentation.BuildConfig
import com.groupa.coreui.R
import com.groupa.coreui.car3d.CarCameraPreset
import com.groupa.coreui.car3d.CarFrameState
import com.groupa.coreui.car3d.CarPaint
import com.groupa.coreui.car3d.CarPoint
import com.groupa.coreui.car3d.FilamentCarViewer

private data class NamedCarPaint(
    val name: String,
    val paint: CarPaint,
)

private val carPaints = listOf(
    NamedCarPaint("Carmine Candy", CarPaint(Color(0xFF991111), Color(0xFF1A1414))),
    NamedCarPaint("Pearly Swirly", CarPaint(Color(0xFFE8E0D0), Color(0xFF2A2520))),
    NamedCarPaint("Torched Graphite", CarPaint(Color(0xFF333333), Color(0xFF1A1A1A))),
)

private val carCameraPresets = listOf(
    "Default" to CarCameraPreset(
        position = CarPoint(3.5f, 1.8f, 5.5f),
        target = CarPoint(0f, 0.3f, 0f),
    ),
    "Front" to CarCameraPreset(
        position = CarPoint(0f, 0.4f, 4.5f),
        target = CarPoint(0f, 0.3f, 0f),
    ),
    "Back" to CarCameraPreset(
        position = CarPoint(0f, 0.6f, -4.0f),
        target = CarPoint(0f, 0.3f, 0f),
    ),
    "Side" to CarCameraPreset(
        position = CarPoint(5.0f, 0.4f, 0f),
        target = CarPoint(0f, 0.3f, 0f),
    ),
    "Top" to CarCameraPreset(
        position = CarPoint(0f, 5.5f, 0.5f),
        target = CarPoint(0f, 0.3f, 0f),
    ),
    "Interior" to CarCameraPreset(
        position = CarPoint(0f, 0.7f, 1.2f),
        target = CarPoint(0f, 0.3f, -0.5f),
    ),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Car3DScreen(
    carId: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var selectedPaintIndex by rememberSaveable { mutableStateOf(0) }
    var selectedCameraPresetIndex by rememberSaveable { mutableStateOf(0) }
    var turntableEnabled by rememberSaveable { mutableStateOf(true) }
    var doorLeftOpen by rememberSaveable { mutableStateOf(false) }
    var doorRightOpen by rememberSaveable { mutableStateOf(false) }
    var hoodOpen by rememberSaveable { mutableStateOf(false) }
    var hatchOpen by rememberSaveable { mutableStateOf(false) }
    var spinWheels by rememberSaveable { mutableStateOf(false) }
    var driveLoop by rememberSaveable { mutableStateOf(false) }
    var explodeView by rememberSaveable { mutableStateOf(false) }
    var brightness by rememberSaveable { mutableStateOf(1f) }

    val selectedPaint = carPaints[selectedPaintIndex]
    val selectedCameraPreset = carCameraPresets[selectedCameraPresetIndex]
    val configuration = LocalConfiguration.current
    val controlPanelMaxHeight = (configuration.screenHeightDp * 0.5f).dp
    val density = LocalDensity.current
    val drawerCollapsedHeight = 28.dp
    val drawerExpandedHeight = controlPanelMaxHeight.coerceAtLeast(drawerCollapsedHeight)
    val drawerCollapsedPx = with(density) { drawerCollapsedHeight.toPx() }
    val drawerExpandedPx = with(density) { drawerExpandedHeight.toPx() }
    val drawerMidpointPx = (drawerCollapsedPx + drawerExpandedPx) / 2f
    val drawerOpenThresholdPx = with(density) { 16.dp.toPx() }
    var drawerOpen by rememberSaveable { mutableStateOf(false) }
    var drawerHeightPx by remember { mutableFloatStateOf(drawerCollapsedPx) }
    var drawerAnimationJob by remember { mutableStateOf<Job?>(null) }
    val drawerAnimationScope = rememberCoroutineScope()

    fun animateDrawerTo(open: Boolean) {
        drawerOpen = open
        drawerAnimationJob?.cancel()
        drawerAnimationJob = drawerAnimationScope.launch {
            animate(
                initialValue = drawerHeightPx,
                targetValue = if (open) drawerExpandedPx else drawerCollapsedPx,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            ) { value, _ ->
                drawerHeightPx = value
            }
        }
    }

    LaunchedEffect(drawerCollapsedPx, drawerExpandedPx) {
        drawerHeightPx = if (drawerOpen) drawerExpandedPx else drawerCollapsedPx
    }

    val doorLeftProgress by animateFloatAsState(
        targetValue = if (doorLeftOpen) 1f else 0f,
        animationSpec = tween(durationMillis = 350),
    )
    val doorRightProgress by animateFloatAsState(
        targetValue = if (doorRightOpen) 1f else 0f,
        animationSpec = tween(durationMillis = 350),
    )
    val hoodProgress by animateFloatAsState(
        targetValue = if (hoodOpen) 1f else 0f,
        animationSpec = tween(durationMillis = 350),
    )
    val hatchProgress by animateFloatAsState(
        targetValue = if (hatchOpen) 1f else 0f,
        animationSpec = tween(durationMillis = 350),
    )
    val explodeProgress by animateFloatAsState(
        targetValue = if (explodeView) 1f else 0f,
        animationSpec = tween(durationMillis = 350),
    )
    val frameState = CarFrameState(
        turntableEnabled = turntableEnabled,
        doorLeftProgress = doorLeftProgress,
        doorRightProgress = doorRightProgress,
        hoodProgress = hoodProgress,
        hatchProgress = hatchProgress,
        explodeProgress = explodeProgress,
        spinWheels = spinWheels,
        driveLoop = driveLoop,
    )
    val downloadState = CarModelDownloadManager.state.collectAsState().value
    val downloadedModelFile = downloadState.modelFile

    LaunchedEffect(BuildConfig.CAR_3D_MODEL_URL) {
        CarModelDownloadManager.start(context, BuildConfig.CAR_3D_MODEL_URL)
    }

    DisposableEffect(Unit) {
        onDispose {
            CarModelDownloadManager.markScreenExited()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0F)),
    ) {
        if (downloadedModelFile != null) {
            FilamentCarViewer(
                cameraPreset = selectedCameraPreset.second,
                paint = selectedPaint.paint,
                brightness = brightness,
                frameState = frameState,
                modelFile = downloadedModelFile,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = if (downloadState.status == CarModelDownloadStatus.Failed) {
                        "3D model download failed"
                    } else {
                        "Downloading 3D model"
                    },
                    color = Color(0xFFE0E0E0),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (downloadState.status == CarModelDownloadStatus.Failed) {
                    Text(
                        text = downloadState.error ?: "Please try again later.",
                        color = Color(0xFF888888),
                        fontSize = 13.sp,
                    )
                    Button(
                        onClick = {
                            CarModelDownloadManager.retry(
                                context,
                                BuildConfig.CAR_3D_MODEL_URL,
                            )
                        },
                    ) {
                        Text(text = "Retry download")
                    }
                } else {
                    if (downloadState.totalBytes > 0) {
                        LinearProgressIndicator(
                            progress = {
                                (
                                    downloadState.downloadedBytes.toFloat() /
                                        downloadState.totalBytes
                                    ).coerceIn(0f, 1f)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Text(
                        text = if (downloadState.totalBytes > 0) {
                            "Downloaded ${formatMegabytes(downloadState.downloadedBytes)} of " +
                                formatMegabytes(downloadState.totalBytes)
                        } else {
                            "Downloaded ${formatMegabytes(downloadState.downloadedBytes)}"
                        },
                        color = Color(0xFF888888),
                        fontSize = 13.sp,
                    )
                    Button(
                        onClick = onBack,
                    ) {
                        Text(text = "Notify me when model downloaded")
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xCC0A0A0F))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = Color(0xFFE0E0E0),
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.title_car_3d),
                    color = Color(0xFFE0E0E0),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = carId,
                    color = Color(0xFF888888),
                    fontSize = 12.sp,
                )
            }
        }

        if (downloadedModelFile != null) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(with(density) { drawerHeightPx.toDp() }),
            color = Color(0xEA101018),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x14FFFFFF)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                DrawerGrip(
                    onDragStart = { drawerAnimationJob?.cancel() },
                    onDrag = { dragAmount ->
                        drawerHeightPx = (drawerHeightPx - dragAmount)
                            .coerceIn(drawerCollapsedPx, drawerExpandedPx)
                    },
                    onDragEnd = {
                        animateDrawerTo(
                            if (drawerOpen) {
                                drawerHeightPx > drawerMidpointPx
                            } else {
                                drawerHeightPx > drawerCollapsedPx + drawerOpenThresholdPx
                            },
                        )
                    },
                    onDragCancel = {
                        animateDrawerTo(drawerOpen)
                    },
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                SectionTitle("Paint Color")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    carPaints.forEachIndexed { index, paint ->
                        PaintSwatch(
                            paint = paint,
                            selected = index == selectedPaintIndex,
                            onClick = { selectedPaintIndex = index },
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedPaint.name,
                        color = Color(0xFFE0E0E0),
                        fontSize = 13.sp,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                }

                SectionTitle("View Mode")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton("Turntable", turntableEnabled) { turntableEnabled = true }
                    ActionButton("Free Orbit", !turntableEnabled) { turntableEnabled = false }
                }

                SectionTitle("Animations")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ActionButton("Left Door", doorLeftOpen) { doorLeftOpen = !doorLeftOpen }
                    ActionButton("Right Door", doorRightOpen) { doorRightOpen = !doorRightOpen }
                    ActionButton("Hood", hoodOpen) { hoodOpen = !hoodOpen }
                    ActionButton("Hatch", hatchOpen) { hatchOpen = !hatchOpen }
                    ActionButton("Spin Wheels", spinWheels) { spinWheels = !spinWheels }
                    ActionButton("Drive Loop", driveLoop) { driveLoop = !driveLoop }
                    ActionButton("Explode", explodeView) {
                        explodeView = !explodeView
                        if (explodeView) {
                            doorLeftOpen = false
                            doorRightOpen = false
                            hoodOpen = false
                            hatchOpen = false
                        }
                    }
                }

                SectionTitle("Lighting")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Brightness",
                        color = Color(0xFFE0E0E0),
                        fontSize = 13.sp,
                    )
                    Slider(
                        value = brightness,
                        onValueChange = { brightness = it },
                        valueRange = 0.2f..2f,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${(brightness * 100).toInt()}%",
                        color = Color(0xFF888888),
                        fontSize = 12.sp,
                    )
                }

                SectionTitle("Camera Presets")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    carCameraPresets.forEachIndexed { index, (presetName, _) ->
                        ActionButton(presetName, index == selectedCameraPresetIndex) {
                            selectedCameraPresetIndex = index
                        }
                    }
                }

                SectionTitle("Vehicle Specs")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        SpecRow("Type", "Concept Coupe")
                        SpecRow("Doors", "2-Door")
                        SpecRow("Style", "Futuristic GT")
                        SpecRow("Paint", selectedPaint.name)
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        SpecRow("Body", "Carbon Fiber")
                        SpecRow("Engine", "Mid-Engine")
                        SpecRow("Wheels", "20\" Alloy")
                        SpecRow("Source", "Sketchfab")
                    }
                }

                Button(
                    onClick = {
                        selectedPaintIndex = 0
                        selectedCameraPresetIndex = 0
                        turntableEnabled = true
                        doorLeftOpen = false
                        doorRightOpen = false
                        hoodOpen = false
                        hatchOpen = false
                        spinWheels = false
                        driveLoop = false
                        explodeView = false
                        brightness = 1f
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x143B82F6)),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(text = "Reset All", color = Color(0xFF3B82F6))
                }
            }
        }
        }
    }
}

}

private fun formatMegabytes(bytes: Long): String =
    "%.1f MB".format(bytes / 1024f / 1024f)

@Composable
private fun DrawerGrip(
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragCancel() },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(3.dp)
                .background(Color(0x4DFFFFFF), RoundedCornerShape(2.dp)),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color(0xFF888888),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun ActionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0x263B82F6) else Color(0x0FFFFFFF),
        ),
        shape = RoundedCornerShape(10.dp),
    ) {
        Text(
            text = text,
            color = if (selected) Color(0xFF3B82F6) else Color(0xFFE0E0E0),
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun PaintSwatch(
    paint: NamedCarPaint,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .background(
                brush = Brush.linearGradient(
                    listOf(paint.paint.mainColor, paint.paint.mainColor.copy(alpha = 0.75f)),
                ),
                shape = CircleShape,
            )
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) Color.White else Color(0x33FFFFFF),
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
    )
}

@Composable
private fun SpecRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = Color(0xFF888888),
            fontSize = 11.sp,
        )
        Text(
            text = value,
            color = Color(0xFFE0E0E0),
            fontSize = 11.sp,
        )
    }
}
