package it.forgottenworld.tradingcards.commands.subcommands

import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.model.BoosterPack
import it.forgottenworld.tradingcards.util.tC
import it.forgottenworld.tradingcards.util.sendPrefixedMessage
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.CommandSender

fun cmdGiveBoosterPack(sender: CommandSender, args: Array<String>): Boolean {

    if (!sender.hasPermission("fwtradingcards.giveboosterpack")) {
        sendPrefixedMessage(sender, Messages.NoPerms)
        return true
    }

    if (args.size <= 2) {
        sendPrefixedMessage(sender, Messages.GiveBoosterPackUsage)
        return true
    }

    if (!Config.PLUGIN.contains("BoosterPacks.${args[2].replace(" ", "_")}")) {
        sendPrefixedMessage(sender, Messages.NoBoosterPack)
        return true
    }

    if (Bukkit.getPlayer(args[1]) == null) {
        sendPrefixedMessage(sender, Messages.NoPlayer)
        return true
    }

    val p = Bukkit.getPlayer(args[1])

    if (p!!.inventory.firstEmpty() != -1) {
        p.sendMessage(tC("${Messages.Prefix} ${Messages.BoosterPackMsg}"))
        p.inventory.addItem(BoosterPack.getItemStack(args[2]))
    } else if (p.gameMode == GameMode.SURVIVAL) {
        p.sendMessage(tC("${Messages.Prefix} ${Messages.BoosterPackMsg}"))
        p.world.dropItem(p.location, BoosterPack.getItemStack(args[2]))
    }

    return true
}