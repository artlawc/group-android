package com.groupa.presentation.bodymuscle

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BodyMuscleDataTest {

    private val frontMuscle = BodyMuscle(
        id = "head",
        name = "Head",
        group = "Head & Neck",
        view = BodyView.Front,
        pathData = "M 0,0 L 10,0 L 10,10 L 0,10 Z",
    )

    private val backMuscle = BodyMuscle(
        id = "lats-left",
        name = "Left Lat",
        group = "Back",
        view = BodyView.Back,
        pathData = "M 0,0 L 10,0 L 10,10 L 0,10 Z",
    )

    private val bodyData = BodyMuscleData(
        frontViewBox = Rect(0f, 0f, 35f, 93f),
        backViewBox = Rect(37f, 0f, 72f, 93f),
        frontMuscles = listOf(frontMuscle),
        backMuscles = listOf(backMuscle),
    )

    @Test
    fun colorForIntensityReturnsTheWebDemoGradient() {
        assertEquals(Color(0xFF94A3B8), BodyMuscleData.colorForIntensity(0))
        assertEquals(Color(0xFFFB923C), BodyMuscleData.colorForIntensity(4))
        assertEquals(Color(0xFFEF4444), BodyMuscleData.colorForIntensity(7))
        assertEquals(Color(0xFF7F1D1D), BodyMuscleData.colorForIntensity(10))
    }

    @Test
    fun colorForIntensityClampsOutOfRangeValues() {
        assertEquals(Color(0xFF94A3B8), BodyMuscleData.colorForIntensity(-1))
        assertEquals(Color(0xFF7F1D1D), BodyMuscleData.colorForIntensity(11))
    }

    @Test
    fun viewForReturnsFrontForUnknownMuscleIds() {
        assertEquals(BodyView.Front, bodyData.viewFor("unknown"))
        assertEquals(BodyView.Front, bodyData.viewFor(null))
    }

    @Test
    fun viewForReturnsBackForBackMuscleIds() {
        assertEquals(BodyView.Back, bodyData.viewFor("lats-left"))
    }

    @Test
    fun selectedMuscleReturnsNullForUnknownIds() {
        assertNull(bodyData.selectedMuscle("does-not-exist"))
    }

    @Test
    fun groupsReturnsDistinctGroupNames() {
        assertEquals(listOf("Head & Neck"), bodyData.groups(BodyView.Front))
        assertEquals(listOf("Back"), bodyData.groups(BodyView.Back))
    }

    @Test
    fun averageIntensityReturnsRoundedSessionIntensity() {
        val muscles = listOf(frontMuscle, backMuscle)
        val intensities = mapOf("head" to 0, "lats-left" to 1)

        assertEquals(1, BodyMuscleData.averageIntensity(intensities, muscles))
    }

    @Test
    fun averageIntensityReturnsZeroForEmptyMuscleList() {
        assertEquals(0, BodyMuscleData.averageIntensity(emptyMap(), emptyList()))
    }

    @Test
    fun viewportTransformPreservesSvgAspectRatioAndCentersTheViewBox() {
        val viewBox = Rect(37f, 0f, 72f, 93f)
        val transform = BodyMuscleGeometry.viewportTransform(
            viewBox = viewBox,
            canvasWidth = 300f,
            canvasHeight = 640f,
        )

        assertEquals(640f / 93f, transform.scaleX, 0.001f)
        assertEquals(transform.scaleX, transform.scaleY, 0f)
        val topLeft = transform.mapPoint(Offset(viewBox.left, viewBox.top))
        val bottomRight = transform.mapPoint(Offset(viewBox.right, viewBox.bottom))
        val expectedLeft = (300f - 35f * transform.scaleX) / 2f
        assertEquals(expectedLeft, topLeft.x, 0.001f)
        assertEquals(0f, topLeft.y, 0.001f)
        assertEquals(300f - expectedLeft, bottomRight.x, 0.001f)
        assertEquals(640f, bottomRight.y, 0.001f)
        assertEquals(viewBox.left, transform.toSource(topLeft).x, 0.001f)
        assertEquals(viewBox.top, transform.toSource(topLeft).y, 0.001f)
    }

    @Test
    fun viewportTransformAppliesPanAndZoom() {
        val viewBox = Rect(0f, 0f, 35f, 93f)
        val base = BodyMuscleGeometry.viewportTransform(
            viewBox = viewBox,
            canvasWidth = 300f,
            canvasHeight = 640f,
        )
        val pan = Offset(20f, 10f)
        val transformed = BodyMuscleGeometry.viewportTransform(
            viewBox = viewBox,
            canvasWidth = 300f,
            canvasHeight = 640f,
            zoom = 2f,
            pan = pan,
        )
        val sourcePoint = Offset(10f, 20f)

        assertEquals(base.scaleX * 2f, transformed.scaleX, 0.001f)
        assertEquals(base.scaleY * 2f, transformed.scaleY, 0.001f)
        assertEquals(
            Offset(
                base.mapPoint(sourcePoint).x * 2f + pan.x,
                base.mapPoint(sourcePoint).y * 2f + pan.y,
            ),
            transformed.mapPoint(sourcePoint),
        )
    }

    @Test
    fun pinchPanKeepsTheSourceAnchorUnderTheCurrentFingerCentroid() {
        val viewBox = Rect(0f, 0f, 35f, 93f)
        val sourceAnchor = Offset(10f, 20f)
        val screenAnchor = Offset(123f, 234f)
        val pan = BodyMuscleGeometry.panForZoomedPoint(
            viewBox = viewBox,
            canvasWidth = 300f,
            canvasHeight = 640f,
            zoom = 2.5f,
            sourcePoint = sourceAnchor,
            screenPoint = screenAnchor,
        )
        val transformed = BodyMuscleGeometry.viewportTransform(
            viewBox = viewBox,
            canvasWidth = 300f,
            canvasHeight = 640f,
            zoom = 2.5f,
            pan = pan,
        )

        assertEquals(screenAnchor, transformed.mapPoint(sourceAnchor))
    }

}
