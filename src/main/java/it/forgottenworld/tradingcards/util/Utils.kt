package it.forgottenworld.tradingcards.util

import it.forgottenworld.tradingcards.TradingCards
import it.forgottenworld.tradingcards.data.General
import it.forgottenworld.tradingcards.data.Messages
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.max

enum class MobType { HOSTILE, PASSIVE, NEUTRAL, BOSS }

fun getMobType(type: EntityType) =
        when {
            isMobHostile(type) -> MobType.HOSTILE
            isMobNeutral(type) -> MobType.NEUTRAL
            isMobPassive(type) -> MobType.PASSIVE
            isMobBoss(type) -> MobType.BOSS
            else -> null
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

private var bossMobs = setOf(
        EntityType.ENDER_DRAGON,
        EntityType.WITHER
)

fun isMobHostile(e: EntityType) = hostileMobs.contains(e)

fun isMobNeutral(e: EntityType) = neutralMobs.contains(e)

fun isMobPassive(e: EntityType) = passiveMobs.contains(e)

fun sendPrefixedMessage(sender: CommandSender, message: String) =
        sender.sendMessage(tC("${Messages.Prefix} $message"))

fun isMobBoss(e: EntityType) = bossMobs.contains(e)

fun tC(string: String) = ChatColor.translateAlternateColorCodes('&', string)

fun wrapString(s: String): Collection<String> {
    var tail = ChatColor.stripColor(s) ?: return setOf()
    val th = General.InfoLineLength / 3
    val res = mutableListOf<String>()

    while (tail.isNotBlank()) {
        val head = tail.take(General.InfoLineLength)
        tail = tail.drop(General.InfoLineLength)
        val i = head.lastIndexOf(' ')

        if (i <= th)
            res.add(tC("&f &7- &f$head"))
        else {
            res.add(tC("&f &7- &f${head.take(i)}"))
            tail = head.substring(i + 1) + tail
        }
    }
    return res
}

fun formatTitle(title: String): String {
    val line = "&7[&foOo&7]&f____________________________________________________&7[&foOo&7]&f"
    val pivot = line.length / 2
    val center = "&7.< &3$title&7 >.&f"
    return line.substring(0, max(0, pivot - center.length / 2)) +
            center +
            line.substring(pivot + center.length / 2)
}

fun String.capitalizeFully() = split(" ").joinToString(" ") { it.toLowerCase().capitalize() }

inline fun bukkitTaskAsync(crossinline action: (BukkitRunnable.() -> Unit)) =
    object : BukkitRunnable() {
        override fun run() = action()
    }.runTaskAsynchronously(TradingCards.instance)

inline fun bukkitTaskTimer(delay: Long, interval: Long, crossinline action: (BukkitRunnable.() -> Unit)) =
    object : BukkitRunnable() {
        override fun run() = action()
    }.runTaskTimer(TradingCards.instance, delay, interval)