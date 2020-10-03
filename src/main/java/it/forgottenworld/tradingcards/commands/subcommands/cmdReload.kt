package it.forgottenworld.tradingcards.commands.subcommands

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.data.General
import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.util.sendPrefixedMessage
import org.bukkit.command.CommandSender

fun cmdReload(sender: CommandSender): Boolean {

    if (sender.hasPermission("fwtradingcards.reload")) {
        sendPrefixedMessage(sender, Messages.Reload)
        Config.reloadAllConfigs()
        if (General.ScheduleCards) TradingCards.instance.task.startTimer()
        return true
    }

    sendPrefixedMessage(sender, Messages.NoPerms)
    return false
}