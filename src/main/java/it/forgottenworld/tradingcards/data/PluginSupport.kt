package it.forgottenworld.tradingcards.data

import it.forgottenworld.tradingcards.config.Config
import java.util.*

object PluginSupport {

    object Vault {

        var Enabled = false
        var ClosedEconomy = true
        lateinit var ServerAccount: UUID
    }

    fun load() {
        with (Vault) {
            Enabled = Config.PLUGIN.getBoolean("PluginSupport.Vault.Vault-Enabled", false)
            ClosedEconomy = Config.PLUGIN.getBoolean("PluginSupport.Vault.Closed-Economy", true)
            ServerAccount = UUID.fromString(Config.PLUGIN.getString("PluginSupport.Vault.Server-Account") ?: "")
        }
    }
}