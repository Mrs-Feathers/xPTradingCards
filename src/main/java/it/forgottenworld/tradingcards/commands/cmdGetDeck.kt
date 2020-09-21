package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.deck.DeckManager
import it.forgottenworld.tradingcards.util.cMsg
import it.forgottenworld.tradingcards.util.tcMsg
import org.apache.commons.lang3.StringUtils
import org.bukkit.GameMode
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

fun cmdGetDeck(p: Player, args: Array<String>, messagesConfig: FileConfiguration): Boolean {

    if (!p.hasPermission("fwtc.getdeck")) {
        tcMsg(p, "${messagesConfig.getString("Messages.NoPerms")}")
        return true
    }
    if (args.size <= 1) {
        tcMsg(p, "${messagesConfig.getString("Messages.GetDeckUsage")}")
        return true
    }
    if (!StringUtils.isNumeric(args[1])) {
        tcMsg(p, "${messagesConfig.getString("Messages.GetDeckUsage")}")
        return true
    }
    if (!p.hasPermission("fwtc.decks.${args[1]}")) {
        tcMsg(p, "${messagesConfig.getString("Messages.MaxDecks")}")
        return true
    }

    if (DeckManager.hasDeck(p, args[1].toInt())) {
        tcMsg(p, "${messagesConfig.getString("Messages.AlreadyHaveDeck")}"); return true
    }

    if (p.inventory.firstEmpty() != -1) {
        p.sendMessage(cMsg("${messagesConfig.getString("Messages.Prefix")} ${messagesConfig.getString("Messages.GiveDeck")}"))
        p.inventory.addItem(DeckManager.createDeck(p, args[1].toInt()))
    } else {
        if (p.gameMode == GameMode.SURVIVAL) {
            p.sendMessage(cMsg("${messagesConfig.getString("Messages.Prefix")} ${messagesConfig.getString("Messages.GiveDeck")}"))
            p.world.dropItem(p.location, DeckManager.createDeck(p, args[1].toInt()))
        }
    }
    return true
}