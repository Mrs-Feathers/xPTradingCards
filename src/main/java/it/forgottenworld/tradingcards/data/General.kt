package it.forgottenworld.tradingcards.data

import it.forgottenworld.tradingcards.config.Config
import org.bukkit.Material


object General {
    
    lateinit var ServerName: String
    var DebugMode: Boolean = false
    var ShowCommandUsage: Boolean = true
    var HideEnchants: Boolean = false
    var AmericanMode: Boolean = false
    lateinit var CardMaterial: Material
    lateinit var CardPrefix: String
    lateinit var BoosterPackMaterial: Material
    lateinit var BoosterPackPrefix: String
    lateinit var DeckMaterial: Material
    lateinit var DeckPrefix: String
    lateinit var ShinyName: String
    var ScheduleCards: Boolean = false
    lateinit var ScheduleCardRarity: String
    var ScheduleCardTimeInHours: Int = 1
    var SpawnerBlock: Boolean = false
    //lateinit var SpawnerMobName: String
    var AutoAddPlayers: Boolean = false
    lateinit var AutoAddPlayerRarity: String
    lateinit var PlayerOpRarity: String
    lateinit var PlayerSeries: String
    lateinit var PlayerType: String
    var PlayerHasShinyVersion: Boolean = false
    var PlayerDropsCard: Boolean = true
    var PlayerDropsCardRarity: Int = 100
    var InfoLineLength: Int = 25

    fun load() {
        with (Config.PLUGIN) {

            ServerName = getString("General.Server-Name") ?: "ERROR"
            DebugMode = getBoolean("General.Debug-Mode", false)
            ShowCommandUsage = getBoolean("General.Show-Command-Usage", true)
            HideEnchants = getBoolean("General.Hide-Enchants", true)
            AmericanMode = getBoolean("General.American-Mode", false)
            CardMaterial = Material.getMaterial(getString("General.Card-Material") ?: "PAPER") ?: Material.PAPER
            CardPrefix = getString("General.Card-Prefix") ?: "ERROR"
            BoosterPackMaterial = Material.getMaterial(getString("General.BoosterPack-Material") ?: "BOOK") ?: Material.BOOK
            BoosterPackPrefix = getString("General.BoosterPack-Prefix") ?: "ERROR"
            DeckMaterial = Material.getMaterial(getString("General.Deck-Material") ?: "BOOK") ?: Material.BOOK
            DeckPrefix = getString("General.Deck-Prefix") ?: "ERROR"
            ShinyName = getString("General.Shiny-Name") ?: "ERROR"
            ScheduleCards = getBoolean("General.Schedule-Cards", true)
            ScheduleCardRarity = getString("General.Schedule-Card-Rarity") ?: "ERROR"
            ScheduleCardTimeInHours = getInt("General.Schedule-Card-Time-In-Hours", 1)
            SpawnerBlock = getBoolean("General.Spawner-Block", true)
            //SpawnerMobName = getString("General.Spawner-Mob-Name") ?: "ERROR"
            AutoAddPlayers = getBoolean("General.Auto-Add-Players", false)
            AutoAddPlayerRarity = getString("General.Auto-Add-Player-Rarity") ?: "ERROR"
            PlayerOpRarity = getString("General.Player-Op-Rarity") ?: "ERROR"
            PlayerSeries = getString("General.Player-Series") ?: "ERROR"
            PlayerType = getString("General.Player-Type") ?: "ERROR"
            PlayerHasShinyVersion = getBoolean("General.Player-Has-Shiny-Version", false)
            PlayerDropsCard = getBoolean("General.Player-Drops-Card", false)
            PlayerDropsCardRarity = getInt("General.Player-Drops-Card-Rarity", 100)
            InfoLineLength = getInt("General.Info-Line-Length", 25)
        }
    }
            
}