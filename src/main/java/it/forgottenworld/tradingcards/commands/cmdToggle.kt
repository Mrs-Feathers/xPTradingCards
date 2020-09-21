package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.util.*
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

fun cmdToggle(p: Player, messagesConfig: FileConfiguration) = when {
    isOnList(p) && blacklistMode() == 'b' -> {
        removeFromList(p)
        tcMsg(p, "${messagesConfig.getString("Messages.ToggleEnabled")}")
        true
    }
    isOnList(p) && blacklistMode() == 'w' -> {
        removeFromList(p)
        tcMsg(p, "${messagesConfig.getString("Messages.ToggleDisabled")}")
        true
    }
    !isOnList(p) && blacklistMode() == 'b' -> {
        addToList(p)
        tcMsg(p, "${messagesConfig.getString("Messages.ToggleDisabled")}")
        true
    }
    !isOnList(p) && blacklistMode() == 'w' -> {
        addToList(p)
        tcMsg(p, "${messagesConfig.getString("Messages.ToggleEnabled")}")
        true
    }
    else -> false
}