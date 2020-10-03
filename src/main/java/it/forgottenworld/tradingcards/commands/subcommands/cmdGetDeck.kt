package it.forgottenworld.tradingcards.commands.subcommands

import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.manager.DeckManager.createDeckAndItemStack
import it.forgottenworld.tradingcards.manager.DeckManager.hasDeck
import it.forgottenworld.tradingcards.util.tC
import it.forgottenworld.tradingcards.util.sendPrefixedMessage
import org.bukkit.GameMode
import org.bukkit.entity.Player


fun cmdGetDeck(p: Player, args: Array<String>): Boolean {

    if (!p.hasPermission("fwtradingcards.getdeck")) {
        sendPrefixedMessage(p, Messages.NoPerms)
        return true
    }

    if (args.size <= 1) {
        sendPrefixedMessage(p, Messages.GetDeckUsage)
        return true
    }

    val deckId = args[1].toIntOrNull()
    if (deckId == null) {
        sendPrefixedMessage(p, Messages.GetDeckUsage)
        return true
    }

    if (!p.hasPermission("fwtradingcards.decks.${args[1]}")) {
        sendPrefixedMessage(p, Messages.MaxDecks)
        return true
    }

    if (p.hasDeck(deckId)) {
        sendPrefixedMessage(p, Messages.AlreadyHaveDeck)
        return true
    }

    if (p.inventory.firstEmpty() != -1) {
        p.sendMessage(tC("${Messages.Prefix} ${Messages.GiveDeck}"))
        p.inventory.addItem(p.createDeckAndItemStack(args[1].toInt()))
    } else if (p.gameMode == GameMode.SURVIVAL) {
        p.sendMessage(tC("${Messages.Prefix} ${Messages.GiveDeck}"))
        p.world.dropItem(p.location, p.createDeckAndItemStack(args[1].toInt()))
    }

    return true
}