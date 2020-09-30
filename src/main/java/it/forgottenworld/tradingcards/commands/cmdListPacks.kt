package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.data.BoosterPacks
import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.util.cMsg
import it.forgottenworld.tradingcards.util.tcMsg
import org.bukkit.command.CommandSender

fun cmdListPacks(sender: CommandSender): Boolean {

    if (!sender.hasPermission("fwtc.listpacks")) {
        tcMsg(sender, Messages.NoPerms)
        return true
    }

    sender.sendMessage(cMsg("&6--- Booster Packs ---"))

    BoosterPacks.values.forEachIndexed { i, b ->

        sender.sendMessage(cMsg(
                "&6${i + 1}) &e$b${
                    if (b.price > 0.0) " &7(&aPrice: ${b.price})" else ""}"))

        sender.sendMessage(cMsg(
                "  &7- &f&o${b.numNormalCards} ${b.normalCardRarity.name}, ${
                    if (b.numExtraCards > 0) 
                        "${b.numExtraCards} ${b.extraCardRarity?.name}, " 
                    else ""}, ${
                    b.numSpecialCards} ${b.specialCardRarity}")
        )
    }

    return true
}