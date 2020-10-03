package it.forgottenworld.tradingcards.commands.subcommands

import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.manager.CardManager
import it.forgottenworld.tradingcards.model.Rarity
import it.forgottenworld.tradingcards.util.sendPrefixedMessage
import it.forgottenworld.tradingcards.util.tC
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType

fun cmdGiveRandomCard(sender: CommandSender, args: Array<String>): Boolean {

    if (!sender.hasPermission("fwtradingcards.randomcard")) {
        sendPrefixedMessage(sender, Messages.NoPerms)
        return true
    }

    if (args.size <= 2) {
        sendPrefixedMessage(sender, Messages.GiveRandomCardUsage)
        return true
    }

    if (Bukkit.getPlayer(args[2]) == null) {
        sendPrefixedMessage(sender, Messages.NoPlayer)
        return true
    }

    val p = Bukkit.getPlayer(args[2])
    try {
        val rare = Rarity.calculate(EntityType.valueOf(args[1].toUpperCase()), true) ?: return true

        sendPrefixedMessage(sender, Messages.GiveRandomCardMsg.replaceFirst("%player%", p!!.name))

        if (p.inventory.firstEmpty() != -1) {
            p.sendMessage(tC("${Messages.Prefix} ${Messages.GiveRandomCard}"))
            CardManager.createRandomCardItemStack(rare).let { p.inventory.addItem(it) }
        } else if (p.gameMode == GameMode.SURVIVAL) {
            p.sendMessage(tC("${Messages.Prefix} ${Messages.GiveRandomCard}"))
            CardManager.createRandomCardItemStack(rare).let { p.world.dropItem(p.location, it) }
        }
    } catch (e: IllegalArgumentException) {
        sendPrefixedMessage(sender, Messages.NoEntity)
    }

    return true
}