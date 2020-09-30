package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.data.General
import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.data.Rarities
import it.forgottenworld.tradingcards.util.tcMsg
import org.bukkit.ChatColor
import org.bukkit.entity.Player

fun cmdWorth(p: Player): Boolean {

    if (!p.hasPermission("fwtc.worth")) {
        tcMsg(p, Messages.NoPerms)
        return true
    }

    if (!TradingCards.instance.hasVault) {
        tcMsg(p, Messages.NoVault)
        return true
    }

    if (p.inventory.getItem(p.inventory.heldItemSlot)?.type != General.CardMaterial) {
        tcMsg(p, Messages.NotACard)
        return true
    }

    val itemInHand = p.inventory.getItem(p.inventory.heldItemSlot)
    val itemName = itemInHand!!.itemMeta!!.displayName

    if (Config.DEBUG) {
        println(itemName)
        println(ChatColor.stripColor(itemName))
    }

    val cardName = ChatColor.stripColor(itemName)?.substringAfter(" ") ?: return true
    val rarity = itemInHand.itemMeta?.lore?.get(3)?.let { ChatColor.stripColor(it) } ?: return true
    val card = Rarities[rarity]?.cards?.get(cardName) ?: return true

    if (Config.DEBUG) {
        println(cardName)
        println(rarity)
    }

    if (card.price > 0.0)
        tcMsg(p, Messages.CanBuy.replaceFirst("%buyAmount%", card.price.toString()))
    else
        tcMsg(p, Messages.CanNotBuy)

    return true
}