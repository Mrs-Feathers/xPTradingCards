package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.config.ConfigManager
import it.forgottenworld.tradingcards.util.tcMsg
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration

fun cmdReload(sender: CommandSender, config: FileConfiguration, messagesConfig: FileConfiguration): Boolean {
    if (sender.hasPermission("fwtc.reload")) {
        tcMsg(sender, "${messagesConfig.getString("Messages.Reload")}")
        ConfigManager.reloadAllConfigs()
        if (config.getBoolean("General.Schedule-Cards")) TradingCards.instance.task.startTimer()
        return true
    }
    tcMsg(sender, "${messagesConfig.getString("Messages.NoPerms")}")
    return false
}