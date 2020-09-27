package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.card.CardManager
import it.forgottenworld.tradingcards.config.Messages
import it.forgottenworld.tradingcards.util.tcMsg
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player


fun cmdGiveShinyCard(p: Player, args: Array<String>, cardsConfig: FileConfiguration): Boolean {
    if (!p.hasPermission("fwtc.giveshinycard")) { tcMsg(p, Messages.NoPerms); return true }
    if (args.size <= 2) { tcMsg(p, Messages.GiveCardUsage); return true }

    if (!cardsConfig.contains("Cards.${args[1].replace("_", " ")}.${args[2]}"))
        tcMsg(p, Messages.NoCard)
    else
        p.inventory.addItem(CardManager.createPlayerCard(
                args[2],
                args[1].replace("_", " "),
                1,
                true))

    return true
}