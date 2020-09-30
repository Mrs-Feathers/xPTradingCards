package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.data.BoosterPacks
import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.data.PluginSupport
import it.forgottenworld.tradingcards.data.Rarities
import it.forgottenworld.tradingcards.manager.CardManager
import it.forgottenworld.tradingcards.manager.DeckManager
import it.forgottenworld.tradingcards.model.BoosterPack
import it.forgottenworld.tradingcards.util.tcMsg
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

private fun cmdBuyCard(args: Array<String>, sender: CommandSender, p: Player): Boolean {

    if (args.size <= 2) {
        tcMsg(sender, Messages.ChooseRarity)
        return true
    }

    if (args.size <= 3) {
        tcMsg(sender, Messages.ChooseCard)
        return true
    }

    val card = Rarities[args[2]]?.cards?.get(args[3])
    if (card == null) {
        tcMsg(sender, Messages.CardDoesntExist)
        return true
    }

    if (card.price <= 0.0) {
        tcMsg(sender, Messages.CannotBeBought)
        return true
    }

    if (TradingCards.econ!!.getBalance(p) < card.price) {
        tcMsg(sender, Messages.NotEnoughMoney)
        return true
    }

    if (PluginSupport.Vault.ClosedEconomy) {
        TradingCards.econ?.withdrawPlayer(p, card.price)
        TradingCards.econ?.depositPlayer(Bukkit.getOfflinePlayer(PluginSupport.Vault.ServerAccount), card.price)
    } else
        TradingCards.econ?.withdrawPlayer(p, card.price)

    if (p.inventory.firstEmpty() != -1)
        p.inventory.addItem(CardManager.getCardItemStack(card, 1))
    else if (p.gameMode == GameMode.SURVIVAL)
        p.world.dropItem(p.location, CardManager.getCardItemStack(card, 1))

    tcMsg(sender, Messages.BoughtCard.replaceFirst("%amount%", card.price.toString()))

    return true
}

private fun cmdBuyPack(args: Array<String>, sender: CommandSender, p: Player): Boolean {

    if (args.size <= 2) { tcMsg(sender, Messages.ChoosePack); return true }

    val pack = BoosterPacks[args[2]]

    if (pack == null) {
        tcMsg(sender, Messages.PackDoesntExist)
        return true
    }

    if (pack.price <= 0.0) {
        tcMsg(sender, Messages.CannotBeBought)
        return true
    }

    if (TradingCards.econ!!.getBalance(p) < pack.price) {
        tcMsg(sender, Messages.NotEnoughMoney)
        return true
    }

    if (PluginSupport.Vault.ClosedEconomy) {
        TradingCards.econ?.withdrawPlayer(p, pack.price)
        TradingCards.econ
                ?.depositPlayer(Bukkit.getOfflinePlayer(PluginSupport.Vault.ServerAccount), pack.price)

    } else TradingCards.econ?.withdrawPlayer(p, pack.price)

    if (p.inventory.firstEmpty() != -1)
        p.inventory.addItem(BoosterPack.getItemStack(args[2]))
    else if (p.gameMode == GameMode.SURVIVAL)
        p.world.dropItem(p.location, BoosterPack.getItemStack(args[2]))

    tcMsg(sender, Messages.BoughtCard.replaceFirst("%amount%", pack.price.toString()))
    return true
}

fun cmdBuy(p: Player, args: Array<String>): Boolean {

    if (!p.hasPermission("fwtc.buy")) {
        tcMsg(p, Messages.NoPerms)
        return true
    }

    if (!TradingCards.instance.hasVault) {
        tcMsg(p, Messages.NoVault)
        return true
    }

    if (args.size <= 1) {
        tcMsg(p, Messages.BuyUsage)
        return true
    }

    return when (args[1].toLowerCase()) {
        "pack" -> cmdBuyPack(args, p, p)
        "card" -> cmdBuyCard(args, p, p)
        else -> {
            tcMsg(p, Messages.BuyUsage)
            true
        }
    }
}