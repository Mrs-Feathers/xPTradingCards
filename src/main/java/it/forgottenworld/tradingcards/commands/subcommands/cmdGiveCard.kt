package it.forgottenworld.tradingcards.commands.subcommands

import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.data.Rarities
import it.forgottenworld.tradingcards.manager.CardManager
import it.forgottenworld.tradingcards.util.sendPrefixedMessage
import org.bukkit.entity.Player

fun cmdGiveCard(p: Player, args: Array<String>): Boolean {

    if (!p.hasPermission("fwtradingcards.givecard")) {
        sendPrefixedMessage(p, Messages.NoPerms)
        return true
    }

    if (args.size <= 2) {
        sendPrefixedMessage(p, Messages.GiveCardUsage)
        return true
    }

    Rarities[args[1]]?.get(args[2])
            ?.let { p.inventory.addItem(CardManager.createCardItemStack(it, 1)) }
            ?: sendPrefixedMessage(p, Messages.NoCard)

    return true
}