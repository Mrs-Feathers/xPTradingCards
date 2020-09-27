package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.config.Messages
import it.forgottenworld.tradingcards.util.*
import org.bukkit.entity.Player

fun cmdToggle(p: Player) = when {
    isOnList(p) && blacklistMode() == 'b' -> {
        removeFromList(p)
        tcMsg(p, Messages.ToggleEnabled)
        true
    }
    isOnList(p) && blacklistMode() == 'w' -> {
        removeFromList(p)
        tcMsg(p, Messages.ToggleDisabled)
        true
    }
    !isOnList(p) && blacklistMode() == 'b' -> {
        addToList(p)
        tcMsg(p, Messages.ToggleDisabled)
        true
    }
    !isOnList(p) && blacklistMode() == 'w' -> {
        addToList(p)
        tcMsg(p, Messages.ToggleEnabled)
        true
    }
    else -> false
}