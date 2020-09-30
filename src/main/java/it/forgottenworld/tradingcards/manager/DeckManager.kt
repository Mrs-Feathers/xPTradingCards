package it.forgottenworld.tradingcards.manager

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.data.Decks
import it.forgottenworld.tradingcards.data.General
import it.forgottenworld.tradingcards.util.cMsg
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object DeckManager {

    private val blankDeck
        get() = ItemStack(General.DeckMaterial).apply {
            itemMeta = itemMeta?.apply {
                persistentDataContainer.set(TradingCards.nameSpacedKey, PersistentDataType.BYTE, 1)
            }
        }

    fun Player.createDeck(deckNum: Int) =
            blankDeck.apply {
                val deckMeta = itemMeta!!
                deckMeta.setDisplayName(cMsg("${General.DeckPrefix}${name}'s Deck #$deckNum"))

                if (General.HideEnchants)
                    deckMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)

                itemMeta?.persistentDataContainer
                        ?.set(NamespacedKey(TradingCards.instance, "deckOwnerUuid"),
                                PersistentDataType.STRING,
                                uniqueId.toString())

                itemMeta = deckMeta
                addUnsafeEnchantment(Enchantment.DURABILITY, 10)
            }

    fun Player.hasDeck(deckNum: Int) =
            inventory.filterNotNull().any {
                it.type == General.DeckMaterial &&
                        it.getEnchantmentLevel(Enchantment.DURABILITY) == 10 &&
                        deckNum == it.itemMeta!!.displayName.split("#")[1].toInt()
            }

    fun Player.openDeck(deckNum: Int) {

        val deck = Decks[uniqueId]?.get(deckNum) ?: return

        val inv = Bukkit.createInventory(null, 27, cMsg("&c${name}'s Deck #$deckNum"))

        for (c in deck.cards.map { CardManager.getCardItemStack(it.card, it.amount, it.isShiny) }) {
            inv.addItem(c)
        }

        openInventory(inv)
    }
}