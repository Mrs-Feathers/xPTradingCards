package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.card.CardManager
import it.forgottenworld.tradingcards.config.Messages
import it.forgottenworld.tradingcards.util.tcMsg
import org.bukkit.entity.Player

fun cmdCreate(p: Player, args: Array<String>): Boolean {

    if (!p.hasPermission("fwtc.create")) {
        tcMsg(p, Messages.NoPerms)
        return true
    }

    if (args.size < 8)
        tcMsg(p, Messages.CreateUsage)
    else {

        CardManager.createCard(
                p,
                args[1].replace("_", " "),
                args[2],
                args[3].replace("_", " "),
                args[4].replace("_", " "),
                setOf("true", "yes", "y").contains(args[5].toLowerCase()),
                args[6].replace("_", " ")
        )

    }

    return false
}