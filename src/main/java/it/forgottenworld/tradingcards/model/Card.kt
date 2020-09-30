package it.forgottenworld.tradingcards.model

import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.data.Chances
import it.forgottenworld.tradingcards.data.General
import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.data.Rarities
import it.forgottenworld.tradingcards.util.capitalizeFully
import it.forgottenworld.tradingcards.util.tc
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import kotlin.random.Random

class Card(
        val name: String,
        val rarity: Rarity,
        private val hasShinyVersion: Boolean,
        val series: String,
        val about: String,
        val type: String,
        val info: String,
        val price: Double) {

    val isShiny
            get() = hasShinyVersion && Random.nextInt(100) + 1 <= Chances.ShinyVersionChance

    companion object {

        private val pattern = Regex("^[a-zA-Z0-9-_]+$")

        fun parseDisplayName(rarityName: String, display: String): String {

            val hasPrefix = General.CardPrefix.isNotBlank()
            val shinyPrefix = General.ShinyName

            val serializedCardName =
                    (ChatColor.stripColor(display) ?: return "None")
                            .let {
                                if (hasPrefix) it.replaceFirst(ChatColor.stripColor(tc(General.CardPrefix)) ?: "", "")
                                else it
                            }.replaceFirst("$shinyPrefix ", "")
                            .trim()
                            .replace(" ", "_")

            return if (Rarities[rarityName]?.cards?.contains(serializedCardName) == true)
                serializedCardName
            else "None"
        }

        fun saveNew(creator: Player, rarity: String, name: String, series: String, type: String, hasShiny: Boolean, info: String) {

            val rar = rarity.capitalizeFully()

            if (Rarities[rar]?.cards?.contains(name) == true) {
                creator.sendMessage(tc("${Messages.Prefix} ${Messages.CreateExists}"))
                return
            }

            if (!name.matches(pattern)) {
                creator.sendMessage(tc("${Messages.Prefix} ${Messages.CreateNoName}"))
                return
            }

            if (!Rarities.contains(rar)) {
                creator.sendMessage(tc("${Messages.Prefix} ${Messages.NoRarity}"))
                return
            }

            Config.CARDS["Cards.$rar.$name.Series"] = if (series.matches(pattern)) series else "None"
            Config.CARDS["Cards.$rar.$name.Type"] = if (type.matches(pattern)) type else "None"
            Config.CARDS["Cards.$rar.$name.Has-Shiny-Version"] = hasShiny
            Config.CARDS["Cards.$rar.$name.Info"] = if (info.matches(pattern)) info else "None"

            Config.saveCardsConfig()

            Rarities[rarity]?.let {
                it.cards.put(
                        name,
                        Card(
                                name.replace(" ", ""),
                                it,
                                hasShiny,
                                series,
                                "None",
                                type,
                                info,
                                0.0
                        )
                )
            }

            creator.sendMessage(tc(
                    "${Messages.Prefix} ${
                        Messages.CreateSuccess
                            .replaceFirst("%name%", name)
                            .replaceFirst("%rarity%", rarity)}"))
        }
    }
}