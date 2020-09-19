package it.forgottenworld.tradingcards.listeners

import it.forgottenworld.tradingcards.TradingCards
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDeathEvent
import java.util.*

class EntityListener(val tradingCards: TradingCards) : Listener {

    @EventHandler
    fun onEntityDeath(e: EntityDeathEvent) {
        val config = tradingCards.configManager.pluginConfig.config!!
        val cardManager = tradingCards.cardManager
        var drop = false
        var worldName = ""
        var worlds: List<String?> = ArrayList()
        if (e.entity.killer != null) {
            val p = e.entity.killer
            drop = if (tradingCards.utils.isOnList(p) && tradingCards.utils.blacklistMode() == 'b') {
                false
            } else if (!tradingCards.utils.isOnList(p) && tradingCards.utils.blacklistMode() == 'b') {
                true
            } else tradingCards.utils.isOnList(p) && tradingCards.utils.blacklistMode() == 'w'
            worldName = p!!.world.name
            worlds = config.getStringList("World-Blacklist")
        }
        if (drop &&
                !worlds.contains(worldName)) {
            var rare = tradingCards.utils.calculateRarity(e.entityType, false)
            if (config.getBoolean("Chances.Boss-Drop") && tradingCards.utils.isMobBoss(e.entityType)) rare = config.getString("Chances.Boss-Drop-Rarity")
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
        val config = tradingCards.configManager.pluginConfig.config!!
        if (e.entity !is Player &&
                e.spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER && config.getBoolean("General.Spawner-Block")) {
            e.entity.customName = config.getString("General.Spawner-Mob-Name")
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] Spawner mob renamed.")
            e.entity.removeWhenFarAway = true
        }
    }

}