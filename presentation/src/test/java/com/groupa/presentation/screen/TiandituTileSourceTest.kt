package com.groupa.presentation.screen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.groupa.presentation.BuildConfig

class TiandituTileSourceTest {

    @Test
    fun baseTileUrlsUseVisibleVectorLayerAndTiandituKey() {
        assertTrue(tiandituBaseTileUrls.all { it.contains("DataServer?T=vec_w&tk=${BuildConfig.TIANDITU_MAP_KEY}") })
    }

    @Test
    fun annotationTileUrlsUseAnnotationLayerAndTiandituKey() {
        assertTrue(
            tiandituAnnotationTileUrls.all {
                it.contains("DataServer?T=cva_w&tk=${BuildConfig.TIANDITU_MAP_KEY}")
            },
        )
    }

    @Test
    fun buildsOsmdroidTileQueryParameters() {
        val baseUrl = "https://t0.tianditu.gov.cn/DataServer?T=vec_w&tk=$TIANDITU_MAP_KEY"

        val url = buildTiandituTileUrl(baseUrl, zoom = 12, x = 3344, y = 1554)

        assertEquals("$baseUrl&X=3344&Y=1554&L=12", url)
    }
}
