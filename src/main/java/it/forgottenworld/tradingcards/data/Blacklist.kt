package it.forgottenworld.tradingcards.data

import it.forgottenworld.tradingcards.config.Config
import org.bukkit.entity.Player

object Blacklist {

    lateinit var WorldBlacklist: List<String>
    var WhitelistMode = false
    private lateinit var players: MutableSet<String>

    fun load() {
        WorldBlacklist = Config.PLUGIN.getStringList("World-Blacklist")
        WhitelistMode = Config.PLUGIN.getBoolean("Blacklist.Whitelist-Mode", false)
        players = Config.PLUGIN.getStringList("Blacklist.Players").toMutableSet()
    }

    fun addPlayer(player: Player) {
        players.add(player.name)
        Config.PLUGIN.set("Blacklist.Players", players)
        Config.pluginConfig.save()
    }

    fun removePlayer(player: Player) {
        if (players.remove(player.name)) {
            Config.PLUGIN.set("Blacklist.Players", players)
            Config.pluginConfig.save()
        }
    }

    fun isPlayerBlacklisted(player: Player) = players.contains(player.name)
}





