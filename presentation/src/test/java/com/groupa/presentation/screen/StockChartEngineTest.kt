package com.groupa.presentation.screen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StockChartEngineTest {

    @Test
    fun defaultTransformIsCenteredAndAllowsPan() {
        val transform = defaultStockChartTransform(
            candleCount = 60,
            viewportWidth = 1000f,
        )

        assertTrue(transform.zoom > 1f)
        val expectedPan = -(1000f * transform.zoom - 1000f) / 2f
        assertEquals(expectedPan, transform.panX, 0.001f)
    }

    @Test
    fun panStaysInsideZoomBounds() {
        val viewportWidth = 900f
        val zoom = 2f
        val transform = StockChartTransform(zoom = zoom, panX = 0f)

        val panned = stockChartPanTransform(
            transform = transform,
            panDeltaX = -500f,
            viewportWidth = viewportWidth,
        )

        assertEquals(-500f, panned.panX, 0.001f)
        assertTrue(panned.panX in -900f..0f)
    }

    @Test
    fun zoomTransformKeepsFocusAnchored() {
        val transform = StockChartTransform(zoom = 2f, panX = -100f)
        val focusX = 300f

        val zoomed = stockChartZoomTransform(
            transform = transform,
            focusX = focusX,
            zoomFactor = 1.5f,
            viewportWidth = 800f,
        )

        assertEquals(3f, zoomed.zoom, 0.001f)
        val expectedFocusScreenX = focusX - (focusX - transform.panX) *
            (zoomed.zoom / transform.zoom)

        assertTrue(zoomed.panX <= 0f)
        assertEquals(expectedFocusScreenX, zoomed.panX, 0.001f)
    }
}
