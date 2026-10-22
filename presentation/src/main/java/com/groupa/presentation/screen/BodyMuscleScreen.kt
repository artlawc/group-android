package com.groupa.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.groupa.coreui.R
import com.groupa.coreui.theme.AppBackground
import com.groupa.coreui.theme.CheckoutBlue
import com.groupa.coreui.theme.DividerGray
import com.groupa.coreui.theme.Gray500
import com.groupa.coreui.theme.GroupBlue
import com.groupa.coreui.theme.Ink
import com.groupa.coreui.theme.White
import com.groupa.presentation.bodymuscle.BodyMuscleGeometry
import com.groupa.presentation.bodymuscle.BodyMuscleAssetLoader
import com.groupa.presentation.bodymuscle.BodyMuscleData
import com.groupa.presentation.bodymuscle.BodyMusclePathRenderer
import com.groupa.presentation.bodymuscle.BodyView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MinBodyViewportZoom = 0.5f
private const val MaxBodyViewportZoom = 5f

private data class BodyMusclePinchGesture(
    val startDistance: Float,
    val sourceAnchor: Offset,
    val startZoom: Float,
)

@Composable
fun BodyMuscleScreen(
    initialMuscleId: String?,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val bodyData by produceState<BodyMuscleData?>(initialValue = null, context) {
        value = withContext(Dispatchers.IO) {
            BodyMuscleAssetLoader.load(context)
        }
    }

    var selectedViewName by rememberSaveable { mutableStateOf(BodyView.Front.name) }
    var selectedMuscleId by rememberSaveable { mutableStateOf(initialMuscleId ?: "head") }
    var selectedGroupName by rememberSaveable { mutableStateOf("Head & Neck") }
    val intensities = remember { mutableStateMapOf<String, Int>() }
    var viewportZoom by remember { mutableStateOf(1f) }
    var viewportPan by remember { mutableStateOf(Offset.Zero) }
    var panAnchor by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(bodyData, initialMuscleId) {
        bodyData?.let { data ->
            val selectedMuscle = data.selectedMuscle(initialMuscleId)
            selectedViewName = selectedMuscle?.view?.name ?: BodyView.Front.name
            selectedGroupName = selectedMuscle?.group ?: data.groups(BodyView.Front).first()
        }
    }

    if (bodyData == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val data = bodyData ?: return
    val selectedView = BodyView.valueOf(selectedViewName)
    LaunchedEffect(selectedView) {
        viewportZoom = 1f
        viewportPan = Offset.Zero
        panAnchor = null
    }
    val muscles = data.muscles(selectedView)
    val renderableMuscles = remember(data, selectedView) {
        muscles.map { muscle -> BodyMusclePathRenderer.renderable(muscle) }
    }
    val selectedMuscle = data.selectedMuscle(selectedMuscleId) ?: muscles.first()
    val groupMuscles = muscles.filter { it.group == selectedGroupName }
    val selectedIntensity = intensities[selectedMuscle.id] ?: 0
    val selectedCount = intensities.values.count { it > 0 }
    val averageIntensity = BodyMuscleData.averageIntensity(intensities.toMap(), muscles)
    val viewBox = if (selectedView == BodyView.Front) data.frontViewBox else data.backViewBox

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
                    tint = Ink,
                )
            }
            Text(
                text = stringResource(R.string.title_body_muscles),
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
            Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BodyViewButton(
                    label = stringResource(R.string.body_view_front),
                    selected = selectedView == BodyView.Front,
                    onClick = {
                        selectedViewName = BodyView.Front.name
                        selectedGroupName = data.groups(BodyView.Front).first()
                        selectedMuscleId = data.frontMuscles.first().id
                    },
                )
                BodyViewButton(
                    label = stringResource(R.string.body_view_back),
                    selected = selectedView == BodyView.Back,
                    onClick = {
                        selectedViewName = BodyView.Back.name
                        selectedGroupName = data.groups(BodyView.Back).first()
                        selectedMuscleId = data.backMuscles.first().id
                    },
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = White,
            border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = stringResource(R.string.label_selected_muscle),
                    color = Gray500,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = selectedMuscle.name,
                    color = Ink,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "${selectedMuscle.group} • $selectedIntensity/10",
                    color = CheckoutBlue,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Slider(
                    value = selectedIntensity.toFloat(),
                    onValueChange = { value ->
                        intensities[selectedMuscle.id] = value.toInt().coerceIn(0, 10)
                    },
                    valueRange = 0f..10f,
                    steps = 9,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            data.groups(selectedView).forEach { group ->
                Surface(
                    shape = CircleShape,
                    color = if (group == selectedGroupName) GroupBlue else AppBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
                    modifier = Modifier.clickable { selectedGroupName = group },
                ) {
                    Text(
                        text = group,
                        color = if (group == selectedGroupName) White else Ink,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = White,
            border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.label_muscle_group),
                    color = Gray500,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                groupMuscles.forEach { muscle ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedMuscleId = muscle.id
                                selectedGroupName = muscle.group
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = muscle.name,
                            color = if (muscle.id == selectedMuscleId) CheckoutBlue else Ink,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = "${intensities[muscle.id] ?: 0}",
                            color = Gray500,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = White,
            border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(
                    modifier = Modifier
                        .width(300.dp)
                        .height(640.dp)
                        .pointerInput(renderableMuscles) {
                            var previousTapTimeMillis = 0L
                            var previousTapPosition = Offset.Zero

                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val isDoubleTap = previousTapTimeMillis > 0 &&
                                    down.uptimeMillis - previousTapTimeMillis <=
                                    viewConfiguration.doubleTapTimeoutMillis &&
                                    (down.position - previousTapPosition).getDistance() <=
                                    viewConfiguration.touchSlop * 2f
                                if (isDoubleTap) {
                                    panAnchor = down.position
                                    previousTapTimeMillis = 0
                                }

                                var lastSinglePosition = down.position
                                var pinchGesture: BodyMusclePinchGesture? = null
                                var gestureMoved = false
                                var lastUpTimeMillis = down.uptimeMillis

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val pressedChanges = event.changes.filter { it.pressed }
                                    if (pressedChanges.isEmpty()) {
                                        lastUpTimeMillis = event.changes.firstOrNull()
                                            ?.uptimeMillis
                                            ?: lastUpTimeMillis
                                        break
                                    }

                                    if (pressedChanges.size >= 2) {
                                        val first = pressedChanges[0]
                                        val second = pressedChanges[1]
                                        val firstPosition = first.position
                                        val secondPosition = second.position

                                        if (pinchGesture == null) {
                                            val initialCentroid = Offset(
                                                x = (firstPosition.x + secondPosition.x) / 2f,
                                                y = (firstPosition.y + secondPosition.y) / 2f,
                                            )
                                            val effectiveTransform = BodyMuscleGeometry.viewportTransform(
                                                viewBox = viewBox,
                                                canvasWidth = size.width.toFloat(),
                                                canvasHeight = size.height.toFloat(),
                                                zoom = viewportZoom,
                                                pan = viewportPan,
                                            )
                                            pinchGesture = BodyMusclePinchGesture(
                                                startDistance = (firstPosition - secondPosition)
                                                    .getDistance(),
                                                sourceAnchor = effectiveTransform
                                                    .toSource(initialCentroid),
                                                startZoom = viewportZoom,
                                            )
                                        } else {
                                            val startDistance = pinchGesture.startDistance
                                            if (startDistance > 0f) {
                                                val currentDistance = (firstPosition - secondPosition)
                                                    .getDistance()
                                                val nextZoom = (
                                                    pinchGesture.startZoom *
                                                        currentDistance / startDistance
                                                    ).coerceIn(
                                                        minimumValue = MinBodyViewportZoom,
                                                        maximumValue = MaxBodyViewportZoom,
                                                    )
                                                val currentCentroid = Offset(
                                                    x = (firstPosition.x + secondPosition.x) / 2f,
                                                    y = (firstPosition.y + secondPosition.y) / 2f,
                                                )
                                                viewportZoom = nextZoom
                                                viewportPan = BodyMuscleGeometry.panForZoomedPoint(
                                                    viewBox = viewBox,
                                                    canvasWidth = size.width.toFloat(),
                                                    canvasHeight = size.height.toFloat(),
                                                    zoom = nextZoom,
                                                    sourcePoint = pinchGesture.sourceAnchor,
                                                    screenPoint = currentCentroid,
                                                )
                                            }
                                        }
                                        pressedChanges.forEach { it.consume() }
                                        gestureMoved = true
                                    } else {
                                        val change = pressedChanges.first()
                                        val movement = change.position - lastSinglePosition
                                        if (
                                            isDoubleTap &&
                                            movement.getDistance() > viewConfiguration.touchSlop
                                        ) {
                                            viewportPan += movement
                                            change.consume()
                                            gestureMoved = true
                                        } else if (movement.getDistance() > viewConfiguration.touchSlop) {
                                            gestureMoved = true
                                        }
                                        lastSinglePosition = change.position
                                    }
                                }

                                if (isDoubleTap) {
                                    panAnchor = null
                                    return@awaitEachGesture
                                }

                                if (!gestureMoved) {
                                    val transform = BodyMuscleGeometry.viewportTransform(
                                        viewBox = viewBox,
                                        canvasWidth = size.width.toFloat(),
                                        canvasHeight = size.height.toFloat(),
                                        zoom = viewportZoom,
                                        pan = viewportPan,
                                    )
                                    val sourceOffset = transform.toSource(down.position)
                                    renderableMuscles.firstOrNull { muscle ->
                                        muscle.bounds.contains(sourceOffset)
                                    }?.let { muscle ->
                                        selectedMuscleId = muscle.id
                                        selectedGroupName = muscle.group
                                    }
                                    previousTapTimeMillis = lastUpTimeMillis
                                    previousTapPosition = down.position
                                } else {
                                    previousTapTimeMillis = 0
                                }
                            }
                        },
                ) {
                    val transform = BodyMuscleGeometry.viewportTransform(
                        viewBox = viewBox,
                        canvasWidth = size.width,
                        canvasHeight = size.height,
                        zoom = viewportZoom,
                        pan = viewportPan,
                    )
                    val matrix = Matrix().apply {
                        values[Matrix.ScaleX] = transform.scaleX
                        values[Matrix.SkewX] = 0f
                        values[Matrix.TranslateX] = transform.translateX
                        values[Matrix.SkewY] = 0f
                        values[Matrix.ScaleY] = transform.scaleY
                        values[Matrix.TranslateY] = transform.translateY
                        values[Matrix.Perspective0] = 0f
                        values[Matrix.Perspective1] = 0f
                        values[Matrix.Perspective2] = 1f
                    }
                    renderableMuscles.forEach { muscle ->
                        val canvasPath = Path().apply {
                            addPath(muscle.path, Offset.Zero)
                            transform(matrix)
                        }
                        drawPath(
                            path = canvasPath,
                            color = BodyMuscleData.colorForIntensity(intensities[muscle.id] ?: 0),
                        )
                        drawPath(
                            path = canvasPath,
                            color = if (muscle.id == selectedMuscleId) White else Ink,
                            style = Stroke(width = if (muscle.id == selectedMuscleId) 5f else 2f),
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.label_muscle_group),
                    color = Gray500,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = selectedGroupName,
                    color = Ink,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.label_selected_count),
                    color = Gray500,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "$selectedCount",
                    color = Ink,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.label_average_intensity),
                    color = Gray500,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "$averageIntensity",
                    color = Ink,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        Column {
            Text(
                text = stringResource(R.string.label_intensity_scale),
                color = Gray500,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                (0..10).forEach { intensity ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp)
                            .background(
                                color = BodyMuscleData.colorForIntensity(intensity),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                            ),
                    )
                }
            }
        }

        OutlinedButton(
            onClick = {
                intensities.clear()
                viewportZoom = 1f
                viewportPan = Offset.Zero
                panAnchor = null
                selectedMuscleId = if (selectedView == BodyView.Front) {
                    data.frontMuscles.first().id
                } else {
                    data.backMuscles.first().id
                }
                selectedGroupName = data.groups(selectedView).first()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.action_reset_body_muscles))
        }
    }
}

@Composable
private fun BodyViewButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = if (selected) GroupBlue else AppBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            color = if (selected) White else Ink,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}
