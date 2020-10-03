package it.forgottenworld.tradingcards.commands.subcommands

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.data.BoosterPacks
import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.data.PluginSupport
import it.forgottenworld.tradingcards.data.Rarities
import it.forgottenworld.tradingcards.manager.CardManager
import it.forgottenworld.tradingcards.model.BoosterPack
import it.forgottenworld.tradingcards.util.sendPrefixedMessage
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

private fun cmdBuyCard(args: Array<String>, sender: CommandSender, p: Player): Boolean {

    if (args.size <= 2) {
        sendPrefixedMessage(sender, Messages.ChooseRarity)
        return true
    }

    if (args.size <= 3) {
        sendPrefixedMessage(sender, Messages.ChooseCard)
        return true
    }

    val card = Rarities[args[2]]?.get(args[3])
    if (card == null) {
        sendPrefixedMessage(sender, Messages.CardDoesntExist)
        return true
    }

    if (card.price <= 0.0) {
        sendPrefixedMessage(sender, Messages.CannotBeBought)
        return true
    }

    if (TradingCards.economy!!.getBalance(p) < card.price) {
        sendPrefixedMessage(sender, Messages.NotEnoughMoney)
        return true
    }

    if (PluginSupport.Vault.ClosedEconomy) {
        TradingCards.economy?.withdrawPlayer(p, card.price)
        TradingCards.economy?.depositPlayer(Bukkit.getOfflinePlayer(PluginSupport.Vault.ServerAccount), card.price)
    } else
        TradingCards.economy?.withdrawPlayer(p, card.price)

    if (p.inventory.firstEmpty() != -1)
        p.inventory.addItem(CardManager.createCardItemStack(card, 1))
    else if (p.gameMode == GameMode.SURVIVAL)
        p.world.dropItem(p.location, CardManager.createCardItemStack(card, 1))

    sendPrefixedMessage(sender, Messages.BoughtCard.replaceFirst("%amount%", card.price.toString()))

    return true
}

private fun cmdBuyPack(args: Array<String>, sender: CommandSender, p: Player): Boolean {

    if (args.size <= 2) {
        sendPrefixedMessage(sender, Messages.ChoosePack)
        return true
    }

    val pack = BoosterPacks[args[2]]

    if (pack == null) {
        sendPrefixedMessage(sender, Messages.PackDoesntExist)
        return true
    }

    if (pack.price <= 0.0) {
        sendPrefixedMessage(sender, Messages.CannotBeBought)
        return true
    }

    if (TradingCards.economy!!.getBalance(p) < pack.price) {
        sendPrefixedMessage(sender, Messages.NotEnoughMoney)
        return true
    }

    if (PluginSupport.Vault.ClosedEconomy) {
        TradingCards.economy?.withdrawPlayer(p, pack.price)
        TradingCards.economy
                ?.depositPlayer(Bukkit.getOfflinePlayer(PluginSupport.Vault.ServerAccount), pack.price)

    } else TradingCards.economy?.withdrawPlayer(p, pack.price)

    if (p.inventory.firstEmpty() != -1)
        p.inventory.addItem(BoosterPack.getItemStack(args[2]))
    else if (p.gameMode == GameMode.SURVIVAL)
        p.world.dropItem(p.location, BoosterPack.getItemStack(args[2]))

    sendPrefixedMessage(sender, Messages.BoughtCard.replaceFirst("%amount%", pack.price.toString()))
    return true
}

fun cmdBuy(p: Player, args: Array<String>): Boolean {

    if (!p.hasPermission("fwtradingcards.buy")) {
        sendPrefixedMessage(p, Messages.NoPerms)
        return true
    }

    if (TradingCards.economy == null) {
        sendPrefixedMessage(p, Messages.NoVault)
        return true
    }

    if (args.size <= 1) {
        sendPrefixedMessage(p, Messages.BuyUsage)
        return true
    }

    return when (args[1].toLowerCase()) {
        "pack" -> cmdBuyPack(args, p, p)
        "card" -> cmdBuyCard(args, p, p)
        else -> {
            sendPrefixedMessage(p, Messages.BuyUsage)
            true
        }
    }
}