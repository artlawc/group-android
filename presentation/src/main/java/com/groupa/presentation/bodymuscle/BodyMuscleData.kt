package com.groupa.presentation.bodymuscle

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.PathParser
import org.json.JSONArray
import org.json.JSONObject

enum class BodyView {
    Front,
    Back,
}

data class BodyMuscle(
    val id: String,
    val name: String,
    val group: String,
    val view: BodyView,
    val pathData: String,
)

data class BodyMuscleRenderable(
    val id: String,
    val name: String,
    val group: String,
    val view: BodyView,
    val path: Path,
    val bounds: Rect,
)

data class BodyMuscleData(
    val frontViewBox: Rect,
    val backViewBox: Rect,
    val frontMuscles: List<BodyMuscle>,
    val backMuscles: List<BodyMuscle>,
) {
    fun muscles(view: BodyView): List<BodyMuscle> =
        if (view == BodyView.Front) frontMuscles else backMuscles

    fun groups(view: BodyView): List<String> =
        muscles(view).map { it.group }.distinct()

    fun selectedMuscle(id: String?): BodyMuscle? =
        (frontMuscles + backMuscles).firstOrNull { it.id == id }

    fun viewFor(id: String?): BodyView =
        selectedMuscle(id)?.view ?: BodyView.Front

    companion object {
        private val intensityColors = listOf(
            Color(0xFF94A3B8),
            Color(0xFFFDE047),
            Color(0xFFFACC15),
            Color(0xFFEAB308),
            Color(0xFFFB923C),
            Color(0xFFF97316),
            Color(0xFFEA580C),
            Color(0xFFEF4444),
            Color(0xFFDC2626),
            Color(0xFFB91C1C),
            Color(0xFF7F1D1D),
        )

        fun colorForIntensity(intensity: Int): Color =
            intensityColors[intensity.coerceIn(0, 10)]

        fun averageIntensity(
            intensities: Map<String, Int>,
            muscles: List<BodyMuscle>,
        ): Int {
            if (muscles.isEmpty()) return 0
            val total = muscles.sumOf { intensities[it.id] ?: 0 }
            val average = total.toFloat() / muscles.size
            return if (average >= 0.5f) average.toInt() + 1 else 0
        }
    }
}

object BodyMuscleAssetLoader {
    private const val AssetPath = "bodymuscles/body-muscles.json"

    fun load(context: Context): BodyMuscleData {
        val jsonString = context.assets.open(AssetPath).bufferedReader().use { it.readText() }
        val root = JSONObject(jsonString)
        return BodyMuscleData(
            frontViewBox = parseViewBox(root.getJSONArray("frontViewBox")),
            backViewBox = parseViewBox(root.getJSONArray("backViewBox")),
            frontMuscles = parseMuscles(root.getJSONArray("frontMuscles"), BodyView.Front),
            backMuscles = parseMuscles(root.getJSONArray("backMuscles"), BodyView.Back),
        )
    }

    private fun parseViewBox(values: JSONArray): Rect {
        require(values.length() == 4)
        val left = values.getDouble(0).toFloat()
        val top = values.getDouble(1).toFloat()
        val width = values.getDouble(2).toFloat()
        val height = values.getDouble(3).toFloat()
        require(width > 0f && height > 0f)
        return Rect(
            left = left,
            top = top,
            right = left + width,
            bottom = top + height,
        )
    }

    private fun parseMuscles(values: JSONArray, view: BodyView): List<BodyMuscle> {
        return List(values.length()) { index ->
            val item = values.getJSONObject(index)
            BodyMuscle(
                id = item.getString("id"),
                name = item.getString("name"),
                group = item.getString("group"),
                view = view,
                pathData = item.getString("path"),
            )
        }
    }
}

object BodyMusclePathRenderer {
    fun renderable(muscle: BodyMuscle): BodyMuscleRenderable {
        val path = PathParser().parsePathString(muscle.pathData).toPath()
        return BodyMuscleRenderable(
            id = muscle.id,
            name = muscle.name,
            group = muscle.group,
            view = muscle.view,
            path = path,
            bounds = path.getBounds(),
        )
    }
}

data class BodyMuscleViewportTransform(
    val scaleX: Float,
    val scaleY: Float,
    val translateX: Float,
    val translateY: Float,
) {
    fun mapPoint(point: Offset): Offset =
        Offset(
            x = point.x * scaleX + translateX,
            y = point.y * scaleY + translateY,
        )

    fun toSource(point: Offset): Offset =
        Offset(
            x = (point.x - translateX) / scaleX,
            y = (point.y - translateY) / scaleY,
        )
}

object BodyMuscleGeometry {
    fun viewportTransform(
        viewBox: Rect,
        canvasWidth: Float,
        canvasHeight: Float,
        zoom: Float = 1f,
        pan: Offset = Offset.Zero,
    ): BodyMuscleViewportTransform {
        require(canvasWidth > 0f && canvasHeight > 0f)
        require(viewBox.width > 0f && viewBox.height > 0f)
        require(zoom > 0f)

        val baseScale = minOf(
            canvasWidth / viewBox.width,
            canvasHeight / viewBox.height,
        )
        val centeredWidth = viewBox.width * baseScale
        val centeredHeight = viewBox.height * baseScale
        val baseTranslateX = (canvasWidth - centeredWidth) / 2f - viewBox.left * baseScale
        val baseTranslateY = (canvasHeight - centeredHeight) / 2f - viewBox.top * baseScale
        return BodyMuscleViewportTransform(
            scaleX = baseScale * zoom,
            scaleY = baseScale * zoom,
            translateX = baseTranslateX * zoom + pan.x,
            translateY = baseTranslateY * zoom + pan.y,
        )
    }

    fun panForZoomedPoint(
        viewBox: Rect,
        canvasWidth: Float,
        canvasHeight: Float,
        zoom: Float,
        sourcePoint: Offset,
        screenPoint: Offset,
    ): Offset {
        val base = viewportTransform(viewBox, canvasWidth, canvasHeight)
        val zoomedBasePoint = Offset(
            x = base.mapPoint(sourcePoint).x * zoom,
            y = base.mapPoint(sourcePoint).y * zoom,
        )
        return Offset(
            x = screenPoint.x - zoomedBasePoint.x,
            y = screenPoint.y - zoomedBasePoint.y,
        )
    }
}
