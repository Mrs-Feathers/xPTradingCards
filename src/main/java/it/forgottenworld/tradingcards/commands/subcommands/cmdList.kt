package it.forgottenworld.tradingcards.commands.subcommands

import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.data.Rarities
import it.forgottenworld.tradingcards.util.tC
import it.forgottenworld.tradingcards.util.sendPrefixedMessage
import org.bukkit.command.CommandSender

fun cmdList(sender: CommandSender): Boolean {

    if (!sender.hasPermission("fwtradingcards.list")) {
        sendPrefixedMessage(sender, Messages.NoPerms)
        return true
    }

    sender.sendMessage(tC(Rarities.values
            .flatMap { it.values }
            .take(42)
            .joinToString(", ") { "&7${it.name}&f" } + " &7and more!"))

    return true
}