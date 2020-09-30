package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.data.Blacklist
import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.util.tcMsg
import org.bukkit.entity.Player

fun cmdToggle(p: Player): Boolean {

    if (Blacklist.isPlayerBlacklisted(p)) {
        Blacklist.removePlayer(p)
        tcMsg(p, if (Blacklist.WhitelistMode) Messages.ToggleDisabled else Messages.ToggleEnabled)
    } else {
        Blacklist.addPlayer(p)
        tcMsg(p, if (Blacklist.WhitelistMode) Messages.ToggleEnabled else Messages.ToggleDisabled)
    }

    return true
}