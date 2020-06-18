package it.forgottenworld.tradingcards.util

import it.forgottenworld.tradingcards.TradingCards
import org.apache.commons.lang.WordUtils
import org.apache.commons.lang3.StringUtils
import org.bukkit.ChatColor
import org.bukkit.entity.EntityType
import java.util.*

class Utils {
    companion object {
        private var hostileMobs: Set<EntityType> = setOf(EntityType.SPIDER, EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.BLAZE, EntityType.SILVERFISH, EntityType.GHAST, EntityType.SLIME, EntityType.GUARDIAN, EntityType.MAGMA_CUBE, EntityType.WITCH, EntityType.ENDERMITE)
        private var neutralMobs: Set<EntityType> = setOf(EntityType.ENDERMAN, EntityType.PIG_ZOMBIE, EntityType.WOLF, EntityType.SNOWMAN, EntityType.IRON_GOLEM)
        private var passiveMobs: Set<EntityType> = setOf(EntityType.CHICKEN, EntityType.COW, EntityType.SQUID, EntityType.SHEEP, EntityType.PIG, EntityType.RABBIT, EntityType.VILLAGER, EntityType.BAT, EntityType.HORSE)
        private var bossMobs: Set<EntityType> = setOf(EntityType.ENDER_DRAGON, EntityType.WITHER)

        private fun isMobHostile(e: EntityType): Boolean {
            return hostileMobs.contains(e)
        }

        private fun isMobNeutral(e: EntityType): Boolean {
            return neutralMobs.contains(e)
        }

        private fun isMobPassive(e: EntityType): Boolean {
            return passiveMobs.contains(e)
        }

        private fun isMobBoss(e: EntityType): Boolean {
            return bossMobs.contains(e)
        }

        fun cMsg(message: String?): String {
            return ChatColor.translateAlternateColorCodes('&', message!!)
        }

        fun wrapString(s: String?): List<String?> {
            val parsedString = ChatColor.stripColor(s)
            val addedString = WordUtils.wrap(parsedString, TradingCards.configManager.pluginConfig.config!!.getInt("General.Info-Line-Length", 25), "\n", true)
            val splitString: Array<String> = addedString.split("\n").toTypedArray()
            val finalArray: MutableList<String?> = mutableListOf()
            var arrayOfString1: Array<String>
            val j: Int = splitString.also { arrayOfString1 = it }.size
            for (i in 0 until j) {
                val ss = arrayOfString1[i]
                println(ChatColor.getLastColors(ss))
                finalArray.add(cMsg("&f &7- &f$ss"))
            }
            return finalArray
        }

        private fun calculateRarity(e: EntityType, alwaysDrop: Boolean): String? {
            val config = TradingCards.configManager.pluginConfig.config!!
            val shouldItDrop = Random().nextInt(100) + 1
            val type: String
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] shouldItDrop Num: $shouldItDrop")
            when {
                isMobHostile(e) -> {
                    type = if (!alwaysDrop) {
                        if (shouldItDrop > config.getInt("Chances.Hostile-Chance")) return "None"
                        "Hostile"
                    } else {
                        "Hostile"
                    }
                }
                isMobNeutral(e) -> {
                    type = if (!alwaysDrop) {
                        if (shouldItDrop > config.getInt("Chances.Neutral-Chance")) return "None"
                        "Neutral"
                    } else {
                        "Neutral"
                    }
                }
                isMobPassive(e) -> {
                    type = if (!alwaysDrop) {
                        if (shouldItDrop > config.getInt("Chances.Passive-Chance")) return "None"
                        "Passive"
                    } else {
                        "Passive"
                    }
                }
                isMobBoss(e) -> {
                    if (!alwaysDrop) {
                        if (shouldItDrop > config.getInt("Chances.Boss-Chance")) return "None"
                    }
                    type = "Boss"
                }
                else -> return "None"
            }
            val rarities = config.getConfigurationSection("Rarities")!!
            val rarityKeys = rarities.getKeys(false)
            val rarityChances: MutableMap<String?, Int?> = mutableMapOf()
            val rarityIndexes: MutableMap<Int?, String?> = mutableMapOf()
            var i = 0
            var mini = 0
            val random = Random().nextInt(100000) + 1
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] Random Card Num: $random")
            if (config.getBoolean("General.Debug-Mode")) println("[Cards] Type: $type")
            for (key in rarityKeys) {
                rarityIndexes[i] = key
                i++
                if (config.getBoolean("General.Debug-Mode")) println("[Cards] $i, $key")
                if (config.contains("Chances." + key + "." + StringUtils.capitalize(e.name)) && mini == 0) {
                    if (config.getBoolean("General.Debug-Mode")) println("[Cards] Mini: $i")
                    mini = i
                }
                val chance = config.getInt("Chances.$key.$type", -1)
                if (config.getBoolean("General.Debug-Mode")) println("[Cards] Keys: $key, $chance, i=$i")
                rarityChances[key] = chance
            }
            if (mini != 0) {
                if (config.getBoolean("General.Debug-Mode")) println("[Cards] Mini: $mini")
                if (config.getBoolean("General.Debug-Mode")) println("[Cards] i: $i")
                while (i >= mini) {
                    i--
                    if (config.getBoolean("General.Debug-Mode")) println("[Cards] i: $i")
                    val chance = config.getInt("Chances." + rarityIndexes[i] + "." + StringUtils.capitalize(e.name), -1)
                    if (config.getBoolean("General.Debug-Mode")) println("[Cards] Chance: $chance")
                    if (config.getBoolean("General.Debug-Mode")) println("[Cards] Rarity: " + rarityIndexes[i])
                    if (chance > 0) {
                        if (config.getBoolean("General.Debug-Mode")) println("[Cards] Chance > 0")
                        if (random <= chance) {
                            if (config.getBoolean("General.Debug-Mode")) println("[Cards] Random <= Chance")
                            return rarityIndexes[i]
                        }
                    }
                }
            } else {
                while (i > 0) {
                    i--
                    if (config.getBoolean("General.Debug-Mode")) println("[Cards] Final loop iteration $i")
                    if (config.getBoolean("General.Debug-Mode")) println("[Cards] Iteration " + i + " in HashMap is: " + rarityIndexes[i] + ", " + config.getString(StringBuilder("Rarities.").append(rarityIndexes[i]).append(".Name").toString()))
                    val chance: Int = config.getInt("Chances." + rarityIndexes[i] + "." + type, -1)
                    if (config.getBoolean("General.Debug-Mode")) println("[Cards] " + config.getString(StringBuilder("Rarities.").append(rarityIndexes[i]).append(".Name").toString()) + "'s chance of dropping: " + chance + " out of 100,000")
                    if (config.getBoolean("General.Debug-Mode")) println("[Cards] The random number we're comparing that against is: $random")
                    if (chance > 0 &&
                            random <= chance) {
                        if (config.getBoolean("General.Debug-Mode")) println("[Cards] Yup, looks like $random is definitely lower than $chance!")
                        if (config.getBoolean("General.Debug-Mode")) println("[Cards] Giving a " + config.getString(StringBuilder("Rarities.").append(rarityIndexes[i]).append(".Name").toString()) + " card.")
                        return rarityIndexes[i]
                    }
                }
            }
            return "None"
        }
    }
}