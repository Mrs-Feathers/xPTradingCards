package it.forgottenworld.tradingcards.commands.subcommands

import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.manager.DeckManager.createDeck
import it.forgottenworld.tradingcards.manager.DeckManager.hasDeck
import it.forgottenworld.tradingcards.util.tc
import it.forgottenworld.tradingcards.util.tcMsg
import org.bukkit.GameMode
import org.bukkit.entity.Player



fun cmdGetDeck(p: Player, args: Array<String>): Boolean {

    if (!p.hasPermission("fwtradingcards.getdeck")) {
        tcMsg(p, Messages.NoPerms)
        return true
    }

    if (args.size <= 1) {
        tcMsg(p, Messages.GetDeckUsage)
        return true
    }

    val deckId = args[1].toIntOrNull()
    if (deckId == null) {
        tcMsg(p, Messages.GetDeckUsage)
        return true
    }

    if (!p.hasPermission("fwtradingcards.decks.${args[1]}")) {
        tcMsg(p, Messages.MaxDecks)
        return true
    }

    if (p.hasDeck(deckId)) {
        tcMsg(p, Messages.AlreadyHaveDeck)
        return true
    }

    if (p.inventory.firstEmpty() != -1) {
        p.sendMessage(tc("${Messages.Prefix} ${Messages.GiveDeck}"))
        p.inventory.addItem(p.createDeck(args[1].toInt()))
    } else if (p.gameMode == GameMode.SURVIVAL) {
        p.sendMessage(tc("${Messages.Prefix} ${Messages.GiveDeck}"))
        p.world.dropItem(p.location, p.createDeck(args[1].toInt()))
    }

    return true
}