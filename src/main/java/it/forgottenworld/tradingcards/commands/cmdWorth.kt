package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.util.tcMsg
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

fun cmdWorth(p: Player, config: FileConfiguration, cardsConfig: FileConfiguration, messagesConfig: FileConfiguration): Boolean {

    if (!p.hasPermission("fwtc.worth")) {
        tcMsg(p, "${messagesConfig.getString("Messages.NoPerms")}")
        return true
    }

    if (!TradingCards.instance.hasVault) {
        tcMsg(p, "${messagesConfig.getString("Messages.NoVault")}")
        return true
    }

    if (p.inventory.getItem(p.inventory.heldItemSlot)?.type != Material.valueOf(config.getString("General.Card-Material")!!)) {
        tcMsg(p, "${messagesConfig.getString("Messages.NotACard")}")
        return true
    }

    val itemInHand = p.inventory.getItem(p.inventory.heldItemSlot)
    val itemName = itemInHand!!.itemMeta!!.displayName

    if (Config.DEBUG) {
        println(itemName)
        println(ChatColor.stripColor(itemName))
    }

    val splitName = ChatColor.stripColor(itemName)!!.split(" ")
    val cardName = splitName[if (splitName.size > 1) 1 else 0]
    val rarity = ChatColor.stripColor(itemInHand.itemMeta!!.lore!![3])

    if (Config.DEBUG) {
        println(cardName)
        println(rarity)
    }

    var canBuy = false
    var buyPrice = 0.0

    if (cardsConfig.contains("Cards.$rarity.$cardName.Buy-Price")) {
        buyPrice = cardsConfig.getDouble("Cards.$rarity.$cardName.Buy-Price")
        canBuy = buyPrice > 0.0
    }

    if (canBuy)
        tcMsg(p, messagesConfig.getString("Messages.CanBuy")!!.replaceFirst("%buyAmount%", buyPrice.toString()))
    else
        tcMsg(p, "${messagesConfig.getString("Messages.CanNotBuy")}")

    return true
}