package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.config.Messages
import it.forgottenworld.tradingcards.util.cMsg
import it.forgottenworld.tradingcards.util.tcMsg
import org.apache.commons.lang3.StringUtils
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration

fun cmdList(sender: CommandSender, cardsConfig: FileConfiguration): Boolean {

    if (!sender.hasPermission("fwtc.list")) {
        tcMsg(sender, Messages.NoPerms)
        return true
    }

    for (key in cardsConfig.getConfigurationSection("Cards")!!.getKeys(false)) {

        val keyKeys = cardsConfig.getConfigurationSection("Cards.$key")!!.getKeys(false)

        var msg = ""
        var finalMsg = ""
        var i = 0
        for (key2 in keyKeys) {
            if (i++ > 41)
                finalMsg = "$msg&7and more!"
            else msg = "$msg&7${key2.replace("_", " ")}&f, "
        }

        sender.sendMessage(cMsg("&6--- $key &7(&f$i&7)&6 ---"))
        sender.sendMessage(cMsg(if (finalMsg.isEmpty()) StringUtils.removeEnd(msg, ", ") else finalMsg))
    }

    return true
}