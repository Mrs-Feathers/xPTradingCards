package it.forgottenworld.tradingcards.model

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.data.Chances
import it.forgottenworld.tradingcards.data.General
import it.forgottenworld.tradingcards.data.Messages
import it.forgottenworld.tradingcards.data.Rarities
import it.forgottenworld.tradingcards.util.capitalizeFully
import it.forgottenworld.tradingcards.util.tC
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO
import kotlin.random.Random

class Card(
        val name: String,
        val rarity: Rarity,
        private val hasShinyVersion: Boolean,
        val series: String,
        val about: String,
        val type: String,
        val info: String,
        val price: Double,
        val imageName: String,
        var mapViewId: Int
    ) {

    var image: BufferedImage
    val isShiny
        get() = hasShinyVersion && Random.nextInt(100) + 1 <= Chances.ShinyVersionChance

    init {
        TradingCards.instance.run {
            image = try {
                ImageIO.read(File(this.dataFolder, "images/" + imageName))
            } catch (e: IOException) {
                ImageIO.read(File(this.dataFolder, "images/default.png"))
            }
        }
        if(mapViewId == -1){
            mapViewId = Bukkit.getWorld(General.MainWorldName)?.let { Bukkit.createMap(it).id }!!
            Config.CARDS.set("Cards.${rarity.name}.$name.mapId",mapViewId)
            Config.schedulePersistence()
        }
    }

    companion object {

        private val pattern = Regex("^[a-zA-Z0-9-_]+$")

        fun parseDisplayName(rarityName: String, display: String): String {

            val hasPrefix = General.CardPrefix.isNotBlank()
            val shinyPrefix = General.ShinyName

            val serializedCardName =
                    (ChatColor.stripColor(display) ?: return "None")
                            .let {
                                if (hasPrefix) it.replaceFirst(ChatColor.stripColor(tC(General.CardPrefix)) ?: "", "")
                                else it
                            }.replaceFirst("$shinyPrefix ", "")
                            .trim()
                            .replace(" ", "_")

            return if (Rarities[rarityName]?.contains(serializedCardName) == true)
                serializedCardName
            else "None"
        }

        fun saveNew(creator: Player, rarity: String, name: String, series: String, type: String, hasShiny: Boolean, info: String) {

            val capitalizedRarity = rarity.capitalizeFully()

            if (Rarities[capitalizedRarity]?.contains(name) == true) {
                creator.sendMessage(tC("${Messages.Prefix} ${Messages.CreateExists}"))
                return
            }

            if (!name.matches(pattern)) {
                creator.sendMessage(tC("${Messages.Prefix} ${Messages.CreateNoName}"))
                return
            }

            if (!Rarities.contains(capitalizedRarity)) {
                creator.sendMessage(tC("${Messages.Prefix} ${Messages.NoRarity}"))
                return
            }

            Config.CARDS["Cards.$capitalizedRarity.$name.Series"] = if (series.matches(pattern)) series else "None"
            Config.CARDS["Cards.$capitalizedRarity.$name.Type"] = if (type.matches(pattern)) type else "None"
            Config.CARDS["Cards.$capitalizedRarity.$name.Has-Shiny-Version"] = hasShiny
            Config.CARDS["Cards.$capitalizedRarity.$name.Info"] = if (info.matches(pattern)) info else "None"

            Rarities[rarity]?.let {
                it[name.replace(" ", "_")] =
                        Card(
                                name.replace(" ", ""),
                                it,
                                hasShiny,
                                series,
                                "None",
                                type,
                                info,
                                0.0,
                                "",
                                -1
                        )
            }

            creator.sendMessage(tC(
                    "${Messages.Prefix} ${
                        Messages.CreateSuccess
                                .replaceFirst("%name%", name)
                                .replaceFirst("%rarity%", rarity)
                    }"))


        }

        fun autoSaveNewPlayerCard(player: Player) {

            if (!General.AutoAddPlayers) return

            val rarityName = if (player.isOp) General.PlayerOpRarity else General.AutoAddPlayerRarity
            val rarity = Rarities[rarityName] ?: return
            if (rarity.contains(player.name)) return

            val serializedCardName = player.name.replace(" ", "_")

            val gc = GregorianCalendar()
            gc.timeInMillis = if (player.hasPlayedBefore()) player.firstPlayed else System.currentTimeMillis()
            val day = gc[Calendar.DATE]
            val month = gc[Calendar.MONTH] + 1
            val year = gc[Calendar.YEAR]
            val info = "Joined ${if (General.AmericanMode) "$month/$day/$year" else "$day/$month/$year"}"

            Config.CARDS["Cards.${rarity.name}.$serializedCardName.Series"] = General.PlayerSeries
            Config.CARDS["Cards.${rarity.name}.$serializedCardName.Type"] = General.PlayerType
            Config.CARDS["Cards.${rarity.name}.$serializedCardName.Has-Shiny-Version"] = General.PlayerHasShinyVersion
            Config.CARDS["Cards.${rarity.name}.$serializedCardName.Info"] = info
            Config.CARDS["Cards.${rarity.name}.$serializedCardName.Image"] = ""  //TODO prendere la foto dell'utente da qualche rest api

            rarity[serializedCardName] = Card(
                    player.name,
                    rarity,
                    General.PlayerHasShinyVersion,
                    General.PlayerSeries,
                    "None",
                    General.PlayerType,
                    info,
                    0.0,
                    "",
                    -1
            )
        }
    }
}