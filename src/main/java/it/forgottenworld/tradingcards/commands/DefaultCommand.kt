package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.data.General
import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.util.cMsg
import it.forgottenworld.tradingcards.util.formatTitle
import it.forgottenworld.tradingcards.util.tcMsg
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class DefaultCommand : CommandExecutor, TabExecutor {

    data class UsageItem(
            val perm: String,
            val usage: String,
            val help: String,
            val additionalCondition: Boolean = true)

    private val commandUsages = setOf(
            UsageItem("reload", Messages.ReloadUsage, Messages.ReloadHelp),
            UsageItem("givecard", Messages.GiveCardUsage, Messages.GiveCardHelp),
            UsageItem("giveshinycard", Messages.GiveShinyCardUsage, Messages.GiveShinyCardHelp),
            UsageItem("giverandomcard", Messages.GiveRandomCardUsage, Messages.GiveRandomCardHelp),
            UsageItem("giveboosterpack", Messages.GiveBoosterPackUsage, Messages.GiveBoosterPackHelp),
            UsageItem("giveaway", Messages.GiveawayUsage, Messages.GiveawayHelp),
            UsageItem("getdeck", Messages.GetDeckUsage, Messages.GetDeckHelp),
            UsageItem("list", Messages.ListUsage, Messages.ListHelp),
            UsageItem("listpacks", Messages.ListPacksUsage, Messages.ListPacksHelp),
            UsageItem("toggle", Messages.ToggleUsage, Messages.ToggleHelp),
            UsageItem("create", Messages.CreateUsage, Messages.CreateHelp),
            UsageItem("buy", "BuyUsage", Messages.BuyHelp, TradingCards.instance.hasVault),
            UsageItem("worth", Messages.WorthUsage, Messages.WorthHelp, TradingCards.instance.hasVault))

    private fun showUsage(sender: CommandSender) {

        val showUsage = General.ShowCommandUsage

        sender.sendMessage(cMsg(formatTitle("${General.ServerName} Trading Cards")))

        commandUsages
                .filter { sender.hasPermission("fwtc.${it.perm}") }
                .forEach {
                    sender.sendMessage(cMsg("&7> &3${it.usage}"))

                    if (showUsage)
                        sender.sendMessage(cMsg("   &7- &f&o${it.help}"))
                }
    }

    private val subCommands = setOf(
            "reload",
            "toggle",
            "create",
            "givecard",
            "giveshinycard",
            "giveboosterpack",
            "getdeck",
            "giverandomcard",
            "list",
            "listpacks",
            "giveaway",
            "worth",
            "credits",
            "buy")

    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<String>) =
            subCommands.filter { it.startsWith(args[0]) }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {

        if (args.isEmpty()) {
            showUsage(sender)
            return true
        }

        return when(args[0].toLowerCase()) {
            "reload" -> cmdReload(sender)
            "toggle" -> sender is Player && cmdToggle(sender)
            "create" -> sender is Player && cmdCreate(sender, args)
            "givecard" -> sender is Player && cmdGiveCard(sender, args)
            "giveshinycard" -> sender is Player && cmdGiveShinyCard(sender, args)
            "giveboosterpack" -> cmdGiveBoosterPack(sender, args)
            "getdeck" -> sender is Player && cmdGetDeck(sender, args)
            "giverandomcard" -> cmdGiveRandomCard(sender, args)
            "list" -> cmdList(sender)
            "listpacks" -> cmdListPacks(sender)
            "giveaway"-> cmdGiveaway(sender, args)
            "worth" -> sender is Player && cmdWorth(sender)
            "credits" -> cmdCredits(sender)
            "buy" -> sender is Player && cmdBuy(sender, args)
            else -> { 
                tcMsg(sender, Messages.NoCmd)
                true 
            }
        }
        
    }

}