package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.card.CardManager
import it.forgottenworld.tradingcards.deck.DeckManager
import it.forgottenworld.tradingcards.util.tcMsg
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import java.util.*

fun cmdBuyCard(cardsConfig: FileConfiguration, args: Array<String>, sender: CommandSender, messagesConfig: FileConfiguration, p: Player, config: FileConfiguration): Boolean {
    if (args.size <= 2) { tcMsg(sender, "${messagesConfig.getString("Messages.ChooseRarity")}"); return true }
    if (args.size <= 3) { tcMsg(sender, "${messagesConfig.getString("Messages.ChooseCard")}"); return true }
    if (!cardsConfig.contains("Cards.${args[2]}.${args[3]}")) {
        tcMsg(sender, "${messagesConfig.getString("Messages.CardDoesntExist")}")
        return true
    }

    var buyPrice = 0.0
    var canBuy = false

    if (cardsConfig.contains("Cards.${args[2]}.${args[3]}.Buy-Price")) {
        buyPrice = cardsConfig.getDouble("Cards.${args[2]}.${args[3]}.Buy-Price")
        canBuy = buyPrice > 0.0
    }

    if (!canBuy) {
        tcMsg(sender, "${messagesConfig.getString("Messages.CannotBeBought")}"); return true
    }
    if (TradingCards.econ!!.getBalance(p) < buyPrice) {
        tcMsg(sender, "${messagesConfig.getString("Messages.NotEnoughMoney")}")
        return true
    }

    if (config.getBoolean("PluginSupport.Vault.Closed-Economy")) {
        TradingCards.econ?.withdrawPlayer(p, buyPrice)
        TradingCards.econ
                ?.depositPlayer(Bukkit.getOfflinePlayer(UUID
                        .fromString(config.getString("PluginSupport.Vault.Server-Account"))), buyPrice)

    } else TradingCards.econ?.withdrawPlayer(p, buyPrice)

    if (p.inventory.firstEmpty() != -1)
        p.inventory.addItem(CardManager.createPlayerCard(args[3], args[2], 1, false))
    else if (p.gameMode == GameMode.SURVIVAL)
        p.world.dropItem(p.location, CardManager.createPlayerCard(args[3], args[2], 1, false))

    tcMsg(sender,
            messagesConfig.getString("Messages.BoughtCard")!!
                    .replaceFirst("%amount%", buyPrice.toString()))

    return true
}

fun cmdBuyPack(config: FileConfiguration, args: Array<String>, sender: CommandSender, messagesConfig: FileConfiguration, p: Player): Boolean {

    if (args.size <= 2) { tcMsg(sender, "${messagesConfig.getString("Messages.ChoosePack")}"); return true }
    if (!config.contains("BoosterPacks.${args[2]}")) {
        tcMsg(sender, "${messagesConfig.getString("Messages.PackDoesntExist")}")
        return true
    }


    var buyPrice = 0.0
    var canBuy = false

    if (config.contains("BoosterPacks.${args[2]}.Price")) {
        buyPrice = config.getDouble("BoosterPacks.${args[2]}.Price")
        if (buyPrice > 0.0) canBuy = true
    }

    if (!canBuy) {
        tcMsg(sender, "${messagesConfig.getString("Messages.CannotBeBought")}"); return true
    }
    if (TradingCards.econ!!.getBalance(p) < buyPrice) {
        tcMsg(sender, "${messagesConfig.getString("Messages.NotEnoughMoney")}")
        return true
    }

    if (config.getBoolean("PluginSupport.Vault.Closed-Economy")) {

        TradingCards.econ?.withdrawPlayer(p, buyPrice)
        TradingCards.econ
                ?.depositPlayer(Bukkit.getOfflinePlayer(
                        UUID.fromString(config.getString("PluginSupport.Vault.Server-Account"))), buyPrice)

    } else TradingCards.econ?.withdrawPlayer(p, buyPrice)

    if (p.inventory.firstEmpty() != -1)
        p.inventory.addItem(DeckManager.createBoosterPack(args[2]))
    else if (p.gameMode == GameMode.SURVIVAL)
        p.world.dropItem(p.location, DeckManager.createBoosterPack(args[2]))

    tcMsg(sender, messagesConfig.getString("Messages.BoughtCard")!!.replaceFirst("%amount%", buyPrice.toString()))
    return true
}

fun cmdBuy(p: Player, args: Array<String>, config: FileConfiguration, cardsConfig: FileConfiguration, messagesConfig: FileConfiguration): Boolean {

    if (!p.hasPermission("fwtc.buy")) {
        tcMsg(p, "${messagesConfig.getString("Messages.NoPerms")}")
        return true
    }

    if (!TradingCards.instance.hasVault) {
        tcMsg(p, "${messagesConfig.getString("Messages.NoVault")}")
        return true
    }

    if (args.size <= 1) {
        tcMsg(p, "${messagesConfig.getString("Messages.BuyUsage")}")
        return true
    }

    return when (args[1].toLowerCase()) {
        "pack" -> cmdBuyPack(config, args, p, messagesConfig, p)
        "card" -> cmdBuyCard(cardsConfig, args, p, messagesConfig, p, config)
        else -> {
            tcMsg(p, "${messagesConfig.getString("Messages.BuyUsage")}")
            true
        }
    }
}