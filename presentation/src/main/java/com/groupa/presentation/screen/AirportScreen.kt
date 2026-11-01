package com.groupa.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groupa.coreui.R
import com.groupa.coreui.theme.Ink
import com.groupa.coreui.theme.White
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

internal data class AirportStation(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
)

internal data class AirportFlightRoute(
    val fromIndex: Int,
    val toIndex: Int,
)

private const val AirportMapWidth = 1000f
private const val AirportMapHeight = 600f
private const val MinimumAirportZoom = 0.2f
private const val MaximumAirportZoom = 8f
private const val AirportHitThreshold = 14f

private val icelandLandmassPaths = listOf(
    "M140,80 L200,60 L240,70 L280,50 L320,80 L350,60 L450,70 L550,50 L680,60 L750,90 L850,70 L920,120 L940,190 L900,240 L930,280 L880,310 L820,290 L800,340 L700,350 L650,380 L620,350 L580,390 L520,380 L480,430 L400,420 L350,450 L300,420 L270,430 L220,390 L170,390 L140,350 L150,320 L110,310 L90,280 L140,260 L120,230 L160,210 L190,240 L210,210 L180,180 L230,160 L200,120 L150,140 L110,100 L110,80 Z",
    "M120,220 L100,200 L80,210 L70,180 L90,150 L80,120 L60,110 L50,130 L30,120 L20,90 L40,70 L70,80 L90,50 L120,70 L140,50 L160,80 L130,110 L170,130 L150,170 Z",
)

internal val icelandAirports = listOf(
    AirportStation("reykjavik", "Reykjavik", 230f, 310f),
    AirportStation("keflavik", "Keflavik", 120f, 320f),
    AirportStation("akureyri", "Akureyri", 570f, 130f),
    AirportStation("egilsstadir", "Egilsstadir", 870f, 210f),
    AirportStation("isafjordur", "Isafjordur", 180f, 90f),
    AirportStation("vestmannaeyjar", "Vestmannaeyjar", 360f, 410f),
)

internal val icelandFlightRoutes = listOf(
    AirportFlightRoute(fromIndex = 1, toIndex = 2),
    AirportFlightRoute(fromIndex = 1, toIndex = 3),
    AirportFlightRoute(fromIndex = 0, toIndex = 4),
    AirportFlightRoute(fromIndex = 0, toIndex = 5),
    AirportFlightRoute(fromIndex = 2, toIndex = 3),
)

internal fun parseAirportSvgPath(pathData: String): Path {
    val path = Path()
    val commands = pathData.split(" ", ",").filter { it.isNotBlank() }
    var command = ""
    var index = 0

    fun nextFloat(): Float {
        val value = commands[index].toFloat()
        index += 1
        return value
    }

    while (index < commands.size) {
        val token = commands[index]
        if (token == "M" || token == "L" || token == "Z") {
            command = token
            index += 1
            if (command == "Z") path.close()
            continue
        }

        when (command) {
            "M" -> {
                val x = nextFloat()
                val y = nextFloat()
                path.moveTo(x = x, y = y)
                command = "L"
            }

            "L" -> {
                val x = nextFloat()
                val y = nextFloat()
                path.lineTo(x = x, y = y)
            }

            else -> index += 1
        }
    }

    return path
}

internal fun nearestAirportStation(
    x: Float,
    y: Float,
    threshold: Float = AirportHitThreshold,
): AirportStation? {
    return icelandAirports
        .map { airport -> airport to (Offset(airport.x, airport.y) - Offset(x, y)).getDistance() }
        .filter { (_, distance) -> distance <= threshold }
        .minByOrNull { (_, distance) -> distance }
        ?.first
}

internal fun flightRouteControlPoint(from: AirportStation, to: AirportStation): Offset {
    val midX = (from.x + to.x) / 2f
    val midY = (from.y + to.y) / 2f
    val dx = to.x - from.x
    val dy = to.y - from.y
    return Offset(
        x = midX - dy * 0.2f,
        y = midY + dx * 0.2f,
    )
}

internal fun flightRoutePoint(
    from: AirportStation,
    control: Offset,
    to: AirportStation,
    progress: Float,
): Offset {
    val t = progress.coerceIn(0f, 1f)
    val u = 1f - t
    val x = u * u * from.x + 2f * u * t * control.x + t * t * to.x
    val y = u * u * from.y + 2f * u * t * control.y + t * t * to.y
    return Offset(x = x, y = y)
}

internal fun airportMapInitialTransform(viewportSize: Size): Pair<Offset, Float> {
    if (viewportSize.width <= 0f || viewportSize.height <= 0f) {
        return Offset(x = 24f, y = 24f) to 1f
    }

    val scale = min(
        viewportSize.width / AirportMapWidth,
        viewportSize.height / AirportMapHeight,
    ) * 0.85f
    val x = (viewportSize.width - AirportMapWidth * scale) / 2f
    val y = (viewportSize.height - AirportMapHeight * scale) / 2f
    return Offset(x = x, y = y) to scale
}

internal fun airportMapZoomTransform(
    transform: Pair<Offset, Float>,
    focusX: Float,
    focusY: Float,
    zoomFactor: Float,
    viewportSize: Size,
): Pair<Offset, Float> {
    val (pan, zoom) = transform
    if (viewportSize.width <= 0f || viewportSize.height <= 0f) return transform

    val updatedZoom = (zoom * zoomFactor).coerceIn(MinimumAirportZoom, MaximumAirportZoom)
    val worldX = (focusX - pan.x) / zoom
    val worldY = (focusY - pan.y) / zoom
    val updatedPan = Offset(
        x = focusX - worldX * updatedZoom,
        y = focusY - worldY * updatedZoom,
    )

    return clampAirportMapPan(
        transform = updatedPan to updatedZoom,
        viewportSize = viewportSize,
    )
}

internal fun clampAirportMapPan(
    transform: Pair<Offset, Float>,
    viewportSize: Size,
): Pair<Offset, Float> {
    val (pan, zoom) = transform
    if (viewportSize.width <= 0f || viewportSize.height <= 0f) return transform

    val contentWidth = AirportMapWidth * zoom
    val contentHeight = AirportMapHeight * zoom
    val minX = min(0f, viewportSize.width - contentWidth)
    val minY = min(0f, viewportSize.height - contentHeight)

    return Offset(
        x = pan.x.coerceIn(minX, 0f),
        y = pan.y.coerceIn(minY, 0f),
    ) to zoom
}

internal fun airportMapPanTransform(
    transform: Pair<Offset, Float>,
    panDeltaX: Float,
    panDeltaY: Float,
    viewportSize: Size,
): Pair<Offset, Float> {
    val (pan, zoom) = transform
    val updatedPan = Offset(
        x = pan.x + panDeltaX,
        y = pan.y + panDeltaY,
    )
    return clampAirportMapPan(
        transform = updatedPan to zoom,
        viewportSize = viewportSize,
    )
}

@Composable
fun AirportScreen(
    airportId: String,
    onBack: () -> Unit,
) {
    val textMeasurer = rememberTextMeasurer()
    val mapPaths = remember { icelandLandmassPaths.map(::parseAirportSvgPath) }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var mapTransform by remember {
        mutableStateOf(Offset(x = 24f, y = 24f) to 1f)
    }
    var selectedAirport by remember(airportId) {
        mutableStateOf(
            icelandAirports.firstOrNull { it.id == airportId } ?: icelandAirports[1],
        )
    }
    var animationTime by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(viewportSize) {
        if (viewportSize != IntSize.Zero) {
            mapTransform = airportMapInitialTransform(
                viewportSize = Size(
                    width = viewportSize.width.toFloat(),
                    height = viewportSize.height.toFloat(),
                ),
            )
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            animationTime += 0.5f
            delay(16)
        }
    }

    fun viewportScreenSize(): Size {
        return Size(
            width = viewportSize.width.toFloat(),
            height = viewportSize.height.toFloat(),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB3DFEC))
            .statusBarsPadding()
            .padding(16.dp),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.airport_map_title),
                    color = Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    maxLines = 1,
                )
                Text(
                    text = stringResource(R.string.airport_route_count),
                    color = Ink.copy(alpha = 0.72f),
                    fontSize = 12.sp,
                    maxLines = 1,
                )
            }
            Surface(
                color = White,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
            ) {
                Text(
                    text = stringResource(R.string.airport_network_status),
                    color = Ink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    maxLines = 1,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFB3DFEC))
                .onSizeChanged { size -> viewportSize = size },
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(viewportSize) {
                        detectTransformGestures(panZoomLock = true) { centroid, pan, zoom, _ ->
                            val screenSize = viewportScreenSize()
                            mapTransform = if (zoom != 1f) {
                                val pannedTransform = airportMapPanTransform(
                                    transform = mapTransform,
                                    panDeltaX = pan.x,
                                    panDeltaY = pan.y,
                                    viewportSize = screenSize,
                                )
                                airportMapZoomTransform(
                                    transform = pannedTransform,
                                    focusX = centroid.x,
                                    focusY = centroid.y,
                                    zoomFactor = zoom,
                                    viewportSize = screenSize,
                                )
                            } else {
                                airportMapPanTransform(
                                    transform = mapTransform,
                                    panDeltaX = pan.x,
                                    panDeltaY = pan.y,
                                    viewportSize = screenSize,
                                )
                            }
                        }
                    }
                    .pointerInput(viewportSize) {
                        detectTapGestures(
                            onTap = { offset ->
                                val (pan, zoom) = mapTransform
                                val worldX = (offset.x - pan.x) / zoom
                                val worldY = (offset.y - pan.y) / zoom
                                nearestAirportStation(x = worldX, y = worldY)?.let { airport ->
                                    selectedAirport = airport
                                }
                            },
                        )
                    },
            ) {
                val (pan, zoom) = mapTransform
                val screenSize = viewportScreenSize()

                withTransform(
                    transformBlock = {
                        translate(left = pan.x, top = pan.y)
                        scale(scaleX = zoom, scaleY = zoom)
                    },
                ) {
                    mapPaths.forEach { path ->
                        drawPath(path = path, color = Color(0xFFF3F3F3))
                        drawPath(
                            path = path,
                            color = Color(0xFFAAAAAA),
                            style = Stroke(width = 1f / zoom),
                        )
                    }

                    icelandFlightRoutes.forEach { route ->
                        val from = icelandAirports[route.fromIndex]
                        val to = icelandAirports[route.toIndex]
                        val control = flightRouteControlPoint(from = from, to = to)
                        val routePath = Path().apply {
                            moveTo(x = from.x, y = from.y)
                            quadraticTo(
                                x1 = control.x,
                                y1 = control.y,
                                x2 = to.x,
                                y2 = to.y,
                            )
                        }

                        drawPath(
                            path = routePath,
                            color = Color(0x660055FF),
                            style = Stroke(width = 2f / zoom),
                        )

                        val routePoint = flightRoutePoint(
                            from = from,
                            control = control,
                            to = to,
                            progress = (animationTime % 100f) / 100f,
                        )
                        drawCircle(
                            color = Color(0xFFFF3333),
                            radius = 4f / zoom,
                            center = routePoint,
                        )
                    }

                    icelandAirports.forEach { airport ->
                        val selected = airport.id == selectedAirport.id
                        drawCircle(
                            color = if (selected) Color(0xFFFFAA00) else Color(0xFFFF3333),
                            radius = if (selected) 7f / zoom else 5f / zoom,
                            center = Offset(x = airport.x, y = airport.y),
                        )
                        drawCircle(
                            color = Color.White,
                            radius = if (selected) 7f / zoom else 5f / zoom,
                            center = Offset(x = airport.x, y = airport.y),
                            style = Stroke(width = 1.5f / zoom),
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = airport.name,
                            topLeft = Offset(
                                x = airport.x + 8f / zoom,
                                y = airport.y + 4f / zoom,
                            ),
                            style = TextStyle(
                                color = Color(0xFF333333),
                                fontSize = max(6f, 10f / zoom).sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                color = White.copy(alpha = 0.94f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.airport_selected_airport),
                        color = Ink.copy(alpha = 0.68f),
                        fontSize = 11.sp,
                        maxLines = 1,
                    )
                    Text(
                        text = selectedAirport.name,
                        color = Ink,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Text(
                        text = stringResource(
                            R.string.airport_selected_coordinates,
                            selectedAirport.x.toInt(),
                            selectedAirport.y.toInt(),
                        ),
                        color = Ink.copy(alpha = 0.68f),
                        fontSize = 11.sp,
                        maxLines = 1,
                    )
                }
            }
        }

        OutlinedButton(
            onClick = {
                mapTransform = airportMapInitialTransform(viewportScreenSize())
            },
        ) {
            Text(text = stringResource(R.string.airport_reset_view))
        }
    }
}
