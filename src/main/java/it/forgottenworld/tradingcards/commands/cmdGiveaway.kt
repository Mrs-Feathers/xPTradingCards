package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.data.Rarities
import it.forgottenworld.tradingcards.manager.CardManager
import it.forgottenworld.tradingcards.util.cMsg
import it.forgottenworld.tradingcards.util.tcMsg
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.CommandSender

fun cmdGiveaway(sender: CommandSender, args: Array<String>): Boolean {

    if (!sender.hasPermission("fwtc.giveaway")) {
        tcMsg(sender, Messages.NoPerms)
        return true
    }

    if (args.size <= 1) {
        tcMsg(sender, Messages.GiveawayUsage)
        return true
    }

    if (!Rarities.contains(args[1])) {
        tcMsg(sender, Messages.NoRarity)
        return true
    }

    Bukkit.broadcastMessage(cMsg(
            "${Messages.Prefix} ${
                Messages.Giveaway
                        .replaceFirst("%player%", sender.name)
                        .replaceFirst("%rarity%", args[1])
            }"))

    for (p in Bukkit.getOnlinePlayers()) {
        val card = Rarities[args[1]]!!.cards.values.random()
        if (p.inventory.firstEmpty() != -1)
            p.inventory.addItem(CardManager.getCardItemStack(card, 1))
        else if (p.gameMode == GameMode.SURVIVAL)
            p.world.dropItem(p.location, CardManager.getCardItemStack(card, 1))
    }

    return true
}