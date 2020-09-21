package it.forgottenworld.tradingcards.util

import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.config.ConfigManager
import org.apache.commons.lang.WordUtils
import org.apache.commons.lang3.StringUtils
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.max

fun tcMsg(sender: CommandSender, message: String) {
    sender.sendMessage(cMsg("${Config.MESSAGES.getString("Messages.Prefix")} $message"))
}

fun printDebug(msg: String) {
    if (Config.DEBUG) println(msg)
}

private val hostileMobs = setOf(
        EntityType.ZOMBIE,
        EntityType.SKELETON,
        EntityType.CREEPER,
        EntityType.BLAZE,
        EntityType.SILVERFISH,
        EntityType.GHAST,
        EntityType.SLIME,
        EntityType.GUARDIAN,
        EntityType.MAGMA_CUBE,
        EntityType.WITCH,
        EntityType.DROWNED,
        EntityType.ENDERMITE,
        EntityType.ELDER_GUARDIAN,
        EntityType.ENDERMITE,
        EntityType.EVOKER,
        EntityType.HOGLIN,
        EntityType.HUSK,
        EntityType.PHANTOM,
        EntityType.PIGLIN_BRUTE,
        EntityType.PILLAGER,
        EntityType.RAVAGER,
        EntityType.STRAY,
        EntityType.VEX,
        EntityType.VINDICATOR,
        EntityType.WITHER_SKELETON,
        EntityType.ZOGLIN,
        EntityType.ZOMBIE_VILLAGER
)
private val neutralMobs = setOf(
        EntityType.ENDERMAN,
        EntityType.BEE,
        EntityType.CAVE_SPIDER,
        EntityType.DOLPHIN,
        EntityType.PIGLIN,
        EntityType.WOLF,
        EntityType.ZOMBIFIED_PIGLIN,
        EntityType.SPIDER,
        EntityType.PUFFERFISH,
        EntityType.POLAR_BEAR,
        EntityType.PANDA,
        EntityType.SNOWMAN,
        EntityType.LLAMA,
        EntityType.IRON_GOLEM
)
private val passiveMobs = setOf(
        EntityType.CHICKEN,
        EntityType.FOX,
        EntityType.DONKEY,
        EntityType.SALMON,
        EntityType.SKELETON_HORSE,
        EntityType.SNOWMAN,
        EntityType.STRIDER,
        EntityType.TROPICAL_FISH,
        EntityType.TURTLE,
        EntityType.WANDERING_TRADER,
        EntityType.PARROT,
        EntityType.OCELOT,
        EntityType.MUSHROOM_COW,
        EntityType.MULE,
        EntityType.COW,
        EntityType.COD,
        EntityType.SQUID,
        EntityType.SHEEP,
        EntityType.CAT,
        EntityType.PIG,
        EntityType.RABBIT,
        EntityType.VILLAGER,
        EntityType.BAT,
        EntityType.HORSE
)
private var bossMobs: Set<EntityType> = setOf(
        EntityType.ENDER_DRAGON,
        EntityType.WITHER
)

private fun isMobHostile(e: EntityType) = hostileMobs.contains(e)

private fun isMobNeutral(e: EntityType) = neutralMobs.contains(e)

private fun isMobPassive(e: EntityType) = passiveMobs.contains(e)

fun isMobBoss(e: EntityType) = bossMobs.contains(e)

fun cMsg(message: String) = ChatColor.translateAlternateColorCodes('&', message)

fun wrapString(s: String): Collection<String> =
        WordUtils.wrap(
                ChatColor.stripColor(s),
                Config.PLUGIN.getInt("General.Info-Line-Length", 25),
                "\n",
                true
        ).split("\n").map {
            println(ChatColor.getLastColors(it))
            cMsg("&f &7- &f$it")
        }

fun calculateRarity(e: EntityType, alwaysDrop: Boolean): String {

    val config = Config.PLUGIN
    val debug = Config.DEBUG

    val shouldItDrop = Random().nextInt(100) + 1
    printDebug("[Cards] shouldItDrop Num: $shouldItDrop")

    val type = when {
        isMobHostile(e) -> {
            if (!alwaysDrop && shouldItDrop > config.getInt("Chances.Hostile-Chance")) return "None"
            "Hostile"
        }
        isMobNeutral(e) -> {
            if (!alwaysDrop && shouldItDrop > config.getInt("Chances.Neutral-Chance")) return "None"
            "Neutral"
        }
        isMobPassive(e) -> {
            if (!alwaysDrop && shouldItDrop > config.getInt("Chances.Passive-Chance")) return "None"
            "Passive"
        }
        isMobBoss(e) -> {
            if (!alwaysDrop && shouldItDrop > config.getInt("Chances.Boss-Chance")) return "None"
            "Boss"
        }
        else -> return "None"
    }

    val rarityKeys = config.getConfigurationSection("Rarities")?.getKeys(false) ?: return "None"
    val rarityIndexes: MutableMap<Int, String> = mutableMapOf()
    var mini = 0
    val random = Random().nextInt(100000) + 1

    if (debug) {
        println("[Cards] Random Card Num: $random")
        println("[Cards] Type: $type")
    }

    //val rarityChances = rarityKeys.zip(rarityKeys.mapIndexed {i, key ->
    rarityKeys.forEachIndexed {i, key ->

        rarityIndexes[i] = key
        printDebug("[Cards] $i, $key")

        if (config.contains("Chances.$key.${StringUtils.capitalize(e.name)}") && mini == 0) {
            printDebug("[Cards] Mini: $i")
            mini = i
        }

        config.getInt("Chances.$key.$type", -1).also {
            printDebug("[Cards] Keys: $key, $it, i=$i")
        }
    }
    //).toMap()


    var i = rarityKeys.size
    if (mini != 0) {

        if (debug) {
            println("[Cards] Mini: $mini")
            println("[Cards] i: $i")
        }

        while (i-- >= mini) {

            val chance = config.getInt("Chances.${rarityIndexes[i]}.${StringUtils.capitalize(e.name)}", -1)

            if (debug) {
                println("[Cards] i: $i")
                println("[Cards] Chance: $chance")
                println("[Cards] Rarity: ${rarityIndexes[i]}")
            }

            if (chance > 0) {
                if (debug) println("[Cards] Chance > 0")
                if (random <= chance) {
                    printDebug("[Cards] Random <= Chance")
                    return rarityIndexes[i] ?: "None"
                }
            }

        }

    } else {
        while (i-- > 0) {

            val chance = config.getInt("Chances.${rarityIndexes[i]}.$type", -1)

            if (debug) {
                println("[Cards] Final loop iteration $i")
                println("[Cards] Iteration $i in HashMap is: ${rarityIndexes[i]}, ${config.getString("Rarities.${rarityIndexes[i]}.Name")}")
                println("[Cards] ${config.getString("Rarities.${rarityIndexes[i]}.Name")}'s chance of dropping: $chance out of 100,000")
                println("[Cards] The random number we're comparing that against is: $random")
            }

            if (chance > 0 && random <= chance) {
                if (debug) {
                    println("[Cards] Yup, looks like $random is definitely lower than $chance!")
                    println("[Cards] Giving a ${config.getString("Rarities.${rarityIndexes[i]}.Name")} card.")
                }
                return rarityIndexes[i] ?: "None"
            }

        }
    }
    return "None"
}

fun formatTitle(title: String): String {
    val line = "&7[&foOo&7]&f____________________________________________________&7[&foOo&7]&f"
    val pivot = line.length / 2
    val center = "&7.< &3$title&7 >.&f"
    return line.substring(0, max(0, pivot - center.length / 2)) +
            center +
            line.substring(pivot + center.length / 2)
}

fun isOnList(p: Player) =
        Config.PLUGIN.getStringList("Blacklist.Players").contains(p.name)

fun addToList(p: Player) {
    val config = Config.PLUGIN
    config["Blacklist.Players"] = config.getStringList("Blacklist.Players").apply {
        add(p.name)
    }
    ConfigManager.pluginConfig.save()
}

fun removeFromList(p: Player) {
    val config = Config.PLUGIN
    config["Blacklist.Players"] = config.getStringList("Blacklist.Players").apply {
        remove(p.name)
    }
    ConfigManager.pluginConfig.save()
}

fun blacklistMode() =
    if (Config.PLUGIN.getBoolean("Blacklist.Whitelist-Mode")) 'w' else 'b'