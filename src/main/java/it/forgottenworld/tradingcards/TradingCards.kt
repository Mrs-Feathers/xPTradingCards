package it.forgottenworld.tradingcards

import it.forgottenworld.tradingcards.card.CardManager
import it.forgottenworld.tradingcards.commands.DefaultCommand
import it.forgottenworld.tradingcards.config.ConfigManager
import it.forgottenworld.tradingcards.deck.DeckManager
import it.forgottenworld.tradingcards.listeners.EntityListener
import it.forgottenworld.tradingcards.listeners.PlayerListener
import it.forgottenworld.tradingcards.task.Task
import net.milkbowl.vault.chat.Chat
import net.milkbowl.vault.economy.Economy
import org.bukkit.permissions.Permission
import org.bukkit.plugin.java.JavaPlugin

class TradingCards : JavaPlugin() {
    var hasVault = false
        get

    lateinit var task: Task

    override fun onEnable() {
        try {
            instance = this
            server.pluginManager.addPermission(permRarities)
            server.pluginManager.registerEvents(PlayerListener(), this)
            server.pluginManager.registerEvents(EntityListener(), this)
            getCommand("fwtc")?.setExecutor(DefaultCommand())
            configManager = ConfigManager()
            deckManager = DeckManager()
            cardManager = CardManager()
            if (configManager.pluginConfig.config?.getBoolean("PluginSupport.Vault.Vault-Enabled")!!) {
                if (server.pluginManager.getPlugin("Vault") != null) {
                    setupEconomy()
                    println("[FWTradingCards] Vault hook successful!")
                    hasVault = true
                } else {
                    println("[FWTradingCards] Vault not found, hook unsuccessful!")
                }
            }
            if (configManager.pluginConfig.config?.getBoolean("General.Schedule-Cards")!!) {
                task = Task()
                task.startTimer()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDisable() {
        econ = null
        perms = null
        chat = null
        server.pluginManager.removePermission(permRarities)
    }

    private fun setupEconomy() {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return
        }
        val rsp = server.servicesManager.getRegistration(Economy::class.java) ?: return
        econ = rsp.provider
    }

    companion object {
        var permRarities = Permission("fwtc.rarity")
        var econ: Economy? = null
        var perms: Permission? = null
        var chat: Chat? = null
        lateinit var configManager: ConfigManager
        lateinit var instance: TradingCards
        lateinit var cardManager: CardManager
        lateinit var deckManager: DeckManager
    }
}