package it.forgottenworld.tradingcards.data

import it.forgottenworld.tradingcards.config.Config
import org.bukkit.Material


object General {

    lateinit var ServerName: String
    var ShowCommandUsage = true
    var HideEnchants = false
    var AmericanMode = false
    lateinit var CardMaterial: Material
    lateinit var CardPrefix: String
    lateinit var BoosterPackMaterial: Material
    lateinit var BoosterPackPrefix: String
    lateinit var DeckMaterial: Material
    lateinit var DeckPrefix: String
    lateinit var ShinyName: String
    var ScheduleCards = false
    lateinit var ScheduleCardRarity: String
    var ScheduleCardTimeInHours = 1
    var SpawnerBlock = false
    var AutoAddPlayers = false
    lateinit var AutoAddPlayerRarity: String
    lateinit var PlayerOpRarity: String
    lateinit var PlayerSeries: String
    lateinit var PlayerType: String
    var PlayerHasShinyVersion = false
    var PlayerDropsCard = true
    var PlayerDropsCardRarity = 100
    var InfoLineLength = 25
    var PersistenceInterval = 5
    lateinit var MainWorldName: String


    fun load() {
        with(Config.PLUGIN) {

            ServerName = getString("General.Server-Name") ?: "ERROR"
            ShowCommandUsage = getBoolean("General.Show-Command-Usage", true)
            HideEnchants = getBoolean("General.Hide-Enchants", true)
            AmericanMode = getBoolean("General.American-Mode", false)
            CardMaterial = Material.getMaterial(getString("General.Card-Material") ?: "FILLED_MAP") ?: Material.FILLED_MAP
            CardPrefix = getString("General.Card-Prefix") ?: "ERROR"
            BoosterPackMaterial = Material.getMaterial(getString("General.BoosterPack-Material") ?: "BOOK")
                    ?: Material.BOOK
            BoosterPackPrefix = getString("General.BoosterPack-Prefix") ?: "ERROR"
            DeckMaterial = Material.getMaterial(getString("General.Deck-Material") ?: "BOOK") ?: Material.BOOK
            DeckPrefix = getString("General.Deck-Prefix") ?: "ERROR"
            ShinyName = getString("General.Shiny-Name") ?: "ERROR"
            ScheduleCards = getBoolean("General.Schedule-Cards", true)
            ScheduleCardRarity = getString("General.Schedule-Card-Rarity") ?: "ERROR"
            ScheduleCardTimeInHours = getInt("General.Schedule-Card-Time-In-Hours", 1)
            SpawnerBlock = getBoolean("General.Spawner-Block", true)
            AutoAddPlayers = getBoolean("General.Auto-Add-Players", false)
            AutoAddPlayerRarity = getString("General.Auto-Add-Player-Rarity") ?: "ERROR"
            PlayerOpRarity = getString("General.Player-Op-Rarity") ?: "ERROR"
            PlayerSeries = getString("General.Player-Series") ?: "ERROR"
            PlayerType = getString("General.Player-Type") ?: "ERROR"
            PlayerHasShinyVersion = getBoolean("General.Player-Has-Shiny-Version", false)
            PlayerDropsCard = getBoolean("General.Player-Drops-Card", false)
            PlayerDropsCardRarity = getInt("General.Player-Drops-Card-Rarity", 100)
            InfoLineLength = getInt("General.Info-Line-Length", 25)
            PersistenceInterval = getInt("General.Persistence-Interval", 5)
            MainWorldName = getString("General.MainWorldName", "world") ?: "world"
        }
    }

}