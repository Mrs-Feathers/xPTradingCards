package it.forgottenworld.tradingcards.manager

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.data.Colors
import it.forgottenworld.tradingcards.data.DisplayNames
import it.forgottenworld.tradingcards.data.General
import it.forgottenworld.tradingcards.model.Card
import it.forgottenworld.tradingcards.model.Rarity
import it.forgottenworld.tradingcards.util.cMsg
import it.forgottenworld.tradingcards.util.wrapString
import org.bukkit.ChatColor
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object CardManager {

    private fun setCardItemStackDisplayName(itemStack: ItemStack, card: Card, isShiny: Boolean) {
        val meta = itemStack.itemMeta ?: return
        val shinyPrefix = if (isShiny) General.ShinyName else ""
        meta.setDisplayName(cMsg(DisplayNames.ShinyTitle
                .replaceFirst("%PREFIX%", General.CardPrefix)
                .replaceFirst("%COLOUR%", card.rarity.color)
                .replaceFirst("%NAME%", card.name)
                .replaceFirst("%COST%", card.price.toString())
                .replace("_", " ")
                .replaceFirst("%SHINYPREFIX%", shinyPrefix)))
        itemStack.itemMeta = meta
    }

    private fun setCardItemStackLore(itemStack: ItemStack, card: Card, isShiny: Boolean) {
        val itemMeta = itemStack.itemMeta ?: return

        itemMeta.lore = mutableListOf(cMsg("${Colors.Type}${DisplayNames.Type}: &f${card.type}")).apply {

            if (card.info == "None" || card.info == "")
                add(cMsg("${Colors.Info}${DisplayNames.Info}: &f${card.info}"))
            else {
                add(cMsg("${Colors.Info}${DisplayNames.Info}:"))
                addAll(wrapString(card.info))
            }

            add(cMsg("${Colors.Series}${DisplayNames.Series}: &f${card.series}"))

            if (card.about != "None")
                    add(cMsg("${Colors.About}${DisplayNames.About}: &f${card.about}"))

            add(cMsg("${Colors.Rarity}${ChatColor.BOLD}${if (isShiny) "${General.ShinyName} " else ""}${card.rarity.name}"))
        }

        if (General.HideEnchants)
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)

        itemStack.itemMeta = itemMeta

        if (isShiny)
            itemStack.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10)
    }

    private fun getBlankCardItemStack(quantity: Int) =
            ItemStack(General.CardMaterial, quantity).apply {
                val meta = itemMeta
                meta?.persistentDataContainer?.set(TradingCards.nameSpacedKey, PersistentDataType.BYTE,1)
                meta?.let { itemMeta = it }
            }

    fun getCardItemStack(card: Card, quantity: Int, forcedShiny: Boolean = false) =
            getBlankCardItemStack(quantity).apply {
                val isShiny = forcedShiny || card.isShiny
                setCardItemStackDisplayName(this, card, isShiny)
                setCardItemStackLore(this, card, isShiny)
            }

    fun getRandomCardItemStack(rarity: Rarity) =
            getCardItemStack(rarity.cards.values.random(), 1)
}