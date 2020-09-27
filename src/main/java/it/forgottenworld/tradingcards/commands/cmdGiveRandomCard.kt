package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.card.CardManager
import it.forgottenworld.tradingcards.config.Messages
import it.forgottenworld.tradingcards.util.*
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType

fun cmdGiveRandomCard(sender: CommandSender, args: Array<String>): Boolean {

    if (!sender.hasPermission("fwtc.randomcard")) {
        tcMsg(sender, Messages.NoPerms)
        return true
    }

    if (args.size <= 2) {
        tcMsg(sender, Messages.GiveRandomCardUsage)
        return true
    }

    if (Bukkit.getPlayer(args[2]) == null) {
        tcMsg(sender, Messages.NoPlayer)
        return true
    }

    val p = Bukkit.getPlayer(args[2])
    try {
        val rare = calculateRarity(EntityType.valueOf(args[1].toUpperCase()), true)

        printDebug("[Cards] onCommand.rare: $rare")
        tcMsg(sender, Messages.GiveRandomCardMsg.replaceFirst("%player%", p!!.name))

        if (p.inventory.firstEmpty() != -1) {

            p.sendMessage(cMsg("${Messages.Prefix} ${Messages.GiveRandomCard}"))

            CardManager.generateCard(rare)?.let { p.inventory.addItem(it) }

        } else if (p.gameMode == GameMode.SURVIVAL) {

            p.sendMessage(cMsg("${Messages.Prefix} ${Messages.GiveRandomCard}"))

            CardManager.generateCard(rare)?.let { p.world.dropItem(p.location, it) }

        }
    } catch (e: IllegalArgumentException) {
        tcMsg(sender, Messages.NoEntity)
    }

    return true
}