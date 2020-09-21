package it.forgottenworld.tradingcards.commands

import it.forgottenworld.tradingcards.util.cMsg
import it.forgottenworld.tradingcards.util.tcMsg
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration

fun cmdListPacks(sender: CommandSender, config: FileConfiguration, messagesConfig: FileConfiguration): Boolean {

    if (!sender.hasPermission("fwtc.listpacks")) {
        tcMsg(sender, "${messagesConfig.getString("Messages.NoPerms")}")
        return true
    }

    val cardKeys = config.getConfigurationSection("BoosterPacks")!!.getKeys(false)
    sender.sendMessage(cMsg("&6--- Booster Packs ---"))

    cardKeys.forEachIndexed { i, key ->

        val hasPrice = config.contains("BoosterPacks.$key.Price")
        val hasExtra = config.contains("BoosterPacks.$key.ExtraCardRarity") && config.contains("BoosterPacks.$key.NumExtraCards")

        if (hasPrice)
            sender.sendMessage(cMsg("&6${i + 1}) &e$key &7(&aPrice: ${config.getDouble("BoosterPacks.${key}.Price&7")})"))
        else
            sender.sendMessage(cMsg("&6${i + 1}) &e$key"))

        sender.sendMessage(cMsg(if (hasExtra) "  &7- &f&o${config.getInt("BoosterPacks.$key.NumNormalCards")} ${
            config.getString("BoosterPacks.$key.NormalCardRarity")}, ${
            config.getInt("BoosterPacks.$key.NumExtraCards")} ${
            config.getString("BoosterPacks.$key.ExtraCardRarity")}, ${
            config.getInt("BoosterPacks.$key.NumSpecialCards")} ${
            config.getString("BoosterPacks.$key.SpecialCardRarity")}"
        else "  &7- &f&o${
            config.getInt("BoosterPacks.$key.NumNormalCards")} ${
            config.getString("BoosterPacks.$key.NormalCardRarity")}, ${
            config.getInt("BoosterPacks.$key.NumSpecialCards")} ${
            config.getString("BoosterPacks.$key.SpecialCardRarity")}"))

    }

    return true
}