package it.forgottenworld.tradingcards.commands.subcommands

import it.forgottenworld.tradingcards.util.tc
import it.forgottenworld.tradingcards.util.formatTitle
import org.bukkit.command.CommandSender

fun cmdCredits(sender: CommandSender): Boolean {

    sender.sendMessage(tc(formatTitle("Credits and Special Thanks")))
    sender.sendMessage(tc("&7[&aDeveloper&7] &aLukas Xenoyia Gentle"))
    sender.sendMessage(tc("   &7- &6&oxPXenoyia&f, &6&oXenoyia&f, &6&oxPLukas&f, &6&oSnoopDogg&f"))
    sender.sendMessage(tc("&7[&eSpecial Thanks&7] XpanD, IrChaos, xtechgamer735, PTsandro, " +
            "FlyingSquidwolf, iXRaZoRXi, iToxy, TowelieDOH, Miku_Snow, NOBUTSS, doitliketyler, " +
            "Celebrimbor90, Magz, GypsySix, bumbble, iFosadrink_2, Sunique, TheRealGSD, Zenko, " +
            "Berkth, TubeCraftXXL, Cra2ytig3r, marcosds13, ericbarbwire, Bonzo"))
    return true

}