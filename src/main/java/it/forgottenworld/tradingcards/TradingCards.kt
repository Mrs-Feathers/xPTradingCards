package it.forgottenworld.tradingcards

import it.forgottenworld.tradingcards.commands.TradingCardsCommand
import it.forgottenworld.tradingcards.data.*
import it.forgottenworld.tradingcards.listeners.EntityListener
import it.forgottenworld.tradingcards.listeners.PlayerListener
import it.forgottenworld.tradingcards.task.Task
import net.milkbowl.vault.chat.Chat
import net.milkbowl.vault.economy.Economy
import org.bukkit.NamespacedKey
import org.bukkit.permissions.Permission
import org.bukkit.plugin.java.JavaPlugin

class TradingCards : JavaPlugin() {

    var hasVault = false

    lateinit var task: Task

    override fun onEnable() {
        try {

            saveDefaultConfig()

            instance = this

            General.load()
            PluginSupport.load()
            Chances.load()
            Rarities.load()
            BoosterPacks.load()
            Colors.load()
            DisplayNames.load()
            Blacklist.load()
            Messages.load()
            Decks.load()

            nameSpacedKey = NamespacedKey(this,"uncraftable")

            server.pluginManager.addPermission(permRarities)
            server.pluginManager.registerEvents(PlayerListener(), this)
            server.pluginManager.registerEvents(EntityListener(), this)

            getCommand("fwtradingcards")?.setExecutor(TradingCardsCommand())

            if (PluginSupport.Vault.Enabled)
                println(
                        if (setupEconomy())
                            "[FWTradingCards] Vault hook successful!"
                        else
                            "[FWTradingCards] Vault not found, hook unsuccessful!"
                )

            if (General.ScheduleCards) {
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

    private fun setupEconomy(): Boolean {
        server.pluginManager.getPlugin("Vault") ?: return false
        econ = server.servicesManager.getRegistration(Economy::class.java)?.provider ?: return false
        hasVault = true
        return true
    }

    companion object {
        var permRarities = Permission("fwtradingcards.rarity")
        var econ: Economy? = null
        var perms: Permission? = null
        var chat: Chat? = null

        lateinit var instance: TradingCards
        lateinit var nameSpacedKey: NamespacedKey
    }
}