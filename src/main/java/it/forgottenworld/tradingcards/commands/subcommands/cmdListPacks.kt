package it.forgottenworld.tradingcards.commands.subcommands

import it.forgottenworld.tradingcards.data.BoosterPacks
import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.util.tc
import it.forgottenworld.tradingcards.util.tcMsg
import org.bukkit.command.CommandSender

fun cmdListPacks(sender: CommandSender): Boolean {

    if (!sender.hasPermission("fwtradingcards.listpacks")) {
        tcMsg(sender, Messages.NoPerms)
        return true
    }

    sender.sendMessage(tc("&6--- Booster Packs ---"))

    BoosterPacks.values.forEachIndexed { i, b ->

        sender.sendMessage(tc(
                "&6${i + 1}) &e$b${
                    if (b.price > 0.0) " &7(&aPrice: ${b.price})" else ""}"))

        sender.sendMessage(tc(
                "  &7- &f&o${b.numNormalCards} ${b.normalCardRarity.name}, ${
                    if (b.numExtraCards > 0) 
                        "${b.numExtraCards} ${b.extraCardRarity?.name}, " 
                    else ""}, ${
                    b.numSpecialCards} ${b.specialCardRarity}")
        )
    }

    return true
}