package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.config.Messages
import it.forgottenworld.tradingcards.util.cMsg
import it.forgottenworld.tradingcards.util.formatTitle
import it.forgottenworld.tradingcards.util.tcMsg
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DefaultCommand : CommandExecutor {

    data class UsageItem(
            val perm: String,
            val msg: String,
            val help: String,
            val additionalCondition: Boolean = true)

    private val commandUsages = setOf(
            UsageItem("reload", "ReloadUsage", "ReloadHelp"),
            UsageItem("givecard", "GiveCardUsage", "GiveCardHelp"),
            UsageItem("giveshinycard", "GiveShinyCardUsage", "GiveShinyCardHelp"),
            UsageItem("giverandomcard", "GiveRandomCardUsage", "GiveRandomCardHelp"),
            UsageItem("giveboosterpack", "GiveBoosterPackUsage", "GiveBoosterPackHelp"),
            UsageItem("giveaway", "GiveawayUsage", "GiveawayHelp"),
            UsageItem("getdeck", "GetDeckUsage", "GetDeckHelp"),
            UsageItem("list", "ListUsage", "ListHelp"),
            UsageItem("listpacks", "ListPacksUsage", "ListPacksHelp"),
            UsageItem("toggle", "ToggleUsage", "ToggleHelp"),
            UsageItem("create", "CreateUsage", "CreateHelp"),
            UsageItem("buy", "BuyUsage", "BuyHelp", TradingCards.instance.hasVault),
            UsageItem("worth", "WorthUsage", "WorthHelp", TradingCards.instance.hasVault))

    private fun showUsage(sender: CommandSender) {

        val showUsage = Config.PLUGIN.getBoolean("General.Show-Command-Usage", true)

        sender.sendMessage(cMsg(formatTitle("${Config.PLUGIN.getString("General.Server-Name")} Trading Cards")))

        commandUsages
                .filter { sender.hasPermission("fwtc.${it.perm}") }
                .forEach {
                    sender.sendMessage(cMsg("&7> &3${Config.MESSAGES.getString("Messages.${it.msg}")}"))

                    if (showUsage)
                        sender.sendMessage(cMsg("   &7- &f&o${Config.MESSAGES.getString("Messages.${it.help}")}"))
                }
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {

        if (args.isEmpty()) {
            showUsage(sender)
            return true
        }

        return when(args[0].toLowerCase()) {
            "reload" -> cmdReload(sender, Config.PLUGIN)
            "toggle" -> sender is Player && cmdToggle(sender)
            "create" -> sender is Player && cmdCreate(sender, args)
            "givecard" -> sender is Player && cmdGiveCard(sender, args, Config.CARDS)
            "giveshinycard" -> sender is Player && cmdGiveShinyCard(sender, args, Config.CARDS)
            "giveboosterpack" -> cmdGiveBoosterPack(sender, args)
            "getdeck" -> sender is Player && cmdGetDeck(sender, args)
            "giverandomcard" -> cmdGiveRandomCard(sender, args)
            "list" -> cmdList(sender, Config.CARDS)
            "listpacks" -> cmdListPacks(sender, Config.PLUGIN)
            "giveaway"-> cmdGiveaway(sender, args, Config.CARDS)
            "worth" -> sender is Player && cmdWorth(sender, Config.PLUGIN, Config.CARDS)
            "credits" -> cmdCredits(sender)
            "buy" -> sender is Player && cmdBuy(sender, args, Config.PLUGIN, Config.CARDS)
            else -> { 
                tcMsg(sender, Messages.NoCmd)
                true 
            }
        }
        
    }

}