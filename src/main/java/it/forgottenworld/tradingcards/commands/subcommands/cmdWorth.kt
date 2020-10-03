package it.forgottenworld.tradingcards.commands.subcommands

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.data.General
import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.data.Rarities
import it.forgottenworld.tradingcards.util.sendPrefixedMessage
import org.bukkit.ChatColor
import org.bukkit.entity.Player

fun cmdWorth(p: Player): Boolean {

    if (!p.hasPermission("fwtradingcards.worth")) {
        sendPrefixedMessage(p, Messages.NoPerms)
        return true
    }

    if (TradingCards.economy == null) {
        sendPrefixedMessage(p, Messages.NoVault)
        return true
    }

    if (p.inventory.getItem(p.inventory.heldItemSlot)?.type != General.CardMaterial) {
        sendPrefixedMessage(p, Messages.NotACard)
        return true
    }

    val itemInHand = p.inventory.getItem(p.inventory.heldItemSlot)
    val itemName = itemInHand!!.itemMeta!!.displayName

    val cardName = ChatColor.stripColor(itemName)?.substringAfter(" ") ?: return true
    val rarity = itemInHand.itemMeta?.lore?.get(3)?.let { ChatColor.stripColor(it) } ?: return true
    val card = Rarities[rarity]?.get(cardName) ?: return true

    if (card.price > 0.0)
        sendPrefixedMessage(p, Messages.CanBuy.replaceFirst("%buyAmount%", card.price.toString()))
    else
        sendPrefixedMessage(p, Messages.CanNotBuy)

    return true
}