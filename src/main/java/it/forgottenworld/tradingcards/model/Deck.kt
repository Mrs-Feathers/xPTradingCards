package it.forgottenworld.tradingcards.model

import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.data.Decks
import it.forgottenworld.tradingcards.data.General
import it.forgottenworld.tradingcards.data.Rarities
import it.forgottenworld.tradingcards.manager.DeckManager.openDeck
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class Deck(val cards: List<DeckCardGroup>) {

    data class DeckCardGroup(val card: Card, val amount: Int, val isShiny: Boolean)

    companion object {

        fun deserialize(serializedCards: List<String>) =
                Deck(serializedCards.map {
                    val s = it.split(",")
                    DeckCardGroup(Rarities[s[0]]!![s[1]]!!, s[2].toInt(), s[3] == "yes")
                })

        fun Player.tryOpenDeck() {
            if (inventory.getItem(inventory.heldItemSlot)?.type != General.DeckMaterial) return

            if (gameMode == GameMode.CREATIVE) return

            if (inventory.getItem(inventory.heldItemSlot)
                            ?.getEnchantmentLevel(Enchantment.DURABILITY) != 10) return

            openDeck(inventory
                    .getItem(inventory.heldItemSlot)
                    ?.itemMeta
                    ?.displayName
                    ?.split("#")
                    ?.get(1)
                    ?.toInt()
                    ?: return
            )
        }

        fun trySaveDeck(title: List<String>, deckNum: Int, contents: Array<ItemStack?>) {
            val id = Bukkit
                    .getOfflinePlayers()
                    .find { it.name == ChatColor.stripColor(title[0]) ?: return }
                    ?.uniqueId ?: return

            val serialized: MutableList<String?> = mutableListOf()

            Decks[id]?.set(deckNum, Deck(contents.mapNotNull {
                if (it == null || it.type != General.CardMaterial) null
                else if (it.itemMeta?.hasDisplayName() == true) {
                    val rarityName = ChatColor
                            .stripColor(it.itemMeta!!.lore!!.last())!!
                            .replaceFirst("${General.ShinyName} ", "")
                    val rarity = Rarities[rarityName]!!

                    val cardName = Card.parseDisplayName(rarityName, it.itemMeta!!.displayName)
                    val card = rarity[cardName] ?: return@mapNotNull null

                    val isShiny = it.containsEnchantment(Enchantment.ARROW_INFINITE)
                    val shiny = if (isShiny) "yes" else "no"

                    val serializedString = "$rarityName,$cardName,${it.amount},$shiny"

                    serialized.add(serializedString)
                    DeckCardGroup(card, it.amount, isShiny)
                } else Bukkit.getPlayer(ChatColor.stripColor(title[0])!!)?.let { p ->
                    p.world.dropItem(p.location, it)
                    null
                }
            }))

            Config.DECKS["Decks.Inventories.$id.$deckNum"] = serialized
        }
    }
}