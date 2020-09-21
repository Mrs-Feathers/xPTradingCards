package it.forgottenworld.tradingcards.listeners

import it.forgottenworld.tradingcards.card.CardManager
import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.util.*
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDeathEvent

class EntityListener : Listener {

    @EventHandler
    fun onEntityDeath(e: EntityDeathEvent) {

        val config = Config.PLUGIN

        if (e.entity.killer !is Player) return
        val p = e.entity.killer as Player

        val drop = !isOnList(p) || blacklistMode() != 'b' ||
                !isOnList(p) && blacklistMode() == 'b' ||
                isOnList(p) && blacklistMode() == 'w'

        if (drop && !config.getStringList("World-Blacklist").contains(p.world.name)) {

            val rare = if (config.getBoolean("Chances.Boss-Drop") && isMobBoss(e.entityType))
                config.getString("Chances.Boss-Drop-Rarity") ?: "None"
            else calculateRarity(e.entityType, false)

            if (rare !== "None" &&
                    config.getBoolean("General.Spawner-Block") &&
                    e.entity.customName != null &&
                    e.entity.customName == config.getString("General.Spawner-Mob-Name"))
                printDebug("[Cards] Mob came from spawner, not dropping card.")
            else
                printDebug("[Cards] Successfully generated card.")
                CardManager.generateCard(rare)?.let { e.drops.add(it) }
        }
    }

    @EventHandler
    fun onMobSpawn(e: CreatureSpawnEvent) {

        val config = Config.PLUGIN

        if (e.entity !is Mob ||
                e.spawnReason != CreatureSpawnEvent.SpawnReason.SPAWNER ||
                !config.getBoolean("General.Spawner-Block")) return

        e.entity.customName = config.getString("General.Spawner-Mob-Name")
        e.entity.removeWhenFarAway = true

        printDebug("[Cards] Spawner mob renamed.")
    }

}