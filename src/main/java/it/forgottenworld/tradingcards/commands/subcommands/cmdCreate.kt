package it.forgottenworld.tradingcards.commands.subcommands

import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.model.Card
import it.forgottenworld.tradingcards.util.sendPrefixedMessage
import org.bukkit.entity.Player

fun cmdCreate(p: Player, args: Array<String>): Boolean {

    if (!p.hasPermission("fwtradingcards.create")) {
        sendPrefixedMessage(p, Messages.NoPerms)
        return true
    }

    if (args.size < 7)
        sendPrefixedMessage(p, Messages.CreateUsage)
    else Card.saveNew(
            p,
            args[1].replace("_", " "),
            args[2],
            args[3].replace("_", " "),
            args[4].replace("_", " "),
            setOf("true", "yes", "y").contains(args[5].toLowerCase()),
            args[6].replace("_", " ")
    )

    return false
}