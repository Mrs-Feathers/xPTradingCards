package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.model.BoosterPack
import it.forgottenworld.tradingcards.util.cMsg
import it.forgottenworld.tradingcards.util.tcMsg
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.CommandSender

fun cmdGiveBoosterPack(sender: CommandSender, args: Array<String>): Boolean {

    if (!sender.hasPermission("fwtc.giveboosterpack")) {
        tcMsg(sender, Messages.NoPerms)
        return true
    }

    if (args.size <= 2) {
        tcMsg(sender, Messages.GiveBoosterPackUsage)
        return true
    }

    if (!Config.PLUGIN.contains("BoosterPacks.${args[2].replace(" ", "_")}")) {
        tcMsg(sender, Messages.NoBoosterPack)
        return true
    }

    if (Bukkit.getPlayer(args[1]) == null) {
        tcMsg(sender, Messages.NoPlayer)
        return true
    }

    val p = Bukkit.getPlayer(args[1])

    if (p!!.inventory.firstEmpty() != -1) {
        p.sendMessage(cMsg("${Messages.Prefix} ${Messages.BoosterPackMsg}"))
        p.inventory.addItem(BoosterPack.getItemStack(args[2]))
    } else if (p.gameMode == GameMode.SURVIVAL) {
        p.sendMessage(cMsg("${Messages.Prefix} ${Messages.BoosterPackMsg}"))
        p.world.dropItem(p.location, BoosterPack.getItemStack(args[2]))
    }

    return true
}