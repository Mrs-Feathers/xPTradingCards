package it.forgottenworld.tradingcards.card

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.config.ConfigManager
import it.forgottenworld.tradingcards.config.Messages
import it.forgottenworld.tradingcards.util.cMsg
import it.forgottenworld.tradingcards.util.wrapString
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

object CardManager {

    private val cardNamePattern = Regex("^[a-zA-Z0-9-_]+$")

    private fun getCardFromConfig(rarity: String, cardName: String): Card {
        val config = Config.PLUGIN
        val cardConfig = Config.CARDS

        return Card(
                config.getString("Rarities.$rarity.Colour")!!,
                config.getString("General.Card-Prefix")!!,
                cardConfig.getString("Cards.$rarity.$cardName.Series")!!,
                config.getString("Colours.Series")!!,
                config.getString("DisplayNames.Cards.Series", "Series")!!,
                cardConfig.getString("Cards.$rarity.$cardName.About", "None")!!,
                config.getString("Colours.About")!!,
                config.getString("DisplayNames.Cards.About", "About")!!,
                cardConfig.getString("Cards.$rarity.$cardName.Type")!!,
                config.getString("Colours.Type")!!,
                config.getString("DisplayNames.Cards.Type", "Type")!!,
                cardConfig.getString("Cards.$rarity.$cardName.Info")!!,
                config.getString("Colours.Info")!!,
                config.getString("DisplayNames.Cards.Info", "Info")!!,
                if (Config.CARDS.contains("Cards.$rarity.$cardName.Buy-Price"))
                    Config.CARDS.getDouble("Cards.$rarity.$cardName.Buy-Price").toString()
                else
                    "None"
        )
    }

    private fun setDisplayName(card: ItemStack, crd: Card, cardName: String, isShiny: Boolean) {
        val meta = card.itemMeta ?: return
        val config = Config.PLUGIN
        val shinyPrefix = if (isShiny) config.getString("General.Shiny-Name") ?: "" else ""
        meta.setDisplayName(cMsg(config.getString(
                "DisplayNames.Cards.ShinyTitle")!!
                .replaceFirst("%PREFIX%", crd.prefix)
                .replaceFirst("%COLOUR%", crd.rarityColor)
                .replaceFirst("%NAME%", cardName)
                .replaceFirst("%COST%", crd.cost)
                .replace("_", " ")
                .replaceFirst("%SHINYPREFIX%", shinyPrefix)))
        card.itemMeta = meta
    }

    private fun setLore(card: ItemStack, crd: Card, cardName: String, isShiny: Boolean, rarity: String) {
        val itemMeta = card.itemMeta ?: return
        val config = Config.PLUGIN

        itemMeta.lore = mutableListOf(cMsg("${crd.typeColour}${crd.typeDisplay}: &f${crd.type}")).apply {

            if (crd.info == "None" || crd.info == "")
                add(cMsg("${crd.infoColor}${crd.infoDisplay}: &f${crd.info}"))
            else {
                add(cMsg("${crd.infoColor}${crd.infoDisplay}:"))
                addAll(wrapString(crd.info))
            }

            add(cMsg("${crd.seriesColor}${crd.seriesDisplay}: &f${crd.series}"))

            if (Config.CARDS.contains("Cards.$crd.rarity.$cardName.About"))
                add(cMsg("${crd.aboutColor}${crd.aboutDisplay}: &f${crd.about}"))

            add(cMsg("${crd.rarityColor}${ChatColor.BOLD}${
                if (isShiny) "${config.getString("General.Shiny-Name")} " else ""}$rarity"))
        }

        if (config.getBoolean("General.Hide-Enchants", true))
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)

        if (isShiny)
            card.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10)

        card.itemMeta = itemMeta
    }

    private fun getBlankCard(quantity: Int) =
            ItemStack(Material.getMaterial(Config.PLUGIN.getString("General.Card-Material")!!)!!, quantity).apply {
                val meta = itemMeta
                meta?.persistentDataContainer?.set(TradingCards.nameSpacedKey, PersistentDataType.BYTE,1)
                meta?.let { itemMeta = it }
            }

    private fun getIsShiny(rarity: String, cardName: String) =
            Config.CARDS.getBoolean("Cards.$rarity.$cardName.Has-Shiny-Version") &&
                Random().nextInt(100) + 1 <= Config.PLUGIN.getInt("Chances.Shiny-Version-Chance")

    fun getCard(cardName: String, rarity: String, num: Int, isShiny: Boolean = false): ItemStack {

        val card = getBlankCard(num)
        val crd = getCardFromConfig(rarity, cardName)

        setDisplayName(card, crd, cardName, isShiny)
        setLore(card, crd, cardName, isShiny, rarity)

        return card
    }

    fun createPlayerCard(cardName: String, rarity: String, num: Int, forcedShiny: Boolean) =
            getCard(cardName, rarity, num, if (forcedShiny) true else getIsShiny(rarity, cardName))

    fun createCard(creator: Player, rarity: String, name: String, series: String, type: String, hasShiny: Boolean, info: String) {

        if (Config.CARDS.contains("Cards.$rarity.$name")) {
            creator.sendMessage(cMsg("${Messages.Prefix} ${Messages.CreateExists}"))
            return
        }

        if (!name.matches(cardNamePattern)) {
            creator.sendMessage(cMsg("${Config.CARDS.getString("Messages.Prefix")} ${Config.CARDS.getString("Messages.CreateNoName")}"))
            return
        }

        val keyToUse = Config.CARDS
                .getConfigurationSection("Cards")!!
                .getKeys(false)
                .find { it.equals(rarity, ignoreCase = true) }
                ?: ""

        if (keyToUse.isEmpty()) {
            creator.sendMessage(cMsg("${Config.CARDS.getString("Messages.Prefix")} ${Config.CARDS.getString("Messages.NoRarity")}"))
            return
        }

        Config.CARDS["Cards.$rarity.$name.Series"] = if (series.matches(cardNamePattern)) series else "None"
        Config.CARDS["Cards.$rarity.$name.Type"] = if (type.matches(cardNamePattern)) type else "None"
        Config.CARDS["Cards.$rarity.$name.Has-Shiny-Version"] = hasShiny
        Config.CARDS["Cards.$rarity.$name.Info"] = if (info.matches(cardNamePattern)) info else "None"

        ConfigManager.cardsConfig.save()
        ConfigManager.reloadCardsConfig()
        creator.sendMessage(cMsg(
                "${Messages.Prefix} ${Messages.CreateSuccess
                        .replaceFirst("%name%", name)
                        .replaceFirst("%rarity%", rarity)}"))

    }

    fun getCardName(rarity: String, display: String): String {

        val config = Config.PLUGIN

        val hasPrefix = config.contains("General.Card-Prefix") && !config.getString("General.Card-Prefix").isNullOrBlank()
        val prefix = if (hasPrefix) ChatColor.stripColor(config.getString("General.Card-Prefix")) ?: "" else ""
        val shinyPrefix = config.getString("General.Shiny-Name")!!

        val serializedCardName =
                (ChatColor.stripColor(display) ?: return "None")
                        .let {
                            if (hasPrefix) it.replaceFirst(prefix, "")
                            else it
                        }.replaceFirst("$shinyPrefix ", "")
                        .trim()
                        .replace(" ", "_")

        /*if (serializedCardName.isEmpty()) return "None"

        val keys = Config.CARDS.getConfigurationSection("Cards.$rarity")?.getKeys(false) ?: return "None"

        if (keys.contains(serializedCardName))
            return serializedCardName

        return keys.find { it.startsWith(serializedCardName) } ?: "None"*/

        return if (Config.CARDS.contains("Cards.$rarity.$serializedCardName"))
            serializedCardName
        else "None"


        /* FATHER FORGIVE THEM FOR THEY DO NOT KNOW WHAT THEY DO

        val cleanedArray = ChatColor.stripColor(display)!!.let {

            if (hasPrefix)
                it.replace(prefix, "")
            else
                it
        }.replace("$shinyPrefix ", "").split(" ")

        val keys = Config.CARDS.getConfigurationSection("Cards.$rarity")!!.getKeys(false)

        return keys.find { s ->

            if (Config.DEBUG) {
                println("[Cards] getCardName s: $s")
                println("[Cards] getCardName display: $display")
            }

            val regex = Regex(".*\\b$s\\b.*")
            if (cleanedArray.size > 1) {
                "${cleanedArray[0]}_${cleanedArray[1]}".matches(regex) ||
                        cleanedArray.size > 2 && cleanedArray.drop(1).joinToString("_").matches(regex) ||
                        cleanedArray[0].matches(regex) ||
                        cleanedArray[1].matches(regex)
                if (cleanedArray.size > 2 && ("${cleanedArray[1]}_${cleanedArray[2]}").matches(regex)) return s
                if (cleanedArray.size > 3 && ("${cleanedArray[1]}_${cleanedArray[2]}_${cleanedArray[3]}").matches(regex)) return s
                if (cleanedArray.size > 4 && ("${cleanedArray[1]}_${cleanedArray[2]}_${cleanedArray[3]}_${cleanedArray[4]}").matches(Regex(".*\\b$s\\b.*"))) return s
                if (cleanedArray.size > 5 && ("${cleanedArray[1]}_${cleanedArray[2]}_${cleanedArray[3]}_${cleanedArray[4]}_${cleanedArray[5]}").matches(Regex(".*\\b$s\\b.*"))) return s
            } else cleanedArray[0].matches(regex)
            
            false
        } ?: "None"*/

    }

    fun generateCard(rarity: String): ItemStack? {

        if (rarity == "None") return null

        if (Config.DEBUG) {
            println("[Cards] generateCard.rare: $rarity")
            println("[Cards] generateCard.cardSection: ${Config.CARDS.contains("Cards.$rarity")}")
            println("[Cards] generateCard.rarity: $rarity")
        }

        ConfigManager.reloadPluginConfig()

        val cardSection = Config.CARDS.getConfigurationSection("Cards.$rarity")!!
        val cardName = cardSection.getKeys(false).random()!!

        return getCard(cardName, rarity, 1, getIsShiny(rarity, cardName))
    }
}