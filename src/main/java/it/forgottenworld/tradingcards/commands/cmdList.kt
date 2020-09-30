package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.data.Rarities
import it.forgottenworld.tradingcards.util.cMsg
import it.forgottenworld.tradingcards.util.tcMsg
import org.bukkit.command.CommandSender

fun cmdList(sender: CommandSender): Boolean {

    if (!sender.hasPermission("fwtc.list")) {
        tcMsg(sender, Messages.NoPerms)
        return true
    }

    sender.sendMessage(cMsg(Rarities.values
            .flatMap { it.cards.values }
            .take(42)
            .joinToString(", ") { "&7${it.name}&f" } + " &7and more!"))

    return true
}