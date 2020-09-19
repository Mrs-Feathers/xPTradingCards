package it.forgottenworld.tradingcards.listeners

import it.forgottenworld.tradingcards.TradingCards
import org.apache.commons.lang.WordUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.tags.ItemTagType
import java.util.*

class PlayerListener(val tradingCards: TradingCards) : Listener {

    private val pluginConfig = tradingCards.configManager.pluginConfig.config!!
    private val cardManager = tradingCards.cardManager
    private val configManager = tradingCards.configManager
    private val deckManager = tradingCards.deckManager

    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
        if (pluginConfig.getBoolean("General.Debug-Mode")) println("[Cards] Title: " + e.view.title)
        if (e.view.title.contains("s Deck #")) {
            if (pluginConfig.getBoolean("General.Debug-Mode")) println("[Cards] Deck closed.")
            val contents = e.inventory.contents
            val title: Array<String> = e.view.title.split("'").toTypedArray()
            val titleNum: Array<String> = e.view.title.split("#").toTypedArray()
            val deckNum: Int = titleNum[1].toInt()
            if (pluginConfig.getBoolean("General.Debug-Mode")) println("[Cards] Deck num: $deckNum")
            if (pluginConfig.getBoolean("General.Debug-Mode")) println("[Cards] Title: " + title[0])
            if (pluginConfig.getBoolean("General.Debug-Mode")) println("[Cards] Title: " + title[1])
            val id = Bukkit.getOfflinePlayer(ChatColor.stripColor(title[0])!!).uniqueId
            if (pluginConfig.getBoolean("General.Debug-Mode")) println("[Cards] New ID: $id")
            val serialized: MutableList<String?> = mutableListOf()
            var arrayOfItemStack1: Array<ItemStack?>
            val j: Int = contents.also { arrayOfItemStack1 = it }.size
            for (i in 0 until j) {
                val it = arrayOfItemStack1[i]
                if (it != null && it.type != Material.AIR &&
                        it.type == Material.valueOf(pluginConfig.getString("General.Card-Material")!!)) {
                    if (it.itemMeta!!.hasDisplayName()) {
                        val lore = it.itemMeta!!.lore
                        val shinyPrefix = pluginConfig.getString("General.Shiny-Name")!!
                        val rarity: String = ChatColor.stripColor(lore!![lore.size - 1])!!.replace(shinyPrefix + " ".toRegex(), "")
                        val card = cardManager.getCardName(rarity, it.itemMeta!!.displayName)
                        val amount = it.amount.toString()
                        var shiny = "no"
                        if (it.containsEnchantment(Enchantment.ARROW_INFINITE)) {
                            shiny = "yes"
                        }
                        val serializedString = "$rarity,$card,$amount,$shiny"
                        serialized.add(serializedString)
                        if (pluginConfig.getBoolean("General.Debug-Mode")) {
                            println("[Cards] Added $serializedString to deck file.")
                        }
                    } else if (Bukkit.getOfflinePlayer(ChatColor.stripColor(title[0])!!).isOnline) {
                        val p = Bukkit.getPlayer(ChatColor.stripColor(title[0])!!)
                        val w = p!!.world
                        w.dropItem(p.location, it)
                    }
                }
            }
            configManager.decksConfig.config!!["Decks.Inventories.$id.$deckNum"] = serialized
            configManager.decksConfig.save()
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            val p = event.player
            if (p.inventory.getItem(p.inventory.heldItemSlot)?.type == Material.valueOf(pluginConfig.getString("General.BoosterPack-Material")!!) &&
                    event.player.hasPermission("fwtc.openboosterpack")) {
                if (p.gameMode != GameMode.CREATIVE) {
                    if (p.inventory.getItem(p.inventory.heldItemSlot)!!.containsEnchantment(Enchantment.ARROW_INFINITE)) {
                        if (p.inventory.getItem(p.inventory.heldItemSlot)!!.amount > 1)
                            p.inventory.getItem(p.inventory.heldItemSlot)!!.amount =
                                    p.inventory.getItem(p.inventory.heldItemSlot)!!.amount - 1
                        else p.inventory.removeItem(p.inventory.getItem(p.inventory.heldItemSlot))
                        val boosterPack = event.item
                        val packMeta = boosterPack!!.itemMeta
                        val lore = packMeta!!.lore
                        var hasExtra = false
                        if (lore!!.size > 2) hasExtra = true
                        val line1: Array<String> = lore[0].split(" ", limit = 2).toTypedArray()
                        val line2: Array<String> = lore[1].split(" ", limit = 2).toTypedArray()
                        var line3 = arrayOf("")
                        if (hasExtra) line3 = lore[2].split(" ", limit = 2).toTypedArray()
                        val normalCardAmount: Int = ChatColor.stripColor(line1[0])!!.toInt()
                        val specialCardAmount: Int = ChatColor.stripColor(line2[0])!!.toInt()
                        var extraCardAmount = 0
                        if (hasExtra) extraCardAmount = ChatColor.stripColor(line3[0])!!.toInt()
                        p.sendMessage(tradingCards.utils.cMsg(configManager.messagesConfig.config!!.getString("Messages.Prefix") + " " + configManager.messagesConfig.config!!.getString("Messages.OpenBoosterPack")))
                        for (i in 0 until normalCardAmount) {
                            if (p.inventory.firstEmpty() != -1) {
                                p.inventory.addItem(cardManager.generateCard(WordUtils.capitalizeFully(line1[1])))
                            } else {
                                val curWorld = p.world
                                if (p.gameMode == GameMode.SURVIVAL) {
                                    curWorld.dropItem(p.location, cardManager.generateCard(WordUtils.capitalizeFully(line1[1]))!!)
                                }
                            }
                        }
                        for (i in 0 until specialCardAmount) {
                            if (p.inventory.firstEmpty() != -1) {
                                p.inventory.addItem(cardManager.generateCard(WordUtils.capitalizeFully(line2[1])))
                            } else {
                                val curWorld = p.world
                                if (p.gameMode == GameMode.SURVIVAL) {
                                    curWorld.dropItem(p.location, cardManager.generateCard(WordUtils.capitalizeFully(line2[1]))!!)
                                }
                            }
                        }
                        if (hasExtra) for (i in 0 until extraCardAmount) {
                            if (p.inventory.firstEmpty() != -1) {
                                p.inventory.addItem(cardManager.generateCard(WordUtils.capitalizeFully(line3[1])))
                            } else {
                                val curWorld = p.world
                                if (p.gameMode == GameMode.SURVIVAL) {
                                    curWorld.dropItem(p.location, cardManager.generateCard(WordUtils.capitalizeFully(line3[1]))!!)
                                }
                            }
                        }
                    }
                } else event.player.sendMessage(tradingCards.utils.cMsg(configManager.messagesConfig.config!!.getString("Messages.Prefix") + " " + configManager.messagesConfig.config!!.getString("Messages.NoCreative")))
            }
            if (p.inventory.getItem(p.inventory.heldItemSlot)?.type == Material.valueOf(pluginConfig.getString("General.Deck-Material")!!)) {
                if (pluginConfig.getBoolean("General.Debug-Mode")) println("[Cards] Deck material...")
                if (p.gameMode != GameMode.CREATIVE) {
                    if (pluginConfig.getBoolean("General.Debug-Mode")) println("[Cards] Not creative...")
                    if (p.inventory.getItem(p.inventory.heldItemSlot)?.containsEnchantment(Enchantment.DURABILITY) == true) {
                        if (pluginConfig.getBoolean("General.Debug-Mode")) println("[Cards] Has enchant...")
                        if (p.inventory.getItem(p.inventory.heldItemSlot)?.getEnchantmentLevel(Enchantment.DURABILITY) == 10) {
                            if (pluginConfig.getBoolean("General.Debug-Mode")) println("[Cards] Enchant is level 10...")
                            val name = p.inventory.getItem(p.inventory.heldItemSlot)?.itemMeta!!.displayName
                            val nameSplit: Array<String> = name.split("#").toTypedArray()
                            val num: Int = nameSplit[1].toInt()
                            deckManager.openDeck(p, num)
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {
        if (pluginConfig.getBoolean("General.Player-Drops-Card") && pluginConfig.getBoolean("General.Auto-Add-Players")) {
            val killer: Entity? = e.entity.killer
            if (killer != null) {
                val rarities = pluginConfig.getConfigurationSection("Rarities")!!
                val rarityKeys = rarities.getKeys(false)
                var k: String? = null
                for (key in rarityKeys) {
                    if (configManager.cardsConfig.config!!.contains("Cards." + key + "." + e.entity.name)) {
                        if (pluginConfig.getBoolean("General.Debug-Mode")) println("[Cards] $key")
                        k = key
                    }
                }
                if (k != null) {
                    val rndm = Random().nextInt(100) + 1
                    if (rndm <= pluginConfig.getInt("General.Player-Drops-Card-Rarity")) {
                        e.drops.add(cardManager.createPlayerCard(e.entity.name, k, 1, false))
                        if (pluginConfig.getBoolean("General.Debug-Mode")) println("[Cards] " + e.drops.toString())
                    }
                } else {
                    println("k is null")
                }
            }
        }
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        if (pluginConfig.getBoolean("General.Auto-Add-Players")) {
            val p = e.player
            val gc = GregorianCalendar()
            val date: Int
            val month: Int
            val year: Int
            if (p.hasPlayedBefore()) {
                gc.timeInMillis = p.firstPlayed
            } else {
                gc.timeInMillis = System.currentTimeMillis()
            }
            date = gc[Calendar.DATE]
            month = gc[Calendar.MONTH] + 1
            year = gc[Calendar.YEAR]
            val rarities = pluginConfig.getConfigurationSection("Rarities")!!
            var i = 1
            val rarityKeys = rarities.getKeys(false)
            val children = tradingCards.permRarities.children
            var rarity = pluginConfig.getString("General.Auto-Add-Player-Rarity")!!
            for (key in rarityKeys) {
                i++
                children["fwtc.rarity.$key"] = java.lang.Boolean.FALSE
                tradingCards.permRarities.recalculatePermissibles()
                if (p.hasPermission("fwtc.rarity.$key")) {
                    rarity = key
                    break
                }
            }
            if (p.isOp) rarity = pluginConfig.getString("General.Player-Op-Rarity")!!
            if (!configManager.cardsConfig.config!!.contains("Cards." + rarity + "." + p.name)) {
                val series = pluginConfig.getString("General.Player-Series")!!
                val type = pluginConfig.getString("General.Player-Type")!!
                val hasShiny = pluginConfig.getBoolean("General.Player-Has-Shiny-Version")
                configManager.cardsConfig.config!!["Cards." + rarity + "." + p.name + ".Series"] = series
                configManager.cardsConfig.config!!["Cards." + rarity + "." + p.name + ".Type"] = type
                configManager.cardsConfig.config!!["Cards." + rarity + "." + p.name + ".Has-Shiny-Version"] = hasShiny
                if (pluginConfig.getBoolean("General.American-Mode")) configManager.cardsConfig.config!!["Cards." + rarity + "." + p.name + ".Info"] = "Joined $month/$date/$year" else configManager.cardsConfig.config!!["Cards." + rarity + "." + p.name + ".Info"] = "Joined $date/$month/$year"
                configManager.cardsConfig.save()
                configManager.reloadCardsConfig()
            }
        }
    }

    @EventHandler
    fun onPlayerCraft(e: CraftItemEvent) {
        val items = e.inventory.contents
        items.forEach { item ->
            val customTagContainer = item.itemMeta?.customTagContainer
            if (customTagContainer != null && customTagContainer.hasCustomTag(tradingCards.nameSpacedKey, ItemTagType.BYTE)) {
                e.isCancelled = true
                return
            }
        }
    }
}