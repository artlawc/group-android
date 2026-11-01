package com.groupa.presentation.screen

import android.content.Context
import android.util.Xml
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import com.groupa.coreui.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import java.io.StringReader

private const val MinimumFlightZoom = 0.4f
private const val MaximumFlightZoom = 12f

internal data class FlightSeatPath(
    val path: Path,
    val bounds: Rect,
)

internal data class FlightSeatGroup(
    val name: String,
    val paths: List<FlightSeatPath>,
    val bounds: Rect,
    val center: Offset,
)

internal data class FlightText(
    val content: String,
    val position: Offset,
    val rotationDegrees: Float,
    val fontSize: Float,
)

private data class TextAttrs(
    val x: Float,
    val y: Float,
    val fontSize: Float,
    val transform: String,
)

private data class TextTransform(
    val rotateDegrees: Float,
    val tx: Float,
    val ty: Float,
)

internal data class FlightSeatMap(
    val bounds: Rect,
    val shellPaths: List<FlightSeatPath>,
    val seats: List<FlightSeatGroup>,
    val texts: List<FlightText> = emptyList(),
) {
    fun seatAt(point: Offset, slop: Float = 14f): FlightSeatGroup? {
        return seats
            .filter { seat ->
                point.x in seat.bounds.left - slop..seat.bounds.right + slop &&
                    point.y in seat.bounds.top - slop..seat.bounds.bottom + slop
            }
            .minByOrNull { seat -> (seat.center - point).getDistance() }
    }
}

internal fun Rect.union(other: Rect): Rect {
    return Rect(
        left = min(left, other.left),
        top = min(top, other.top),
        right = max(right, other.right),
        bottom = max(bottom, other.bottom),
    )
}

internal object FlightSeatGeometry {
    fun fit(
        mapBounds: Rect,
        canvasSize: Size,
    ): FlightViewportTransform {
        if (canvasSize.width <= 0f || canvasSize.height <= 0f) {
            return FlightViewportTransform()
        }

        val contentWidth = max(mapBounds.width, 1f)
        val contentHeight = max(mapBounds.height, 1f)
        val zoom = min(
            canvasSize.width / contentWidth,
            canvasSize.height / contentHeight,
        ) * 0.94f
        val contentScreenWidth = contentWidth * zoom
        val contentScreenHeight = contentHeight * zoom

        return FlightViewportTransform(
            pan = Offset(
                x = (canvasSize.width - contentScreenWidth) / 2f - mapBounds.left * zoom,
                y = (canvasSize.height - contentScreenHeight) / 2f - mapBounds.top * zoom,
            ),
            zoom = zoom,
        )
    }
}

private val takenFlightSeats = setOf(
    "26E",
    "26D",
    "26C",
    "25D",
    "23C",
    "21A",
    "20F",
)

@Composable
fun FlightScreen(
    flightId: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val flightMap by produceState<FlightSeatMap?>(initialValue = null, context) {
        value = withContext(Dispatchers.IO) {
            FlightSeatAssetLoader.load(context)
        }
    }

    var selectedSeats by remember { mutableStateOf(emptySet<String>()) }
    var activeSeat by remember { mutableStateOf<String?>(null) }
    var tooltipPoint by remember { mutableStateOf(Offset.Zero) }
    var viewport by remember { mutableStateOf(FlightViewportTransform()) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val textMeasurer = rememberTextMeasurer()
    val viewportState = remember { mutableStateOf(FlightViewportTransform()) }

    LaunchedEffect(flightMap, canvasSize) {
        val map = flightMap
        if (map != null && canvasSize != IntSize.Zero) {
            // Fit and center on the full content (shell + seats + labels), mirroring
            // flight.html's getBBox over the whole viewport group, so the plane is
            // centered instead of off to one side.
            val fitted = FlightSeatGeometry.fit(
                mapBounds = map.bounds,
                canvasSize = Size(
                    width = canvasSize.width.toFloat(),
                    height = canvasSize.height.toFloat(),
                ),
            )
            viewportState.value = fitted
            viewport = fitted
        }
    }

   fun handleSeatTap(tapPoint: Offset, localPoint: Offset) {
       val map = flightMap ?: return
        // Convert the tap from screen space back into SVG source space (inverse of the
        // viewport pan/zoom), mirroring BodyMuscleScreen's offside fix, so the tapped
        // point lines up with the drawn seat geometry.
       val source = viewportState.value.toSource(localPoint)
       val seat = map.seatAt(source)
        if (seat == null) {
            activeSeat = null
            return
        }

        activeSeat = seat.name
        tooltipPoint = tapPoint
        selectedSeats = toggleFlightSeatSelection(
            selectedSeats = selectedSeats,
            seatName = seat.name,
            isTaken = seat.name in takenFlightSeats,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    tint = Color(0xFFE4E6EB),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.flight_title),
                    color = Color(0xFFE4E6EB),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    maxLines = 1,
                )
                Text(
                    text = flightId,
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    maxLines = 1,
                )
            }
            Surface(
                color = Color(0xFF262626),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
            ) {
                Text(
                    text = stringResource(
                        R.string.flight_selected_count,
                        selectedSeats.size,
                    ),
                    color = Color(0xFFE4E6EB),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    maxLines = 1,
                )
            }
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            color = Color.White,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds(),
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { size -> canvasSize = size }
                        .pointerInput(flightMap, canvasSize) {
                            detectTransformGestures(panZoomLock = true) { centroid, pan, zoom, _ ->
                                val current = viewportState.value
                                val next = if (zoom != 1f) {
                                    current.zoomOn(focus = centroid, zoomFactor = zoom)
                                } else {
                                    current.panBy(delta = pan)
                                }
                                if (next != current) {
                                    viewportState.value = next
                                    viewport = next
                                }
                            }
                        }
                        .pointerInput(flightMap, canvasSize) {
                            detectTapGestures(
                                onTap = { localPoint -> handleSeatTap(localPoint, localPoint) },
                            )
                        },
                ) {
                    val map = flightMap ?: return@Canvas
                    val currentViewport = viewportState.value
                    // Viewport maps source -> screen: screen = source * zoom + pan
                    // (BodyMuscleGeometry convention: mapPoint / toSource are exact inverses)
                    val viewportMatrix = Matrix().apply {
                        values[Matrix.ScaleX] = currentViewport.zoom
                        values[Matrix.ScaleY] = currentViewport.zoom
                        values[Matrix.TranslateX] = currentViewport.pan.x
                        values[Matrix.TranslateY] = currentViewport.pan.y
                        values[Matrix.Perspective2] = 1f
                    }

                    map.shellPaths.forEach { shape ->
                        val canvasPath = Path().apply {
                            addPath(shape.path, Offset.Zero)
                            transform(viewportMatrix)
                        }
                        drawPath(path = canvasPath, color = Color.White)
                        drawPath(
                            path = canvasPath,
                            color = Color(0xFF7F7F7F),
                            style = Stroke(width = 1f),
                        )
                    }

                    map.seats.forEach { seat ->
                        val status = flightSeatStatus(
                            seatName = seat.name,
                            isHovered = activeSeat == seat.name,
                            isSelected = seat.name in selectedSeats,
                            isTaken = seat.name in takenFlightSeats,
                        )
                        val colors = flightSeatColors(status)

                        seat.paths.forEach { shape ->
                            val canvasPath = Path().apply {
                                addPath(shape.path, Offset.Zero)
                                transform(viewportMatrix)
                            }
                            drawPath(path = canvasPath, color = colors.first)
                            drawPath(
                                path = canvasPath,
                                color = colors.second,
                                style = Stroke(
                                    width = if (status == FlightSeatStatus.Hovered) {
                                        1.5f
                                    } else {
                                        1f
                                    },
                                ),
                            )
                        }
                    }

                   // Place each label on the seat it belongs to, mirroring flight.html:
                   // row numbers sit on their seat row center line, centered left/right
                   // symmetrically about the plane; column letters sit on their column
                   // center at the top/bottom edge of the plane.
                   val minLeft = map.seats.minOfOrNull { it.bounds.left } ?: 0f
                   val maxRight = map.seats.maxOfOrNull { it.bounds.right } ?: 0f
                   val minTop = map.seats.minOfOrNull { it.bounds.top } ?: 0f
                   val maxBottom = map.seats.maxOfOrNull { it.bounds.bottom } ?: 0f
                   val planeCenterX = (minLeft + maxRight) / 2f
                   val planeCenterY = (minTop + maxBottom) / 2f
                   val labelGap = 20f
                   val leftTextX = minLeft - labelGap
                   val rightTextX = maxRight + labelGap

                   map.texts.forEachIndexed { idx, text ->
                       val content = text.content
                       val isRowNumber = content.matches(Regex("""^\d+$"""))
                       val isColLetter = content.length == 1 && content.first().isLetter()

                       val targetSource: Offset
                       if (isRowNumber) {
                           val rowSeats = map.seats.filter { it.name.dropLast(1) == content }
                            // Pick the lower two seats' y-axis center as the text center,
                            // so the inner (higher) row doesn't pull the text off-axis.
                            val sortedSeats = rowSeats.sortedByDescending { it.bounds.center.y }
                            val rowY = sortedSeats.take(2).fold(0f) { acc, s -> acc + s.bounds.center.y } / sortedSeats.take(2).size.toFloat()
                           val isLeft = text.position.x < planeCenterX
                           val textX = if (isLeft) leftTextX else rightTextX
                           targetSource = Offset(textX, rowY)
                       } else if (isColLetter) {
                           val colSeats = map.seats.filter { it.name.last().toString() == content }
                            val colX = colSeats.fold(0f) { acc, s -> acc + s.bounds.center.x } / colSeats.size.toFloat()
                           val targetY = if (text.position.y < planeCenterY) minTop - labelGap else maxBottom + labelGap
                           targetSource = Offset(colX, targetY)
                       } else {
                           targetSource = text.position
                       }

                       val screenAnchor = Offset(
                           x = targetSource.x * currentViewport.zoom + currentViewport.pan.x,
                           y = targetSource.y * currentViewport.zoom + currentViewport.pan.y,
                       )
                       val textStyle = TextStyle(
                           color = Color.Black,
                           fontSize = text.fontSize.sp,
                       )
                       val textResult = textMeasurer.measure(
                           text = AnnotatedString(text.content),
                           style = textStyle,
                       )
                       val textWidth = textResult.size.width
                       val textHeight = textResult.size.height
                       withTransform(
                           transformBlock = {
                               translate(left = screenAnchor.x, top = screenAnchor.y)
                               rotate(degrees = text.rotationDegrees)
                               translate(left = -textWidth / 2f, top = -textHeight / 2f)
                           },
                       ) {
                           drawText(
                               textMeasurer = textMeasurer,
                               text = text.content,
                               topLeft = Offset.Zero,
                               style = TextStyle(
                                   color = Color.Black,
                                   fontSize = text.fontSize.sp,
                               ),
                           )
                       }
                   }


                }

                val activeSeatName = activeSeat
                if (activeSeatName != null) {
                    val status = flightSeatStatus(
                        seatName = activeSeatName,
                        isHovered = true,
                        isSelected = activeSeatName in selectedSeats,
                        isTaken = activeSeatName in takenFlightSeats,
                    )
                    val statusText = when (status) {
                        FlightSeatStatus.Taken -> stringResource(R.string.flight_status_taken)
                        FlightSeatStatus.Selected -> stringResource(R.string.flight_status_selected)
                        FlightSeatStatus.Hovered -> stringResource(R.string.flight_status_available)
                        FlightSeatStatus.Available -> stringResource(R.string.flight_status_available)
                    }

                    Surface(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = (tooltipPoint.x + 15f).roundToInt(),
                                    y = (tooltipPoint.y + 12f).roundToInt(),
                                )
                            },
                        color = Color(0xD9000000),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            text = stringResource(
                                R.string.flight_seat_tooltip,
                                activeSeatName,
                                statusText,
                            ),
                            color = Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.flight_legend),
                color = Color(0xFF9CA3AF),
                fontSize = 11.sp,
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(
                onClick = {
                    selectedSeats = emptySet()
                    activeSeat = null
                },
            ) {
                Text(text = stringResource(R.string.flight_clear))
            }
        }
    }
}

internal data class FlightViewportTransform(
    val pan: Offset = Offset.Zero,
    val zoom: Float = 1f,
) {
    fun toSource(point: Offset): Offset {
        if (zoom == 0f) return point
        return Offset(
            x = (point.x - pan.x) / zoom,
            y = (point.y - pan.y) / zoom,
        )
    }

    fun zoomOn(focus: Offset, zoomFactor: Float): FlightViewportTransform {
        val nextZoom = (zoom * zoomFactor).coerceIn(MinimumFlightZoom, MaximumFlightZoom)
        val source = toSource(focus)
        val nextPan = Offset(
            x = focus.x - source.x * nextZoom,
            y = focus.y - source.y * nextZoom,
        )
        return copy(pan = nextPan, zoom = nextZoom)
    }

    fun panBy(delta: Offset): FlightViewportTransform =
        copy(pan = Offset(x = pan.x + delta.x, y = pan.y + delta.y))
}

internal enum class FlightSeatStatus {
    Available,
    Hovered,
    Selected,
    Taken,
}

internal fun isFlightSeatName(name: String): Boolean =
    Regex("^[1-4]?[0-9][A-K]$").matches(name)

internal fun toggleFlightSeatSelection(
    selectedSeats: Set<String>,
    seatName: String,
    isTaken: Boolean,
): Set<String> {
    if (isTaken) return selectedSeats
    return if (selectedSeats.contains(seatName)) {
        selectedSeats - seatName
    } else {
        selectedSeats + seatName
    }
}

internal fun flightSeatStatus(
    seatName: String,
    isHovered: Boolean,
    isSelected: Boolean,
    isTaken: Boolean,
): FlightSeatStatus {
    return when {
        isTaken -> FlightSeatStatus.Taken
        isSelected -> FlightSeatStatus.Selected
        isHovered -> FlightSeatStatus.Hovered
        else -> FlightSeatStatus.Available
    }
}

internal fun flightSeatColors(status: FlightSeatStatus): Pair<Color, Color> {
    return when (status) {
        FlightSeatStatus.Taken -> Color(0xFFBF0E08) to Color(0xFF900C06)
        FlightSeatStatus.Selected -> Color(0xFF008000) to Color(0xFF004D00)
        FlightSeatStatus.Hovered -> Color(0x664CAF50) to Color(0xFF4CAF50)
        FlightSeatStatus.Available -> Color.Transparent to Color(0xFF999999)
    }
}

internal object FlightSeatAssetLoader {
    private const val AssetPath = "flight/flight-seats.svg"

    fun load(context: Context): FlightSeatMap {
        val svgText = context.assets.open(AssetPath).bufferedReader().use { reader ->
            reader.readText()
        }
        return parseFlightSeatSvg(svgText)
    }

    fun parseFlightSeatSvg(svgText: String): FlightSeatMap {
        val parser = Xml.newPullParser()
        parser.setInput(StringReader(svgText))

       val shellPathData = mutableListOf<String>()
       val seatPathData = mutableListOf<String>()
       val seats = mutableListOf<FlightSeatGroup>()
        val texts = mutableListOf<FlightText>()
       val matrices = mutableListOf<Matrix>()
       matrices.add(Matrix())

       var groupDepth = 0
       var seatGroupDepth = 0
       var currentSeatName = ""
        var currentText: TextAttrs? = null
        var pendingTextContent: String? = null
       var event = parser.eventType

        fun svgTransform(): Matrix = matrices.last()

        fun pathFrom(pathData: String): FlightSeatPath {
            val androidPath = PathParser().parsePathString(pathData).toPath()
            androidPath.transform(svgTransform())
            return FlightSeatPath(
                path = androidPath,
                bounds = androidPath.getBounds(),
            )
        }

        fun endSeat() {
            if (seatGroupDepth == 0) return
            val paths = seatPathData.map(::pathFrom)
            if (paths.isNotEmpty()) {
                var bounds = paths.first().bounds
                paths.drop(1).forEach { path ->
                    bounds = bounds.union(path.bounds)
                }
                val center = Offset(
                    x = (bounds.left + bounds.right) / 2f,
                    y = (bounds.top + bounds.bottom) / 2f,
                )
                seats.add(
                    FlightSeatGroup(
                        name = currentSeatName,
                        paths = paths,
                        bounds = bounds,
                        center = center,
                    ),
                )
            }
            seatPathData.clear()
         seatGroupDepth = 0
     }
       fun endText() {
           val attrs = currentText ?: return
           currentText = null
           val content = (pendingTextContent ?: "").trim()
           pendingTextContent = null
           if (content.isEmpty()) return
           val transform = parseTextTransform(attrs.transform)
           val posX = if (attrs.x != 0f) attrs.x else transform.tx
           val posY = if (attrs.y != 0f) attrs.y else transform.ty
            // Apply the text element's own transform (rotate/translate) to its anchor (x, y),
            // then bake the root view transform (translate(0,2000) rotate(-90)) into it.
            // Root: rotate(-90) => (x, y) -> (y, -x); then translate(0,2000) => (y, 2000 - x).
            // This matches pathFrom(), which bakes svgTransform() into every seat path,
            // so labels land in the exact same source space as seats.
            val rad = transform.rotateDegrees * Math.PI / 180f
            val c = kotlin.math.cos(rad).toFloat()
            val s = kotlin.math.sin(rad).toFloat()
            var anchorX = posX * c - posY * s + transform.tx
            var anchorY = posX * s + posY * c + transform.ty
            val position = Offset(x = anchorY, y = 2000f - anchorX)
            val rotationDegrees = transform.rotateDegrees - 90f
            texts.add(
                FlightText(
                    content = content,
                    position = position,
                    rotationDegrees = rotationDegrees,
                    fontSize = attrs.fontSize,
                ),
            )
        }


  while (event != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
         when (event) {
                org.xmlpull.v1.XmlPullParser.START_TAG -> {
                    val tagName = parser.name
                    if (tagName == "g") {
                        groupDepth += 1
                        val seatName = parser.getAttributeValue(null, "name").orEmpty()
                        if (isFlightSeatName(seatName)) {
                            if (seatGroupDepth == 0) {
                                seatGroupDepth = groupDepth
                                currentSeatName = seatName
                                seatPathData.clear()
                            }
                        }

                        val transform = parser.getAttributeValue(null, "transform")
                        if (!transform.isNullOrBlank()) {
                            matrices.add(parseSvgTransform(transform))
                        }
                    }

                    if (tagName == "path" || tagName == "rect") {
                        val pathData = parser.getAttributeValue(null, "d")
                        val rectX = parser.getAttributeValue(null, "x")?.toFloatOrNull()
                        val rectY = parser.getAttributeValue(null, "y")?.toFloatOrNull()
                        val rectWidth = parser.getAttributeValue(null, "width")?.toFloatOrNull()
                        val rectHeight = parser.getAttributeValue(null, "height")?.toFloatOrNull()

                        val resolvedPathData = when {
                            tagName == "path" && !pathData.isNullOrEmpty() -> pathData
                            tagName == "rect" && rectX != null && rectY != null &&
                                rectWidth != null && rectHeight != null -> {
                                "M ${rectX},$rectY h $rectWidth v $rectHeight h -$rectWidth Z"
                            }
                            else -> null
                        }

                        if (resolvedPathData != null && seatGroupDepth > 0) {
                            seatPathData.add(resolvedPathData)
                       } else if (resolvedPathData != null) {
                           shellPathData.add(resolvedPathData)
                       }
                   }
                    if (tagName == "text") {
                        currentText = TextAttrs(
                            x = parser.getAttributeValue(null, "x")?.toFloatOrNull() ?: 0f,
                            y = parser.getAttributeValue(null, "y")?.toFloatOrNull() ?: 0f,
                            fontSize = parser.getAttributeValue(null, "font-size")?.toFloatOrNull() ?: 12f,
                            transform = parser.getAttributeValue(null, "transform").orEmpty(),
                        )
                        pendingTextContent = ""
                    }
               }

                org.xmlpull.v1.XmlPullParser.TEXT -> {
                    if (pendingTextContent != null) {
                        pendingTextContent += parser.text
                    }
                }
                org.xmlpull.v1.XmlPullParser.END_TAG -> {
                    if (parser.name == "g") {
                        endSeat()
                        groupDepth -= 1
                    }
                    if (parser.name == "text") {
                        endText()
                    }
                }
            }
            event = parser.next()
        }

       val shellPaths = shellPathData.map(::pathFrom)
       var contentBounds = shellPaths.firstOrNull()?.bounds ?: Rect.Zero
      shellPaths.drop(1).forEach { shell ->
          contentBounds = contentBounds.union(shell.bounds)
      }
       seats.forEach { seat ->
           contentBounds = contentBounds.union(seat.bounds)
       }

       return FlightSeatMap(
           bounds = contentBounds,
           shellPaths = shellPaths,
           seats = seats,
           texts = texts,
       )
    }

   private fun parseSvgTransform(value: String): Matrix {
       val matrix = Matrix()
        val transformPattern = Regex("(translate|rotate|matrix)\\s*\\(([^)]*)\\)")
       transformPattern.findAll(value).forEach { match ->
           val operation = match.groupValues[1]
           val arguments = match.groupValues[2]
                .split(Regex("\\s|,"))
                .map { token -> token.trim() }
                .filter { token -> token.isNotEmpty() }
                .mapNotNull { token -> token.toFloatOrNull() }

            when (operation) {
                "translate" -> {
                    val translateX = arguments.getOrNull(0) ?: 0f
                    val translateY = arguments.getOrNull(1) ?: 0f
                    matrix.translate(translateX, translateY)
                }

                "rotate" -> {
                    val degrees = arguments.getOrNull(0) ?: 0f
                    matrix.rotateZ(degrees)
                }

                "matrix" -> {
                    // SVG matrix(a b c d e f): x' = a*x + c*y + e, y' = b*x + d*y + f
                    // Compose Matrix values: [ScaleX, SkewX, TransX, SkewY, ScaleY, TransY, ...]
                    val a = arguments.getOrNull(0) ?: 1f
                    val b = arguments.getOrNull(1) ?: 0f
                    val c = arguments.getOrNull(2) ?: 0f
                    val d = arguments.getOrNull(3) ?: 1f
                    val e = arguments.getOrNull(4) ?: 0f
                    val f = arguments.getOrNull(5) ?: 0f
                    matrix.values[Matrix.ScaleX] = a
                    matrix.values[Matrix.SkewX] = c
                    matrix.values[Matrix.TranslateX] = e
                    matrix.values[Matrix.SkewY] = b
                    matrix.values[Matrix.ScaleY] = d
                    matrix.values[Matrix.TranslateY] = f
                    matrix.values[Matrix.Perspective0] = 0f
                    matrix.values[Matrix.Perspective1] = 0f
                    matrix.values[Matrix.Perspective2] = 1f
                }
            }
        }
      return matrix
  }
   private fun parseTextTransform(value: String): TextTransform {
       var rotateDegrees = 0f
       var tx = 0f
       var ty = 0f
       val pattern = Regex("(translate|rotate|matrix)\\s*\\(([^)]*)\\)")
       pattern.findAll(value).forEach { match ->
           val operation = match.groupValues[1]
           val arguments = match.groupValues[2]
                .split(Regex("\\s|,"))
                .map { token -> token.trim() }
                .filter { token -> token.isNotEmpty() }
                .mapNotNull { token -> token.toFloatOrNull() }
            when (operation) {
                "translate" -> {
                    tx = arguments.getOrNull(0) ?: 0f
                    ty = arguments.getOrNull(1) ?: 0f
                }
                "rotate" -> {
                    rotateDegrees = arguments.getOrNull(0) ?: 0f
                }
                "matrix" -> {
                    tx = arguments.getOrNull(4) ?: 0f
                    ty = arguments.getOrNull(5) ?: 0f
                }
            }
        }
        return TextTransform(rotateDegrees, tx, ty)
    }
}


