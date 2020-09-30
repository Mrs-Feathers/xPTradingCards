package it.forgottenworld.tradingcards.listeners

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.data.Decks
import it.forgottenworld.tradingcards.data.General
import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.data.Rarities
import it.forgottenworld.tradingcards.manager.CardManager
import it.forgottenworld.tradingcards.manager.DeckManager.openDeck
import it.forgottenworld.tradingcards.model.Card
import it.forgottenworld.tradingcards.model.Deck
import it.forgottenworld.tradingcards.util.cMsg
import it.forgottenworld.tradingcards.util.capitalizeFully
import it.forgottenworld.tradingcards.util.printDebug
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
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
import kotlin.random.Random

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

        val id = Bukkit
                .getOfflinePlayers()
                .find { it.name == ChatColor.stripColor(title[0]) ?: return }
                ?.uniqueId ?: return

        val serialized: MutableList<String?> = mutableListOf()

        Decks[id]!![deckNum] = Deck(id, contents.mapNotNull {
            if (it == null || it.type != General.CardMaterial) null
            if (it.itemMeta?.hasDisplayName() == true) {

                val rarityName = ChatColor
                        .stripColor(it.itemMeta!!.lore!!.last())!!
                        .replaceFirst("${General.ShinyName} ", "")
                val rarity = Rarities[rarityName]!!

                val cardName = Card.parseDisplayName(rarityName, it.itemMeta!!.displayName)
                val card = rarity.cards[cardName]!!

                val isShiny = it.containsEnchantment(Enchantment.ARROW_INFINITE)
                val shiny = if (isShiny) "yes" else "no"

                val serializedString = "$rarityName,$cardName,${it.amount},$shiny"

                serialized.add(serializedString)
                Deck.DeckCardGroup(card, it.amount, isShiny)
            } else Bukkit.getPlayer(ChatColor.stripColor(title[0])!!)?.let {p ->
                p.world.dropItem(p.location, it)
                null
            }
        })
        Config.DECKS["Decks.Inventories.$id.$deckNum"] = serialized
        Config.decksConfig.save()
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
        val p = event.player

        fun doAfter() {
            if (p.inventory.getItem(p.inventory.heldItemSlot)?.type
                    != General.DeckMaterial) return

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

            p.openDeck(name.split("#")[1].toInt())
        }

        if (p.inventory.getItem(p.inventory.heldItemSlot)?.type == General.BoosterPackMaterial
                && event.player.hasPermission("fwtc.openboosterpack")) {

            if (p.gameMode == GameMode.CREATIVE) {
                event.player.sendMessage(cMsg(
                        "${Messages.Prefix} ${Messages.NoCreative}"))
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

            p.sendMessage(cMsg("${Messages.Prefix} ${Messages.OpenBoosterPack}"))

            val normalRarity = Rarities[line1[1].capitalizeFully()] ?: return
            val specialRarity = Rarities[line2[1].capitalizeFully()] ?: return

            for (i in 0 until normalCardAmount) {
                if (p.inventory.firstEmpty() != -1)
                    p.inventory.addItem(CardManager.getRandomCardItemStack(normalRarity))
                else if (p.gameMode == GameMode.SURVIVAL)
                    p.world.dropItem(p.location, CardManager.getRandomCardItemStack(normalRarity))
            }

            for (i in 0 until specialCardAmount) {
                if (p.inventory.firstEmpty() != -1)
                    p.inventory.addItem(CardManager.getRandomCardItemStack(specialRarity))
                else if (p.gameMode == GameMode.SURVIVAL)
                    p.world.dropItem(p.location, CardManager.getRandomCardItemStack(specialRarity))
            }

            if (hasExtra) for (i in 0 until extraCardAmount) {

                val extraRarity = Rarities[line3[1].capitalizeFully()]
                if (extraRarity != null) {
                    if (p.inventory.firstEmpty() != -1)
                        p.inventory.addItem(CardManager.getRandomCardItemStack(extraRarity))
                    else if (p.gameMode == GameMode.SURVIVAL)
                        p.world.dropItem(p.location, CardManager.getRandomCardItemStack(extraRarity))
                }
            }
        }

        doAfter()
    }

    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {

        if (!General.PlayerDropsCard ||
                !General.AutoAddPlayers ||
                e.entity.killer == null) return

        Rarities.values
                .lastOrNull { it.cards.contains(e.entity.name) }
                ?.let { it.cards[e.entity.name] }
                ?.let {
                    if (Random.nextInt(100) + 1 <= General.PlayerDropsCardRarity) {
                        e.drops.add(CardManager.getCardItemStack(it, 1))
                        printDebug("[Cards] ${e.drops}")
                    }
                }
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {

        if (!General.AutoAddPlayers) return

        val cardsConfig = Config.CARDS
        val p = e.player
        val gc = GregorianCalendar()

        gc.timeInMillis = if (p.hasPlayedBefore()) p.firstPlayed else System.currentTimeMillis()

        val date = gc[Calendar.DATE]
        val month = gc[Calendar.MONTH] + 1
        val year = gc[Calendar.YEAR]

        val children = TradingCards.permRarities.children

        val rarity = if (p.isOp)
            General.PlayerOpRarity
        else
            Rarities.keys.find {
                children["fwtc.rarity.$it"] = false
                TradingCards.permRarities.recalculatePermissibles()
                p.hasPermission("fwtc.rarity.$it")
            } ?: General.AutoAddPlayerRarity

        if (cardsConfig.contains("Cards.$rarity.${p.name}")) return

        cardsConfig["Cards.$rarity.${p.name}.Series"] = General.PlayerSeries
        cardsConfig["Cards.$rarity.${p.name}.Type"] = General.PlayerType
        cardsConfig["Cards.$rarity.${p.name}.Has-Shiny-Version"] = General.PlayerHasShinyVersion
        val info = if (General.AmericanMode)
            "Joined $month/$date/$year"
        else
            "Joined $date/$month/$year"
        cardsConfig["Cards.$rarity.${p.name}.Info"] = info


        Config.cardsConfig.save()

        Rarities[rarity]?.let {
            it.cards.put(
                    p.name,
                    Card(
                            p.name.replace(" ", ""),
                            it,
                            General.PlayerHasShinyVersion,
                            General.PlayerSeries,
                            "None",
                            General.PlayerType,
                            info,
                            0.0
                    )
            )
        }
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