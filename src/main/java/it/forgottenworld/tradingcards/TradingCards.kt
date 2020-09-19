package it.forgottenworld.tradingcards

import it.forgottenworld.tradingcards.card.CardManager
import it.forgottenworld.tradingcards.commands.DefaultCommand
import it.forgottenworld.tradingcards.config.ConfigManager
import it.forgottenworld.tradingcards.deck.DeckManager
import it.forgottenworld.tradingcards.listeners.EntityListener
import it.forgottenworld.tradingcards.listeners.PlayerListener
import it.forgottenworld.tradingcards.task.Task
import it.forgottenworld.tradingcards.util.Utils
import net.milkbowl.vault.chat.Chat
import net.milkbowl.vault.economy.Economy
import org.bukkit.NamespacedKey
import org.bukkit.permissions.Permission
import org.bukkit.plugin.java.JavaPlugin

class TradingCards : JavaPlugin() {

    var hasVault = false
    var permRarities = Permission("fwtc.rarity")
    var econ: Economy? = null
    var perms: Permission? = null
    var chat: Chat? = null
    lateinit var configManager: ConfigManager
    lateinit var cardManager: CardManager
    lateinit var deckManager: DeckManager
    lateinit var nameSpacedKey: NamespacedKey
    lateinit var task: Task
    lateinit var utils: Utils

    override fun onEnable() {
        try {
            utils = Utils(this)
            nameSpacedKey = NamespacedKey(this,"uncraftable")
            configManager = ConfigManager(this)
            deckManager = DeckManager(this)
            cardManager = CardManager(this)
            loadConfig()
            registerEvents()
            registerCommands()
            registerPermissions()
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

    private fun loadConfig(){
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
            task = Task(this)
            task.startTimer()
        }
    }

    private fun registerEvents(){
        server.pluginManager.registerEvents(PlayerListener(this), this)
        server.pluginManager.registerEvents(EntityListener(this), this)
    }

    private fun registerCommands(){
        getCommand("fwtc")?.setExecutor(DefaultCommand(this))
    }

    private fun registerPermissions(){
        server.pluginManager.addPermission(permRarities)
    }
}