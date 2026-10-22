package com.groupa.presentation.screen

import com.groupa.data.repository.MockGroupRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StockInfoScreenTest {

    @Test
    fun chartPointsUseTheFullWidthAndNormalizeHistoryValues() {
        val points = buildStockChartPoints(
            history = listOf(10.0, 12.0, 8.0, 14.0),
            width = 300f,
            height = 100f,
        )

        assertEquals(0f, points.first().x, 0.001f)
        assertEquals(300f, points.last().x, 0.001f)
        assertTrue(points[0].y > points[3].y)
        assertTrue(points[2].y > points[3].y)
    }

    @Test
    fun chartPointsHandleEmptyAndSingleValueHistory() {
        assertTrue(buildStockChartPoints(emptyList(), 100f, 100f).isEmpty())

        val point = buildStockChartPoints(listOf(20.0), 100f, 100f).single()
        assertEquals(0f, point.x, 0.001f)
    }

    @Test
    fun stockNotificationLinksToAStockAndStockDataIsAvailable() = runBlocking {
        val repository = MockGroupRepository()
        val messages = repository.messages.first()
        val stocks = repository.stocks.first()
        val stockMessage = messages.first { it.stockId != null }
        val linkedStock = stocks.first { it.id == stockMessage.stockId }

        assertEquals("GOUP", linkedStock.symbol)
        assertTrue(linkedStock.history.size > 1)
    }
}
