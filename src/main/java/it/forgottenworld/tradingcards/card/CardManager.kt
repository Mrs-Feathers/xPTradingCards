package it.forgottenworld.tradingcards.card

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.config.ConfigManager
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.*
import it.forgottenworld.tradingcards.util.Utils.Companion.cMsg
import it.forgottenworld.tradingcards.util.Utils.Companion.wrapString

class CardManager() {

    private val configManager = TradingCards.configManager

    fun createPlayerCard(cardName: String, rarity: String, num: Int, forcedShiny: Boolean): ItemStack {
        val config = configManager.pluginConfig.config!!
        val card = getBlankCard(num)
        val hasShinyVersion = configManager.cardsConfig.config!!.getBoolean("Cards.$rarity.$cardName.Has-Shiny-Version")
        var isShiny = false
        if (hasShinyVersion) {
            val shinyRandom = Random().nextInt(100) + 1
            if (shinyRandom <= config.getInt("Chances.Shiny-Version-Chance")) isShiny = true
        }
        if (forcedShiny) isShiny = true
        val rarityColour = config.getString("Rarities.$rarity.Colour")!!
        val prefix = config.getString("General.Card-Prefix")!!
        val series = configManager.cardsConfig.config!!.getString("Cards.$rarity.$cardName.Series")!!
        val seriesColour = config.getString("Colours.Series")!!
        val seriesDisplay = config.getString("DisplayNames.Cards.Series", "Series")!!
        val about = configManager.cardsConfig.config!!.getString("Cards.$rarity.$cardName.About", "None")!!
        val aboutColour = config.getString("Colours.About")!!
        val aboutDisplay = config.getString("DisplayNames.Cards.About", "About")!!
        val type = configManager.cardsConfig.config!!.getString("Cards.$rarity.$cardName.Type")!!
        val typeColour = config.getString("Colours.Type")!!
        val typeDisplay = config.getString("DisplayNames.Cards.Type", "Type")!!
        val info = configManager.cardsConfig.config!!.getString("Cards.$rarity.$cardName.Info")!!
        val infoColour = config.getString("Colours.Info")!!
        val infoDisplay = config.getString("DisplayNames.Cards.Info", "Info")!!
        val shinyPrefix = config.getString("General.Shiny-Name")!!
        val cost: String = if (configManager.cardsConfig.config!!.contains("Cards.$rarity.$cardName.Buy-Price")) configManager.cardsConfig.config!!.getDouble("Cards.$rarity.$cardName.Buy-Price").toString() else "None"
        val cmeta = card.itemMeta
        if (isShiny) {
            cmeta!!.setDisplayName(cMsg(config.getString("DisplayNames.Cards.ShinyTitle")!!.replace("%PREFIX%".toRegex(), prefix).replace("%COLOUR%".toRegex(), rarityColour).replace("%NAME%".toRegex(), cardName).replace("%COST%".toRegex(), cost).replace("%SHINYPREFIX%".toRegex(), shinyPrefix).replace("_".toRegex(), " ")))
        } else cmeta!!.setDisplayName(cMsg(config.getString("DisplayNames.Cards.Title")!!.replace("%PREFIX%".toRegex(), prefix).replace("%COLOUR%".toRegex(), rarityColour).replace("%NAME%".toRegex(), cardName).replace("%COST%".toRegex(), cost).replace("_".toRegex(), " ")))
        val lore: MutableList<String?> = mutableListOf()
        lore.add(cMsg("$typeColour$typeDisplay: &f$type"))
        if (info == "None" || info == "") {
            lore.add(cMsg("$infoColour$infoDisplay: &f$info"))
        } else {
            lore.add(cMsg("$infoColour$infoDisplay:"))
            lore.addAll(wrapString(info))
        }
        lore.add(cMsg("$seriesColour$seriesDisplay: &f$series"))
        if (configManager.cardsConfig.config!!.contains("Cards.$rarity.$cardName.About")) lore.add(cMsg("$aboutColour$aboutDisplay: &f$about"))
        if (isShiny) lore.add(cMsg(rarityColour + ChatColor.BOLD + config.getString("General.Shiny-Name") + " " + rarity)) else lore.add(cMsg(rarityColour + ChatColor.BOLD + rarity))
        cmeta.lore = lore
        if (config.getBoolean("General.Hide-Enchants", true)) cmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        card.itemMeta = cmeta
        if (isShiny) {
            card.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10)
        }
        return card
    }

    private fun getBlankCard(quantity: Int): ItemStack {
        return ItemStack(Material.getMaterial(configManager.pluginConfig.config!!.getString("General.Card-Material")!!)!!, quantity)
    }

    fun getNormalCard(cardName: String, rarity: String, num: Int): ItemStack {
        val config = configManager.pluginConfig.config!!
        val card = getBlankCard(num)
        val rarityColour = config.getString("Rarities.$rarity.Colour")!!
        val prefix = config.getString("General.Card-Prefix")!!
        val series = configManager.cardsConfig.config!!.getString("Cards.$rarity.$cardName.Series")!!
        val seriesColour = config.getString("Colours.Series")!!
        val seriesDisplay = config.getString("DisplayNames.Cards.Series", "Series")!!
        val about = configManager.cardsConfig.config!!.getString("Cards.$rarity.$cardName.About", "None")!!
        val aboutColour = config.getString("Colours.About")!!
        val aboutDisplay = config.getString("DisplayNames.Cards.About", "About")!!
        val type = configManager.cardsConfig.config!!.getString("Cards.$rarity.$cardName.Type")!!
        val typeColour = config.getString("Colours.Type")!!
        val typeDisplay = config.getString("DisplayNames.Cards.Type", "Type")!!
        val info = configManager.cardsConfig.config!!.getString("Cards.$rarity.$cardName.Info")!!
        val infoColour = config.getString("Colours.Info")!!
        val infoDisplay = config.getString("DisplayNames.Cards.Info", "Info")!!
        val cost: String = if (configManager.cardsConfig.config!!.contains("Cards.$rarity.$cardName.Buy-Price")) configManager.cardsConfig.config!!.getDouble("Cards.$rarity.$cardName.Buy-Price").toString() else "None"
        val cmeta = card.itemMeta
        cmeta!!.setDisplayName(cMsg(config.getString("DisplayNames.Cards.Title")!!.replace("%PREFIX%".toRegex(), prefix).replace("%COLOUR%".toRegex(), rarityColour).replace("%NAME%".toRegex(), cardName).replace("%COST%".toRegex(), cost).replace("_".toRegex(), " ")))
        val lore: MutableList<String?> = mutableListOf()
        lore.add(cMsg("$typeColour$typeDisplay: &f$type"))
        if (info == "None" || info == "") {
            lore.add(cMsg("$infoColour$infoDisplay: &f$info"))
        } else {
            lore.add(cMsg("$infoColour$infoDisplay:"))
            lore.addAll(wrapString(info))
        }
        lore.add(cMsg("$seriesColour$seriesDisplay: &f$series"))
        if (configManager.cardsConfig.config!!.contains("Cards.$rarity.$cardName.About")) lore.add(cMsg("$aboutColour$aboutDisplay: &f$about"))
        lore.add(cMsg(rarityColour + ChatColor.BOLD + rarity))
        cmeta.lore = lore
        if (config.getBoolean("General.Hide-Enchants", true)) cmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        card.itemMeta = cmeta
        return card
    }

    private fun createCard(creator: Player, rarity: String, name: String, series: String, type: String, hasShiny: Boolean, info: String) {
        if (!configManager.cardsConfig.config!!.contains("Cards.$rarity.$name")) {
            if (name.matches(Regex("^[a-zA-Z0-9-_]+$"))) {
                val rarities = configManager.cardsConfig.config!!.getConfigurationSection("Cards")!!
                val rarityKeys = rarities.getKeys(false)
                var keyToUse = ""
                for (key in rarityKeys) {
                    if (key.equals(rarity, ignoreCase = true)) {
                        keyToUse = key
                    }
                }
                if (keyToUse != "") {
                    val regex = Regex("^[a-zA-Z0-9-_]+$")
                    val series1 = if (series.matches(regex)) series else "None"
                    val type1 = if (type.matches(regex)) type else "None"
                    val info1 = if (info.matches(regex)) info else "None"
                    val hasShiny1: Boolean = hasShiny
                    configManager.cardsConfig.config!!["Cards.$rarity.$name.Series"] = series1
                    configManager.cardsConfig.config!!["Cards.$rarity.$name.Type"] = type1
                    configManager.cardsConfig.config!!["Cards.$rarity.$name.Has-Shiny-Version"] = hasShiny1
                    configManager.cardsConfig.config!!["Cards.$rarity.$name.Info"] = info1
                    configManager.cardsConfig.save()
                    configManager.reloadCardsConfig()
                    creator.sendMessage(cMsg(configManager.cardsConfig.config!!.getString("Messages.Prefix") + " " + configManager.cardsConfig.config!!.getString("Messages.CreateSuccess")!!.replace("%name%".toRegex(), name).replace("%rarity%".toRegex(), rarity)))
                } else {
                    creator.sendMessage(cMsg(configManager.cardsConfig.config!!.getString("Messages.Prefix") + " " + configManager.cardsConfig.config!!.getString("Messages.NoRarity")))
                }
            } else {
                creator.sendMessage(cMsg(configManager.cardsConfig.config!!.getString("Messages.Prefix") + " " + configManager.cardsConfig.config!!.getString("Messages.CreateNoName")))
            }
        } else creator.sendMessage(cMsg(configManager.cardsConfig.config!!.getString("Messages.Prefix") + " " + configManager.cardsConfig.config!!.getString("Messages.CreateExists")))
    }

    fun getCardName(rarity: String, display: String): String {
        val config = configManager.pluginConfig.config!!
        var hasPrefix = false
        var prefix: String? = ""
        if (config.contains("General.Card-Prefix") && config.getString("General.Card-Prefix") !== "") {
            hasPrefix = true
            prefix = ChatColor.stripColor(config.getString("General.Card-Prefix"))
        }
        val shinyPrefix = config.getString("General.Shiny-Name")!!
        var cleaned = ChatColor.stripColor(display)
        if (hasPrefix) cleaned = cleaned!!.replace(prefix!!.toRegex(), "")
        cleaned = cleaned!!.replace(shinyPrefix + " ".toRegex(), "")
        val cleanedArray: Array<String> = cleaned.split(" ").toTypedArray()
        val cs = configManager.cardsConfig.config!!.getConfigurationSection("Cards.$rarity")!!
        val keys = cs.getKeys(false)
        for (s in keys) {
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] getCardName s: $s")
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] getCardName display: $display")
            val regex = Regex(".*\\b$s\\b.*")
            if (cleanedArray.size > 1) {
                if (config.getBoolean("General.Debug-Mode")) println("[Cards] cleanedArray > 1")
                if ((cleanedArray[0] + "_" + cleanedArray[1]).matches(regex)) return s
                if (cleanedArray.size > 2 && (cleanedArray[1] + "_" + cleanedArray[2]).matches(regex)) return s
                if (cleanedArray.size > 3 && (cleanedArray[1] + "_" + cleanedArray[2] + "_" + cleanedArray[3]).matches(regex)) return s
                if (cleanedArray.size > 4 && (cleanedArray[1] + "_" + cleanedArray[2] + "_" + cleanedArray[3] + "_" + cleanedArray[4]).matches(Regex(".*\\b$s\\b.*"))) return s
                if (cleanedArray.size > 5 && (cleanedArray[1] + "_" + cleanedArray[2] + "_" + cleanedArray[3] + "_" + cleanedArray[4] + "_" + cleanedArray[5]).matches(Regex(".*\\b$s\\b.*"))) return s
                if (cleanedArray[0].matches(regex)) return s
                if (cleanedArray[1].matches(regex)) {
                    return s
                }
            } else if (cleanedArray[0].matches(regex)) {
                return s
            }
        }
        return "None"
    }

    fun generateCard(rare: String?): ItemStack? {
        val config = configManager.pluginConfig.config!!
        if (rare != "None") {
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] generateCard.rare: $rare")
            val card = getBlankCard(1)
            configManager.reloadPluginConfig()
            val cardSection = configManager.cardsConfig.config!!.getConfigurationSection("Cards.$rare")!!
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] generateCard.cardSection: " + configManager.cardsConfig.config!!.contains(StringBuilder("Cards.").append(rare).toString()))
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] generateCard.rarity: $rare")
            val cards = cardSection.getKeys(false)
            val cardNames: MutableList<String?> = mutableListOf()
            cardNames.addAll(cards)
            val cIndex = Random().nextInt(cardNames.size)
            val cardName = cardNames[cIndex]
            val hasShinyVersion = configManager.cardsConfig.config!!.getBoolean("Cards.$rare.$cardName.Has-Shiny-Version")
            var isShiny = false
            if (hasShinyVersion) {
                val shinyRandom = Random().nextInt(100) + 1
                if (shinyRandom <= config.getInt("Chances.Shiny-Version-Chance")) isShiny = true
            }
            val rarityColour = config.getString("Rarities.$rare.Colour")!!
            val prefix = config.getString("General.Card-Prefix")!!
            val series = configManager.cardsConfig.config!!.getString("Cards.$rare.$cardName.Series")!!
            val seriesColour = config.getString("Colours.Series")!!
            val seriesDisplay = config.getString("DisplayNames.Cards.Series", "Series")!!
            val about = configManager.cardsConfig.config!!.getString("Cards.$rare.$cardName.About", "None")!!
            val aboutColour = config.getString("Colours.About")!!
            val aboutDisplay = config.getString("DisplayNames.Cards.About", "About")!!
            val type = configManager.cardsConfig.config!!.getString("Cards.$rare.$cardName.Type")!!
            val typeColour = config.getString("Colours.Type")!!
            val typeDisplay = config.getString("DisplayNames.Cards.Type", "Type")!!
            val info = configManager.cardsConfig.config!!.getString("Cards.$rare.$cardName.Info")!!
            val infoColour = config.getString("Colours.Info")!!
            val infoDisplay = config.getString("DisplayNames.Cards.Info", "Info")!!
            val shinyPrefix = config.getString("General.Shiny-Name")!!
            val cost: String
            cost = if (configManager.cardsConfig.config!!.contains("Cards.$rare.$cardName.Buy-Price")) configManager.cardsConfig.config!!.getDouble("Cards.$rare.$cardName.Buy-Price").toString() else "None"
            val cmeta = card.itemMeta
            if (isShiny) {
                cmeta!!.setDisplayName(cMsg(config.getString("DisplayNames.Cards.ShinyTitle")!!.replace("%PREFIX%".toRegex(), prefix).replace("%COLOUR%".toRegex(), rarityColour).replace("%NAME%".toRegex(), cardName!!).replace("%COST%".toRegex(), cost).replace("%SHINYPREFIX%".toRegex(), shinyPrefix).replace("_".toRegex(), " ")))
            } else cmeta!!.setDisplayName(cMsg(config.getString("DisplayNames.Cards.Title")!!.replace("%PREFIX%".toRegex(), prefix).replace("%COLOUR%".toRegex(), rarityColour).replace("%NAME%".toRegex(), cardName!!).replace("%COST%".toRegex(), cost).replace("_".toRegex(), " ")))
            val lore: MutableList<String?> = mutableListOf()
            lore.add(cMsg("$typeColour$typeDisplay: &f$type"))
            if (info == "None" || info == "") {
                lore.add(cMsg("$infoColour$infoDisplay: &f$info"))
            } else {
                lore.add(cMsg("$infoColour$infoDisplay:"))
                lore.addAll(wrapString(info))
            }
            lore.add(cMsg("$seriesColour$seriesDisplay: &f$series"))
            if (configManager.cardsConfig.config!!.contains("Cards.$rare.$cardName.About")) lore.add(cMsg("$aboutColour$aboutDisplay: &f$about"))
            if (isShiny) lore.add(cMsg(rarityColour + ChatColor.BOLD + config.getString("General.Shiny-Name") + " " + rare)) else lore.add(cMsg(rarityColour + ChatColor.BOLD + rare))
            cmeta.lore = lore
            if (config.getBoolean("General.Hide-Enchants", true)) cmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            card.itemMeta = cmeta
            if (isShiny) {
                card.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10)
            }
            return card
        }
        return null
    }
}