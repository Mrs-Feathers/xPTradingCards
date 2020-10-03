package it.forgottenworld.tradingcards.listeners

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.data.Blacklist
import it.forgottenworld.tradingcards.data.Chances
import it.forgottenworld.tradingcards.data.General
import it.forgottenworld.tradingcards.data.Rarities
import it.forgottenworld.tradingcards.manager.CardManager
import it.forgottenworld.tradingcards.model.Rarity
import it.forgottenworld.tradingcards.util.isMobBoss
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.metadata.FixedMetadataValue

class EntityListener : Listener {

    @EventHandler
    fun onEntityDeath(e: EntityDeathEvent) {

        if (e.entity.killer !is Player) return
        val p = e.entity.killer as Player

        if (Blacklist.isWorldBlacklisted(p.world) || Blacklist.isPlayerBlacklisted(p) != Blacklist.WhitelistMode)
            return

        val rarity = if (Chances.BossDrop && isMobBoss(e.entityType)) Rarities[Chances.BossDropRarity] ?: return
        else Rarity.calculate(e.entityType, false) ?: return

        if (!General.SpawnerBlock || e.entity.getMetadata("fromSpawner").firstOrNull()?.asBoolean() != true)
            e.drops.add(CardManager.createRandomCardItemStack(rarity))
    }

    @EventHandler
    fun onMobSpawn(e: CreatureSpawnEvent) {

        if (e.entity !is Mob ||
                e.spawnReason != CreatureSpawnEvent.SpawnReason.SPAWNER ||
                !General.SpawnerBlock) return

        e.entity.setMetadata("fromSpawner", FixedMetadataValue(TradingCards.instance, true))
        e.entity.removeWhenFarAway = true

    }

}