package com.ridestracker.util

import com.ridestracker.domain.model.MapStyle
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.MapTileIndex

object MapTileUtil {

    private val darkTileSource: ITileSource = XYTileSource(
        "CartoDarkMatter", 0, 20, 256, ".png",
        arrayOf(
            "https://a.basemaps.cartocdn.com/dark_all/",
            "https://b.basemaps.cartocdn.com/dark_all/",
            "https://c.basemaps.cartocdn.com/dark_all/"
        )
    )

    // Esri uses a z/y/x path order with no file extension, unlike osmdroid's default z/x/y.ext scheme.
    private val satelliteTileSource: ITileSource = object : XYTileSource(
        "EsriWorldImagery", 0, 19, 256, "",
        arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/")
    ) {
        override fun getTileURLString(pMapTileIndex: Long): String {
            val zoom = MapTileIndex.getZoom(pMapTileIndex)
            val x = MapTileIndex.getX(pMapTileIndex)
            val y = MapTileIndex.getY(pMapTileIndex)
            return "$baseUrl$zoom/$y/$x"
        }
    }

    fun tileSourceFor(style: MapStyle): ITileSource = when (style) {
        MapStyle.STANDARD -> TileSourceFactory.MAPNIK
        MapStyle.DARK -> darkTileSource
        MapStyle.SATELLITE -> satelliteTileSource
        MapStyle.TERRAIN -> TileSourceFactory.OpenTopo
    }
}
