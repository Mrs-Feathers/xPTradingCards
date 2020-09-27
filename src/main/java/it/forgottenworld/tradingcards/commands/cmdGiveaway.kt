package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.card.CardManager
import it.forgottenworld.tradingcards.util.cMsg
import it.forgottenworld.tradingcards.util.tcMsg
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration

fun cmdGiveaway(sender: CommandSender, args: Array<String>, cardsConfig: FileConfiguration, messagesConfig: FileConfiguration): Boolean {
    if (!sender.hasPermission("fwtc.giveaway")) {
        tcMsg(sender, "${messagesConfig.getString("Messages.NoPerms")}")
        return true
    }
    if (args.size <= 1) {
        tcMsg(sender, "${messagesConfig.getString("Messages.GiveawayUsage")}"); return true
    }

    val rarityKeys = cardsConfig.getConfigurationSection("Cards")!!.getKeys(false)
    val keyToUse = rarityKeys.find { it.equals(args[1].replace("_", " "), ignoreCase = true) } ?: ""

    if (keyToUse.isEmpty()) {
        tcMsg(sender, "${messagesConfig.getString("Messages.NoRarity")}"); return true
    }

    Bukkit.broadcastMessage(cMsg(
            "${messagesConfig.getString("Messages.Prefix")} ${
                messagesConfig.getString("Messages.Giveaway")!!
                        .replaceFirst("%player%", sender.name)
                        .replaceFirst("%rarity%", keyToUse)
            }"))

    val cardKeys = cardsConfig.getConfigurationSection("Cards.$keyToUse")!!.getKeys(false).toList()
    for (p in Bukkit.getOnlinePlayers()) {
        val cardName = cardKeys.random()
        if (p.inventory.firstEmpty() != -1)
            p.inventory.addItem(CardManager.createPlayerCard(cardName, keyToUse, 1, false))
        else if (p.gameMode == GameMode.SURVIVAL)
            p.world.dropItem(p.location, CardManager.createPlayerCard(cardName, keyToUse, 1, false))
    }

    return true
}