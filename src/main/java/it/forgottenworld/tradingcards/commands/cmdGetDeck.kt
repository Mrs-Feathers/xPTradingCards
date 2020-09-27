package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.config.Messages
import it.forgottenworld.tradingcards.deck.DeckManager
import it.forgottenworld.tradingcards.util.cMsg
import it.forgottenworld.tradingcards.util.tcMsg
import org.apache.commons.lang3.StringUtils
import org.bukkit.GameMode
import org.bukkit.entity.Player

fun cmdGetDeck(p: Player, args: Array<String>): Boolean {

    if (!p.hasPermission("fwtc.getdeck")) {
        tcMsg(p, Messages.NoPerms)
        return true
    }

    if (args.size <= 1) {
        tcMsg(p, Messages.GetDeckUsage)
        return true
    }

    if (!StringUtils.isNumeric(args[1])) {
        tcMsg(p, Messages.GetDeckUsage)
        return true
    }

    if (!p.hasPermission("fwtc.decks.${args[1]}")) {
        tcMsg(p, Messages.MaxDecks)
        return true
    }

    if (DeckManager.hasDeck(p, args[1].toInt())) {
        tcMsg(p, Messages.AlreadyHaveDeck)
        return true
    }

    if (p.inventory.firstEmpty() != -1) {
        p.sendMessage(cMsg("${Messages.Prefix} ${Messages.GiveDeck}"))
        p.inventory.addItem(DeckManager.createDeck(p, args[1].toInt()))
    } else {
        if (p.gameMode == GameMode.SURVIVAL) {
            p.sendMessage(cMsg("${Messages.Prefix} ${Messages.GiveDeck}"))
            p.world.dropItem(p.location, DeckManager.createDeck(p, args[1].toInt()))
        }
    }
    return true
}