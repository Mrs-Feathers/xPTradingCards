package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.card.CardManager
import it.forgottenworld.tradingcards.util.tcMsg
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

fun cmdCreate(p: Player, messagesConfig: FileConfiguration, args: Array<String>): Boolean {

    if (!p.hasPermission("fwtc.create")) {
        tcMsg(p, "${messagesConfig.getString("Messages.NoPerms")}")
        return true
    }

    if (args.size < 8)
        tcMsg(p, "${messagesConfig.getString("Messages.CreateUsage")}")
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