package it.forgottenworld.tradingcards.commands.subcommands

import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.data.Rarities
import it.forgottenworld.tradingcards.manager.CardManager
import it.forgottenworld.tradingcards.util.tC
import it.forgottenworld.tradingcards.util.sendPrefixedMessage
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.CommandSender

fun cmdGiveaway(sender: CommandSender, args: Array<String>): Boolean {

    if (!sender.hasPermission("fwtradingcards.giveaway")) {
        sendPrefixedMessage(sender, Messages.NoPerms)
        return true
    }

    if (args.size <= 1) {
        sendPrefixedMessage(sender, Messages.GiveawayUsage)
        return true
    }

    if (!Rarities.contains(args[1])) {
        sendPrefixedMessage(sender, Messages.NoRarity)
        return true
    }

    Bukkit.broadcastMessage(tC(
            "${Messages.Prefix} ${
                Messages.Giveaway
                        .replaceFirst("%player%", sender.name)
                        .replaceFirst("%rarity%", args[1])
            }"))

    for (p in Bukkit.getOnlinePlayers()) {
        val card = Rarities[args[1]]!!.values.random()
        if (p.inventory.firstEmpty() != -1)
            p.inventory.addItem(CardManager.createCardItemStack(card, 1))
        else if (p.gameMode == GameMode.SURVIVAL)
            p.world.dropItem(p.location, CardManager.createCardItemStack(card, 1))
    }

    return true
}