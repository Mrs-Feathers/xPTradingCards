package it.forgottenworld.tradingcards.util

import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import java.awt.image.BufferedImage

class MapRenderer(val image: BufferedImage, contextual: Boolean) : MapRenderer(contextual) {

    override fun render(mapView: MapView, mapCanvas: MapCanvas, player: Player) {
        mapCanvas.drawImage(0,0,image)
    }
}