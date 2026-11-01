package com.groupa.presentation.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import com.caverock.androidsvg.SVG
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groupa.coreui.R
import com.groupa.coreui.theme.Ink
import com.groupa.coreui.theme.White
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

internal data class NavIndoorPathPoint(
    val x: Float,
    val y: Float,
)

internal data class NavIndoorPathSample(
    val x: Float,
    val y: Float,
    val angleDegrees: Float,
)

private const val NavIndoorMarkerSpeedPx = 2f

/**
 * The navigation actor's SVG symbol path — the same symbol path used by
 * lottery/guide.html, so the Android actor matches the web reference exactly.
 */
const val symbolPathString =
    "M35.5 40.5c0-22.16 17.84-40 40-40s40 17.84 40 40c0 1.6939-.1042 3.3626-.3067 5H35.8067c-.2025-1.6374-.3067-3.3061-.3067-5zm90.9621-2.6663c-.62-1.4856-.9621-3.1182-.9621-4.8337 0-6.925 5.575-12.5 12.5-12.5s12.5 5.575 12.5 12.5a12.685 12.685 0 0 1-.1529 1.9691l.9537.5506-15.6454 27.0986-.1554-.0897V65.5h-28.7285c-7.318 9.1548-18.587 15-31.2715 15s-23.9535-5.8452-31.2715-15H15.5v-2.8059l-.0937.0437-8.8727-19.0274C2.912 41.5258.5 37.5549.5 33c0-6.925 5.575-12.5 12.5-12.5S25.5 26.075 25.5 33c0 .9035-.0949 1.784-.2753 2.6321L29.8262 45.5h92.2098z"

internal val navIndoorRoutePoints = listOf(
    NavIndoorPathPoint(110.6189462165178f, 456.64349563895087f),
    NavIndoorPathPoint(124.10988522879458f, 450.8570048730469f),
    NavIndoorPathPoint(123.9272226116071f, 389.9520693708147f),
    NavIndoorPathPoint(61.58708083147317f, 386.87942320312504f),
    NavIndoorPathPoint(61.58708083147317f, 72.8954315876116f),
    NavIndoorPathPoint(258.29514854771196f, 72.8954315876116f),
    NavIndoorPathPoint(260.75457021484374f, 336.8559607533482f),
    NavIndoorPathPoint(280.5277985253906f, 410.2406672084263f),
    NavIndoorPathPoint(275.948185765904f, 528.0254369698661f),
    NavIndoorPathPoint(111.06907909458701f, 552.795792593471f),
    NavIndoorPathPoint(118.87138231445309f, 701.365737015904f),
    NavIndoorPathPoint(221.36468155133926f, 758.7870354617745f),
    NavIndoorPathPoint(307.86195445452006f, 742.164737297712f),
    NavIndoorPathPoint(366.8489324762834f, 560.9895157073103f),
    NavIndoorPathPoint(492.8750778390066f, 560.9895157073103f),
    NavIndoorPathPoint(492.8750778390066f, 827.9639780566406f),
    NavIndoorPathPoint(294.9255269587053f, 827.9639780566406f),
    NavIndoorPathPoint(282.79803391043527f, 868.2476088113839f),
)

internal fun navIndoorRouteBounds(points: List<NavIndoorPathPoint>): Rect {
    if (points.isEmpty()) return Rect(0f, 0f, 0f, 0f)
    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }
    val minY = points.minOf { it.y }
    val maxY = points.maxOf { it.y }
    return Rect(left = minX, top = minY, right = maxX, bottom = maxY)
}

internal fun navIndoorRouteLength(points: List<NavIndoorPathPoint>): Float {
    if (points.size < 2) return 0f
    var total = 0f
    for (index in 1 until points.size) {
        total += hypot(points[index].x - points[index - 1].x, points[index].y - points[index - 1].y)
    }
    return total
}

internal fun navIndoorRoutePointAtDistance(points: List<NavIndoorPathPoint>, distance: Float): NavIndoorPathSample {
    if (points.isEmpty()) return NavIndoorPathSample(0f, 0f, 0f)
    if (points.size == 1) {
        return NavIndoorPathSample(points[0].x, points[0].y, 0f)
    }
    if (distance <= 0f) {
        return NavIndoorPathSample(points[0].x, points[0].y, 0f)
    }

    var remaining = distance
    for (index in 1 until points.size) {
        val deltaX = points[index].x - points[index - 1].x
        val deltaY = points[index].y - points[index - 1].y
        val segmentLength = hypot(deltaX, deltaY)
        if (remaining <= segmentLength) {
            val progress = if (segmentLength == 0f) 0f else remaining / segmentLength
            return NavIndoorPathSample(
                x = points[index - 1].x + deltaX * progress,
                y = points[index - 1].y + deltaY * progress,
                angleDegrees = Math.toDegrees(atan2(deltaY.toDouble(), deltaX.toDouble())).toFloat(),
            )
        }
        remaining -= segmentLength
    }

    val last = points.last()
    val previous = points[points.size - 2]
    return NavIndoorPathSample(
        x = last.x,
        y = last.y,
        angleDegrees = Math.toDegrees(atan2((last.y - previous.y).toDouble(), (last.x - previous.x).toDouble())).toFloat(),
    )
}

internal fun navIndoorRouteProgress(points: List<NavIndoorPathPoint>, distance: Float): Float {
    val total = navIndoorRouteLength(points)
    if (total <= 0f) return 0f
    return (distance / total).coerceIn(0f, 1f)
}

internal fun navIndoorRouteNextDistance(points: List<NavIndoorPathPoint>, distance: Float): Float {
    val total = navIndoorRouteLength(points)
    if (total <= 0f) return 0f
    val updated = distance + NavIndoorMarkerSpeedPx
    return if (updated >= total) 0f else updated
}

/**
 * Viewport transform that maps SVG document coordinates -> screen coordinates.
 * The same transform is applied to the route track and the actor so everything
 * shares a single coordination system (the actor stays on the track).
 */
internal data class NavIndoorTransform(
    val pan: Offset,
    val zoom: Float,
) {
    fun map(point: Offset): Offset =
        Offset(point.x * zoom + pan.x, point.y * zoom + pan.y)

    fun toMatrix(): Matrix = Matrix().apply {
        values[Matrix.ScaleX] = zoom
        values[Matrix.ScaleY] = zoom
        values[Matrix.TranslateX] = pan.x
        values[Matrix.TranslateY] = pan.y
        values[Matrix.Perspective2] = 1f
    }
}

internal object NavIndoorViewportTransform {
    fun fit(contentBounds: Rect, canvasSize: Size): NavIndoorTransform {
        if (canvasSize.width <= 0f || canvasSize.height <= 0f) {
            return NavIndoorTransform(Offset.Zero, 1f)
        }
        val contentWidth = max(contentBounds.width, 1f)
        val contentHeight = max(contentBounds.height, 1f)
        val zoom = min(canvasSize.width / contentWidth, canvasSize.height / contentHeight)
        val contentScreenWidth = contentWidth * zoom
        val contentScreenHeight = contentHeight * zoom
        return NavIndoorTransform(
            pan = Offset(
                x = (canvasSize.width - contentScreenWidth) / 2f - contentBounds.left * zoom,
                y = (canvasSize.height - contentScreenHeight) / 2f - contentBounds.top * zoom,
            ),
            zoom = zoom,
        )
    }
}

internal data class NavIndoorSvgAsset(
    val bounds: Rect,
    val image: ImageBitmap,
)

@Composable
fun NavIndoorScreen(
    navIndoorId: String,
    onBack: () -> Unit,
) {
    val textMeasurer = rememberTextMeasurer()
    val context = LocalContext.current
    val routePoints = remember(navIndoorId) { navIndoorRoutePoints }
    var routeDistance by remember(navIndoorId) { mutableFloatStateOf(0f) }
    var isRouteAnimating by remember { mutableStateOf(true) }

    LaunchedEffect(isRouteAnimating, routePoints) {
        while (isRouteAnimating) {
            delay(16)
            routeDistance = navIndoorRouteNextDistance(routePoints, routeDistance)
        }
    }

    val routeProgress = navIndoorRouteProgress(routePoints, routeDistance)

    // Read the floorplan SVG's intrinsic document size so 1 SVG unit == 1 unit in
    // the route coordinate system, and render it once to a bitmap so the canvas
    // background is never blank. Shared with the route via [navTransform].
    val svgAsset by produceState<NavIndoorSvgAsset?>(initialValue = null, navIndoorId) {
        value = withContext(Dispatchers.IO) {
            try {
                val svg = SVG.getFromInputStream(
                    context.assets.open("navindoor/macodrum-lv5-floorplan-web.svg")
                )
                val w = svg.documentWidth.coerceAtLeast(1f)
                val h = svg.documentHeight.coerceAtLeast(1f)
                val bitmap = Bitmap.createBitmap(
                    w.coerceAtLeast(1f).toInt(),
                    h.coerceAtLeast(1f).toInt(),
                    Bitmap.Config.ARGB_8888,
                )
                svg.renderToCanvas(AndroidCanvas(bitmap))
                NavIndoorSvgAsset(
                    bounds = Rect(0f, 0f, w, h),
                    image = bitmap.asImageBitmap(),
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    var canvasSize by remember { mutableStateOf(Size.Zero) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
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
                    text = stringResource(R.string.nav_indoor_title),
                    color = Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    maxLines = 1,
                )
                Text(
                    text = stringResource(R.string.nav_indoor_subtitle),
                    color = Ink.copy(alpha = 0.72f),
                    fontSize = 12.sp,
                    maxLines = 1,
                )
            }
            Surface(
                color = Color(0xFFF1F5F9),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
            ) {
                Text(
                    text = stringResource(
                        R.string.nav_indoor_route_progress,
                        (routeProgress * 100f).toInt(),
                    ),
                    color = Ink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFF4F4F4))
                .padding(12.dp),
        ) {
            val navTransform = remember(svgAsset?.bounds, canvasSize) {
                val bounds = svgAsset?.bounds ?: Rect.Zero
                NavIndoorViewportTransform.fit(bounds, canvasSize)
            }

            // Background: the rendered floorplan SVG. ContentScale.Fit matches
            // [NavIndoorViewportTransform.fit] exactly, so the background stays
            // aligned with the route track and the canvas is never blank.
            val backgroundImage = svgAsset?.image
            if (backgroundImage != null) {
                Image(
                    bitmap = backgroundImage,
                    contentScale = ContentScale.Fit,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            val actorPath = remember(navIndoorId) {
                Path().apply { parseSymbolPath(symbolPathString) }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
            ) {
                val bounds = svgAsset?.bounds ?: Rect.Zero
                val viewportMatrix = navTransform.toMatrix()

                // Route track (same transform as the SVG document bounds).
                val routePath = Path().apply {
                    moveTo(routePoints[0].x, routePoints[0].y)
                    for (index in 1 until routePoints.size) {
                        lineTo(routePoints[index].x, routePoints[index].y)
                    }
                    transform(viewportMatrix)
                }
                val trackStroke = 12f / coerceAtLeast(navTransform.zoom, 0.001f)
                drawPath(
                    path = routePath,
                    color = Color(0x66A84E32),
                    style = Stroke(width = trackStroke),
                )
                drawPath(
                    path = routePath,
                    color = Color(0xFFC46E54),
                    style = Stroke(width = trackStroke / 2f),
                )

                // Start / end markers (same transform).
                val routeStart = navTransform.map(Offset(routePoints[0].x, routePoints[0].y))
                val routeEnd = navTransform.map(Offset(routePoints.last().x, routePoints.last().y))
                val dotRadius = 8f / coerceAtLeast(navTransform.zoom, 0.001f)
                drawCircle(color = Color(0xFF16A34A), radius = dotRadius, center = routeStart)
                drawCircle(color = Color.White, radius = dotRadius * 0.375f, center = routeStart)
                drawCircle(color = Color(0xFFDC2626), radius = dotRadius, center = routeEnd)
                drawCircle(color = Color.White, radius = dotRadius * 0.375f, center = routeEnd)

                // Actor / marker: the custom SVG symbol from lottery/guide.html
                // (symbolPathString), translated to its position on the track in
                // document space, rotated with the track direction (+90°), scaled
                // and centered — then mapped to screen space with the same viewport
                // transform as the route so it stays on the track.
                val markerSample = navIndoorRoutePointAtDistance(routePoints, routeDistance)

                val rot = ((markerSample.angleDegrees + 90f) * PI / 180f).toFloat()
                val cos = cos(rot)
                val sin = sin(rot)

                val screenSpaceActorPath = Path().apply {
                    parseSymbolPath(symbolPathString) { x, y ->
                        // Document space: translate(-75, -40), scale(0.15), rotate(+90°).
                        val x1 = 0.15f * (x - 75f)
                        val y1 = 0.15f * (y - 40f)
                        val x2 = cos * x1 - sin * y1
                        val y2 = sin * x1 + cos * y1
                        // Screen space: track position, then the viewport transform.
                        Pair(
                            navTransform.zoom * (x2 + markerSample.x) + navTransform.pan.x,
                            navTransform.zoom * (y2 + markerSample.y) + navTransform.pan.y,
                        )
                    }
                }
                drawPath(path = screenSpaceActorPath, color = Color(0xFFA84E32))

                drawText(
                    textMeasurer = textMeasurer,
                    text = routePoints[0].x.toInt().toString(),
                    topLeft = Offset(x = routeStart.x - 8f, y = routeStart.y - 28f),
                    style = TextStyle(color = Color(0xFF166534), fontSize = 9.sp, fontWeight = FontWeight.Bold),
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = routePoints.last().x.toInt().toString(),
                    topLeft = Offset(x = routeEnd.x - 8f, y = routeEnd.y - 28f),
                    style = TextStyle(color = Color(0xFF991B1B), fontSize = 9.sp, fontWeight = FontWeight.Bold),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = { isRouteAnimating = !isRouteAnimating },
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(
                        if (isRouteAnimating) {
                            R.string.nav_indoor_pause
                        } else {
                            R.string.nav_indoor_play
                        },
                    ),
                )
            }
            OutlinedButton(
                onClick = { routeDistance = 0f },
                modifier = Modifier.weight(1f),
            ) {
                Text(text = stringResource(R.string.nav_indoor_reset))
            }
        }
    }
}

private fun coerceAtLeast(value: Float, min: Float): Float = if (value < min) min else value

/**
 * Parses the SVG path data used for the navigation actor ([symbolPathString]) into this
 * [Path]. Supports the subset used by the actor: M/m, Z/z, L/l, H/h, V/v, C/c, S/s, A/a.
 * Relative commands are interpreted relative to the current point, exactly as in SVG.
 * An optional [transform] is applied to every point (useful for mapping document space
 * to screen space).
 */
private fun Path.parseSymbolPath(
    pathData: String,
    transform: ((x: Float, y: Float) -> Pair<Float, Float>)? = null,
) {
    val tokens = tokenizeSvgPathData(pathData)
    var index = 0

    var curX = 0f
    var curY = 0f
    var startX = 0f
    var startY = 0f
    // Control point of the most recent cubic or smooth-cubic command, mirrored by 's'.
    var ctrlX = 0f
    var ctrlY = 0f
    var lastCommand = '\u0000'

    fun nextFloat(): Float = tokens[index].toFloat().also { index++ }
    fun isCommand(token: String) = token.length == 1 && token.first() in "MLCSQTAHVZmlcsqtahvz"
    fun point(x: Float, y: Float): Pair<Float, Float> = transform?.invoke(x, y) ?: Pair(x, y)

    while (index < tokens.size) {
        val command = tokens[index++]
        val first = command.first()
        val isRelative = first.isLowerCase()
        val cmd = first.uppercaseChar()

        fun absX(dx: Float) = if (isRelative) curX + dx else dx
        fun absY(dy: Float) = if (isRelative) curY + dy else dy

        when (cmd) {
            'M' -> {
                val x = absX(nextFloat())
                val y = absY(nextFloat())
                curX = x
                curY = y
                startX = x
                startY = y
                ctrlX = x
                ctrlY = y
                val (tx, ty) = point(x, y)
                moveTo(tx, ty)
            }
            'Z' -> {
                close()
                curX = startX
                curY = startY
            }
            'L' -> {
                val x = absX(nextFloat())
                val y = absY(nextFloat())
                curX = x
                curY = y
                val (tx, ty) = point(x, y)
                lineTo(tx, ty)
            }
            'H' -> {
                val x = absX(nextFloat())
                curX = x
                val (tx, ty) = point(x, curY)
                lineTo(tx, ty)
            }
            'V' -> {
                val y = absY(nextFloat())
                curY = y
                val (tx, ty) = point(curX, y)
                lineTo(tx, ty)
            }
            'C' -> {
                val x1 = absX(nextFloat())
                val y1 = absY(nextFloat())
                val x2 = absX(nextFloat())
                val y2 = absY(nextFloat())
                val x = absX(nextFloat())
                val y = absY(nextFloat())
                curX = x
                curY = y
                ctrlX = x2
                ctrlY = y2
                val (t1x, t1y) = point(x1, y1)
                val (t2x, t2y) = point(x2, y2)
                val (tx, ty) = point(x, y)
                cubicTo(t1x, t1y, t2x, t2y, tx, ty)
            }
            'S' -> {
                val x2 = absX(nextFloat())
                val y2 = absY(nextFloat())
                val x = absX(nextFloat())
                val y = absY(nextFloat())
                val x1 = if (lastCommand == 'C' || lastCommand == 'S') 2f * curX - ctrlX else curX
                val y1 = if (lastCommand == 'C' || lastCommand == 'S') 2f * curY - ctrlY else curY
                curX = x
                curY = y
                ctrlX = x2
                ctrlY = y2
                val (t1x, t1y) = point(x1, y1)
                val (t2x, t2y) = point(x2, y2)
                val (tx, ty) = point(x, y)
                cubicTo(t1x, t1y, t2x, t2y, tx, ty)
            }
            'A' -> {
                val rx = Math.abs(nextFloat())
                val ry = Math.abs(nextFloat())
                val xAxisRotation = nextFloat()
                val largeArc = nextFloat()
                val sweep = nextFloat()
                val endX = if (isRelative) curX + nextFloat() else nextFloat()
                val endY = if (isRelative) curY + nextFloat() else nextFloat()
                this.appendArcTo(curX, curY, endX, endY, rx, ry, xAxisRotation, largeArc, sweep) { x, y ->
                    point(x, y)
                }
                curX = endX
                curY = endY
                ctrlX = endX
                ctrlY = endY
            }
        }
        lastCommand = cmd
    }
}

/**
 * Appends an elliptical arc from (startX, startY) to (endX, endY) to this [Path],
 * approximated by cubic segments (W3C arc-to-Bézier conversion). Each resulting cubic
 * is passed through [pointTransform] (e.g. document-space -> screen-space).
 */
private fun Path.appendArcTo(
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float,
    rx: Float,
    ry: Float,
    xAxisRotation: Float,
    largeArc: Float,
    sweep: Float,
    pointTransform: ((x: Float, y: Float) -> Pair<Float, Float>)?,
) {
    if (startX == endX && startY == endY) return
    val rX = Math.abs(rx)
    val rY = Math.abs(ry)
    if (rX == 0f || rY == 0f) {
        val (tx, ty) = pointTransform?.invoke(endX, endY) ?: Pair(endX, endY)
        lineTo(tx, ty)
        return
    }

    val pi = PI.toFloat()

    // Step 1: rotate into the ellipse frame.
    val dx = (startX - endX) / 2f
    val dy = (startY - endY) / 2f
    val rot = xAxisRotation * pi / 180f
    val cos = cos(rot)
    val sin = sin(rot)
    val x1p = cos * dx + sin * dy
    val y1p = -sin * dx + cos * dy

    // Step 2: correct out-of-range radii.
    val radiiCheck = (x1p * x1p) / (rX * rX) + (y1p * y1p) / (rY * rY)
    var rxAdj = rX
    var ryAdj = rY
    if (radiiCheck > 1f) {
        val scale = sqrt(radiiCheck)
        rxAdj *= scale
        ryAdj *= scale
    }

    // Step 3: compute the ellipse center.
    val sign = if (largeArc == sweep) -1f else 1f
    val numerator = rxAdj * rxAdj * ryAdj * ryAdj - rxAdj * rxAdj * y1p * y1p - ryAdj * ryAdj * x1p * x1p
    val denominator = rxAdj * rxAdj * y1p * y1p + ryAdj * ryAdj * x1p * x1p
    val coef = sign * sqrt(max(0f, numerator / denominator))
    val cxp = coef * rxAdj * y1p / ryAdj
    val cyp = coef * -ryAdj * x1p / rxAdj
    val centerX = cos * cxp - sin * cyp + (startX + endX) / 2f
    val centerY = sin * cxp + cos * cyp + (startY + endY) / 2f

    // Step 4: start and sweep angles.
    val v1x = (x1p - cxp) / rxAdj
    val v1y = (y1p - cyp) / ryAdj
    val v2x = (-x1p - cxp) / rxAdj
    val v2y = (-y1p - cyp) / ryAdj
    var deltaAngle = atan2(v1x * v2y - v1y * v2x, v1x * v2x + v1y * v2y).toFloat()
    if (sweep == 0f && deltaAngle > 0f) deltaAngle -= 2f * pi
    if (sweep == 1f && deltaAngle < 0f) deltaAngle += 2f * pi

    // Step 5: approximate with cubic segments of at most 90 degrees each.
    val segments = ceil(Math.abs(deltaAngle) / (pi / 2f)).toInt().coerceAtLeast(1)
    val theta = deltaAngle / segments
    var t = atan2(v1y, v1x).toFloat()
    repeat(segments) {
        val t2 = t + theta
        val alpha = sin(theta) * (sqrt(4f + 3f * tan(theta / 2f) * tan(theta / 2f)) - 1f) / 3f
        val x1u = cos(t)
        val y1u = sin(t)
        val x2u = cos(t2)
        val y2u = sin(t2)
        val c1px = rxAdj * (x1u - alpha * y1u)
        val c1py = rY * (y1u + alpha * x1u)
        val c2px = rxAdj * (x2u + alpha * y2u)
        val c2py = rY * (y2u - alpha * x2u)
        val endPx = rxAdj * x2u
        val endPy = rY * y2u
        val (tc1x, tc1y) = pointTransform?.invoke(
            centerX + cos * c1px - sin * c1py,
            centerY + sin * c1px + cos * c1py,
        ) ?: Pair(
            centerX + cos * c1px - sin * c1py,
            centerY + sin * c1px + cos * c1py,
        )
        val (tc2x, tc2y) = pointTransform?.invoke(
            centerX + cos * c2px - sin * c2py,
            centerY + sin * c2px + cos * c2py,
        ) ?: Pair(
            centerX + cos * c2px - sin * c2py,
            centerY + sin * c2px + cos * c2py,
        )
        val (tex, tey) = pointTransform?.invoke(
            centerX + cos * endPx - sin * endPy,
            centerY + sin * endPx + cos * endPy,
        ) ?: Pair(
            centerX + cos * endPx - sin * endPy,
            centerY + sin * endPx + cos * endPy,
        )
        cubicTo(tc1x, tc1y, tc2x, tc2y, tex, tey)
        t = t2
    }
}

/**
 * Splits an SVG path data string into command and number tokens, handling commands
 * glued to numbers (e.g. `40.5c0`) and signed/exponent notation.
 */
private fun tokenizeSvgPathData(pathData: String): List<String> {
    val tokens = mutableListOf<String>()
    var i = 0
    while (i < pathData.length) {
        val ch = pathData[i]
        when {
            ch.isWhitespace() || ch == ',' -> i++
            ch in "MLCSQTAHVZmlcsqtahvz" -> {
                tokens.add(ch.toString())
                i++
            }
           else -> {
               val start = i
               i++
                var seenDot = pathData[start] == '.'
               while (i < pathData.length) {
                   val c = pathData[i]
                   when {
                        c.isDigit() -> i++
                        c == '.' -> {
                            if (seenDot) break
                            seenDot = true
                            i++
                        }
                       c == 'e' || c == 'E' -> {
                           i++
                           if (i < pathData.length && (pathData[i] == '+' || pathData[i] == '-')) i++
                        }
                        else -> break
                    }
                }
                tokens.add(pathData.substring(start, i))
            }
        }
    }
    return tokens
}
