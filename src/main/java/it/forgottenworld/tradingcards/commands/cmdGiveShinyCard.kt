package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.data.Rarities
import it.forgottenworld.tradingcards.manager.CardManager
import it.forgottenworld.tradingcards.util.tcMsg
import org.bukkit.entity.Player


fun cmdGiveShinyCard(p: Player, args: Array<String>): Boolean {

    if (!p.hasPermission("fwtc.giveshinycard")) {
        tcMsg(p, Messages.NoPerms)
        return true
    }

    if (args.size <= 2) {
        tcMsg(p, Messages.GiveCardUsage)
        return true
    }

    Rarities[args[1]]?.cards?.get(args[2])
            ?.let { p.inventory.addItem(CardManager.getCardItemStack(it, 1, true)) }
            ?: tcMsg(p, Messages.NoCard)

    return true
}