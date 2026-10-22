package com.groupa.presentation.screen

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.groupa.coreui.component.CircularIconButton
import com.groupa.coreui.theme.AppBackground
import com.groupa.coreui.theme.Gray500
import com.groupa.coreui.theme.GroupBlue
import com.groupa.coreui.theme.Ink
import com.groupa.coreui.theme.White
import com.groupa.coreui.R
import com.groupa.presentation.BuildConfig
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay

internal val TIANDITU_MAP_KEY: String = BuildConfig.TIANDITU_MAP_KEY

internal val tiandituBaseTileUrls = (0..7).map { serverIndex ->
    "https://t$serverIndex.tianditu.gov.cn/DataServer?T=vec_w&tk=$TIANDITU_MAP_KEY"
}.toTypedArray()

internal val tiandituAnnotationTileUrls = (0..7).map { serverIndex ->
    "https://t$serverIndex.tianditu.gov.cn/DataServer?T=cva_w&tk=$TIANDITU_MAP_KEY"
}.toTypedArray()

internal fun buildTiandituTileUrl(
    baseUrl: String,
    zoom: Int,
    x: Int,
    y: Int,
): String = "$baseUrl&X=$x&Y=$y&L=$zoom"

internal val tiandituBaseTileSource: OnlineTileSourceBase = object : XYTileSource(
    "TianDiTu VEC",
    0,
    20,
    256,
    "",
    tiandituBaseTileUrls,
) {
    override fun getTileURLString(pMapTileIndex: Long) = buildTiandituTileUrl(
        baseUrl = baseUrl,
        zoom = MapTileIndex.getZoom(pMapTileIndex),
        x = MapTileIndex.getX(pMapTileIndex),
        y = MapTileIndex.getY(pMapTileIndex),
    )
}

internal val tiandituAnnotationTileSource: OnlineTileSourceBase = object : XYTileSource(
    "TianDiTu CVA",
    0,
    20,
    256,
    "",
    tiandituAnnotationTileUrls,
) {
    override fun getTileURLString(pMapTileIndex: Long) = buildTiandituTileUrl(
        baseUrl = baseUrl,
        zoom = MapTileIndex.getZoom(pMapTileIndex),
        x = MapTileIndex.getX(pMapTileIndex),
        y = MapTileIndex.getY(pMapTileIndex),
    )
}

@Composable
fun MapViewScreen(
    target: String,
    label: String,
    onBack: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val mapTarget = remember(target) { parseMapTarget(target) }
    val mapView = remember(context, target, label) {
        createOsmdroidView(context, mapTarget, label)
    }

    DisposableEffect(mapView) {
        onDispose {
            mapView.onDetach()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                onClick = onBack,
            )
            Text(
                text = stringResource(R.string.title_map),
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
            )
        }
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            factory = { mapView },
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = White,
            shadowElevation = 12.dp,
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = GroupBlue,
                    modifier = Modifier.size(22.dp),
                )
                Column {
                    Text(
                        text = stringResource(R.string.label_map_provider),
                        color = Gray500,
                        fontSize = 12.sp,
                    )
                    Text(
                        text = stringResource(R.string.label_map_destination),
                        color = Gray500,
                        fontSize = 12.sp,
                    )
                    Text(
                        text = label,
                        color = Ink,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                    Text(
                        text = target,
                        color = Gray500,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}

private fun createOsmdroidView(
    context: Context,
    target: MapTarget?,
    label: String,
): MapView {
    Configuration.getInstance().apply {
        userAgentValue = context.packageName
        isDebugMode = true
        isDebugMapView = true
        isDebugTileProviders = true
        isDebugMapTileDownloader = true
    }

    return MapView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        setTileSource(tiandituBaseTileSource)
        minZoomLevel = 5.0
        maxZoomLevel = 20.0
        isTilesScaledToDpi = true
        setMultiTouchControls(true)
        overlayManager.tilesOverlay.isEnabled = true

        val annotationTileOverlay = TilesOverlay(
            MapTileProviderBasic(context, tiandituAnnotationTileSource),
            context,
        ).apply {
            setUseDataConnection(true)
            setLoadingBackgroundColor(Color.TRANSPARENT)
            setLoadingLineColor(Color.TRANSPARENT)
        }
        overlays.add(annotationTileOverlay)

        val center = target?.let { GeoPoint(it.latitude, it.longitude) }
            ?: GeoPoint(37.7749, -122.4194)
        controller.setZoom(16.5)
        controller.setCenter(center)

        if (target != null) {
            overlays.add(
                Marker(this).apply {
                    position = center
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = label.ifBlank { "${target.latitude}, ${target.longitude}" }
                },
            )
        }
    }
}

internal data class MapTarget(
    val latitude: Double,
    val longitude: Double,
)

internal fun parseMapTarget(target: String): MapTarget? {
    val coordinates = target.split(',')
    val latitude = coordinates.getOrNull(0)?.trim()?.toDoubleOrNull()
    val longitude = coordinates.getOrNull(1)?.trim()?.toDoubleOrNull()

    return if (
        latitude != null &&
        longitude != null &&
        latitude in -90.0..90.0 &&
        longitude in -180.0..180.0
    ) {
        MapTarget(latitude, longitude)
    } else {
        null
    }
}
