package com.groupa.presentation.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import com.caverock.androidsvg.SVG
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import java.util.regex.Pattern

internal data class CarMapPathPoint(
    val x: Float,
    val y: Float,
)

internal data class CarMapPathSample(
    val x: Float,
    val y: Float,
    val angleDegrees: Float,
)

internal data class CarMapTrack(
    val color: Color,
    val speedPx: Float,
    val coords: List<CarMapPathPoint>,
)

const val carSymbolPathString =
    "M87.1667 3.8333L80.5.5h-60l-6.6667 3.3333L.5 70.5v130l10 10h80l10 -10v-130z" +
    "M15.5 190.5l15 -20h40l15 20zm75 -65l-15 5v35l15 15zm-80 0l15 5v35l-15 15zm65 0l15 -5v-40l-15 20zm-50 0l-15 -5v-40l15 20zm 65,-55 -15,25 c -15,-5 -35,-5 -50,0 l -15,-25 c 25,-15 55,-15 80,0 z"

internal val carMapTracks = listOf(
    CarMapTrack(
        color = Color(0xFFA10000),
        speedPx = 1.5f,
        coords = listOf(
            CarMapPathPoint(50.875133928571415f, 242.66287667410717f),
            CarMapPathPoint(62.03696428571425f, 264.482421875f),
            CarMapPathPoint(72.63357421874997f, 273.62779017857144f),
            CarMapPathPoint(92.78291852678569f, 285.869140625f),
            CarMapPathPoint(113.43637834821425f, 287.21854073660717f),
            CarMapPathPoint(141.44788783482142f, 288.92947823660717f),
            CarMapPathPoint(191.71686104910714f, 289.5507114955357f),
            CarMapPathPoint(198.3060072544643f, 294.0673828125f),
            CarMapPathPoint(204.99699497767858f, 304.60288783482144f),
            CarMapPathPoint(210.79177734375003f, 316.7373046875f),
            CarMapPathPoint(212.45179408482142f, 329.3656529017857f),
            CarMapPathPoint(210.8885267857143f, 443.3925083705358f),
            CarMapPathPoint(215.35936941964286f, 453.00634765625f),
            CarMapPathPoint(224.38761997767858f, 452.15087890625f),
            CarMapPathPoint(265.71490792410714f, 452.20179966517856f),
            CarMapPathPoint(493.3408844866072f, 453.77525111607144f),
            CarMapPathPoint(572.8892940848216f, 448.77992466517856f),
            CarMapPathPoint(608.9513755580358f, 448.43366350446433f),
            CarMapPathPoint(619.99099609375f, 450.8778599330358f),
            CarMapPathPoint(624.2479715401787f, 456.2194475446429f),
            CarMapPathPoint(628.1434095982145f, 463.9899553571429f),
            CarMapPathPoint(629.8492550223216f, 476.0276227678571f),
            CarMapPathPoint(631.2750362723216f, 535.7322126116071f),
            CarMapPathPoint(624.6757059151787f, 546.6496233258929f),
            CarMapPathPoint(617.1801702008929f, 552.6480887276786f),
            CarMapPathPoint(603.7269056919645f, 554.5066964285714f),
            CarMapPathPoint(588.0178515625f, 557.5517578125f),
            CarMapPathPoint(529.4386104910716f, 556.2991071428571f),
            CarMapPathPoint(422.1994921875001f, 551.38525390625f),
            CarMapPathPoint(291.66921875f, 552.5767996651786f),
            CarMapPathPoint(219.4279380580357f, 547.4949079241071f),
            CarMapPathPoint(209.53912667410714f, 541.5931919642858f),
            CarMapPathPoint(206.70793247767858f, 526.1947544642858f),
            CarMapPathPoint(206.70793247767858f, 507.4049944196429f),
            CarMapPathPoint(206.12234375000003f, 468.7663225446429f),
            CarMapPathPoint(204.48778738839286f, 459.44782366071433f),
            CarMapPathPoint(197.56256417410714f, 452.8943219866071f),
            CarMapPathPoint(170.31995814732142f, 456.27546037946433f),
            CarMapPathPoint(1.8078906249999704f, 460.5935407366071f),
        ),
    ),
    CarMapTrack(
        color = Color(0xFF00067D),
        speedPx = 1.2f,
        coords = listOf(
            CarMapPathPoint(779.4595368303574f, 287.98744419642856f),
            CarMapPathPoint(689.07009765625f, 291.0477818080357f),
            CarMapPathPoint(301.83300223214286f, 290.49783761160717f),
            CarMapPathPoint(229.31165736607142f, 291.73011997767856f),
            CarMapPathPoint(220.73660156250003f, 297.4077845982143f),
            CarMapPathPoint(214.74832031250003f, 308.52378627232144f),
            CarMapPathPoint(213.82156250000003f, 421.35400390625f),
            CarMapPathPoint(213.19523716517858f, 443.0564313616071f),
            CarMapPathPoint(222.31005301339286f, 455.95465959821433f),
            CarMapPathPoint(271.71846540178575f, 454.37611607142856f),
            CarMapPathPoint(359.64843191964286f, 455.9393833705358f),
            CarMapPathPoint(580.2524358258929f, 448.11286272321433f),
            CarMapPathPoint(627.7156752232145f, 460.7463030133929f),
            CarMapPathPoint(632.3290959821429f, 536.6386021205358f),
            CarMapPathPoint(628.9123130580358f, 548.4776785714286f),
            CarMapPathPoint(612.5667494419645f, 556.8235909598214f),
            CarMapPathPoint(543.7167912946429f, 555.4741908482143f),
            CarMapPathPoint(429.1756361607143f, 551.9402901785714f),
            CarMapPathPoint(293.42089285714286f, 551.2172154017858f),
            CarMapPathPoint(226.20039899553575f, 556.0699637276786f),
            CarMapPathPoint(215.49176339285714f, 562.7253069196429f),
            CarMapPathPoint(213.21051339285714f, 591.6024693080358f),
            CarMapPathPoint(212.00878348214286f, 625.6735491071429f),
            CarMapPathPoint(197.43017020089286f, 645.0743582589286f),
            CarMapPathPoint(187.41405691964286f, 647.0857282366071f),
            CarMapPathPoint(101.79589285714286f, 649.0207170758929f),
            CarMapPathPoint(69.96023437499997f, 650.1613420758929f),
            CarMapPathPoint(56.48150948660714f, 656.8268694196429f),
            CarMapPathPoint(51.11446149553569f, 665.2542550223214f),
        ),
    ),
    CarMapTrack(
        color = Color(0xFF997405),
        speedPx = 0.9f,
        coords = listOf(
            CarMapPathPoint(2.5920703124999704f, 450.66908482142856f),
            CarMapPathPoint(204.0651450892857f, 453.13364955357144f),
            CarMapPathPoint(378.72844029017864f, 453.13874162946433f),
            CarMapPathPoint(551.1817745535716f, 456.1532505580358f),
            CarMapPathPoint(578.3734598214287f, 456.91196986607144f),
            CarMapPathPoint(601.2317885044645f, 458.9895368303571f),
            CarMapPathPoint(614.1503850446429f, 462.1669921875f),
            CarMapPathPoint(618.99294921875f, 479.68882533482144f),
            CarMapPathPoint(620.0826534598216f, 513.5969587053571f),
            CarMapPathPoint(615.6932840401787f, 528.7306082589286f),
            CarMapPathPoint(608.4829045758929f, 533.2625558035714f),
            CarMapPathPoint(592.7127455357145f, 534.9582170758929f),
            CarMapPathPoint(583.09890625f, 527.5492466517858f),
            CarMapPathPoint(578.6535239955358f, 516.4077845982143f),
            CarMapPathPoint(578.6535239955358f, 498.36146763392856f),
            CarMapPathPoint(577.9966462053571f, 477.0613141741071f),
            CarMapPathPoint(575.3691350446429f, 469.1940569196429f),
            CarMapPathPoint(569.0753292410716f, 462.63037109375f),
            CarMapPathPoint(553.9518638392858f, 460.6444614955358f),
            CarMapPathPoint(298.10051060267864f, 465.61432756696433f),
            CarMapPathPoint(193.49908761160714f, 460.1759905133929f),
            CarMapPathPoint(116.40505859374997f, 465.78236607142856f),
            CarMapPathPoint(3.5137360491071092f, 463.47565569196433f),
        ),
    ),
)

internal fun carMapRouteLength(points: List<CarMapPathPoint>): Float {
    if (points.size < 2) return 0f
    var total = 0f
    for (index in 1 until points.size) {
        total += hypot(points[index].x - points[index - 1].x, points[index].y - points[index - 1].y)
    }
    return total
}

internal fun carMapRoutePointAtDistance(points: List<CarMapPathPoint>, distance: Float): CarMapPathSample {
    if (points.isEmpty()) return CarMapPathSample(0f, 0f, 0f)
    if (points.size == 1) return CarMapPathSample(points[0].x, points[0].y, 0f)
    if (distance <= 0f) {
        return CarMapPathSample(points[0].x, points[0].y, angleAt(points, 0))
    }

    var remaining = distance
    for (index in 1 until points.size) {
        val deltaX = points[index].x - points[index - 1].x
        val deltaY = points[index].y - points[index - 1].y
        val segmentLength = hypot(deltaX, deltaY)
        if (remaining <= segmentLength) {
            val progress = if (segmentLength == 0f) 0f else remaining / segmentLength
            return CarMapPathSample(
                x = points[index - 1].x + deltaX * progress,
                y = points[index - 1].y + deltaY * progress,
                angleDegrees = angleAt(points, index - 1),
            )
        }
        remaining -= segmentLength
    }

    val last = points.last()
    val previous = points[points.size - 2]
    return CarMapPathSample(
        x = last.x,
        y = last.y,
        angleDegrees = Math.toDegrees(atan2((last.y - previous.y).toDouble(), (last.x - previous.x).toDouble())).toFloat(),
    )
}

private fun angleAt(points: List<CarMapPathPoint>, segmentIndex: Int): Float {
    val p1 = points[segmentIndex]
    val p2 = points[min(segmentIndex + 1, points.size - 1)]
    return Math.toDegrees(atan2((p2.y - p1.y).toDouble(), (p2.x - p1.x).toDouble())).toFloat()
}

internal data class CarMapSvgAsset(
    val bounds: Rect,
    val image: ImageBitmap,
)

@Composable
fun CarMapScreen(
    carMapId: String,
    onBack: () -> Unit,
) {
    val textMeasurer = rememberTextMeasurer()
    val context = LocalContext.current
    val tracks = remember(carMapId) { carMapTracks }

    var trackDistances by remember(tracks) {
        mutableStateOf(tracks.map { 0f })
    }
    var isMapAnimating by remember { mutableStateOf(true) }

    LaunchedEffect(isMapAnimating, tracks) {
        while (isMapAnimating) {
            delay(16)
            trackDistances = trackDistances.withIndex().map { (index, distance) ->
                val track = tracks[index]
                val total = carMapRouteLength(track.coords)
                val updated = if (total <= 0f) 0f else distance + track.speedPx
                if (updated >= total) 0f else updated
            }.toList()
        }
    }

    val svgAsset by produceState<CarMapSvgAsset?>(initialValue = null, carMapId) {
        value = withContext(Dispatchers.IO) {
            try {
                val svgStream = context.assets.open("carmap/ksia-ext-plan-min.svg")
                val viewBox = parseSvgViewBox(svgStream)
                svgStream.close()
                val w = viewBox.first.coerceAtLeast(1f)
                val h = viewBox.second.coerceAtLeast(1f)

                val svg = SVG.getFromInputStream(context.assets.open("carmap/ksia-ext-plan-min.svg"))
                val bitmap = Bitmap.createBitmap(w.toInt(), h.toInt(), Bitmap.Config.ARGB_8888)
                svg.renderToCanvas(AndroidCanvas(bitmap))
                CarMapSvgAsset(
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
                    text = "Car Map",
                    color = Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    maxLines = 1,
                )
                Text(
                    text = "Live traffic routes on the KSIA extended floor plan",
                    color = Ink.copy(alpha = 0.72f),
                    fontSize = 12.sp,
                    maxLines = 1,
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
            val bounds = svgAsset?.bounds ?: Rect.Zero

            val scale = canvasSize.width / coerceAtLeast(bounds.width, 1f)
            val panX = 0f
            val panY = (canvasSize.height - bounds.height * scale) / 2f
            val screenMatrix = Matrix().apply {
                values[0] = scale
                values[4] = scale
                values[2] = panX
                values[5] = panY
                values[8] = 1f
            }

            val symbolPath = remember(carMapId) {
                Path().apply { parseCarSymbolPath(carSymbolPathString) }
            }

            val backgroundImage = svgAsset?.image
            if (backgroundImage != null) {
                Image(
                    bitmap = backgroundImage,
                    contentScale = ContentScale.FillWidth,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
            ) {
                val showRoutePaths = true
                val globalLineWidth = 2.5f

                tracks.forEach { track ->
                    val trackPath = Path().apply {
                        if (showRoutePaths) {
                            moveTo(track.coords[0].x, track.coords[0].y)
                            for (index in 1 until track.coords.size) {
                                lineTo(track.coords[index].x, track.coords[index].y)
                            }
                        }
                        transform(screenMatrix)
                    }
                    val trackStroke = globalLineWidth / coerceAtLeast(scale, 0.001f)
                    drawPath(
                        path = trackPath,
                        color = track.color.copy(alpha = 0.35f),
                        style = Stroke(width = trackStroke),
                    )
                }

                tracks.forEachIndexed { index, track ->
                    val sample = carMapRoutePointAtDistance(track.coords, trackDistances[index])
                    val screenX = sample.x * scale + panX
                    val screenY = sample.y * scale + panY
                    val carScale = 0.08f * (canvasSize.width / 600f)

                    val rot = ((sample.angleDegrees + 90f) * PI / 180f).toFloat()
                    val c = cos(rot)
                    val s = sin(rot)
                    val cs = carScale
                    val screenSpaceActorPath = Path().apply {
                        parseCarSymbolPath(carSymbolPathString) { x, y ->
                            val x1 = cs * (x - 50f)
                            val y1 = cs * (y - 105f)
                            val x2 = c * x1 - s * y1
                            val y2 = s * x1 + c * y1
                            Pair(screenX + x2, screenY + y2)
                        }
                    }
                    drawPath(path = screenSpaceActorPath, color = track.color)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = { isMapAnimating = !isMapAnimating },
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(
                        if (isMapAnimating) {
                            R.string.nav_indoor_pause
                        } else {
                            R.string.nav_indoor_play
                        },
                    ),
                )
            }
            OutlinedButton(
                onClick = { trackDistances = tracks.map { 0f } },
                modifier = Modifier.weight(1f),
            ) {
                Text(text = stringResource(R.string.nav_indoor_reset))
            }
        }
    }
}

private fun coerceAtLeast(value: Float, min: Float): Float = if (value < min) min else value

private fun Path.parseCarSymbolPath(pathData: String, transform: ((x: Float, y: Float) -> Pair<Float, Float>)? = null) {
    val tokens = tokenizeSvgPathData(pathData)
    var index = 0

    var curX = 0f
    var curY = 0f
    var startX = 0f
    var startY = 0f
    var ctrlX = 0f
    var ctrlY = 0f
    var lastCommand = '\u0000'

    fun nextFloat(): Float = tokens[index].toFloat().also { index++ }
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
        }
        lastCommand = cmd
    }
}

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

private fun parseSvgViewBox(stream: java.io.InputStream): Pair<Float, Float> {
    val text = stream.bufferedReader().readText()
    val match = Pattern.compile("viewBox\\s*=\\s*[\"']([\\d.]+)\\s+([\\d.]+)\\s+([\\d.]+)\\s+([\\d.]+)[\"']", Pattern.CASE_INSENSITIVE).matcher(text)
    return if (match.find()) {
        Pair(match.group(3).toFloat(), match.group(4).toFloat())
    } else {
        Pair(0f, 0f)
    }
}
