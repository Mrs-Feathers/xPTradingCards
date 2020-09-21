package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.card.CardManager
import it.forgottenworld.tradingcards.util.*
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.EntityType

fun cmdGiveRandomCard(sender: CommandSender, args: Array<String>, messagesConfig: FileConfiguration): Boolean {

    if (!sender.hasPermission("fwtc.randomcard")) {
        tcMsg(sender, "${messagesConfig.getString("Messages.NoPerms")}")
        return true
    }

    if (args.size <= 2) {
        tcMsg(sender, "${messagesConfig.getString("Messages.GiveRandomCardUsage")}")
        return true
    }

    if (Bukkit.getPlayer(args[2]) == null) {
        tcMsg(sender, "${messagesConfig.getString("Messages.NoPlayer")}")
        return true
    }

    val p = Bukkit.getPlayer(args[2])
    try {
        val rare = calculateRarity(EntityType.valueOf(args[1].toUpperCase()), true)

        printDebug("[Cards] onCommand.rare: $rare")
        tcMsg(sender, messagesConfig.getString("Messages.GiveRandomCardMsg")!!.replace("%player%", p!!.name))

        if (p.inventory.firstEmpty() != -1) {

            p.sendMessage(cMsg("${messagesConfig
                    .getString("Messages.Prefix")} ${messagesConfig
                    .getString("Messages.GiveRandomCard")}"))

            CardManager.generateCard(rare)?.let { p.inventory.addItem(it) }

        } else if (p.gameMode == GameMode.SURVIVAL) {

            p.sendMessage(cMsg("${messagesConfig
                    .getString("Messages.Prefix")} ${messagesConfig
                    .getString("Messages.GiveRandomCard")}"))

            CardManager.generateCard(rare)?.let { p.world.dropItem(p.location, it) }

        }
    } catch (e: IllegalArgumentException) {
        tcMsg(sender, "${messagesConfig.getString("Messages.NoEntity")}")
    }

    return true
}