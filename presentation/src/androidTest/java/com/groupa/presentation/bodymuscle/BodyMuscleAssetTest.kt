package com.groupa.presentation.bodymuscle

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BodyMuscleAssetTest {

    @Test
    fun assetLoadsEverySvgPathIntoValidGeometry() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val data = BodyMuscleAssetLoader.load(context)

        assertEquals(Rect(0f, 0f, 35f, 93f), data.frontViewBox)
        assertEquals(Rect(37f, 0f, 72f, 93f), data.backViewBox)
        assertEquals(40, data.frontMuscles.size)
        assertEquals(49, data.backMuscles.size)

        listOf(
            data.frontViewBox to data.frontMuscles,
            data.backViewBox to data.backMuscles,
        ).forEach { (viewBox, muscles) ->
            muscles.forEach { muscle ->
                val renderable = BodyMusclePathRenderer.renderable(muscle)

                assertFalse(renderable.path.isEmpty)
                assertTrue(renderable.bounds.left >= viewBox.left - 0.1f)
                assertTrue(renderable.bounds.top >= viewBox.top - 0.1f)
                assertTrue(renderable.bounds.right <= viewBox.right + 0.1f)
                assertTrue(renderable.bounds.bottom <= viewBox.bottom + 0.1f)
            }
        }
    }

    @Test
    fun pathParserSupportsAbsoluteAndRelativeSvgCommands() {
        val absolute = BodyMusclePathRenderer.renderable(
            BodyMuscle(
                id = "absolute",
                name = "Absolute",
                group = "Test",
                view = BodyView.Front,
                pathData = "M 10,10 L 20,10 L 20,20 L 10,20 Z",
            ),
        )
        val relative = BodyMusclePathRenderer.renderable(
            BodyMuscle(
                id = "relative",
                name = "Relative",
                group = "Test",
                view = BodyView.Front,
                pathData = "m 10,10 10,0 0,10 -10,0 z",
            ),
        )

        assertEquals(Rect(10f, 10f, 20f, 20f), absolute.bounds)
        assertEquals(Rect(10f, 10f, 20f, 20f), relative.bounds)
    }
}
