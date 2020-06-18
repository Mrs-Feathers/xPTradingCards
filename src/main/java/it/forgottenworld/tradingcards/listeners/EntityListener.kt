package it.forgottenworld.tradingcards.listeners

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.util.Utils.Companion.blacklistMode
import it.forgottenworld.tradingcards.util.Utils.Companion.calculateRarity
import it.forgottenworld.tradingcards.util.Utils.Companion.isMobBoss
import it.forgottenworld.tradingcards.util.Utils.Companion.isOnList
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDeathEvent
import java.util.ArrayList

class EntityListener : Listener {

    @EventHandler
    fun onEntityDeath(e: EntityDeathEvent) {
        val config = TradingCards.configManager.pluginConfig.config!!
        val cardManager = TradingCards.cardManager
        var drop = false
        var worldName = ""
        var worlds: List<String?> = ArrayList()
        if (e.entity.killer != null) {
            val p = e.entity.killer
            drop = if (isOnList(p) && blacklistMode() == 'b') {
                false
            } else if (!isOnList(p) && blacklistMode() == 'b') {
                true
            } else isOnList(p) && blacklistMode() == 'w'
            worldName = p!!.world.name
            worlds = config.getStringList("World-Blacklist")
        }
        if (drop &&
                !worlds.contains(worldName)) {
            var rare = calculateRarity(e.entityType, false)
            if (config.getBoolean("Chances.Boss-Drop") && isMobBoss(e.entityType)) rare = config.getString("Chances.Boss-Drop-Rarity")
            var cancelled = false
            if (rare !== "None") {
                if (config.getBoolean("General.Spawner-Block") &&
                        e.entity.customName != null &&
                        e.entity.customName == config.getString("General.Spawner-Mob-Name")) {
                    if (config.getBoolean("General.Debug-Mode")) println("[Cards] Mob came from spawner, not dropping card.")
                    cancelled = true
                }
                if (!cancelled) {
                    if (config.getBoolean("General.Debug-Mode")) println("[Cards] Successfully generated card.")
                    if (cardManager.generateCard(rare) != null) e.drops.add(cardManager.generateCard(rare))
                }
            }
        }
    }

    @EventHandler
    fun onMobSpawn(e: CreatureSpawnEvent) {
        val config = TradingCards.configManager.pluginConfig.config!!
        if (e.entity !is Player &&
                e.spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER && config.getBoolean("General.Spawner-Block")) {
            e.entity.customName = config.getString("General.Spawner-Mob-Name")
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] Spawner mob renamed.")
            e.entity.removeWhenFarAway = true
        }
    }

}