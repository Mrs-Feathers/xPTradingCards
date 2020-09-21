package it.forgottenworld.tradingcards.listeners

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.card.CardManager
import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.config.ConfigManager
import it.forgottenworld.tradingcards.deck.DeckManager
import it.forgottenworld.tradingcards.util.cMsg
import it.forgottenworld.tradingcards.util.printDebug
import org.apache.commons.lang.WordUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*

class PlayerListener : Listener {

    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {

        printDebug("[Cards] Title: ${e.view.title}")
        if (!e.view.title.contains("s Deck #")) return
        printDebug("[Cards] Deck closed.")

        val contents = e.inventory.contents
        val title = e.view.title.split("'")
        val titleNum = e.view.title.split("#")
        val deckNum = titleNum[1].toInt()

        if (Config.DEBUG) {
            println("[Cards] Deck num: $deckNum")
            println("[Cards] Title: ${title[0]}")
            println("[Cards] Title: ${title[1]}")
        }

        val id = Bukkit
                .getOfflinePlayers()
                .find { it.name == ChatColor.stripColor(title[0]) ?: return }
                ?.uniqueId ?: return
        printDebug("[Cards] New ID: $id")

        val serialized: MutableList<String?> = mutableListOf()

        contents.forEach {

            if (it != null &&
                    it.type != Material.AIR &&
                    it.type == Material.valueOf(Config.PLUGIN.getString("General.Card-Material")!!)) {

                if (it.itemMeta!!.hasDisplayName()) {

                    val lore = it.itemMeta!!.lore
                    val shinyPrefix = Config.PLUGIN.getString("General.Shiny-Name")!!
                    val rarity = ChatColor.stripColor(lore!![lore.size - 1])!!.replace("$shinyPrefix ", "")
                    val card = CardManager.getCardName(rarity, it.itemMeta!!.displayName)
                    val amount = it.amount.toString()
                    val shiny = if (it.containsEnchantment(Enchantment.ARROW_INFINITE)) "yes" else "no"
                    val serializedString = "$rarity,$card,$amount,$shiny"

                    serialized.add(serializedString)
                    if (Config.DEBUG)
                        println("[Cards] Added $serializedString to deck file.")

                } else Bukkit.getPlayer(ChatColor.stripColor(title[0])!!)?.let {p ->
                    p.world.dropItem(p.location, it)
                }
            }
        }
        Config.DECKS["Decks.Inventories.$id.$deckNum"] = serialized
        ConfigManager.decksConfig.save()
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
        val p = event.player

        fun doAfter() {
            if (p.inventory.getItem(p.inventory.heldItemSlot)?.type
                    != Material.valueOf(Config.PLUGIN.getString("General.Deck-Material")!!)) return

            printDebug("[Cards] Deck material...")
            if (p.gameMode == GameMode.CREATIVE) return

            printDebug("[Cards] Not creative...")
            if (p.inventory.getItem(p.inventory.heldItemSlot)
                            ?.containsEnchantment(Enchantment.DURABILITY) != true) return

            printDebug("[Cards] Has enchant...")
            if (p.inventory.getItem(p.inventory.heldItemSlot)
                            ?.getEnchantmentLevel(Enchantment.DURABILITY) != 10) return

            printDebug("[Cards] Enchant is level 10...")
            val name = p.inventory.getItem(p.inventory.heldItemSlot)?.itemMeta!!.displayName

            DeckManager.openDeck(p, name.split("#")[1].toInt())
        }

        if (p.inventory.getItem(p.inventory.heldItemSlot)?.type
                == Material.valueOf(Config.PLUGIN.getString("General.BoosterPack-Material")!!) &&
                event.player.hasPermission("fwtc.openboosterpack")) {

            if (p.gameMode == GameMode.CREATIVE) {
                event.player.sendMessage(cMsg(
                        "${
                            ConfigManager
                                    .messagesConfig
                                    .config
                                    ?.getString("Messages.Prefix")
                        } ${
                            ConfigManager
                                    .messagesConfig
                                    .config
                                    ?.getString("Messages.NoCreative")
                        }"))
                doAfter()
                return
            }

            if (p.inventory.getItem(p.inventory.heldItemSlot)?.containsEnchantment(Enchantment.ARROW_INFINITE) == false) {
                doAfter()
                return
            }

            if (p.inventory.getItem(p.inventory.heldItemSlot)!!.amount > 1)
                p.inventory.getItem(p.inventory.heldItemSlot)!!.amount -= 1
            else
                p.inventory.removeItem(p.inventory.getItem(p.inventory.heldItemSlot))

            val boosterPack = event.item
            val packMeta = boosterPack!!.itemMeta
            val lore = packMeta!!.lore
            var hasExtra = false

            if (lore!!.size > 2) hasExtra = true
            val line1 = lore[0].split(" ", limit = 2)
            val line2 = lore[1].split(" ", limit = 2)
            val line3 = if (hasExtra) lore[2].split(" ", limit = 2) else listOf("")

            val normalCardAmount = ChatColor.stripColor(line1[0])!!.toInt()
            val specialCardAmount = ChatColor.stripColor(line2[0])!!.toInt()
            var extraCardAmount = 0
            if (hasExtra) extraCardAmount = ChatColor.stripColor(line3[0])!!.toInt()

            p.sendMessage(cMsg("${Config.MESSAGES.getString("Messages.Prefix")} ${Config.MESSAGES.getString("Messages.OpenBoosterPack")}"))

            for (i in 0 until normalCardAmount) {
                if (p.inventory.firstEmpty() != -1)
                    p.inventory.addItem(CardManager.generateCard(WordUtils.capitalizeFully(line1[1])))
                else if (p.gameMode == GameMode.SURVIVAL)
                    p.world.dropItem(p.location, CardManager.generateCard(WordUtils.capitalizeFully(line1[1]))!!)
            }

            for (i in 0 until specialCardAmount) {
                if (p.inventory.firstEmpty() != -1)
                    p.inventory.addItem(CardManager.generateCard(WordUtils.capitalizeFully(line2[1])))
                else if (p.gameMode == GameMode.SURVIVAL)
                    p.world.dropItem(p.location, CardManager.generateCard(WordUtils.capitalizeFully(line2[1]))!!)
            }

            if (hasExtra) for (i in 0 until extraCardAmount) {
                if (p.inventory.firstEmpty() != -1)
                    p.inventory.addItem(CardManager.generateCard(WordUtils.capitalizeFully(line3[1])))
                else if (p.gameMode == GameMode.SURVIVAL)
                    p.world.dropItem(p.location, CardManager.generateCard(WordUtils.capitalizeFully(line3[1]))!!)
            }
        }

        doAfter()
    }

    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {

        if (!Config.PLUGIN.getBoolean("General.Player-Drops-Card") ||
                !Config.PLUGIN.getBoolean("General.Auto-Add-Players") ||
                e.entity.killer == null) return

        Config.PLUGIN.getConfigurationSection("Rarities")
                ?.getKeys(false)
                ?.last { k ->
                    Config.CARDS.contains("Cards.$k.${e.entity.name}")
                            .also { if (it) printDebug("[Cards] $it") }
                }?.let {
                    if (Random().nextInt(100) + 1 <= Config.PLUGIN.getInt("General.Player-Drops-Card-Rarity")) {
                        e.drops.add(CardManager.createPlayerCard(e.entity.name, it, 1, false))
                        printDebug("[Cards] ${e.drops}")
                    }
                } ?: println("key is null")
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {

        val cardsConfig = Config.CARDS

        if (!Config.PLUGIN.getBoolean("General.Auto-Add-Players")) return

        val p = e.player
        val gc = GregorianCalendar()

        gc.timeInMillis = if (p.hasPlayedBefore()) p.firstPlayed else System.currentTimeMillis()

        val date = gc[Calendar.DATE]
        val month = gc[Calendar.MONTH] + 1
        val year = gc[Calendar.YEAR]

        val rarities = Config.PLUGIN.getConfigurationSection("Rarities")!!

        val rarityKeys = rarities.getKeys(false)
        val children = TradingCards.permRarities.children

        val rarity = if (p.isOp)
            Config.PLUGIN.getString("General.Player-Op-Rarity") ?: return
        else
            rarityKeys.find {
                children["fwtc.rarity.$it"] = false
                TradingCards.permRarities.recalculatePermissibles()
                p.hasPermission("fwtc.rarity.$it")
            } ?: Config.PLUGIN.getString("General.Auto-Add-Player-Rarity") ?: return

        if (cardsConfig.contains("Cards.$rarity.${p.name}")) return

        val series = Config.PLUGIN.getString("General.Player-Series")!!
        val type = Config.PLUGIN.getString("General.Player-Type")!!
        val hasShiny = Config.PLUGIN.getBoolean("General.Player-Has-Shiny-Version")

        cardsConfig["Cards.$rarity.${p.name}.Series"] = series
        cardsConfig["Cards.$rarity.${p.name}.Type"] = type
        cardsConfig["Cards.$rarity.${p.name}.Has-Shiny-Version"] = hasShiny

        cardsConfig["Cards.$rarity.${p.name}.Info"] =
                if (Config.PLUGIN.getBoolean("General.American-Mode"))
                    "Joined $month/$date/$year"
                else
                    "Joined $date/$month/$year"

        ConfigManager.cardsConfig.save()
        ConfigManager.reloadCardsConfig()
    }

    @EventHandler
    fun onPlayerCraft(e: CraftItemEvent){
        if (e.inventory.contents.any {
            it.itemMeta
                    ?.persistentDataContainer
                    ?.has(TradingCards.nameSpacedKey, PersistentDataType.BYTE) == true
        }) e.isCancelled = true
    }
}