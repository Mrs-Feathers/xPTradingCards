package it.forgottenworld.tradingcards.listeners

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.data.General
import it.forgottenworld.tradingcards.data.Rarities
import it.forgottenworld.tradingcards.manager.CardManager
import it.forgottenworld.tradingcards.model.BoosterPack.Companion.tryOpenBoosterPack
import it.forgottenworld.tradingcards.model.Card
import it.forgottenworld.tradingcards.model.Deck.Companion.tryOpenDeck
import it.forgottenworld.tradingcards.model.Deck.Companion.trySaveDeck
import it.forgottenworld.tradingcards.util.MapRenderer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
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

    @EventHandler
    fun onItemSwitch(e: PlayerItemHeldEvent){
        val itemStack = e.player.inventory.itemInMainHand
        if (itemStack.type != Material.FILLED_MAP) return
        val itemMeta = itemStack.itemMeta
        if (itemMeta == null) return
        val rarity = itemMeta.persistentDataContainer.get(NamespacedKey(TradingCards.instance,"rarity"), PersistentDataType.STRING)
        val name = itemMeta.persistentDataContainer.get(NamespacedKey(TradingCards.instance,"name"), PersistentDataType.STRING)
        val card = Rarities[rarity]?.get(name)
        val mapMeta = itemMeta as MapMeta
        val mapView = card?.mapViewId?.let {
            @Suppress("DEPRECATION")
            //Reason: https://www.spigotmc.org/threads/getmap-in-1-13.333754/#post-3115997
            Bukkit.getMap(it)
        }
        mapView?.renderers?.clear()
        card?.image?.let { MapRenderer(it,false) }?.let { mapView?.addRenderer(it) }
        mapMeta.mapView = mapView
        itemStack.setItemMeta(mapMeta)
        e.player.updateInventory()
    }

    @EventHandler
    fun onItemSwap(e: InventoryClickEvent){
        if (e.whoClicked !is Player) return
        val player = e.whoClicked as Player
        val itemStack = player.inventory.itemInMainHand
        if (itemStack.type != Material.FILLED_MAP) return
        val itemMeta = itemStack.itemMeta
        if (itemMeta == null) return
        val rarity = itemMeta.persistentDataContainer.get(NamespacedKey(TradingCards.instance,"rarity"), PersistentDataType.STRING)
        val name = itemMeta.persistentDataContainer.get(NamespacedKey(TradingCards.instance,"name"), PersistentDataType.STRING)
        val card = Rarities[rarity]?.get(name)
        val mapMeta = itemMeta as MapMeta
        val mapView = card?.mapViewId?.let {
            @Suppress("DEPRECATION")
            //Reason: https://www.spigotmc.org/threads/getmap-in-1-13.333754/#post-3115997
            Bukkit.getMap(it)
        }
        mapView?.renderers?.clear()
        card?.image?.let { MapRenderer(it,false) }?.let { mapView?.addRenderer(it) }
        mapMeta.mapView = mapView
        itemStack.setItemMeta(mapMeta)
        player.updateInventory()
    }
}