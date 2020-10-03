package it.forgottenworld.tradingcards.listeners

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.data.General
import it.forgottenworld.tradingcards.data.Rarities
import it.forgottenworld.tradingcards.manager.CardManager
import it.forgottenworld.tradingcards.model.BoosterPack.Companion.tryOpenBoosterPack
import it.forgottenworld.tradingcards.model.Card
import it.forgottenworld.tradingcards.model.Deck.Companion.tryOpenDeck
import it.forgottenworld.tradingcards.model.Deck.Companion.trySaveDeck
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.persistence.PersistentDataType
import kotlin.random.Random

class PlayerListener : Listener {

    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {

        if (!e.view.title.contains("s Deck #")) return

        val contents = e.inventory.contents
        val title = e.view.title.split("'")
        val titleNum = e.view.title.split("#")
        val deckNum = titleNum[1].toInt()

        trySaveDeck(title, deckNum, contents)
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return

        event.player.tryOpenDeck()
        event.player.tryOpenBoosterPack()
    }

    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {

        if (!General.PlayerDropsCard ||
                !General.AutoAddPlayers ||
                e.entity.killer == null) return

        Rarities.values
                .lastOrNull { it.contains(e.entity.name) }
                ?.let { it[e.entity.name] }
                ?.let {
                    if (Random.nextInt(100) + 1 <= General.PlayerDropsCardRarity)
                        e.drops.add(CardManager.createCardItemStack(it, 1))
                }
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) = Card.autoSaveNewPlayerCard(e.player)

    @EventHandler
    fun onPlayerCraft(e: CraftItemEvent) {
        if (e.inventory.contents.any {
                    it.itemMeta
                            ?.persistentDataContainer
                            ?.has(NamespacedKey(TradingCards.instance, "uncraftable"), PersistentDataType.BYTE) == true
                }) e.isCancelled = true
    }
}