package it.forgottenworld.tradingcards

import it.forgottenworld.tradingcards.commands.TradingCardsCommand
import it.forgottenworld.tradingcards.config.Config
import it.forgottenworld.tradingcards.data.General
import it.forgottenworld.tradingcards.data.PluginSupport
import it.forgottenworld.tradingcards.listeners.EntityListener
import it.forgottenworld.tradingcards.listeners.PlayerListener
import it.forgottenworld.tradingcards.task.Task
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.java.JavaPlugin

class TradingCards : JavaPlugin() {

    lateinit var task: Task

    override fun onEnable() {
        try {

            saveDefaultConfig()

            instance = this

            Config.reloadAllConfigs()

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

            Config.schedulePersistence()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDisable() {
        economy = null
    }

    private fun setupEconomy(): Boolean {
        server.pluginManager.getPlugin("Vault") ?: return false
        economy = server.servicesManager.getRegistration(Economy::class.java)?.provider
        return economy != null
    }

    companion object {
        var economy: Economy? = null

        lateinit var instance: TradingCards
    }
}