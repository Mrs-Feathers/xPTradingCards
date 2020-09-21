package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.deck.DeckManager
import it.forgottenworld.tradingcards.util.cMsg
import it.forgottenworld.tradingcards.util.tcMsg
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration

fun cmdGiveBoosterPack(sender: CommandSender, args: Array<String>, messagesConfig: FileConfiguration): Boolean {

    if (!sender.hasPermission("fwtc.giveboosterpack")) {
        tcMsg(sender, "${messagesConfig.getString("Messages.NoPerms")}")
        return true
    }

    if (args.size <= 2) {
        tcMsg(sender, "${messagesConfig.getString("Messages.GiveBoosterPackUsage")}")
        return true
    }

    if (!Config.PLUGIN.contains("BoosterPacks.${args[2].replace(" ", "_")}")) {
        tcMsg(sender, "${messagesConfig.getString("Messages.NoBoosterPack")}")
        return true
    }

    if (Bukkit.getPlayer(args[1]) == null) {
        tcMsg(sender, "${messagesConfig.getString("Messages.NoPlayer")}")
        return true
    }

    val p = Bukkit.getPlayer(args[1])

    if (p!!.inventory.firstEmpty() != -1) {
        p.sendMessage(cMsg("${messagesConfig.getString("Messages.Prefix")} ${messagesConfig.getString("Messages.BoosterPackMsg")}"))
        p.inventory.addItem(DeckManager.createBoosterPack(args[2]))
    } else if (p.gameMode == GameMode.SURVIVAL) {
        p.sendMessage(cMsg("${messagesConfig.getString("Messages.Prefix")} ${messagesConfig.getString("Messages.BoosterPackMsg")}"))
        p.world.dropItem(p.location, DeckManager.createBoosterPack(args[2]))
    }

    return true
}